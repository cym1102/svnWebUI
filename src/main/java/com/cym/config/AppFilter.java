package com.cym.config;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Filter;
import org.noear.solon.core.handle.FilterChain;

import com.cym.model.User;
import com.cym.sqlhelper.bean.Page;

@Component
public class AppFilter implements Filter {
	@Inject
	VersionConfig versionConfig;

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
					ctx.redirect("/adminPage/login");
					return;
				}

				// 检查权限
				if (user.getType() == 0) {
					if (ctx.path().contains("adminPage/config")//
							|| ctx.path().contains("adminPage/group")//
							|| ctx.path().contains("adminPage/repository")//
							|| ctx.path().contains("adminPage/user")//
					) {
						ctx.redirect("/adminPage/info");
						return;
					}
				}
			}

			ctx.attrSet("jsrandom", System.currentTimeMillis());
			ctx.attrSet("currentVersion", versionConfig.currentVersion);
			ctx.attrSet("ctx", ctx.url().replace(ctx.path(), ""));
			ctx.attrSet("page", new Page<>());
			ctx.attrSet("user", ctx.session("user"));

			chain.doFilter(ctx);

		} catch (Throwable e) {
			e.printStackTrace();
		}

	}
}