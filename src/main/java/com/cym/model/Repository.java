package com.cym.model;

import com.cym.sqlhelper.bean.BaseModel;
import com.cym.sqlhelper.config.InitValue;
import com.cym.sqlhelper.config.Table;

@Table
public class Repository extends BaseModel {
	String name;

	// 全体权限 'r' 'rw' 'no'
	@InitValue("no")
	String allPermission;

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
