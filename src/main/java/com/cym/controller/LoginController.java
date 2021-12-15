package com.cym.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.cym.model.User;
import com.cym.service.UserService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import com.wf.captcha.utils.CaptchaUtil;

/**
 * 登录页
 * 
 * @author Useristrator
 *
 */
@RequestMapping("/adminPage/login")
@Controller
public class LoginController extends BaseController {
	@Autowired
	UserService userService;

	@RequestMapping("")
	public ModelAndView admin(ModelAndView modelAndView, HttpServletRequest request, HttpSession httpSession) {
		modelAndView.addObject("adminCount", sqlHelper.findAllCount(User.class));
		modelAndView.setViewName("/adminPage/login/index");
		return modelAndView;
	}

	@RequestMapping(value = "login")
	@ResponseBody
	public JsonResult submitLogin(String name, String pass, String code, HttpServletRequest request) {
		if (!CaptchaUtil.ver(code, request)) {
			CaptchaUtil.clear(request); // 销毁验证码
			return renderError("验证码不正确"); // 验证码不正确
		}
		CaptchaUtil.clear(request); // 销毁验证码
		
		User user = userService.login(name, pass);
		if (user == null) {
			return renderError("登录失败,请检查用户名密码");
		}

		request.getSession().setAttribute("user", user);
		return renderSuccess();
	}

	@RequestMapping("addAdmin")
	@ResponseBody
	public JsonResult addAdmin(String trueName, String name, String pass) {

		Long adminCount = sqlHelper.findAllCount(User.class);
		if (adminCount > 0) {
			return renderError("管理员已存在");
		}
		User user = new User();
		user.setTrueName(trueName);
		user.setName(name);
		user.setPass(pass);
		user.setType(1);
		sqlHelper.insert(user);

		return renderSuccess();
	}

	@RequestMapping("loginOut")
	public String loginOut(HttpSession httpSession) {
		httpSession.removeAttribute("user");
		return "redirect:/adminPage/login";
	}

	@RequestMapping("/getCode")
	public void getCode(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
		SpecCaptcha specCaptcha = new SpecCaptcha(100, 40, 4);
		specCaptcha.setCharType(Captcha.TYPE_ONLY_NUMBER);
		CaptchaUtil.out(specCaptcha, httpServletRequest, httpServletResponse);
	}

}
