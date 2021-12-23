package com.cym.ext;

import java.util.List;

import com.cym.model.Group;
import com.cym.model.User;
import com.cym.sqlhelper.config.InitValue;

public class GroupExt extends Group {

	List<User> userList;

	List<String> userIds;

	@InitValue("r")
	//读写类型 'r' 'w' 'rw'
	String permission;

	public List<String> getUserIds() {
		return userIds;
	}

	public void setUserIds(List<String> userIds) {
		this.userIds = userIds;
	}

	public String getPermission() {
		return permission;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

	public List<User> getUserList() {
		return userList;
	}

	public void setUserList(List<User> userList) {
		this.userList = userList;
	}

}
