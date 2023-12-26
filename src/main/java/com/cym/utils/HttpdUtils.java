package com.cym.utils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.noear.solon.annotation.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.RuntimeUtil;

@Component
public class HttpdUtils {
	Logger logger = LoggerFactory.getLogger(HttpdUtils.class);

	public void modHttpdPort(String port) {
		List<String> lines = FileUtil.readLines(new File("/etc/apache2/httpd.conf"), Charset.forName("UTF-8"));
		List<String> reLines = new ArrayList<String>();
		for (String line : lines) {
			if (line.startsWith("Listen ")) {
				line = "Listen " + port;
			}
			reLines.add(line);
		}
		FileUtil.writeLines(reLines, new File("/etc/apache2/httpd.conf"), Charset.forName("UTF-8"));

	}

	public void releaseFile() {
		ClassPathResource resource = new ClassPathResource("file/dav_svn.conf");
		FileUtil.writeFromStream(resource.getStream(), "/etc/apache2/conf.d/dav_svn.conf");
	}

	public void start() {
		String rs = RuntimeUtil.execForStr("httpd -k start");
		logger.error("启动结果:" + rs);
	}

	public void stop() {
		RuntimeUtil.exec("pkill httpd");
		FileUtil.del("/run/apache2/httpd.pid"); // 删除pid保证下次重启能正常启动
	}

}
