package com.cym.utils;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Init;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.ISVNDirEntryHandler;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import com.cym.config.HomeConfig;
import com.cym.config.InitConfig;
import com.cym.ext.TreeNode;
import com.cym.service.RepositoryService;
import com.cym.service.SettingService;
import com.cym.sqlhelper.bean.Page;
import com.cym.sqlhelper.utils.SqlHelper;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

@Component
public class PathUtls {
	Logger logger = LoggerFactory.getLogger(getClass());
	@Inject
	SqlHelper sqlHelper;
	@Inject
	SettingService settingService;
	@Inject
	RepositoryService repositoryService;
	@Inject
	HomeConfig homeConfig;
	@Inject
	SvnAdminUtils svnAdminUtils;

	public void upload(String svnUrl, String dir, String filePath, String userName, String userPass) {
		try {
			svnUrl = transLocalhost(svnUrl);
			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(userName, userPass.toCharArray());

			DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(true);
			SVNClientManager clientManager = SVNClientManager.newInstance(options, authManager);

			File file = new File(filePath);
			clientManager.getCommitClient().doImport(file, SVNURL.parseURIEncoded(svnUrl + "/" + dir + "/" + file.getName()), "上传文件", null, false, false, SVNDepth.INFINITY);

		} catch (SVNException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void createPath(String svnUrl, String dir, String userName, String userPass) {
		try {
			svnUrl = transLocalhost(svnUrl);
			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(userName, userPass.toCharArray());

			DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(true);
			SVNClientManager clientManager = SVNClientManager.newInstance(options, authManager);
			clientManager.getCommitClient().doMkDir(new SVNURL[] { SVNURL.parseURIEncoded(svnUrl + "/" + dir) }, "创建文件夹");

		} catch (SVNException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void removePath(String svnUrl, String dir, String userName, String userPass) {
		try {
			svnUrl = transLocalhost(svnUrl);
			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(userName, userPass.toCharArray());

			DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(true);
			SVNClientManager clientManager = SVNClientManager.newInstance(options, authManager);
			clientManager.getCommitClient().doDelete(new SVNURL[] { SVNURL.parseURIEncoded(svnUrl + "/" + dir) }, "删除文件");

		} catch (SVNException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public Page<SVNLogEntry> seeLog(String svnUrl, String userName, String userPass, Page page) throws SVNException {

		svnUrl = transLocalhost(svnUrl);
		SVNRepository svnRepository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(svnUrl));
		ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(userName, userPass.toCharArray());
		svnRepository.setAuthenticationManager(authManager);

		// 查询出一共的条数
		Long count = svnRepository.getLatestRevision();
		page.setCount(count);

		if (count == 0) {
			return page;
		}

		Long start = svnRepository.getLatestRevision() - ((page.getCurr() - 1) * page.getLimit());
		Long end = start - page.getLimit() + 1;

		if (end < 1l) {
			end = 1l;
		}

		LinkedList logs = (LinkedList) svnRepository.log(new String[] { "" }, null, //
				start, // 起始index
				end, // 结束index
				true, true);
		page.setRecords(new ArrayList(logs));
		return page;
	}

	public List<TreeNode> getPath(String url, String userName, String userPass) {
		String relativePath = getRelativePath(url);

		List<TreeNode> list = new ArrayList<TreeNode>();
		try {
			url = transLocalhost(url);
			SVNRepository svnRepository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(userName, userPass.toCharArray());
			svnRepository.setAuthenticationManager(authManager);

			Collection<SVNDirEntry> entries = svnRepository.getDir(relativePath, -1, null, (Collection) null);
			for (SVNDirEntry svnDirEntry : entries) {
				TreeNode treeNode = new TreeNode();
				treeNode.setId(svnDirEntry.getURL().toString());
				treeNode.setName(svnDirEntry.getName());
				treeNode.setIsParent(svnDirEntry.getKind().toString().equals("dir") ? "true" : "false");

				list.add(treeNode);
			}

		} catch (SVNException e) {
			logger.error(e.getMessage(), e);
		}
		return list;
	}

	private String transLocalhost(String url) {
		String host = url.split("/")[2].split(":")[0];
		return url.replace(host, "localhost");

	}

	public String getRelativePath(String url) {
		List<String> relativePaths = new ArrayList<String>();
		String[] urls = url.split("/");
		for (int i = 4; i < urls.length; i++) {
			relativePaths.add(urls[i]);
		}

		return "/" + StrUtil.join("/", relativePaths);
	}

	public String buildUrl(String port) {
		String url = null;
		if (SystemTool.inDocker() && "http".equals(settingService.get("protocol"))) {
			url = "http://" + getIP();
			if (!port.equals("80")) {
				url += (":" + port);
			}
		} else {
			url = "svn://" + getIP();
			if (!port.equals("3690")) {
				url += (":" + port);
			}
		}
		return url;
	}

	public String getIP() {
		String host = settingService.get("host");
		if (StrUtil.isNotEmpty(host)) {
			return host;
		}

		URI uri = null;
		try {
			if (Context.current() != null) {
				uri = new URI(Context.current().url() + "/");
				return uri.getHost();
			}
		} catch (URISyntaxException e) {
			logger.error(e.getMessage(), e);
		}
		return "localhost";
	}

}
