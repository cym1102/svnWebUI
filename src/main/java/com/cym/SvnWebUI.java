package com.cym;

import org.noear.solon.Solon;

public class SvnWebUI {

	public static void main(String[] args) {
		Solon.start(SvnWebUI.class, args, app -> {
			app.onError(e -> e.printStackTrace());

			app.onEvent(freemarker.template.Configuration.class, cfg -> {
				cfg.setSetting("classic_compatible", "true");
				cfg.setSetting("number_format", "0.##");
			});

		});
	}
}
