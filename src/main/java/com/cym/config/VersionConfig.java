package com.cym.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Init;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.utils.SystemTool;

@Configuration
public class VersionConfig {
	Logger logger = LoggerFactory.getLogger(VersionConfig.class);

	public String currentVersion;

	@Init
	public void init() {
		// 获取版本号
		try {
			currentVersion = getFromPom();
		} catch (Exception e) {
			logger.info(e.getMessage(), e);
		}

	}

	public String getFromPom() throws FileNotFoundException, IOException, XmlPullParserException {

		// 查看jar包里面pom.properties版本号
		String jarPath = VersionConfig.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		jarPath = java.net.URLDecoder.decode(jarPath, "UTF-8");
		try {
			System.out.println(jarPath);
			URL url = new URL("jar:file:" + jarPath + "!/META-INF/maven/com.cym/svnWebUI/pom.properties");
			System.out.println(url);
			InputStream inputStream = url.openStream();
			Properties properties = new Properties();
			properties.load(inputStream);
			String version = properties.getProperty("version");
			return version;
		} catch (Exception e) {
			// 开发过程中查看pom.xml版本号
			MavenXpp3Reader reader = new MavenXpp3Reader();
			String basePath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
			if (SystemTool.isWindows() && basePath.startsWith("/")) {
				basePath = basePath.substring(1);
			}
			if (basePath.indexOf("/target/") != -1) {
				basePath = basePath.substring(0, basePath.indexOf("/target/"));
			}
			Model model = reader.read(new FileReader(new File(basePath + File.separator +"pom.xml")));
			String version = model.getVersion();
			return version;
		}

	}
}
