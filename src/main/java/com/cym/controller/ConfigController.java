package com.cym.controller;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.config.HomeConfig;
import com.cym.model.WebHook;
import com.cym.service.ConfigService;
import com.cym.service.SettingService;
import com.cym.service.WebHookService;
import com.cym.utils.BaseController;
import com.cym.utils.HttpdUtils;
import com.cym.utils.JsonResult;
import com.cym.utils.SystemTool;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.json.JSONUtil;

@Controller
@Mapping("/adminPage/config")
public class ConfigController extends BaseController {
	Logger logger = LoggerFactory.getLogger(ConfigController.class);

	
	@Inject
	SettingService settingService;
	@Inject
	HomeConfig homeConfig;
	@Inject
	ConfigService configService;
	@Inject
	HttpdUtils httpdUtils;
	@Inject
	WebHookService webHookService;

	@Mapping("")
	public ModelAndView index() {
		boolean hasSvnserve = false;

		if (SystemTool.inDocker()) {
			hasSvnserve = true;
		} else {
			if (SystemTool.isWindows()) {
				String[] command = { "where svnserve" };
				String rs = RuntimeUtil.execForStr(command);
				hasSvnserve = rs.contains("svnserve.exe");
			} else {
				String[] command = { "which svnserve" };
				String rs = RuntimeUtil.execForStr(command);
				hasSvnserve = rs.contains("svnserve");
			}
		}

		ModelAndView modelAndView = new ModelAndView("/adminPage/config/index.html");
		modelAndView.put("port", settingService.get("port"));
		modelAndView.put("hasSvnserve", hasSvnserve);
		return modelAndView;
	}

	@Mapping("getStatus")
	public JsonResult getStatus() {
		Boolean isRun = false;
		String status = "";

		if (SystemTool.inDocker()) {
			String[] command = { "/bin/sh", "-c", "ps -ef|grep httpd" };
			String rs = RuntimeUtil.execForStr(command);
			isRun = rs.contains("httpd -k start");
		} else {
			if (SystemTool.isWindows()) {
				String[] command = { "tasklist" };
				String rs = RuntimeUtil.execForStr(command);
				isRun = rs.toLowerCase().contains("svnserve");
			} else {
				String[] command = { "/bin/sh", "-c", "ps -ef|grep svnserve" };
				String rs = RuntimeUtil.execForStr(command);
				isRun = rs.contains("svnserve -d -r");
			}
		}

		if (isRun) {
			status = "<span class='green'>已启动</span>";
		} else {
			status = "<span class='red'>未启动</span>";
		}

		return renderSuccess(status);
	}

	@Mapping("getWebHook")
	public JsonResult getWebHook() {
		WebHook webHook = webHookService.get();
		return renderSuccess(webHook);
	}

	@Mapping("saveHook")
	public JsonResult saveHook(WebHook webHook) {
		WebHook webHookOrg = webHookService.get();
		webHookOrg.setOpen(webHook.getOpen());
		webHookOrg.setUrl(webHook.getUrl());
		webHookOrg.setPassword(webHook.getPassword());
		sqlHelper.updateById(webHookOrg);

		return renderSuccess();
	}

	@Mapping("start")
	public JsonResult start(String port) {
		settingService.set("port", port);

		if (SystemTool.inDocker()) {
			httpdUtils.modHttpdPort(port);
			httpdUtils.start();
		} else {
			if (SystemTool.isWindows()) {
				String home = homeConfig.home;

				// 释放vbs
				String cmd = "svnserve.exe -d -r " + (home + File.separator + "repo").replace("/", "\\") + " --listen-port " + port;
				List<String> vbs = new ArrayList<>();
				vbs.add("set ws=WScript.CreateObject(\"WScript.Shell\")");
				vbs.add("ws.Run \"" + cmd + " \",0");
				FileUtil.writeLines(vbs, home + "/run.vbs", Charset.forName("UTF-8"));

				RuntimeUtil.execForStr("wscript " + home + "/run.vbs");
			} else {
				RuntimeUtil.execForStr("svnserve -d -r " + homeConfig.home + "/repo --listen-port " + port);
			}
		}
		return renderSuccess();
	}

	@Mapping("stop")
	public JsonResult stop() {
		if (SystemTool.inDocker()) {
			httpdUtils.stop();
		} else {
			if (SystemTool.isWindows()) {
				RuntimeUtil.execForStr("taskkill /f /im svnserve.exe");
			} else {
				RuntimeUtil.execForStr("pkill svnserve");
			}
		}
		return renderSuccess();
	}

	@Mapping("test.js")
	public JsonResult test(Context ctx) throws IOException {
		logger.info(ctx.body());
		return renderSuccess();
	}

}
