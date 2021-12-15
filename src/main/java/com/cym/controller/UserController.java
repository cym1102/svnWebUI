package com.cym.controller;

import java.nio.charset.Charset;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.cym.config.SqlConfig;
import com.cym.model.User;
import com.cym.service.ConfigService;
import com.cym.service.UserService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;

import cn.craccd.sqlHelper.bean.Page;
import cn.hutool.core.io.FileUtil;

@Controller
@RequestMapping("/adminPage/user")
public class UserController extends BaseController {
	@Autowired
	UserService userService;
	@Autowired
	ConfigService configService;
	@Autowired
	SqlConfig sqlConfig;
	@RequestMapping("")
	public ModelAndView index(HttpSession httpSession, ModelAndView modelAndView, Page page, String keywords) {


		page = userService.search(page, keywords);

		modelAndView.addObject("keywords", keywords);
		modelAndView.addObject("page", page);
		modelAndView.setViewName("/adminPage/user/index");
		return modelAndView;
	}

	@Transactional
	@RequestMapping("addOver")
	@ResponseBody
	public JsonResult addOver(User user) {
		User userOrg = userService.getByName(user.getName(), user.getId());
		if (userOrg != null) {
			return renderError("此登录名已存在");
		}

		sqlHelper.insertOrUpdate(user);
		configService.refresh();
		return renderSuccess();
	}

	@RequestMapping("detail")
	@ResponseBody
	public JsonResult detail(String id) {
		User user = sqlHelper.findById(id, User.class);
		return renderSuccess(user);
	}

	@Transactional
	@RequestMapping("del")
	@ResponseBody
	public JsonResult del(String id) {
		userService.deleteById(id);
		configService.refresh();
		return renderSuccess();
	}
	
	
	@Transactional
	@RequestMapping("importOver")
	@ResponseBody
	public JsonResult importOver(String dirTemp) {

		List<String> lines = FileUtil.readLines(dirTemp, Charset.forName("UTF-8"));
		for(String line:lines) {
			if(line.contains("=")) {
				String name = line.split("=")[0].trim();
				String pass = line.split("=")[1].trim();
				
				userService.importUser(name,pass);
			}
		}
		
		FileUtil.del(dirTemp);

		return renderSuccess();
	}

}
