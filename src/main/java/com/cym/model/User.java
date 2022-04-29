package com.cym.model;

import com.cym.sqlhelper.bean.BaseModel;
import com.cym.sqlhelper.config.InitValue;
import com.cym.sqlhelper.config.Table;

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
	@InitValue("0")
	Integer type;

	// 姓名
	String trueName;

	// 状态 0:启用 1:停用
	@InitValue("0")
	Integer open;


	public Integer getOpen() {
		return open;
	}

	public void setOpen(Integer open) {
		this.open = open;
	}

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
