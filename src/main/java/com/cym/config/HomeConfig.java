package com.cym.config;

import java.io.File;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Init;
import org.noear.solon.annotation.Inject;

import com.cym.utils.JarUtil;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

@Component
public class HomeConfig {
	@Inject("${project.home}")
	public String home;

	@Init(index = 0)
	public void init() {
		if (StrUtil.isEmpty(home)) {
			// 获取jar位置
			File file = new File(JarUtil.getCurrentFilePath());

			if (file.getPath().contains("target") && file.getPath().contains("classes")) {
				home = FileUtil.getUserHomePath() + File.separator + "svnWebUI";
			} else {
				home = file.getParent();
			}
		}
	}

}
