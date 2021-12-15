package com.cym.config;

import java.net.URI;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.cym.model.User;
import com.cym.utils.JsonResult;

import cn.craccd.sqlHelper.utils.SqlHelper;

@Component
public class AdminInterceptor implements HandlerInterceptor {

	@Autowired
	SqlHelper sqlHelper;
	@Value("${project.version}")
	String currentVersion;

	/*
	 * 视图渲染之后的操作
	 */
	@Override
	public void afterCompletion(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, Exception arg3) throws Exception {

	}

	/*
	 * 处理请求完成后视图渲染之前的处理操作
	 */
	@Override
	public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, ModelAndView arg3) throws Exception {

	}

	/*
	 * 进入controller层之前拦截请求
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object obj) throws Exception {
		String ctx = getIP(request.getRequestURL().toString() + "/");
		request.setAttribute("ctx", ctx);
		request.setAttribute("jsrandom", System.currentTimeMillis());
		request.setAttribute("currentVersion", currentVersion);

		Map<String, String[]> map = request.getParameterMap();
		if (hasXss(map)) {
			response.getWriter().write("xss attack");
			return false;
		}

		if (request.getRequestURL().toString().contains("login")) {
			return true;
		}

		User user = (User) request.getSession().getAttribute("user");
		if (user == null) {
			response.sendRedirect(ctx + "/adminPage/login");
			return false;
		}

		return true;
	}

	private boolean hasXss(Map<String, String[]> map) {
		for (String[] obj : map.values()) {
			for (String str : obj) {
				if (str.toLowerCase().contains("<img") //
						|| str.toLowerCase().contains("<iframe") //
						|| str.toLowerCase().contains("<frame") //
						|| str.toLowerCase().contains("<script") //
						|| str.toLowerCase().contains("<marquee") //
						|| str.toLowerCase().contains("<a")) {
					return true;
				}
			}
		}

		return false;
	}

	public static String getIP(String url) {
		URI effectiveURI = null;

		try {
			URI uri = new URI(url);
			effectiveURI = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), null, null, null);
		} catch (Throwable var4) {
			effectiveURI = null;
		}
		return effectiveURI.toString();
	}

}