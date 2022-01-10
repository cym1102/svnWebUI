package com.cym.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.ModelAndView;

import com.cym.config.HomeConfig;
import com.cym.config.ProjectConfig;
import com.cym.service.SettingService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;
import com.cym.utils.SystemTool;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;

@Controller
@Mapping("/adminPage/config")
public class ConfigController extends BaseController {

	@Inject
	SettingService settingService;
	
	@Inject
	HomeConfig homeConfig;

	@Mapping("")
	public ModelAndView index() {
		boolean hasSvnserve = false;
		if (SystemTool.isWindows()) {
			String[] command = { "where svnserve" };
			String rs = RuntimeUtil.execForStr(command);
			hasSvnserve = rs.contains("svnserve.exe");

		} else {
			String[] command = { "which svnserve" };
			String rs = RuntimeUtil.execForStr(command);
			hasSvnserve = rs.contains("svnserve");
		}

		ModelAndView modelAndView = buildMav("/adminPage/config/index.html");
		modelAndView.put("port", settingService.get("port"));
		modelAndView.put("hasSvnserve", hasSvnserve);
		return modelAndView;
	}

	@Mapping("getStatus")
	public JsonResult getStatus() {
		Boolean isRun = false;
		if (SystemTool.isWindows()) {
			String[] command = { "tasklist" };
			String rs = RuntimeUtil.execForStr(command);
			isRun = rs.toLowerCase().contains("svnserve");
		} else {
			String[] command = { "/bin/sh", "-c", "ps -ef|grep svnserve" };
			String rs = RuntimeUtil.execForStr(command);
			isRun = rs.contains("svnserve -d -r");
		}

		String status = "";
		if (isRun) {
			status = "<span class='green'>已启动</span>";
		} else {
			status = "<span class='red'>未启动</span>";
		}

		return renderSuccess(status);
	}

	@Mapping("start")

	public JsonResult start(String port) {
		settingService.set("port", port);

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
		return renderSuccess();
	}

	@Mapping("stop")
	public JsonResult stop() {
		if (SystemTool.isWindows()) {
			RuntimeUtil.execForStr("taskkill /f /im svnserve.exe");
		} else {
			RuntimeUtil.execForStr("pkill svnserve");
		}
		return renderSuccess();
	}

}
