package com.cym.ext;

import com.cym.model.User;
import com.cym.sqlhelper.config.InitValue;

public class UserExt extends User {
	@InitValue("r")
	String permission;


	public String getPermission() {
		return permission;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

}
