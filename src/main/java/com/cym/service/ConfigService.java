package com.cym.service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.cym.config.SqlConfig;
import com.cym.ext.GroupExt;
import com.cym.ext.UserExt;
import com.cym.model.Group;
import com.cym.model.Repository;
import com.cym.model.RepositoryGroup;
import com.cym.model.RepositoryUser;
import com.cym.model.User;

import cn.craccd.sqlHelper.utils.ConditionAndWrapper;
import cn.craccd.sqlHelper.utils.SqlHelper;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.StrUtil;

@Service
public class ConfigService {
	@Autowired
	SqlHelper sqlHelper;
	@Autowired
	SqlConfig sqlConfig;
	@Autowired
	RepositoryService repositoryService;
	@Autowired
	GroupService groupService;

	public void refresh() {
		List<Repository> repositories = sqlHelper.findAll(Repository.class);
		String passwd = sqlConfig.home + "/repo/conf/passwd";
		String authz = sqlConfig.home + "/repo/conf/authz";

		for (Repository repository : repositories) {
			String svnserve_conf = sqlConfig.home + "/repo/" + repository.getName() + "/conf/svnserve.conf";

			try {
				ClassPathResource resource = new ClassPathResource("file/svnserve.conf");
				FileUtil.writeFromStream(resource.getInputStream(), svnserve_conf);
			} catch (IORuntimeException | IOException e) {
				e.printStackTrace();
			}
		}

		// 用户名密码
		List<String> passwdLines = new ArrayList<>();
		passwdLines.add("[users]");
		List<User> userList = sqlHelper.findAll(User.class);
		for (User user : userList) {
			passwdLines.add(user.getName() + " = " + user.getPass());
		}
		FileUtil.writeLines(passwdLines, passwd, Charset.forName("UTF-8"));

		// 小组
		List<String> authzLines = new ArrayList<>();
		authzLines.add("[groups]");
		List<Group> groupList = sqlHelper.findAll(Group.class);
		for (Group group : groupList) {
			String groupStr = group.getName() + " = ";
			List<User> users = groupService.getUserList(group.getId());
			List<String> names = users.stream().map(User::getName).collect(Collectors.toList());
			groupStr += StrUtil.join(",", names);
			authzLines.add(groupStr);
		}

		// 权限
		for (Repository repository : repositories) {
			List<String> paths = getPaths(repository.getId());
			for (String path : paths) {
				authzLines.add("[" + repository.getName() + ":" + replaceEnd(path) + "]");

				List<RepositoryGroup> repositoryGroups = sqlHelper.findListByQuery(new ConditionAndWrapper()//
						.eq(RepositoryGroup::getRepositoryId, repository.getId())//
						.eq(RepositoryGroup::getPath, path), //
						RepositoryGroup.class);
				for (RepositoryGroup repositoryGroup : repositoryGroups) {
					Group group = sqlHelper.findById(repositoryGroup.getGroupId(), Group.class);
					authzLines.add("@" + group.getName() + " = " + repositoryGroup.getPermission());
				}

				List<RepositoryUser> repositoryUsers = sqlHelper.findListByQuery(new ConditionAndWrapper()//
						.eq(RepositoryUser::getRepositoryId, repository.getId())//
						.eq(RepositoryUser::getPath, path), //
						RepositoryUser.class);
				for (RepositoryUser repositoryUser : repositoryUsers) {
					User user = sqlHelper.findById(repositoryUser.getUserId(), User.class);
					authzLines.add(user.getName() + " = " + repositoryUser.getPermission());
				}

			}
		}
		FileUtil.writeLines(authzLines, authz, Charset.forName("UTF-8"));

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

}
