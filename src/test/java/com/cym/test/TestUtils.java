package com.cym.test;

import org.junit.Test;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Init;
import org.noear.solon.annotation.Inject;

import com.cym.model.User;
import com.cym.sqlhelper.utils.ConditionAndWrapper;
import com.cym.sqlhelper.utils.SqlHelper;

@Component
public class TestUtils {

	@Inject
	SqlHelper sqlHelper;
	
	@Init
	public void test() {
		User user = sqlHelper.findOneByQuery(new ConditionAndWrapper()	, User.class);
		sqlHelper.updateById(user);
	}
}
