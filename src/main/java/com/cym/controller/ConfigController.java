package com.cym.controller;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.cym.config.SqlConfig;
import com.cym.service.SettingService;
import com.cym.utils.BaseController;
import com.cym.utils.JarUtil;
import com.cym.utils.JsonResult;
import com.cym.utils.SystemTool;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;

@Controller
@RequestMapping("/adminPage/config")
public class ConfigController extends BaseController {

	@Autowired
	SqlConfig sqlConfig;
	@Autowired
	SettingService settingService;

	@RequestMapping("")
	public ModelAndView index(HttpSession httpSession, ModelAndView modelAndView) {
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

		modelAndView.addObject("port", settingService.get("port"));
		modelAndView.addObject("hasSvnserve", hasSvnserve);
		modelAndView.setViewName("/adminPage/config/index");
		return modelAndView;
	}

	@RequestMapping("getStatus")
	@ResponseBody
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

	@RequestMapping("start")
	@ResponseBody
	public JsonResult start(String port) {
		settingService.set("port", port);

		if (SystemTool.isWindows()) {
			String home = sqlConfig.home;
//			if (!home.contains(":")) {
//				// 获取盘符
//				home = JarUtil.getCurrentFilePath().split(":")[0] + ":" + home;
//			}

			// 释放vbs
			String cmd = "svnserve.exe -d -r " + (home + File.separator + "repo").replace("/", "\\") + " --listen-port " + port;
			List<String> vbs = new ArrayList<>();
			vbs.add("set ws=WScript.CreateObject(\"WScript.Shell\")");
			vbs.add("ws.Run \"" + cmd + " \",0");
			FileUtil.writeLines(vbs, home + "/run.vbs", Charset.forName("UTF-8"));

			RuntimeUtil.execForStr("wscript " + home + "/run.vbs");
		} else {
			RuntimeUtil.execForStr("svnserve -d -r " + sqlConfig.home + "/repo --listen-port " + port);
		}
		return renderSuccess();
	}

	@RequestMapping("stop")
	@ResponseBody
	public JsonResult stop() {
		if (SystemTool.isWindows()) {
			RuntimeUtil.execForStr("taskkill /f /im svnserve.exe");
		} else {
			RuntimeUtil.execForStr("pkill svnserve");
		}
		return renderSuccess();
	}

}
