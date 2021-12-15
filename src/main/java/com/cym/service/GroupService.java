package com.cym.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cym.model.Group;
import com.cym.model.GroupUser;
import com.cym.model.RepositoryGroup;
import com.cym.model.User;

import cn.craccd.sqlHelper.bean.Page;
import cn.craccd.sqlHelper.utils.ConditionAndWrapper;
import cn.craccd.sqlHelper.utils.ConditionOrWrapper;
import cn.craccd.sqlHelper.utils.SqlHelper;
import cn.hutool.core.util.StrUtil;

@Service
public class GroupService {

	@Autowired
	SqlHelper sqlHelper;

	public Page search(Page page, String keywords) {
		ConditionAndWrapper conditionAndWrapper = new ConditionAndWrapper();

		if (StrUtil.isNotEmpty(keywords)) {
			conditionAndWrapper.and(new ConditionOrWrapper().like(Group::getName, keywords));
		}

		Page<Group> pageResp = sqlHelper.findPage(conditionAndWrapper, page, Group.class);

		return pageResp;
	}

	public Group getByName(String name, String groupId) {
		ConditionAndWrapper conditionAndWrapper = new ConditionAndWrapper().eq(Group::getName, name);
		if (StrUtil.isNotEmpty(groupId)) {
			conditionAndWrapper.ne(Group::getId, groupId);
		}

		return sqlHelper.findOneByQuery(conditionAndWrapper, Group.class);
	}

	public void deleteById(String groupId) {
		sqlHelper.deleteById(groupId, Group.class);
		sqlHelper.deleteByQuery(new ConditionAndWrapper().eq(GroupUser::getGroupId, groupId), GroupUser.class);
		sqlHelper.deleteByQuery(new ConditionAndWrapper().eq(RepositoryGroup::getGroupId, groupId), RepositoryGroup.class);
	}

	public Boolean isUserSelect(String userId, String groupId) {
		return sqlHelper.findCountByQuery(new ConditionAndWrapper().eq(GroupUser::getUserId, userId).eq(GroupUser::getGroupId, groupId), GroupUser.class) > 0;
	}

	public void insertOrUpdate(Group group, String[] userIds) {
		sqlHelper.insertOrUpdate(group);

		sqlHelper.deleteByQuery(new ConditionAndWrapper().eq(GroupUser::getGroupId, group.getId()), GroupUser.class);
		if (userIds != null) {
			for (String userId : userIds) {
				GroupUser groupUser = new GroupUser();
				groupUser.setUserId(userId);
				groupUser.setGroupId(group.getId());
				sqlHelper.insert(groupUser);
			}
		}
	}

	public List<User> getUserList(String groupId) {
		List<String> userIds = sqlHelper.findPropertiesByQuery(new ConditionAndWrapper().eq(GroupUser::getGroupId, groupId), GroupUser.class, GroupUser::getUserId);
		return sqlHelper.findListByIds(userIds, User.class);
	}

	public void importGroup(String name, String users) {
		Group group = sqlHelper.findOneByQuery(new ConditionAndWrapper().eq(Group::getName, name), Group.class);
		if (group == null) {
			group = new Group();
			group.setName(name);
			sqlHelper.insert(group);
		}
		
		if (StrUtil.isNotEmpty(users)) {
			String[] userNames = users.split(",");
			for (String userName : userNames) {
				User user = sqlHelper.findOneByQuery(new ConditionAndWrapper().eq(User::getName, userName.trim()), User.class);
				if (user != null) {
					GroupUser groupUser = new GroupUser();
					groupUser.setGroupId(group.getId());
					groupUser.setUserId(user.getId());
					sqlHelper.insert(groupUser);
				}
			}
		}

	}

}
