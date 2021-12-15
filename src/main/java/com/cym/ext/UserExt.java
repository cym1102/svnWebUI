package com.cym.ext;

import com.cym.model.User;

import cn.craccd.sqlHelper.config.InitValue;
import io.swagger.annotations.ApiModelProperty;

public class UserExt extends User {
	@InitValue("r")
	@ApiModelProperty("读写类型 'r' 'w' 'rw'")
	String permission;


	public String getPermission() {
		return permission;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

}
