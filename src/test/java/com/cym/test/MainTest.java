package com.cym.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.cym.SvnWebUI;
import com.cym.config.SqlConfig;
import com.cym.ext.Path;
import com.cym.model.Repository;
import com.cym.service.RepositoryService;
import com.cym.utils.JarUtil;
import com.cym.utils.SystemTool;

import cn.craccd.sqlHelper.utils.SnowFlake;
import cn.craccd.sqlHelper.utils.SqlHelper;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.json.JSONUtil;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SvnWebUI.class)
public class MainTest {
	@Autowired
	SqlConfig sqlConfig;
	@Autowired
	SqlHelper sqlHelper;
	@Autowired
	RepositoryService repositoryService;
	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Test
	public void testStartUp() {
		String name = "${java:os}";
		logger.info("hello:{}" , name); 
	}

	public static void main(String[] args) {

	}
}
