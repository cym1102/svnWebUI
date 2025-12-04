package com.cym.controller;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Context;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import com.cym.ext.TreeNode;
import com.cym.model.User;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;
import com.cym.utils.PathUtls;
import com.cym.utils.SvnAdminUtils;

import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.net.URLEncodeUtil;
import cn.hutool.core.util.StrUtil;

@Controller
@Mapping("/adminPage/selectRoot")
public class SelectRootController extends BaseController {
	@Inject
	SvnAdminUtils svnAdminUtils;
	@Inject
	PathUtls pathUtls;

	@Mapping("getFileList")
	public List<TreeNode> getFileList(String id, String url) {
		User user = getLoginUser();

		String userName = user.getName();
		String userPass = user.getPass();
		if (user.getType() == 1) {
			userName = svnAdminUtils.adminUserName;
			userPass = svnAdminUtils.adminUserPass;
		}

		if (StrUtil.isEmpty(id)) {
			id = url;
		}
		id = URLDecoder.decode(id, Charset.forName("UTF-8"));
		List<TreeNode> list = pathUtls.getPath(id, userName, userPass);

		sortFile(list);

		return list;
	}

	@Mapping("mkdir")
	public JsonResult addFileDir(String url) {
		User user = getLoginUser();

		String userName = user.getName();
		String userPass = user.getPass();
		if (user.getType() == 1) {
			userName = svnAdminUtils.adminUserName;
			userPass = svnAdminUtils.adminUserPass;
		}
//		String baseUrl = getBase(svnUrl); // 获取仓库根目录
//		String targetUrl = getTarget(svnUrl, dir);// 获取文件所在路径
		pathUtls.createPath(url, userName, userPass);
		return renderSuccess();
	}



	@Mapping("upload")
	public JsonResult upload(String url, String filePath) {
		User user = getLoginUser();

		String userName = user.getName();
		String userPass = user.getPass();
		if (user.getType() == 1) {
			userName = svnAdminUtils.adminUserName;
			userPass = svnAdminUtils.adminUserPass;
		}
//		String baseUrl = getBase(svnUrl); // 获取仓库根目录
//		String targetUrl = getTarget(svnUrl, dir);// 获取文件所在路径
		pathUtls.upload(url, filePath, userName, userPass);
		return renderSuccess();
	}

	@Mapping("rmfile")
	public JsonResult rmfile(String url) {
		User user = getLoginUser();

		String userName = user.getName();
		String userPass = user.getPass();
		if (user.getType() == 1) {
			userName = svnAdminUtils.adminUserName;
			userPass = svnAdminUtils.adminUserPass;
		}
//		String baseUrl = getBase(svnUrl); // 获取仓库根目录
//		String targetUrl = getTarget(svnUrl, dir);// 获取文件所在路径
		pathUtls.removePath(url, userName, userPass);
		return renderSuccess();
	}

//	// 获取仓库根目录
//	private String getBase(String svnUrl) {
//
//		String[] svnUrls = svnUrl.split("/");
//
//		String rsUrl = "";
//		for (int i = 0; i <= 3; i++) {
//			rsUrl += svnUrls[i] + "/";
//		}
//
//		return rsUrl;
//	}
//	
//	// 获取目录目录
//	private String getTarget(String svnUrl, String dir) {
//		String[] svnUrls = svnUrl.split("/");
//
//		String rsUrl = "";
//		for (int i = 4; i < svnUrls.length; i++) {
//			rsUrl += svnUrls[i] + "/";
//		}
//
//		return rsUrl + dir;
//	}

	@Mapping("download")
	public void download(String url, Context context) throws SVNException, IOException {
		User user = getLoginUser();

		String userName = user.getName();
		String userPass = user.getPass();
		if (user.getType() == 1) {
			userName = svnAdminUtils.adminUserName;
			userPass = svnAdminUtils.adminUserPass;
		}

		url = URLDecoder.decode(url, Charset.forName("UTF-8"));

		SVNRepository svnRepository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
		ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(userName, userPass.toCharArray());
		svnRepository.setAuthenticationManager(authManager);

		String fileName = getFileName(url);

		context.headerAdd("Accept-Ranges", "bytes");
		context.headerAdd("Content-Type", "application/octet-stream");
		context.headerAdd("Content-Disposition", "attachment;filename=" + URLEncodeUtil.encode(fileName, Charset.forName("UTF-8")));
		svnRepository.getFile(pathUtls.getRelativePath(url), -1, null, context.outputStream());

	}
}
