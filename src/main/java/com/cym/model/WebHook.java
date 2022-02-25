package com.cym.model;

import org.noear.solon.annotation.Inject;

import com.cym.sqlhelper.bean.BaseModel;
import com.cym.sqlhelper.config.Table;

@Table
public class WebHook extends BaseModel {
	// 目标地址
	String url;
	// 密码
	String password;
	// 是否开启
	@Inject("false")
	Boolean open;

	public Boolean getOpen() {
		return open;
	}

	public void setOpen(Boolean open) {
		this.open = open;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
