package com.cym.config;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.cym.controller.ConfigController;
import com.cym.service.SettingService;
import com.cym.utils.FilePermissionUtil;
import com.cym.utils.JarUtil;
import com.cym.utils.SystemTool;

import cn.craccd.sqlHelper.utils.SqlHelper;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;

@Configuration
@ComponentScan(basePackages = { "cn.craccd" })
public class SqlConfig {
	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	SqlHelper sqlHelper;

	@Value("${project.home}")
	public String home;
	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	ConfigController configController;
	@Autowired
	SettingService settingService;

	@PostConstruct
	public void init() {
		// 获取盘符
		if (SystemTool.isWindows() && !home.contains(":")) {
			home = JarUtil.getCurrentFilePath().split(":")[0] + ":" + home;
		}

		// 检查目录读写权限
		if (!FilePermissionUtil.canWrite(new File(home))) {
			logger.error(home + " " + "目录没有可写权限,请重新指定.");
			SpringApplication.exit(applicationContext);
		}

		// 初始化svn端口
		if (settingService.get("port") == null) {
			settingService.set("port", "3690");
		}

		// 创建仓库目录
		FileUtil.mkdir(home + File.separator + "repo");
		FileUtil.mkdir(home + File.separator + "temp");

		// 判断是否是容器中
		if (inDocker()) {
			configController.start(settingService.get("port"));
		}
	}

	/**
	 * 是否在docker中
	 * 
	 * @return
	 */
	private Boolean inDocker() {
		if (SystemTool.isLinux()) {
			List<String> rs = RuntimeUtil.execForLines("cat /proc/1/cgroup");
			for (String str : rs) {
				if (str.contains("docker")) {
					logger.info("I am in docker");
					return true;
				}
				if (str.contains("kubepods")) {
					logger.info("I am in k8s");
					return true;
				}
			}
		}

		logger.info("I am not in docker");
		return false;
	}
}
