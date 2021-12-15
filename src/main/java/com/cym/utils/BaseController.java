package com.cym.utils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;

import com.cym.model.User;

import cn.craccd.sqlHelper.utils.SqlHelper;
import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelWriter;

/**
 * Author: D.Yang Email: koyangslash@gmail.com Date: 16/10/9 Time: 下午1:37
 * Describe: 基础控制器
 */
public class BaseController {
	@Autowired
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

	protected User getLoginUser(HttpSession httpSession) {
		return (User) httpSession.getAttribute("user");
	}


	protected void handleStream(HttpServletResponse response, ExcelWriter writer, String fileName) throws IOException {
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
		response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
		ServletOutputStream out = response.getOutputStream();
		writer.flush(out, true);
		writer.close();
		IoUtil.close(out);
	}


	public static String getIP(String url) {
		URI uri = null;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return uri.getHost();
	}

}
