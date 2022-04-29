package com.cym.utils;

import java.util.Comparator;
import java.util.List;

import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.Context;

import com.cym.config.VersionConfig;
import com.cym.ext.TreeNode;
import com.cym.model.User;
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

	protected String getFileName(String relativePath) {
		if (relativePath.contains("/")) {
			String[] names = relativePath.split("/");
			return names[names.length - 1];
		}
		return relativePath;
	}

	// 按文件夹进行排序
	protected void sortFile(List<TreeNode> list) {
		list.sort(new Comparator<TreeNode>() {

			@Override
			public int compare(TreeNode o1, TreeNode o2) {

				if (o1.getIsParent().equals("true") && o2.getIsParent().equals("false")) {
					return -1;
				}
				if (o1.getIsParent().equals("false") && o2.getIsParent().equals("true")) {
					return 1;
				}

				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});
	}

	// 包含特殊字符
	protected boolean isSpecialChar(String str) {
		if (str.contains("\\") || str.contains("/")) {
			return true;
		}
		return false;
	}


}
