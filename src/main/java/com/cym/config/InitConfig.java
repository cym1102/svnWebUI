package com.cym.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Init;
import org.noear.solon.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;

import com.cym.controller.ConfigController;
import com.cym.model.User;
import com.cym.service.ConfigService;
import com.cym.service.SettingService;
import com.cym.service.UserService;
import com.cym.sqlhelper.config.DataSourceEmbed;
import com.cym.sqlhelper.config.Table;
import com.cym.sqlhelper.utils.ConditionAndWrapper;
import com.cym.sqlhelper.utils.SqlHelper;
import com.cym.utils.FilePermissionUtil;
import com.cym.utils.HttpdUtils;
import com.cym.utils.SystemTool;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.ClassUtil;
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
	@Inject("${project.beanPackage}")
	String packageName;
	@Inject
	DataSourceEmbed dataSourceEmbed;
	@Inject
	SqlHelper sqlHelper;
	@Inject
	HttpdUtils httpdUtils;
	@Inject
	ConfigService configService;
	@Inject
	ScheduleTask scheduleTask;
	@Inject
	UserService userService;

	@Init
	public void init() {
		// h2转sqlite
		if (FileUtil.exist(homeConfig.home + "h2.mv.db")) {
			transferSql();
		}
				
		// 打印密码
		if (findPass) {
			List<User> users = sqlHelper.findAll(User.class);
			for (User user : users) {
				System.out.println("用户名:" + user.getName() + " 密码:" + user.getPass());
			}
			System.exit(1);
		}

		// 初始化svn端口和协议
		if (settingService.get("port") == null) {
			settingService.set("port", "3690");
		}
		if (settingService.get("protocol") == null) {
			if (SystemTool.inDocker()) {
				settingService.set("protocol", "http");
			} else {
				settingService.set("protocol", "svn");
			}
		}

		// 检查目录读写权限
		if (!FilePermissionUtil.canWrite(new File(homeConfig.home))) {
			logger.error(homeConfig.home + " " + "目录没有可写权限,请重新指定.");
			return;
		}

		// 创建仓库目录
		FileUtil.mkdir(homeConfig.home + "repo");
		FileUtil.mkdir(homeConfig.home + "temp");

		// 判断是否是容器中
		if (SystemTool.inDocker()) {
			// 释放配置文件
			httpdUtils.releaseFile();
			// 修改端口号配置文件
			httpdUtils.modHttpdPort(settingService.get("port"));
		}

		// 服务端先杀掉启动
		configController.stop();
		configController.start(settingService.get("port"), settingService.get("host"), settingService.get("protocol"));

		// 预热定时任务
		scheduleTask.hookTasks();

		// 刷新配置文件
		configService.refresh();
		
		// 展示logo
		showLogo();

	}

	/**
	 * 显示logo
	 * 
	 * @throws IOException
	 */
	private void showLogo() {
		try {
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
		} catch (IOException e) {
			logger.info(e.getMessage(), e);
		}
	}
	
	private void transferSql() {
		// 关闭sqlite连接
		dataSourceEmbed.getDataSource().close();
		// 建立h2连接
		HikariConfig dbConfig = new HikariConfig();
		dbConfig.setJdbcUrl("jdbc:h2:" + homeConfig.home + "h2");
		dbConfig.setUsername("sa");
		dbConfig.setPassword("");
		dbConfig.setMaximumPoolSize(1);
		HikariDataSource dataSourceH2 = new HikariDataSource(dbConfig);
		dataSourceEmbed.setDataSource(dataSourceH2);
		// 读取全部数据
		Map<String, List<?>> map = readAll();

		// 关闭h2连接
		dataSourceH2.close();

		// 重新建立sqlite连接
		dataSourceEmbed.init();

		// 导入数据
		insertAll(map);

		// 重命名h2文件
		FileUtil.rename(new File(homeConfig.home + "h2.mv.db"), homeConfig.home + "h2.mv.db.bak", true);
	}
	
	private Map<String, List<?>> readAll() {
		Map<String, List<?>> map = new HashMap<>();

		Set<Class<?>> set = ClassUtil.scanPackage(packageName);
		for (Class<?> clazz : set) {
			Table table = clazz.getAnnotation(Table.class);
			if (table != null) {
				map.put(clazz.getName(), sqlHelper.findAll(clazz));
			}
		}

		return map;
	}

	private void insertAll(Map<String, List<?>> map) {
		try {
			for (String key : map.keySet()) {
				sqlHelper.deleteByQuery(new ConditionAndWrapper(), Class.forName(key));

				sqlHelper.insertAll(map.get(key));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}
