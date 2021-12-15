package com.cym.ext;

import java.util.List;

import com.cym.model.Repository;

public class RepositoryExt extends Repository {

	String url;
	
	String permission;
	
	

	public String getPermission() {
		return permission;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
