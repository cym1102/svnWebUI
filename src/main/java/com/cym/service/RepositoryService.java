package com.cym.service;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.noear.solon.annotation.Inject;
import org.noear.solon.extend.aspect.annotation.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.config.HomeConfig;
import com.cym.ext.GroupExt;
import com.cym.ext.UserExt;
import com.cym.model.Group;
import com.cym.model.GroupUser;
import com.cym.model.Repository;
import com.cym.model.RepositoryGroup;
import com.cym.model.RepositoryUser;
import com.cym.model.User;
import com.cym.sqlhelper.bean.Page;
import com.cym.sqlhelper.bean.Sort;
import com.cym.sqlhelper.bean.Sort.Direction;
import com.cym.sqlhelper.utils.ConditionAndWrapper;
import com.cym.sqlhelper.utils.ConditionOrWrapper;
import com.cym.sqlhelper.utils.SqlHelper;
import com.cym.utils.BeanExtUtil;
import com.cym.utils.SystemTool;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;

@Service
public class RepositoryService {
	Logger logger = LoggerFactory.getLogger(getClass());
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
		String dir = homeConfig.home + "repo" + File.separator + repository.getName();
		FileUtil.del(dir);

		sqlHelper.deleteById(repositoryId, Repository.class);
		sqlHelper.deleteByQuery(new ConditionAndWrapper().eq(RepositoryGroup::getRepositoryId, repositoryId), RepositoryGroup.class);
		sqlHelper.deleteByQuery(new ConditionAndWrapper().eq(RepositoryUser::getRepositoryId, repositoryId), RepositoryUser.class);

	}

	public void insertOrUpdate(String name) {

		// 创建仓库
		String dir = homeConfig.home + "repo" + File.separator + name;
		if (!FileUtil.exist(dir + File.separator + "db")) {
			ClassPathResource resource = new ClassPathResource("file/repo.zip");
			InputStream inputStream = resource.getStream();
			File temp = new File(homeConfig.home + "temp" + File.separator + "repo.zip");
			FileUtil.writeFromStream(inputStream, temp);
			FileUtil.mkdir(dir);
			ZipUtil.unzip(temp, new File(dir));
			FileUtil.del(temp);
		}

		Repository repository = new Repository();
		repository.setName(name);
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

	public Page userPermission(Page page, String repositoryId, String keywords, String order) {
		ConditionAndWrapper conditionAndWrapper = new ConditionAndWrapper().eq(RepositoryUser::getRepositoryId, repositoryId);
		if (StrUtil.isNotEmpty(keywords)) {
			ConditionOrWrapper conditionOrWrapper = new ConditionOrWrapper();
			List<String> userIds = sqlHelper.findIdsByQuery(new ConditionOrWrapper().like(User::getName, keywords).like(User::getTrueName, keywords), User.class);
			if (userIds.size() > 0) {
				conditionOrWrapper.in(RepositoryUser::getUserId, userIds);
			}
			List<String> repositoryIds = sqlHelper.findIdsByQuery(new ConditionOrWrapper().like(Repository::getName, keywords), Repository.class);
			if (repositoryIds.size() > 0) {
				conditionOrWrapper.in(RepositoryUser::getRepositoryId, repositoryIds);
			}

			conditionOrWrapper.like(RepositoryUser::getPath, keywords);
			conditionAndWrapper.and(conditionOrWrapper);
		}

		Sort sort = new Sort();
		if (order.equals("time")) {
			sort.add(RepositoryUser::getId, Direction.DESC);
		}
		if (order.equals("path")) {
			sort.add(RepositoryUser::getPath, Direction.ASC);
		}

		return sqlHelper.findPage(conditionAndWrapper, sort, page, RepositoryUser.class);
	}

	public Page groupPermission(Page page, String repositoryId, String keywords, String order) {
		ConditionAndWrapper conditionAndWrapper = new ConditionAndWrapper().eq(RepositoryGroup::getRepositoryId, repositoryId);
		if (StrUtil.isNotEmpty(keywords)) {
			ConditionOrWrapper conditionOrWrapper = new ConditionOrWrapper();
			List<String> groupIds = sqlHelper.findIdsByQuery(new ConditionOrWrapper().like(Group::getName, keywords), Group.class);
			if (groupIds.size() > 0) {
				conditionOrWrapper.in(RepositoryGroup::getGroupId, groupIds);
			}
			List<String> repositoryIds = sqlHelper.findIdsByQuery(new ConditionOrWrapper().like(Repository::getName, keywords), Repository.class);
			if (repositoryIds.size() > 0) {
				conditionOrWrapper.in(RepositoryUser::getRepositoryId, repositoryIds);
			}

			conditionOrWrapper.like(RepositoryGroup::getPath, keywords);
			conditionAndWrapper.and(conditionOrWrapper);
		}

		Sort sort = new Sort();
		if (order.equals("time")) {
			sort.add(RepositoryGroup::getId, Direction.DESC);
		}
		if (order.equals("path")) {
			sort.add(RepositoryGroup::getPath, Direction.ASC);
		}

		return sqlHelper.findPage(conditionAndWrapper, sort, page, RepositoryGroup.class);
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
		String dir = homeConfig.home + "repo" + File.separator + name;
		return FileUtil.exist(dir);
	}

	public void scan() {
		File dir = new File(homeConfig.home + "repo" + File.separator);
		for (File file : dir.listFiles()) {
			if (FileUtil.isDirectory(file) //
					&& FileUtil.exist(file.getPath() + File.separator + "conf") //
					&& FileUtil.exist(file.getPath() + File.separator + "db") //
					&& FileUtil.exist(file.getPath() + File.separator + "hooks") //
					&& FileUtil.exist(file.getPath() + File.separator + "locks")) {
				Long count = sqlHelper.findCountByQuery(new ConditionAndWrapper().eq(Repository::getName, file.getName()), Repository.class);
				if (count == 0) {
					insertOrUpdate(file.getName());
				}
			}
		}
	}

	public Repository getByUrl(String url) {

		String[] urls = url.split("/");
		String name = urls[3];

		return sqlHelper.findOneByQuery(new ConditionAndWrapper().eq(Repository::getName, name), Repository.class);
	}

	public void allPermissionOver(String id, String allPermission) {
		Repository repository = sqlHelper.findById(id, Repository.class);
		repository.setAllPermission(allPermission);
		sqlHelper.updateById(repository);
	}

	public List<Repository> getListByAll() {

		return sqlHelper.findListByQuery(new ConditionOrWrapper().eq(Repository::getAllPermission, "r").eq(Repository::getAllPermission, "rw"), Repository.class);
	}

}
