package com.cym.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.noear.solon.annotation.Inject;
import org.noear.solon.extend.aspect.annotation.Service;

import com.cym.config.HomeConfig;
import com.cym.config.InitConfig;
import com.cym.ext.GroupExt;
import com.cym.ext.UserExt;
import com.cym.model.Group;
import com.cym.model.GroupUser;
import com.cym.model.Repository;
import com.cym.model.RepositoryGroup;
import com.cym.model.RepositoryUser;
import com.cym.model.User;
import com.cym.sqlhelper.bean.Page;
import com.cym.sqlhelper.utils.ConditionAndWrapper;
import com.cym.sqlhelper.utils.ConditionOrWrapper;
import com.cym.sqlhelper.utils.SqlHelper;
import com.cym.utils.BeanExtUtil;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;

@Service
public class RepositoryService {

	@Inject
	SqlHelper sqlHelper;
	@Inject
	HomeConfig homeConfig;

	public Page search(Page page, String keywords) {
		ConditionAndWrapper conditionAndWrapper = new ConditionAndWrapper();

		if (StrUtil.isNotEmpty(keywords)) {
			conditionAndWrapper.and(new ConditionOrWrapper().like(Repository::getName, keywords));
		}

		Page pageResp = sqlHelper.findPage(conditionAndWrapper, page, Repository.class);

		return pageResp;
	}

	public Repository getByName(String name, String repositoryId) {
		ConditionAndWrapper conditionAndWrapper = new ConditionAndWrapper().eq(Repository::getName, name);
		if (StrUtil.isNotEmpty(repositoryId)) {
			conditionAndWrapper.ne(Repository::getId, repositoryId);
		}

		return sqlHelper.findOneByQuery(conditionAndWrapper, Repository.class);
	}

	public void deleteById(String repositoryId) {
		Repository repository = sqlHelper.findById(repositoryId, Repository.class);
		String dir = homeConfig.home + "/repo/" + repository.getName();
		FileUtil.del(dir);

		sqlHelper.deleteById(repositoryId, Repository.class);
		sqlHelper.deleteByQuery(new ConditionAndWrapper().eq(RepositoryGroup::getRepositoryId, repositoryId), RepositoryGroup.class);
		sqlHelper.deleteByQuery(new ConditionAndWrapper().eq(RepositoryUser::getRepositoryId, repositoryId), RepositoryUser.class);

	}

	public void insertOrUpdate(Repository repository, Boolean del) {

		if (StrUtil.isEmpty(repository.getId())) {
			// 创建仓库
			String dir = homeConfig.home + "/repo/" + repository.getName();
			if (del) {
				FileUtil.del(dir);
				FileUtil.mkdir(dir);

				try {
					String rs = RuntimeUtil.execForStr("svnadmin", "create", dir);
					System.out.println(rs);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

			// 拷贝配置文件
			String svnserve_conf = homeConfig.home + "/repo/" + repository.getName() + "/conf/svnserve.conf";
			ClassPathResource resource = new ClassPathResource("file/svnserve.conf");
			FileUtil.writeFromStream(resource.getStream(), svnserve_conf);
		}

		sqlHelper.insertOrUpdate(repository);

	}

	public List<UserExt> getUserExts(String id) {
		List<RepositoryUser> repositoryUsers = sqlHelper.findListByQuery(new ConditionAndWrapper().ne(RepositoryUser::getPermission, "n").eq(RepositoryUser::getRepositoryId, id),
				RepositoryUser.class);
		List<UserExt> userExts = new ArrayList<>();
		for (RepositoryUser repositoryUser : repositoryUsers) {
			User user = sqlHelper.findById(repositoryUser.getUserId(), User.class);
			UserExt userExt = BeanExtUtil.copyNewByProperties(user, UserExt.class);
			userExt.setPermission(repositoryUser.getPermission());
			userExts.add(userExt);
		}
		return userExts;
	}

	public List<GroupExt> getGroupExts(String id) {
		List<RepositoryGroup> repositoryGroups = sqlHelper.findListByQuery(new ConditionAndWrapper().ne(RepositoryUser::getPermission, "n").eq(RepositoryGroup::getRepositoryId, id),
				RepositoryGroup.class);
		List<GroupExt> groupExts = new ArrayList<>();
		for (RepositoryGroup repositoryGroup : repositoryGroups) {
			Group group = sqlHelper.findById(repositoryGroup.getGroupId(), Group.class);
			GroupExt groupExt = BeanExtUtil.copyNewByProperties(group, GroupExt.class);
			groupExt.setPermission(repositoryGroup.getPermission());
			groupExts.add(groupExt);
		}
		return groupExts;
	}

	public Page userPermission(Page page, String repositoryId) {
		return sqlHelper.findPage(new ConditionAndWrapper().eq(RepositoryUser::getRepositoryId, repositoryId), page, RepositoryUser.class);
	}

	public Page groupPermission(Page page, String repositoryId) {
		return sqlHelper.findPage(new ConditionAndWrapper().eq(RepositoryGroup::getRepositoryId, repositoryId), page, RepositoryGroup.class);
	}

	public String getUserPermission(String userId, String repositoryId) {

		RepositoryUser repositoryUser = sqlHelper.findOneByQuery(new ConditionAndWrapper().eq(RepositoryUser::getUserId, userId).eq(RepositoryUser::getRepositoryId, repositoryId),
				RepositoryUser.class);

		return repositoryUser.getPermission();
	}

	public String getGroupPermission(String groupId, String repositoryId) {
		RepositoryGroup repositoryGroup = sqlHelper.findOneByQuery(new ConditionAndWrapper().eq(RepositoryGroup::getGroupId, groupId).eq(RepositoryUser::getRepositoryId, repositoryId),
				RepositoryGroup.class);

		return repositoryGroup.getPermission();
	}

	public void addUser(RepositoryUser repositoryUser) {
		sqlHelper.insertOrUpdate(repositoryUser);

	}

	public void delUser(String repositoryUserId) {
		sqlHelper.deleteById(repositoryUserId, RepositoryUser.class);

	}

	public void addGroup(RepositoryGroup repositoryGroup) {
		sqlHelper.insertOrUpdate(repositoryGroup);

	}

	public void delGroup(String repositoryGroupId) {
		sqlHelper.deleteById(repositoryGroupId, RepositoryGroup.class);
	}

	public Boolean hasUser(String userId, String path, String repositoryId, String id) {
		ConditionAndWrapper conditionAndWrapper = new ConditionAndWrapper()//
				.eq(RepositoryUser::getRepositoryId, repositoryId)//
				.eq(RepositoryUser::getPath, path)//
				.eq(RepositoryUser::getUserId, userId);
		if (StrUtil.isNotEmpty(id)) {
			conditionAndWrapper.ne(RepositoryUser::getId, id);
		}
		return sqlHelper.findCountByQuery(conditionAndWrapper, RepositoryUser.class) > 0;
	}

	public Boolean hasGroup(String groupId, String path, String repositoryId, String id) {
		ConditionAndWrapper conditionAndWrapper = new ConditionAndWrapper()//
				.eq(RepositoryGroup::getPath, path) //
				.eq(RepositoryGroup::getRepositoryId, repositoryId) //
				.eq(RepositoryGroup::getGroupId, groupId);
		if (StrUtil.isNotEmpty(id)) {
			conditionAndWrapper.ne(RepositoryGroup::getId, id);
		}
		return sqlHelper.findCountByQuery(conditionAndWrapper, RepositoryGroup.class) > 0;
	}

	public List<RepositoryUser> getListByUser(String userId) {

		List<RepositoryUser> list = sqlHelper.findListByQuery(new ConditionAndWrapper().eq(RepositoryUser::getUserId, userId), RepositoryUser.class);

		List<String> groupIds = sqlHelper.findPropertiesByQuery(new ConditionAndWrapper().eq(GroupUser::getUserId, userId), GroupUser.class, GroupUser::getGroupId);

		List<RepositoryGroup> groups = sqlHelper.findListByQuery(new ConditionAndWrapper().in(RepositoryGroup::getGroupId, groupIds), RepositoryGroup.class);
		for (RepositoryGroup repositoryGroup : groups) {
			RepositoryUser repositoryUser = new RepositoryUser();
			repositoryUser.setUserId(userId);
			repositoryUser.setRepositoryId(repositoryGroup.getRepositoryId());
			repositoryUser.setPermission(repositoryGroup.getPermission());
			repositoryUser.setPath(repositoryGroup.getPath());

			list.add(repositoryUser);
		}

		return list;
	}

	public Boolean hasDir(String name) {
		String dir = homeConfig.home + "/repo/" + name;
		return FileUtil.exist(dir);
	}

	public void scan() {
		File dir = new File(homeConfig.home + "/repo/");
		for (File file : dir.listFiles()) {
			if (FileUtil.isDirectory(file) && !file.getName().equalsIgnoreCase("conf")) {
				Long count = sqlHelper.findCountByQuery(new ConditionAndWrapper().eq(Repository::getName, file.getName()), Repository.class);
				if (count == 0) {
					Repository repository = new Repository();
					repository.setName(file.getName());
					insertOrUpdate(repository, false);
				}
			}
		}
	}

}
