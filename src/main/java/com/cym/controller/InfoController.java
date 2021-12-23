package com.cym.controller;

import java.util.List;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.ModelAndView;

import com.cym.config.ProjectConfig;
import com.cym.ext.RepositoryUserExt;
import com.cym.model.Repository;
import com.cym.model.RepositoryUser;
import com.cym.model.User;
import com.cym.service.RepositoryService;
import com.cym.service.SettingService;
import com.cym.utils.BaseController;
import com.cym.utils.BeanExtUtil;
import com.cym.utils.JsonResult;

@Controller
@Mapping("/adminPage/info")
public class InfoController extends BaseController {

	@Inject
	ProjectConfig projectConfig;
	@Inject
	RepositoryService repositoryService;
	@Inject
	SettingService settingService;

	@Mapping("")
	public ModelAndView index() {
		String port = settingService.get("port");

		User user = getLoginUser();

		List<RepositoryUser> list = repositoryService.getListByUser(user.getId());
		List<RepositoryUserExt> repositoryUserList = BeanExtUtil.copyListByProperties(list, RepositoryUserExt.class);

		for (RepositoryUserExt repositoryUserExt : repositoryUserList) {
			Repository repository = sqlHelper.findById(repositoryUserExt.getRepositoryId(), Repository.class);
			repositoryUserExt.setRepository(repository);

			String url = "svn://" + getIP();
			if (!port.equals("3690")) {
				url += (":" + port);
			}
			url += ("/" + repository.getName() + repositoryUserExt.getPath());
			repositoryUserExt.setPath(url);
		}
		
		ModelAndView modelAndView = buildMav("/adminPage/info/index.html");
		modelAndView.put("repositoryUserList", repositoryUserList);
		modelAndView.put("trueName", user.getTrueName());
		return modelAndView;
	}

	@Mapping("changeOver")

	public JsonResult changeOver( String oldPass, String newPass, String repeatPass) {
		User user = getLoginUser();
		if (!user.getPass().equals(oldPass)) {
			return renderError("旧密码不正确");
		}

		user.setPass(newPass);
		sqlHelper.updateById(user);

		return renderSuccess();
	}
}
