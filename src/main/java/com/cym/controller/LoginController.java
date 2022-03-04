package com.cym.controller;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.ModelAndView;

import com.cym.model.User;
import com.cym.service.UserService;
import com.cym.sqlhelper.bean.Page;
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
@Mapping("/adminPage/login")
@Controller
public class LoginController extends BaseController {
	@Inject
	UserService userService;

	@Mapping("")
	public ModelAndView admin() {
		ModelAndView modelAndView = new ModelAndView("/adminPage/login/index.html");
		modelAndView.put("adminCount", sqlHelper.findAllCount(User.class));

		return modelAndView;
	}

	@Mapping(value = "login")
	public JsonResult submitLogin(String name, String pass, String code) {
		String captcha = (String) Context.current().session("captcha");
		if (!code.equals(captcha)) {
			Context.current().sessionRemove("captcha"); // 销毁验证码
			return renderError("验证码不正确"); // 验证码不正确
		}
		Context.current().sessionRemove("captcha"); // 销毁验证码

		User user = userService.login(name, pass);
		if (user == null) {
			return renderError("登录失败,请检查用户名密码");
		}

		if (user.getOpen() != 0) {
			return renderError("该用户已停用");
		}

		Context.current().sessionSet("user", user);
		return renderSuccess();
	}

	@Mapping("addAdmin")
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

	@Mapping("loginOut")
	public void loginOut(Context ctx) {
		ctx.redirect("/adminPage/login");
	}

	@Mapping("/getCode")
	public void getCode() throws Exception {
		Context.current().headerAdd("Pragma", "No-cache");
		Context.current().headerAdd("Cache-Control", "no-cache");
		Context.current().headerAdd("Expires", "0");
		Context.current().contentType("image/gif");

		SpecCaptcha specCaptcha = new SpecCaptcha(100, 40, 4);
		specCaptcha.setCharType(Captcha.TYPE_ONLY_NUMBER);

		Context.current().sessionSet("captcha", specCaptcha.text().toLowerCase());
		specCaptcha.out(Context.current().outputStream());
	}

}
