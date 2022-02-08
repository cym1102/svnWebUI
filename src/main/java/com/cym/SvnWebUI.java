package com.cym;

import org.noear.solon.Solon;

public class SvnWebUI {

	public static void main(String[] args) {
		Solon.start(SvnWebUI.class, args, app -> {
			app.onError(e -> e.printStackTrace());
			
			app.before(c -> {
				String path = c.path();
				while (path.contains("//")) {
					path = path.replace("//", "/");
				}
				c.pathNew(path);
			});;

			app.onEvent(freemarker.template.Configuration.class, cfg -> {
				cfg.setSetting("classic_compatible", "true");
				cfg.setSetting("number_format", "0.##");
			});

		});
	}
}
