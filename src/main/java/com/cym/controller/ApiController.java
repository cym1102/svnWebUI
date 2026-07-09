package com.cym.controller;

import org.noear.solon.Solon;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Context;

import com.cym.model.Repository;
import com.cym.model.RepositoryUser;
import com.cym.model.User;
import com.cym.service.ConfigService;
import com.cym.service.RepositoryService;
import com.cym.service.UserService;
import com.cym.sqlhelper.utils.ConditionAndWrapper;
import com.cym.sqlhelper.utils.SqlHelper;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;
import com.cym.utils.SvnAdminUtils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

/**
 * open-API:供后端(just-photo-admin)provision/revoke svn 账号与授权.
 *
 * <p>鉴权:请求头 {@code X-Api-Token} 必须与 token 一致;token 未配则整个 open-API 禁用(零副作用)。
 * <p>token 来源以环境变量 {@code SVNWEBUI_API_TOKEN} 为准(生产路径);为便于自动化测试注入,
 * 当 env 为空时回退读取配置项 {@code svnwebui.api-token}(生产不配置该项,故仍等价于 env-only)。
 * <p>所有方法体全裹 try/catch(Throwable) 返 renderError,绝不外抛——AppFilter 的 catch(Throwable)
 * 会吞掉未捕获异常导致空响应。
 */
@Controller
@Mapping("/api")
public class ApiController extends BaseController {
	@Inject
	UserService userService;
	@Inject
	RepositoryService repositoryService;
	@Inject
	ConfigService configService;
	@Inject
	SqlHelper sqlHelper;
	@Inject
	SvnAdminUtils svnAdminUtils;

	/**
	 * 读取 open-API token。生产以环境变量 SVNWEBUI_API_TOKEN 为准;env 空时回退配置项 svnwebui.api-token(测试注入用)。
	 */
	protected String getApiToken() {
		String token = System.getenv("SVNWEBUI_API_TOKEN");
		if (StrUtil.isEmpty(token)) {
			token = Solon.cfg().get("svnwebui.api-token");
		}
		return token;
	}

	/**
	 * token 校验(必须在任何 DB 写之前调用)。校验通过返 null,否则返对应错误响应。
	 */
	private JsonResult checkToken() {
		String cfg = getApiToken();
		if (StrUtil.isEmpty(cfg)) {
			return renderError("api disabled");
		}
		String hdr = Context.current().header("X-Api-Token");
		if (!cfg.equals(hdr)) {
			return renderAuthError();
		}
		return null;
	}

	/**
	 * provision:覆盖式收敛某 user 在某 repo 的授权。
	 * 请求体 {user, pass, repo, paths:[{path, permission}]}。
	 */
	@Mapping("/provision")
	public JsonResult provision() {
		try {
			// 1. token 校验(DB 写前)
			JsonResult authError = checkToken();
			if (authError != null) {
				return authError;
			}

			JSONObject body = JSONUtil.parseObj(Context.current().body());

			// 2. repo 必须存在(不建 repo)
			Repository repo = repositoryService.getByName(body.getStr("repo"), null);
			if (repo == null) {
				return renderError("仓库不存在");
			}

			// 3. 排除保留(管理员)用户名
			String uname = body.getStr("user");
			if (StrUtil.isEmpty(uname)) {
				return renderError("用户名为空");
			}
			if (svnAdminUtils.getAdminUserName().equals(uname)) {
				return renderError("保留用户名");
			}

			// 4. 建号(open=0,普通用户)或改密幂等
			User u = userService.getByName(uname, null);
			if (u == null) {
				u = new User();
				u.setName(uname);
				u.setPass(body.getStr("pass"));
				u.setType(0);
				sqlHelper.insert(u);
			} else {
				u.setPass(body.getStr("pass"));
				sqlHelper.updateById(u);
			}

			// 5. 清空该 user 在该 repo 的全部旧授权(覆盖式收敛第一步)
			sqlHelper.deleteByQuery(new ConditionAndWrapper()//
					.eq(RepositoryUser::getUserId, u.getId())//
					.eq(RepositoryUser::getRepositoryId, repo.getId()), //
					RepositoryUser.class);

			// 6. 逐 path 重新写入期望授权
			JSONArray paths = body.getJSONArray("paths");
			if (paths != null) {
				for (int i = 0; i < paths.size(); i++) {
					JSONObject p = paths.getJSONObject(i);
					String path = p.getStr("path");
					String permission = p.getStr("permission");
					if (StrUtil.isEmpty(path) || StrUtil.isEmpty(permission)) {
						continue;
					}
					RepositoryUser ru = new RepositoryUser();
					ru.setRepositoryId(repo.getId());
					ru.setUserId(u.getId());
					ru.setPath(path);
					ru.setPermission(permission);
					sqlHelper.insert(ru);
				}
			}

			// 7. 重生成 passwd/authz 配置文件
			configService.refresh();

			return renderSuccess();
		} catch (Throwable t) {
			return renderError(t.getMessage());
		}
	}

	/**
	 * revoke:硬删 user 及其全部授权(级联)。user 不存在也返 success(幂等)。
	 * 请求体 {user, repo}。
	 */
	@Mapping("/revoke")
	public JsonResult revoke() {
		try {
			// 1. token 校验(DB 写前)
			JsonResult authError = checkToken();
			if (authError != null) {
				return authError;
			}

			JSONObject body = JSONUtil.parseObj(Context.current().body());

			String uname = body.getStr("user");
			User u = userService.getByName(uname, null);
			if (u != null) {
				// deleteById 级联删除该 user 全部 RepositoryUser/GroupUser
				userService.deleteById(u.getId());
			}

			configService.refresh();

			return renderSuccess();
		} catch (Throwable t) {
			return renderError(t.getMessage());
		}
	}

}
