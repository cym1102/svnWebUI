package com.cym.service;

import org.noear.solon.annotation.Inject;
import org.noear.solon.extend.aspect.annotation.Service;

import com.cym.model.GroupUser;
import com.cym.model.RepositoryUser;
import com.cym.model.User;
import com.cym.sqlhelper.bean.Page;
import com.cym.sqlhelper.utils.ConditionAndWrapper;
import com.cym.sqlhelper.utils.ConditionOrWrapper;
import com.cym.sqlhelper.utils.SqlHelper;

import cn.hutool.core.util.StrUtil;

@Service
public class UserService {
	@Inject
	SqlHelper sqlHelper;

	public User login(String name, String pass) {
		ConditionAndWrapper conditionAndWrapper = new ConditionAndWrapper().eq(User::getName, name).eq(User::getPass, pass);

		return sqlHelper.findOneByQuery(conditionAndWrapper, User.class);
	}

	public Page<User> search(Page page, String keywords) {
		ConditionAndWrapper conditionAndWrapper = new ConditionAndWrapper();

		if (StrUtil.isNotEmpty(keywords)) {
			conditionAndWrapper.and(new ConditionOrWrapper().like(User::getName, keywords));
		}

		Page<User> pageResp = sqlHelper.findPage(conditionAndWrapper, page, User.class);

		return pageResp;
	}

	public void deleteById(String userId) {
		sqlHelper.deleteById(userId, User.class);
		sqlHelper.deleteByQuery(new ConditionAndWrapper().eq(GroupUser::getUserId, userId), GroupUser.class);
		sqlHelper.deleteByQuery(new ConditionAndWrapper().eq(RepositoryUser::getUserId, userId), RepositoryUser.class);
	}

	public User getByName(String name, String userId) {
		ConditionAndWrapper conditionAndWrapper = new ConditionAndWrapper().eq(User::getName, name);
		if (StrUtil.isNotEmpty(userId)) {
			conditionAndWrapper.ne(User::getId, userId);
		}

		return sqlHelper.findOneByQuery(conditionAndWrapper, User.class);
	}

	public void importUser(String name, String pass) {
		Long count = sqlHelper.findCountByQuery(new ConditionAndWrapper().eq(User::getName, name), User.class);
		if (count == 0) {
			User user = new User();
			user.setName(name);
			user.setPass(pass);
			user.setTrueName(name);
			user.setType(0);
			sqlHelper.insert(user);
		}

	}

}
