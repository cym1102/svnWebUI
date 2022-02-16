package com.cym.service;

import java.util.ArrayList;
import java.util.List;

import org.noear.solon.annotation.Inject;
import org.noear.solon.extend.aspect.annotation.Service;

import com.cym.model.Group;
import com.cym.model.GroupGroup;
import com.cym.model.GroupUser;
import com.cym.model.RepositoryGroup;
import com.cym.model.User;
import com.cym.sqlhelper.bean.Page;
import com.cym.sqlhelper.utils.ConditionAndWrapper;
import com.cym.sqlhelper.utils.ConditionOrWrapper;
import com.cym.sqlhelper.utils.SqlHelper;

import cn.hutool.core.util.StrUtil;

@Service
public class GroupService {
	private static ArrayList<String> stack = new ArrayList<>();

	@Inject
	SqlHelper sqlHelper;

	public Page search(Page page, String keywords)  {
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

	public void insertOrUpdate(Group group, String[] userIds, String[] groupIds) {
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

		sqlHelper.deleteByQuery(new ConditionAndWrapper().eq(GroupGroup::getMasterGroupId, group.getId()), GroupGroup.class);
		if (groupIds != null) {
			for (String groupId : groupIds) {
				GroupGroup groupGroup = new GroupGroup();
				groupGroup.setMasterGroupId(group.getId());
				groupGroup.setSlaveGroupId(groupId);
				sqlHelper.insert(groupGroup);
			}
		}
	}

	public List<User> getUserList(String groupId)  {
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

	public List<Group> getGroupList(String groupId) {

		List<String> groupIds = sqlHelper.findPropertiesByQuery(new ConditionAndWrapper().eq(GroupGroup::getMasterGroupId, groupId), GroupGroup.class, GroupGroup::getSlaveGroupId);
		return sqlHelper.findListByIds(groupIds, Group.class);

	}

	public String checkLoop(String topId, String[] groupIds) {
		stack.clear();
		stack.add(topId); // 入栈
		if (isLoopRelate(topId, groupIds)) {
			String link = "";
			for (int i = 0; i < stack.size(); i++) {
				Group group = sqlHelper.findById(stack.get(i), Group.class);
				link += group.getName();
				if (i != stack.size() - 1) {
					link += " > ";
				}
			}

			return link;
		}

		return null;
	}

	/**
	 * 检查是否循环依赖
	 *
	 * @param clazz
	 * @return 是否循环依赖
	 */
	private boolean isLoopRelate(String topId, String[] groupIds) {
		for (String groupId : groupIds) {
			stack.add(groupId); // 入栈

			// 寻找依赖类
			String[] relateIds = findRelateIds(groupId);

			for (String relateId : relateIds) {
				if (relateId.equals(topId)) {
					stack.add(relateId); // 入栈
					return true;
				}
			}

			if (isLoopRelate(topId, relateIds)) {
				return true;
			}

			stack.remove(groupId); // 出栈
		}
		return false;
	}

	private String[] findRelateIds(String groupId) {
		List<String> relateIds = sqlHelper.findPropertiesByQuery(new ConditionAndWrapper().eq(GroupGroup::getMasterGroupId, groupId), GroupGroup.class, GroupGroup::getSlaveGroupId);

		return relateIds.toArray(new String[] {});
	}

}
