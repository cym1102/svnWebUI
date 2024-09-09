package com.cym.test;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Init;
import org.noear.solon.annotation.Inject;

import com.cym.config.HomeConfig;
import com.cym.model.User;
import com.cym.sqlhelper.utils.ConditionAndWrapper;
import com.cym.sqlhelper.utils.SqlHelper;

import cn.hutool.core.util.RuntimeUtil;

@Component
public class TestUtils {

	@Inject
	SqlHelper sqlHelper;
	@Inject
	HomeConfig homeConfig;
	
	@Init
	public void test() {
	}
	
	
	public static void main(String[] args) {
		
	}
}
