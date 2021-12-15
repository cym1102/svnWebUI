package com.cym.controller;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.cym.config.SqlConfig;
import com.cym.utils.BaseController;
import com.cym.utils.JarUtil;
import com.cym.utils.JsonResult;
import com.cym.utils.SystemTool;

import cn.hutool.core.io.FileUtil;

@Controller
@RequestMapping("")
public class MainController extends BaseController {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	SqlConfig sqlConfig;

	@RequestMapping("")
	public String main() {

		return "redirect:/adminPage/login";
	}

	@ResponseBody
	@RequestMapping("/adminPage/main/upload")
	public JsonResult upload(@RequestParam("file") MultipartFile file, HttpSession httpSession) {
		try {
			File temp = new File(FileUtil.getTmpDir() + "/" + file.getOriginalFilename());
			file.transferTo(temp);

			// 移动文件
			File dest = new File(sqlConfig.home + "/temp/" + file.getOriginalFilename());
			FileUtil.move(temp, dest, true);

			String path = dest.getPath();

			if (SystemTool.isWindows() && !path.contains(":")) {
				// 获取盘符
				path = (JarUtil.getCurrentFilePath().split(":")[0] + ":" + path).replace("/", "\\");
			}

			return renderSuccess(path);
		} catch (IllegalStateException | IOException e) {
			logger.error(e.getMessage(), e);
		}

		return renderError();
	}

}
