package com.cym.model;

import com.cym.sqlhelper.bean.BaseModel;
import com.cym.sqlhelper.config.InitValue;
import com.cym.sqlhelper.config.Table;

@Table
public class Repository extends BaseModel {

	// 名称
	String name;
	// 全体权限 'r' 'rw' 'no'
	@InitValue("no")
	String allPermission;
	// 备注
	String mark;
	/**
	 * 是否启用 true:启用(默认) false:禁用
	 */
	@InitValue("true")
	Boolean enable;

	public Boolean getEnable() {
		return enable;
	}

	public void setEnable(Boolean enable) {
		this.enable = enable;
	}

	public String getMark() {
		return mark;
	}

	public void setMark(String mark) {
		this.mark = mark;
	}

	public String getAllPermission() {
		return allPermission;
	}

	public void setAllPermission(String allPermission) {
		this.allPermission = allPermission;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
