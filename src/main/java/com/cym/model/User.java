package com.cym.model;

import org.springframework.beans.factory.annotation.Value;

import cn.craccd.sqlHelper.bean.BaseModel;
import cn.craccd.sqlHelper.config.Table;

/**
 * 用户
 * 
 * @author 陈钇蒙
 *
 */
@Table
public class User extends BaseModel {

	// 登录名
	String name;
	// 密码
	String pass;

	// 权限 0:普通用戶 1:管理員
	@Value("0")
	Integer type;

	// 姓名
	String trueName;

	public String getTrueName() {
		return trueName;
	}

	public void setTrueName(String trueName) {
		this.trueName = trueName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

}
