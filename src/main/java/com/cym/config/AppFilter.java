package com.cym.config;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Filter;
import org.noear.solon.core.handle.FilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.model.User;
import com.cym.sqlhelper.bean.Page;

import cn.hutool.core.util.StrUtil;

@Component
public class AppFilter implements Filter {
	Logger logger = LoggerFactory.getLogger(getClass());
	@Inject
	VersionConfig versionConfig;

	@Override
	public void doFilter(Context ctx, FilterChain chain) throws Throwable {
		try {
			if (ctx.path().toLowerCase().contains("adminPage".toLowerCase()) //
					&& !ctx.path().toLowerCase().contains("/adminPage/login".toLowerCase()) //
					&& !ctx.path().toLowerCase().endsWith(".js".toLowerCase()) //
					&& !ctx.path().toLowerCase().endsWith(".css".toLowerCase()) //
					&& !ctx.path().toLowerCase().endsWith(".jpg".toLowerCase()) //
					&& !ctx.path().toLowerCase().endsWith(".png".toLowerCase()) //
			) {
				// 检查登录
				User user = (User) ctx.session("user");
				if (user == null) {
					ctx.redirect("/adminPage/login");
					return;
				}

				// 检查权限
				if (user.getType() == 0) {
					if (ctx.path().toLowerCase().contains("adminPage/config".toLowerCase())//
							|| ctx.path().toLowerCase().contains("adminPage/group".toLowerCase())//
							|| ctx.path().toLowerCase().contains("adminPage/repository".toLowerCase())//
							|| ctx.path().toLowerCase().contains("adminPage/user".toLowerCase())//
					) {
						ctx.redirect("/adminPage/info");
						return;
					}
				}
			}

			ctx.attrSet("jsrandom", System.currentTimeMillis());
			ctx.attrSet("currentVersion", versionConfig.currentVersion);
			ctx.attrSet("ctx", getCtxStr(ctx));
			ctx.attrSet("page", new Page<>());
			ctx.attrSet("user", ctx.session("user"));

			chain.doFilter(ctx);

		} catch (Throwable e) {
			// e.printStackTrace();
			logger.error(e.getMessage(), e);
		}

	}

	public String getCtxStr(Context context) {
		String httpHost = context.header("X-Forwarded-Host");
		String realPort = context.header("X-Forwarded-Port");
		String host = context.header("Host");

		String ctx = "//";
		if (StrUtil.isNotEmpty(httpHost)) {
			ctx += httpHost;
		} else if (StrUtil.isNotEmpty(host)) {
			ctx += host;
			if (!host.contains(":") && StrUtil.isNotEmpty(realPort)) {
				ctx += ":" + realPort;
			}
		} else {
			host = context.url().split("/")[2];
			ctx += host;
			if (!host.contains(":") && StrUtil.isNotEmpty(realPort)) {
				ctx += ":" + realPort;
			}
		}
		return ctx;

	}
}