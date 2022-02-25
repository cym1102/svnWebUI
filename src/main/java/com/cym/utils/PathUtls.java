package com.cym.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.Context;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import com.cym.config.HomeConfig;
import com.cym.config.InitConfig;
import com.cym.model.TreeNode;
import com.cym.service.RepositoryService;
import com.cym.service.SettingService;
import com.cym.sqlhelper.utils.SqlHelper;

import cn.hutool.core.util.StrUtil;

@Component
public class PathUtls {
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

	public List<TreeNode> getPath(String url) {
		String relativePath = getRelativePath(url);

		List<TreeNode> list = new ArrayList<TreeNode>();
		try {
			SVNRepository svnRepository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(svnAdminUtils.adminUserName, svnAdminUtils.adminUserPass.toCharArray());
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
			e.printStackTrace();
		}
		return list;
	}

	public String getRelativePath(String url) {
		List<String> relativePaths = new ArrayList<String>();
		String[] urls = url.split("/");
		for (int i = 4; i < urls.length; i++) {
			relativePaths.add(urls[i]);
		}

		return "/" + StrUtil.join("/", relativePaths);
	}

	public String baseUrl() {
		String protocol = SystemTool.inDocker() ? "http" : "svn";
		return protocol + "://localhost:" + settingService.get("port") + "/";
	}

	public String buildUrl(String port) {
		String url = null;
		if (SystemTool.inDocker()) {
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
		URI uri = null;
		try {
			if (Context.current() != null) {
				uri = new URI(Context.current().url() + "/");
				return uri.getHost();
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return "localhost";
	}
}
