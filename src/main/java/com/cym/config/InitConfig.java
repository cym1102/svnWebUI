package com.cym.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Init;
import org.noear.solon.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.controller.ConfigController;
import com.cym.model.User;
import com.cym.service.SettingService;
import com.cym.sqlhelper.utils.SqlHelper;
import com.cym.utils.FilePermissionUtil;
import com.cym.utils.HttpdUtils;
import com.cym.utils.SystemTool;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.RuntimeUtil;

@Component
public class InitConfig {
	Logger logger = LoggerFactory.getLogger(InitConfig.class);
	@Inject
	HomeConfig homeConfig;
	@Inject
	ConfigController configController;
	@Inject
	VersionConfig versionConfig;
	@Inject
	SettingService settingService;
	@Inject("${project.findPass}")
	Boolean findPass;
	@Inject
	SqlHelper sqlHelper;
	@Inject
	HttpdUtils httpdUtils;

	@Init
	public void init() {
		// 打印密码
		if (findPass) {
			List<User> users = sqlHelper.findAll(User.class);
			for (User user : users) {
				System.out.println("用户名:" + user.getName() + " 密码:" + user.getPass());
			}
			System.exit(1);
		}

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
		if (SystemTool.inDocker()) {
			// 修改端口号
			httpdUtils.modHttpdPort(settingService.get("port"));
			// 目录授权
			RuntimeUtil.execForStr("chown apache.apache -R " + homeConfig.home + File.separator + "repo/");
			// 启动httpd
			httpdUtils.start();
		} else {
			// 启动svnserve
			configController.start(settingService.get("port"));
		}

		// 展示logo
		try {
			showLogo();
		} catch (IOException e) {
			logger.info(e.getMessage(), e);
		}
	}

	/**
	 * 显示logo
	 * 
	 * @throws IOException
	 */
	private void showLogo() throws IOException {
		ClassPathResource resource = new ClassPathResource("banner.txt");
		BufferedReader reader = resource.getReader(Charset.forName("utf-8"));
		String str = null;
		StringBuilder stringBuilder = new StringBuilder();
		// 使用readLine() 比较方便的读取一行
		while (null != (str = reader.readLine())) {
			stringBuilder.append(str + "\n");
		}
		reader.close();// 关闭流

		stringBuilder.append("svnWebUI " + versionConfig.currentVersion + "\n");

		logger.info(stringBuilder.toString());

	}
}
