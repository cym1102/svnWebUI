package com.cym.utils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.noear.solon.annotation.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.config.InitConfig;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;

@Component
public class HttpdUtils {
	Logger logger = LoggerFactory.getLogger(HttpdUtils.class);
	
	public void modHttpdPort(String port) {
		List<String> lines = FileUtil.readLines(new File("/etc/httpd/conf/httpd.conf"), Charset.forName("UTF-8"));
		List<String> reLines = new ArrayList<String>();
		for (String line : lines) {
			if (line.startsWith("Listen ")) {
				line = "Listen " + port;
			}
			reLines.add(line);
		}
		FileUtil.writeLines(reLines, new File("/etc/httpd/conf/httpd.conf"), Charset.forName("UTF-8"));
	}

	public void start() {
		String rs = RuntimeUtil.execForStr("httpd -k start");
		logger.info(rs);
	}

	public void stop() {
		RuntimeUtil.execForStr("pkill httpd");
	}

}
