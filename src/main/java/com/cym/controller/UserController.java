package com.cym.controller;

import java.nio.charset.Charset;
import java.util.List;

import javax.naming.NamingException;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.ModelAndView;

import com.cym.config.InitConfig;
import com.cym.model.User;
import com.cym.service.ConfigService;
import com.cym.service.UserService;
import com.cym.sqlhelper.bean.Page;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

@Controller
@Mapping("/adminPage/user")
public class UserController extends BaseController {
	@Inject
	UserService userService;
	@Inject
	ConfigService configService;
	@Inject
	InitConfig projectConfig;

	@Mapping("")
	public ModelAndView index(Page page, String keywords) {

		page = userService.search(page, keywords);
		ModelAndView modelAndView = new ModelAndView("/adminPage/user/index.html");
		modelAndView.put("keywords", keywords);
		modelAndView.put("page", page);
		return modelAndView;
	}

	@Mapping("addOver")
	public JsonResult addOver(User user) {
		if (StrUtil.isEmpty(user.getName())) {
			return renderError("用户名为空");
		}
		if (isSpecialChar(user.getName())) {
			return renderError("名称包含特殊字符");
		}
		if (StrUtil.isEmpty(user.getPass())) {
			return renderError("密码为空");
		}

		User userOrg = userService.getByName(user.getName(), user.getId());
		if (userOrg != null) {
			return renderError("此登录名已存在");
		}

		sqlHelper.insertOrUpdate(user);
		configService.refresh();
		return renderSuccess();
	}

	@Mapping("detail")
	public JsonResult detail(String id) {
		User user = sqlHelper.findById(id, User.class);
		return renderSuccess(user);
	}

	@Mapping("del")
	public JsonResult del(String id) {
		userService.deleteById(id);
		configService.refresh();
		return renderSuccess();
	}

	@Mapping("importOver")
	public JsonResult importOver(String dirTemp) {

		List<String> lines = FileUtil.readLines(dirTemp, Charset.forName("UTF-8"));
		for (String line : lines) {
			if (line.contains("=")) {
				String name = line.split("=")[0].trim();
				String pass = line.split("=")[1].trim();

				userService.importUser(name, pass);
			}
		}

		FileUtil.del(dirTemp);
		configService.refresh();
		return renderSuccess();
	}

}
