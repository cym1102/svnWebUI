package com.cym;

import org.noear.solon.Solon;
import org.noear.solon.scheduling.annotation.EnableScheduling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EnableScheduling
public class SvnWebUI {
	static Logger logger = LoggerFactory.getLogger(SvnWebUI.class);

	public static void main(String[] args) {
//		Solon.start(SvnWebUI.class, args, app -> {
//			app.onError(e -> logger.error(e.getMessage(), e));
//
//			app.before(c -> {
//				String path = c.path();
//				while (path.contains("//")) {
//					path = path.replace("//", "/");
//				}
//				c.pathNew(path);
//			});
//
//			app.onEvent(freemarker.template.Configuration.class, cfg -> {
//				cfg.setSetting("classic_compatible", "true");
//				cfg.setSetting("number_format", "0.##");
//			});
//			
//			app.router().caseSensitive(true);
//		});
		
		Solon.start(SvnWebUI.class, args);
	}
}
