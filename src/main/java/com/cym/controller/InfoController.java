package com.cym.controller;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.ModelAndView;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import com.cym.config.InitConfig;
import com.cym.ext.RepositoryUserExt;
import com.cym.ext.TreeNode;
import com.cym.model.Repository;
import com.cym.model.RepositoryUser;
import com.cym.model.User;
import com.cym.service.ConfigService;
import com.cym.service.RepositoryService;
import com.cym.service.SettingService;
import com.cym.utils.BaseController;
import com.cym.utils.BeanExtUtil;
import com.cym.utils.JsonResult;
import com.cym.utils.PathUtls;
import com.cym.utils.SvnAdminUtils;

import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.net.URLEncoder;
import cn.hutool.core.util.StrUtil;

@Controller
@Mapping("/adminPage/info")
public class InfoController extends BaseController {

	@Inject
	InitConfig projectConfig;
	@Inject
	RepositoryService repositoryService;
	@Inject
	SettingService settingService;
	@Inject
	PathUtls pathUtls;
	@Inject
	ConfigService configService;

	@Mapping("")
	public ModelAndView index() {
		String port = settingService.get("port");

		User user = getLoginUser();

		List<RepositoryUser> list = repositoryService.getListByUser(user.getId());
		List<RepositoryUserExt> repositoryUserList = BeanExtUtil.copyListByProperties(list, RepositoryUserExt.class);

		for (RepositoryUserExt repositoryUserExt : repositoryUserList) {
			Repository repository = sqlHelper.findById(repositoryUserExt.getRepositoryId(), Repository.class);
			repositoryUserExt.setRepository(repository);

			String url = pathUtls.buildUrl(port);
			url += ("/" + repository.getName() + repositoryUserExt.getPath());
			repositoryUserExt.setPath(url);
		}

		List<Repository> repositories = repositoryService.getListByAllPermission();
		for (Repository repository : repositories) {
			RepositoryUserExt repositoryUserExt = new RepositoryUserExt();
			repositoryUserExt.setRepository(repository);
			repositoryUserExt.setPermission(repository.getAllPermission());
			repositoryUserExt.setUserId(user.getId());

			String url = pathUtls.buildUrl(port);
			url += ("/" + repository.getName());
			repositoryUserExt.setPath(url);
			repositoryUserList.add(repositoryUserExt);
		}

		ModelAndView modelAndView = new ModelAndView("/adminPage/info/index.html");
		modelAndView.put("repositoryUserList", repositoryUserList);
		modelAndView.put("trueName", user.getTrueName());
		return modelAndView;
	}

	@Mapping("changeOver")
	public JsonResult changeOver(String oldPass, String newPass, String repeatPass) {
		User user = getLoginUser();
		if (!user.getPass().equals(oldPass)) {
			return renderError("旧密码不正确");
		}

		user.setPass(newPass);
		sqlHelper.updateById(user);
		configService.refresh();
		return renderSuccess();
	}

}
