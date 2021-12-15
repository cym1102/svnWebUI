package com.cym.controller;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.cym.config.SqlConfig;
import com.cym.ext.RepositoryExt;
import com.cym.ext.RepositoryUserExt;
import com.cym.model.Group;
import com.cym.model.Repository;
import com.cym.model.RepositoryUser;
import com.cym.model.User;
import com.cym.service.RepositoryService;
import com.cym.service.SettingService;
import com.cym.utils.BaseController;
import com.cym.utils.BeanExtUtil;
import com.cym.utils.JarUtil;
import com.cym.utils.JsonResult;
import com.cym.utils.SystemTool;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;

@Controller
@RequestMapping("/adminPage/info")
public class InfoController extends BaseController {

	@Autowired
	SqlConfig sqlConfig;
	@Autowired
	RepositoryService repositoryService;
	@Autowired
	SettingService settingService;

	@RequestMapping("")
	public ModelAndView index(HttpServletRequest request, HttpSession httpSession, ModelAndView modelAndView) {
		String port = settingService.get("port");

		User user = getLoginUser(httpSession);

		List<RepositoryUser> list = repositoryService.getListByUser(user.getId());
		List<RepositoryUserExt> repositoryUserList = BeanExtUtil.copyListByProperties(list, RepositoryUserExt.class);

		for (RepositoryUserExt repositoryUserExt : repositoryUserList) {
			Repository repository = sqlHelper.findById(repositoryUserExt.getRepositoryId(), Repository.class);
			repositoryUserExt.setRepository(repository);

			String url = "svn://" + getIP(request.getRequestURL().toString() + "/");
			if (!port.equals("3690")) {
				url += (":" + port);
			}
			url += ("/" + repository.getName() + repositoryUserExt.getPath());
			repositoryUserExt.setPath(url);
		}

		modelAndView.addObject("repositoryUserList", repositoryUserList);
		modelAndView.addObject("trueName", user.getTrueName());
		modelAndView.setViewName("/adminPage/info/index");
		return modelAndView;
	}

	@Transactional
	@RequestMapping("changeOver")
	@ResponseBody
	public JsonResult changeOver(HttpSession httpSession, String oldPass, String newPass, String repeatPass) {
		User user = getLoginUser(httpSession);
		if(!user.getPass().equals(oldPass)) {
			return renderError("旧密码不正确");
		}
		
		user.setPass(newPass);
		sqlHelper.updateById(user); 
		
		return renderSuccess();
	}
}
