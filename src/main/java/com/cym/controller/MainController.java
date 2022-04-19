package com.cym.controller;

import java.io.File;
import java.io.IOException;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.config.HomeConfig;
import com.cym.config.InitConfig;
import com.cym.utils.BaseController;
import com.cym.utils.JarUtil;
import com.cym.utils.JsonResult;
import com.cym.utils.SystemTool;

import cn.hutool.core.io.FileUtil;

@Controller
@Mapping("")
public class MainController extends BaseController {
	static Logger logger = LoggerFactory.getLogger(MainController.class);
	@Inject
	InitConfig projectConfig;
	
	@Inject
	HomeConfig homeConfig;
	
	@Mapping("/")
	public void jump(Context ctx) {
		ctx.redirect("/adminPage/login");
	}

	@Mapping("/adminPage/main/upload")
	public JsonResult upload(Context context, UploadedFile file) {
		try {
			File temp = new File(FileUtil.getTmpDir() + "/" + file.name.replace(" ", "_"));
			file.transferTo(temp);

			// 移动文件
			File dest = new File(homeConfig.home + "temp/" + file.name.replace(" ", "_")); 
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
