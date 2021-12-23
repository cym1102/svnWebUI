package com.cym.config;

import java.util.Map;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Filter;
import org.noear.solon.core.handle.FilterChain;

import com.cym.model.User;

@Component
public class AppFilter implements Filter {

	@Inject("${project.version}")
	String currentVersion;

	@Override
	public void doFilter(Context ctx, FilterChain chain) throws Throwable {
		try {

			if (ctx.path().contains("adminPage") //
					&& !ctx.path().contains("/adminPage/login") //
					&& !ctx.path().endsWith(".js") //
					&& !ctx.path().endsWith(".css") //
					&& !ctx.path().endsWith(".jpg") //
					&& !ctx.path().endsWith(".png") //
			) {
				// 检查登录
				User user = (User) ctx.session("user");
				if (user == null) {
					ctx.redirect(ctx.url().replace(ctx.path(), "/"));
					return;
				}
			}

			chain.doFilter(ctx);

		} catch (Throwable e) {
			e.printStackTrace();
		}

	}
}