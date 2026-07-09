package com.cym.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.noear.solon.Solon;
import org.noear.solon.net.http.HttpUtils;
import org.noear.solon.test.HttpTester;
import org.noear.solon.test.SolonTest;

import com.cym.SvnWebUI;
import com.cym.model.GroupUser;
import com.cym.model.Repository;
import com.cym.model.RepositoryGroup;
import com.cym.model.RepositoryUser;
import com.cym.model.User;
import com.cym.sqlhelper.utils.ConditionAndWrapper;
import com.cym.sqlhelper.utils.SqlHelper;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

/**
 * open-API 验收测试(SVNWEBUI-1/2/3/4/7a)。
 *
 * <p>启动完整 Solon 容器(内嵌 undertow),经真实 HTTP 打 /api/*,覆盖 AppFilter + Controller + DB + refresh 全链路。
 * <p>token 通过配置项 svnwebui.api-token 注入(生产仍以 env SVNWEBUI_API_TOKEN 为准);测试前后 put/remove 该配置项以切换启用/禁用。
 * <p>DB 为内嵌 sqlite,home 指向 ./target/svnWebUI-test/(静态块预建,避免 HomeConfig 不可写→System.exit)。
 */
@SolonTest(value = SvnWebUI.class, properties = { "project.home=./target/svnWebUI-test/", "database.type=sqlite", "server.port=16060" })
public class ApiControllerTest extends HttpTester {

	static final String TEST_HOME = "./target/svnWebUI-test/";
	static final String TOKEN_KEY = "svnwebui.api-token";
	static final String TOKEN = "secret-token-123";
	static final String REPO = "knowledge-repo";

	static {
		// HomeConfig.init 会对 home 目录做 canWrite 检查,不可写则 System.exit(1);启动前先建好目录
		FileUtil.mkdir(new File(TEST_HOME));
	}

	SqlHelper sqlHelper() {
		return Solon.context().getBean(SqlHelper.class);
	}

	void enableToken() {
		Solon.cfg().put(TOKEN_KEY, TOKEN);
	}

	void disableToken() {
		Solon.cfg().remove(TOKEN_KEY);
	}

	@BeforeEach
	public void clean() {
		SqlHelper sqlHelper = sqlHelper();
		sqlHelper.deleteByQuery(new ConditionAndWrapper(), User.class);
		sqlHelper.deleteByQuery(new ConditionAndWrapper(), RepositoryUser.class);
		sqlHelper.deleteByQuery(new ConditionAndWrapper(), RepositoryGroup.class);
		sqlHelper.deleteByQuery(new ConditionAndWrapper(), GroupUser.class);
		sqlHelper.deleteByQuery(new ConditionAndWrapper(), Repository.class);
		disableToken();
		// 单测无真实 svn repo,关闭 rw 目录预建(file:// mkdir);建目录能力由灰度/E2E 验证
		Solon.cfg().put("svnwebui.ensure-dir", "false");
	}

	// ---- HTTP 辅助 ----

	JSONObject post(String apiPath, String token, String jsonBody) throws Exception {
		HttpUtils req = path(apiPath);
		if (token != null) {
			req.header("X-Api-Token", token);
		}
		String resp = req.bodyOfJson(jsonBody).post();
		return JSONUtil.parseObj(resp);
	}

	String repoId() {
		Repository repo = sqlHelper().findOneByQuery(new ConditionAndWrapper().eq(Repository::getName, REPO), Repository.class);
		return repo == null ? null : repo.getId();
	}

	void createRepo() {
		Repository repo = new Repository();
		repo.setName(REPO);
		sqlHelper().insert(repo); // @InitValue: enable=true, allPermission=no
	}

	long userCount() {
		return sqlHelper().findCountByQuery(new ConditionAndWrapper(), User.class);
	}

	long repoUserCount() {
		return sqlHelper().findCountByQuery(new ConditionAndWrapper(), RepositoryUser.class);
	}

	User getUser(String name) {
		return sqlHelper().findOneByQuery(new ConditionAndWrapper().eq(User::getName, name), User.class);
	}

	List<RepositoryUser> repoUsersOf(String userId) {
		return sqlHelper().findListByQuery(new ConditionAndWrapper().eq(RepositoryUser::getUserId, userId), RepositoryUser.class);
	}

	// =========================================================================
	// SVNWEBUI-1: token 未配则整个 open-API 禁用,不执行任何 DB 写
	// =========================================================================
	@Test
	public void token_not_configured_disables_api() throws Exception {
		disableToken();
		createRepo();
		long usersBefore = userCount();

		String body = "{\"user\":\"alice\",\"pass\":\"P\",\"repo\":\"" + REPO + "\",\"paths\":[{\"path\":\"/a\",\"permission\":\"r\"}]}";
		JSONObject resp = post("/api/provision", TOKEN, body);

		assertFalse(resp.getBool("success"), "token 未配时应 success=false");
		assertEquals(usersBefore, userCount(), "禁用状态下不得新增 user");
		assertNull(getUser("alice"), "禁用状态下不得创建 alice");
	}

	// =========================================================================
	// SVNWEBUI-3: 鉴权失败(错误 token)→ success=false, status=401, 且无任何 DB 变化
	// =========================================================================
	@Test
	public void wrong_token_returns_401_and_no_db_change() throws Exception {
		enableToken();
		createRepo();
		// 预置一个已有用户与授权,作为"无副作用"的基线
		User existing = new User();
		existing.setName("bob");
		existing.setPass("oldpass");
		existing.setType(0);
		sqlHelper().insert(existing);
		RepositoryUser ru = new RepositoryUser();
		ru.setUserId(existing.getId());
		ru.setRepositoryId(repoId());
		ru.setPath("/x");
		ru.setPermission("rw");
		sqlHelper().insert(ru);

		long usersBefore = userCount();
		long repoUsersBefore = repoUserCount();

		String body = "{\"user\":\"alice\",\"pass\":\"P\",\"repo\":\"" + REPO + "\",\"paths\":[{\"path\":\"/a\",\"permission\":\"r\"}]}";
		JSONObject resp = post("/api/provision", "WRONG-TOKEN", body);

		assertFalse(resp.getBool("success"), "错误 token 应 success=false");
		assertEquals("401", resp.getStr("status"), "错误 token 应 status=401");
		assertEquals(usersBefore, userCount(), "鉴权失败不得改动 User 行数");
		assertEquals(repoUsersBefore, repoUserCount(), "鉴权失败不得改动 RepositoryUser 行数");
		// bob 授权保持不变
		assertEquals("rw", getUser("bob") == null ? null : repoUsersOf(getUser("bob").getId()).get(0).getPermission());
	}

	// =========================================================================
	// 输入校验:非法 user/pass/path/permission 在任何 DB 写之前被拒(防 passwd/authz 注入与路径穿越)
	// =========================================================================
	@Test
	public void invalid_input_rejected_without_db_change() throws Exception {
		enableToken();
		createRepo();
		long usersBefore = userCount();
		long repoUsersBefore = repoUserCount();

		// 路径穿越 ..
		assertFalse(post("/api/provision", TOKEN,
				"{\"user\":\"eve\",\"pass\":\"P\",\"repo\":\"" + REPO + "\",\"paths\":[{\"path\":\"/../evil\",\"permission\":\"rw\"}]}")
				.getBool("success"), "路径含 .. 应被拒");
		// 用户名含斜杠
		assertFalse(post("/api/provision", TOKEN,
				"{\"user\":\"a/b\",\"pass\":\"P\",\"repo\":\"" + REPO + "\",\"paths\":[{\"path\":\"/a\",\"permission\":\"rw\"}]}")
				.getBool("success"), "用户名含 / 应被拒");
		// 口令含换行(passwd 注入)
		assertFalse(post("/api/provision", TOKEN,
				"{\"user\":\"eve\",\"pass\":\"P\\ninject = x\",\"repo\":\"" + REPO + "\",\"paths\":[{\"path\":\"/a\",\"permission\":\"rw\"}]}")
				.getBool("success"), "口令含换行应被拒");
		// 非法权限
		assertFalse(post("/api/provision", TOKEN,
				"{\"user\":\"eve\",\"pass\":\"P\",\"repo\":\"" + REPO + "\",\"paths\":[{\"path\":\"/a\",\"permission\":\"rwx\"}]}")
				.getBool("success"), "非法权限应被拒");

		assertEquals(usersBefore, userCount(), "非法输入不得改动 User 行数");
		assertEquals(repoUsersBefore, repoUserCount(), "非法输入不得改动 RepositoryUser 行数");
	}

	// =========================================================================
	// SVNWEBUI-4: 覆盖式收敛 —— 预置 /a=rw,/b=rw → provision paths=[{/a,r}] → 仅剩 /a=r
	// =========================================================================
	@Test
	public void provision_converges_authz_overwrite() throws Exception {
		enableToken();
		createRepo();
		// provision alice 初始 /a=rw,/b=rw
		String body1 = "{\"user\":\"alice\",\"pass\":\"P\",\"repo\":\"" + REPO
				+ "\",\"paths\":[{\"path\":\"/a\",\"permission\":\"rw\"},{\"path\":\"/b\",\"permission\":\"rw\"}]}";
		JSONObject resp1 = post("/api/provision", TOKEN, body1);
		assertTrue(resp1.getBool("success"), "首次 provision 应成功");

		User alice = getUser("alice");
		assertNotNull(alice, "alice 应被创建");
		assertEquals(2, repoUsersOf(alice.getId()).size(), "初始应有 2 条授权");

		// 覆盖式收敛:仅保留 /a=r
		String body2 = "{\"user\":\"alice\",\"pass\":\"P\",\"repo\":\"" + REPO + "\",\"paths\":[{\"path\":\"/a\",\"permission\":\"r\"}]}";
		JSONObject resp2 = post("/api/provision", TOKEN, body2);
		assertTrue(resp2.getBool("success"), "收敛 provision 应成功");

		List<RepositoryUser> rus = repoUsersOf(alice.getId());
		assertEquals(1, rus.size(), "收敛后应仅剩 1 条授权(/b 被删)");
		assertEquals("/a", rus.get(0).getPath(), "唯一授权应为 /a");
		assertEquals("r", rus.get(0).getPermission(), "/a 应从 rw 降为 r");
	}

	// =========================================================================
	// SVNWEBUI-2/4: 新建 user → open=0、type=0,authz 文件含其行;已存在 user → 改密幂等
	// =========================================================================
	@Test
	public void new_user_open_zero_and_existing_user_password_idempotent() throws Exception {
		enableToken();
		createRepo();

		String body1 = "{\"user\":\"carol\",\"pass\":\"pass1\",\"repo\":\"" + REPO + "\",\"paths\":[{\"path\":\"/knowledge/carol\",\"permission\":\"rw\"}]}";
		JSONObject resp1 = post("/api/provision", TOKEN, body1);
		assertTrue(resp1.getBool("success"), "新建 provision 应成功");

		User carol = getUser("carol");
		assertNotNull(carol, "carol 应被创建");
		assertEquals(Integer.valueOf(0), carol.getOpen(), "新建 user 的 open 应为 0(启用)");
		assertEquals(Integer.valueOf(0), carol.getType(), "新建 user 的 type 应为 0(普通用户)");
		assertEquals("pass1", carol.getPass());

		// authz 文件应重生成且包含 carol 的授权行
		File authz = new File(TEST_HOME + "repo/authz");
		assertTrue(authz.exists(), "authz 文件应被重生成");
		String authzContent = FileUtil.readString(authz, Charset.forName("UTF-8"));
		assertTrue(authzContent.contains("carol = rw"), "authz 应含 carol 的授权行,实际:\n" + authzContent);

		// 已存在 user provision → 改密幂等,不新增 user
		long usersBefore = userCount();
		String body2 = "{\"user\":\"carol\",\"pass\":\"pass2\",\"repo\":\"" + REPO + "\",\"paths\":[{\"path\":\"/knowledge/carol\",\"permission\":\"rw\"}]}";
		JSONObject resp2 = post("/api/provision", TOKEN, body2);
		assertTrue(resp2.getBool("success"), "改密 provision 应成功");
		assertEquals(usersBefore, userCount(), "已存在 user 不得新增行");
		assertEquals("pass2", getUser("carol").getPass(), "pass 应被更新为 pass2");
	}

	// =========================================================================
	// SVNWEBUI-7a: revoke 硬删 user 及授权;再次 revoke 同 user 仍 success(幂等)
	// =========================================================================
	@Test
	public void revoke_hard_deletes_and_is_idempotent() throws Exception {
		enableToken();
		createRepo();
		post("/api/provision", TOKEN,
				"{\"user\":\"dave\",\"pass\":\"P\",\"repo\":\"" + REPO + "\",\"paths\":[{\"path\":\"/a\",\"permission\":\"rw\"}]}");
		User dave = getUser("dave");
		assertNotNull(dave, "dave 应存在");
		assertTrue(repoUsersOf(dave.getId()).size() > 0, "dave 应有授权");

		String revokeBody = "{\"user\":\"dave\",\"repo\":\"" + REPO + "\"}";
		JSONObject resp1 = post("/api/revoke", TOKEN, revokeBody);
		assertTrue(resp1.getBool("success"), "revoke 应成功");
		assertNull(getUser("dave"), "dave 用户应被删除");
		assertEquals(0, repoUsersOf(dave.getId()).size(), "dave 全部授权应被删除");

		// 幂等:再次 revoke 同 user 仍 success
		JSONObject resp2 = post("/api/revoke", TOKEN, revokeBody);
		assertTrue(resp2.getBool("success"), "对不存在的 user revoke 仍应 success(幂等)");
	}
}
