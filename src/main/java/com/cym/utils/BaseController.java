package com.cym.utils;

import java.net.URI;
import java.net.URISyntaxException;

import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.ModelAndView;

import com.cym.config.VersionConfig;
import com.cym.model.User;
import com.cym.sqlhelper.bean.Page;
import com.cym.sqlhelper.utils.SqlHelper;

/**
 * Author: D.Yang Email: koyangslash@gmail.com Date: 16/10/9 Time: 下午1:37
 * Describe: 基础控制器
 */
public class BaseController {
	@Inject
	VersionConfig versionConfig;
	
	@Inject
	protected SqlHelper sqlHelper;


	protected JsonResult renderError() {
		JsonResult result = new JsonResult();
		result.setSuccess(false);
		result.setStatus("500");
		return result;
	}

	protected JsonResult renderAuthError() {
		JsonResult result = new JsonResult();
		result.setSuccess(false);
		result.setStatus("401");
		return result;
	}

	protected JsonResult renderError(String msg) {
		JsonResult result = renderError();
		result.setMsg(msg);
		return result;
	}

	protected JsonResult renderSuccess() {
		JsonResult result = new JsonResult();
		result.setSuccess(true);
		result.setStatus("200");
		return result;
	}

	protected JsonResult renderSuccess(Object obj) {
		JsonResult result = renderSuccess();
		result.setObj(obj);
		return result;
	}

	protected User getLoginUser() {
		return (User) Context.current().session("user");
	}



//	public String getIP() {
//		URI uri = null;
//		try {
//			uri = new URI(Context.current().url() + "/");
//		} catch (URISyntaxException e) {
//			e.printStackTrace();
//		}
//		return uri.getHost();
//	}

}
