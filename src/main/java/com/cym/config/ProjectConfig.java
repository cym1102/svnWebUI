package com.cym.config;

import java.io.File;
import java.util.List;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Init;
import org.noear.solon.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.controller.ConfigController;
import com.cym.service.SettingService;
import com.cym.utils.FilePermissionUtil;
import com.cym.utils.JarUtil;
import com.cym.utils.SystemTool;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;

@Component
public class ProjectConfig {
	static Logger logger = LoggerFactory.getLogger(ProjectConfig.class);
	@Inject
	HomeConfig homeConfig;
	@Inject
	ConfigController configController;

	@Inject
	SettingService settingService;

	@Init(index = 30)
	public void init() {
		// 初始化svn端口
		if (settingService.get("port") == null) {
			settingService.set("port", "3690");
		}

		// 检查目录读写权限
		if (!FilePermissionUtil.canWrite(new File(homeConfig.home))) {
			logger.error(homeConfig.home + " " + "目录没有可写权限,请重新指定.");
			return;
		}

		// 创建仓库目录
		FileUtil.mkdir(homeConfig.home + File.separator + "repo");
		FileUtil.mkdir(homeConfig.home + File.separator + "temp");

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
//			List<String> rs = RuntimeUtil.execForLines("cat /proc/1/cgroup");
//			for (String str : rs) {
//				if (str.contains("docker")) {
//					logger.info("I am in docker");
//					return true;
//				}
//				if (str.contains("kubepods")) {
//					logger.info("I am in k8s");
//					return true;
//				}
//			}

			if (FileUtil.exist("/usr/local/bin/entrypoint.sh")) {
				logger.info("I am in docker");
				return true;
			}
		}

		logger.info("I am not in docker");
		return false;
	}
}
