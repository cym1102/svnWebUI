package com.cym.service;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import com.cym.config.HomeConfig;
import com.cym.ext.AsycPack;
import com.cym.model.Group;
import com.cym.model.GroupGroup;
import com.cym.model.GroupUser;
import com.cym.model.Repository;
import com.cym.model.RepositoryGroup;
import com.cym.model.RepositoryUser;
import com.cym.model.Setting;
import com.cym.model.User;
import com.cym.model.WebHook;
import com.cym.sqlhelper.utils.ConditionAndWrapper;
import com.cym.sqlhelper.utils.SqlHelper;
import com.cym.utils.SvnAdminUtils;
import com.cym.utils.SystemTool;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;

@Component
public class ConfigService {
	@Inject
	SqlHelper sqlHelper;
	@Inject
	RepositoryService repositoryService;
	@Inject
	GroupService groupService;
	@Inject
	HomeConfig homeConfig;
	@Inject
	SettingService settingService;

	@Inject
	SvnAdminUtils svnAdminUtils;

	// open-API 与 UI 触发的 refresh 共享同一进程内静态锁,串行化"读 SQLite→生成→原子替换"整段,防半截文件与 lost-update
	private static final Object REFRESH_LOCK = new Object();

	public void refresh() {
		synchronized (REFRESH_LOCK) {
			refreshInternal();
		}
	}

	// 原子写:先写目标同目录的 .tmp,再 rename 覆盖目标(同分区即原子);tmp 必须落目标同目录,否则跨挂载退化为非原子复制
	private void writeLinesAtomic(List<String> lines, String target) {
		File targetFile = new File(target);
		File tmpFile = new File(targetFile.getParentFile(), targetFile.getName() + ".tmp");
		FileUtil.writeLines(lines, tmpFile, Charset.forName("UTF-8"));
		FileUtil.move(tmpFile, targetFile, true);
	}

	private void refreshInternal() {
		String passwd = null;
		if (SystemTool.inDocker() && "http".equals(settingService.get("protocol"))) {
			passwd = homeConfig.home + "repo/httpdPasswd";
		} else {
			passwd = homeConfig.home + "repo/passwd";
		}

		String authz = homeConfig.home + "repo/authz";

		// 用户名密码
		List<String> passwdLines = new ArrayList<>();
		passwdLines.add("[users]");
		if (SystemTool.inDocker() && "http".equals(settingService.get("protocol"))) { // 超级用户
			String pass = RuntimeUtil.execForStr("htpasswd -nb " + svnAdminUtils.adminUserName + " " + svnAdminUtils.adminUserPass);
			if (!pass.contains("Usage:")) {
				passwdLines.add(pass.trim());
			}
		} else {
			passwdLines.add(svnAdminUtils.adminUserName + " = " + svnAdminUtils.adminUserPass);
		}

		List<User> userList = sqlHelper.findListByQuery(new ConditionAndWrapper().eq(User::getOpen, 0), User.class);
		for (User user : userList) {
			if (SystemTool.inDocker() && "http".equals(settingService.get("protocol"))) {
				String pass = RuntimeUtil.execForStr("htpasswd -nb " + user.getName() + " " + user.getPass());
				passwdLines.add(pass);
			} else {
				passwdLines.add(user.getName() + " = " + user.getPass());
			}
		}
		writeLinesAtomic(passwdLines, passwd);

		// 小组
		List<String> authzLines = new ArrayList<>();
		authzLines.add("[groups]");
		List<Group> groupList = sqlHelper.findAll(Group.class);
		for (Group group : groupList) {
			String groupStr = group.getName() + " = ";

			List<User> users = groupService.getUserList(group.getId());
			List<String> names = new ArrayList<>();
			for (User user : users) {
				if (user.getOpen() != null && user.getOpen() == 0) {
					names.add(user.getName());
				}
			}
			List<Group> groups = groupService.getGroupList(group.getId());
			for (Group groupSlave : groups) {
				names.add("@" + groupSlave.getName());
			}

			groupStr += StrUtil.join(",", names);
			authzLines.add(groupStr);
		}

		// 权限
		ClassPathResource resource = new ClassPathResource("file/svnserve.conf");
		List<Repository> repositories = sqlHelper.findAll(Repository.class);
		for (Repository repository : repositories) {
			boolean hasRoot = false; // 是否已配置/的权限

			if (repository.getEnable()) {
				List<String> paths = getPaths(repository.getId());
				for (String path : paths) {
					authzLines.add("[" + repository.getName() + ":" + replaceEnd(path) + "]");

					List<RepositoryGroup> repositoryGroups = sqlHelper.findListByQuery(new ConditionAndWrapper()//
							.eq(RepositoryGroup::getRepositoryId, repository.getId())//
							.eq(RepositoryGroup::getPath, path), //
							RepositoryGroup.class);
					for (RepositoryGroup repositoryGroup : repositoryGroups) {
						Group group = sqlHelper.findById(repositoryGroup.getGroupId(), Group.class);
						if (group != null) {
							authzLines.add("@" + group.getName() + " = " + val(repositoryGroup.getPermission()));
						}
					}

					List<RepositoryUser> repositoryUsers = sqlHelper.findListByQuery(new ConditionAndWrapper()//
							.eq(RepositoryUser::getRepositoryId, repository.getId())//
							.eq(RepositoryUser::getPath, path), //
							RepositoryUser.class);
					for (RepositoryUser repositoryUser : repositoryUsers) {
						User user = sqlHelper.findById(repositoryUser.getUserId(), User.class);
						if (user != null && user.getOpen() != null && user.getOpen() == 0) {
							authzLines.add(user.getName() + " = " + val(repositoryUser.getPermission()));
						}
					}

					if (path.equals("/")) {
						setSpecialPermission(authzLines, repository);
						hasRoot = true;
					}

				}
			}

			if (!hasRoot) {
				authzLines.add("[" + repository.getName() + ":/]");
				setSpecialPermission(authzLines, repository);
			}

			// 拷贝配置文件
			String svnserve_conf = homeConfig.home + "/repo/" + repository.getName() + "/conf/svnserve.conf";
			FileUtil.writeFromStream(resource.getStream(), svnserve_conf);
		}

		writeLinesAtomic(authzLines, authz);

		// 目录授权
		if (SystemTool.inDocker() && "http".equals(settingService.get("protocol"))) {
			RuntimeUtil.execForStr("chown apache.apache -R " + homeConfig.home + File.separator + "repo" + File.separator);
		}
	}

	// 设置特殊权限
	private void setSpecialPermission(List<String> authzLines, Repository repository) {
		authzLines.add(svnAdminUtils.adminUserName + " = rw");
		if (!repository.getAllPermission().equals("no") && repository.getEnable()) { // 全体权限
			authzLines.add("* = " + val(repository.getAllPermission()));
		}
	}

	private String val(String permission) {
		if (permission.equals("no")) {
			return "";
		}
		return permission;
	}

	// 去掉路径最后的/
	private String replaceEnd(String permission) {
		if (permission.endsWith("/") && !permission.equals("/")) {
			permission = permission.substring(0, permission.length() - 1);
		}
		return permission;
	}

	private List<String> getPaths(String repositoryId) {
		List<String> paths = new ArrayList<String>();
		List<String> pathList = sqlHelper.findPropertiesByQuery(new ConditionAndWrapper().eq(RepositoryUser::getRepositoryId, repositoryId), RepositoryUser.class, RepositoryUser::getPath);
		pathList.addAll(sqlHelper.findPropertiesByQuery(new ConditionAndWrapper().eq(RepositoryGroup::getRepositoryId, repositoryId), RepositoryGroup.class, RepositoryGroup::getPath));

		for (String path : pathList) {
			if (!paths.contains(path)) {
				paths.add(path);
			}
		}
		return paths;
	}

	public AsycPack getAsycPack() {
		AsycPack asycPack = new AsycPack();
		asycPack.setGroupGroupList(sqlHelper.findAll(GroupGroup.class));
		asycPack.setGroupList(sqlHelper.findAll(Group.class));
		asycPack.setGroupUserList(sqlHelper.findAll(GroupUser.class));
		asycPack.setRepositoryGroupList(sqlHelper.findAll(RepositoryGroup.class));
		asycPack.setRepositoryList(sqlHelper.findAll(Repository.class));
		asycPack.setRepositoryUserList(sqlHelper.findAll(RepositoryUser.class));
		asycPack.setSettingList(sqlHelper.findAll(Setting.class));
		asycPack.setUserList(sqlHelper.findAll(User.class));
		asycPack.setWebHookList(sqlHelper.findAll(WebHook.class));

		return asycPack;
	}

	public void setAsycPack(AsycPack asycPack) {
		sqlHelper.deleteByQuery(new ConditionAndWrapper(), GroupGroup.class);
		sqlHelper.deleteByQuery(new ConditionAndWrapper(), Group.class);
		sqlHelper.deleteByQuery(new ConditionAndWrapper(), GroupUser.class);
		sqlHelper.deleteByQuery(new ConditionAndWrapper(), RepositoryGroup.class);
		sqlHelper.deleteByQuery(new ConditionAndWrapper(), Repository.class);
		sqlHelper.deleteByQuery(new ConditionAndWrapper(), RepositoryUser.class);
		sqlHelper.deleteByQuery(new ConditionAndWrapper(), Setting.class);
		sqlHelper.deleteByQuery(new ConditionAndWrapper(), User.class);
		sqlHelper.deleteByQuery(new ConditionAndWrapper(), WebHook.class);

		sqlHelper.insertAll(asycPack.getGroupGroupList());
		sqlHelper.insertAll(asycPack.getGroupList());
		sqlHelper.insertAll(asycPack.getGroupUserList());
		sqlHelper.insertAll(asycPack.getRepositoryGroupList());
		sqlHelper.insertAll(asycPack.getRepositoryList());
		sqlHelper.insertAll(asycPack.getRepositoryUserList());
		sqlHelper.insertAll(asycPack.getSettingList());
		sqlHelper.insertAll(asycPack.getUserList());
		sqlHelper.insertAll(asycPack.getWebHookList());
	}
	
	public boolean hasSvnserve() {
		boolean hasSvnserve = false;

		if (SystemTool.inDocker()) {
			hasSvnserve = true;
		} else {
			if (SystemTool.isWindows()) {
				hasSvnserve = FileUtil.exist(homeConfig.home + "/subversion/bin/svnserve.exe");
			} else {
				String[] command = { "which svnserve" };
				String rs = RuntimeUtil.execForStr(command);
				hasSvnserve = rs.contains("svnserve");
			}
		}
		return hasSvnserve;
	}

}
