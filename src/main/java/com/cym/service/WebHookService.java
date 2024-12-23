package com.cym.service;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import com.cym.model.WebHook;
import com.cym.sqlhelper.utils.ConditionAndWrapper;
import com.cym.sqlhelper.utils.SqlHelper;

@Component
public class WebHookService {
	@Inject
	SqlHelper sqlHelper;

	public WebHook get() {
		WebHook webHook = sqlHelper.findOneByQuery(new ConditionAndWrapper(), WebHook.class);

		if (webHook == null) {
			webHook = new WebHook();
			webHook.setUrl("");
			webHook.setPassword("");
			webHook.setOpen(false);
			sqlHelper.insert(webHook);
		}

		return webHook;

	}
}
