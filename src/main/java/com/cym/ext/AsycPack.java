package com.cym.ext;

import java.util.List;

import com.cym.model.Group;
import com.cym.model.GroupGroup;
import com.cym.model.GroupUser;
import com.cym.model.Repository;
import com.cym.model.RepositoryGroup;
import com.cym.model.RepositoryUser;
import com.cym.model.Setting;
import com.cym.model.User;
import com.cym.model.WebHook;

public class AsycPack {
	List<Group> groupList;
	List<GroupGroup> groupGroupList;
	List<GroupUser> groupUserList;
	List<Repository> repositoryList;
	List<RepositoryGroup> repositoryGroupList;
	List<RepositoryUser> repositoryUserList;
	List<Setting> settingList;
	List<User> userList;
	List<WebHook> webHookList;

	public List<Group> getGroupList() {
		return groupList;
	}

	public void setGroupList(List<Group> groupList) {
		this.groupList = groupList;
	}

	public List<GroupGroup> getGroupGroupList() {
		return groupGroupList;
	}

	public void setGroupGroupList(List<GroupGroup> groupGroupList) {
		this.groupGroupList = groupGroupList;
	}

	public List<GroupUser> getGroupUserList() {
		return groupUserList;
	}

	public void setGroupUserList(List<GroupUser> groupUserList) {
		this.groupUserList = groupUserList;
	}

	public List<Repository> getRepositoryList() {
		return repositoryList;
	}

	public void setRepositoryList(List<Repository> repositoryList) {
		this.repositoryList = repositoryList;
	}

	public List<RepositoryGroup> getRepositoryGroupList() {
		return repositoryGroupList;
	}

	public void setRepositoryGroupList(List<RepositoryGroup> repositoryGroupList) {
		this.repositoryGroupList = repositoryGroupList;
	}

	public List<RepositoryUser> getRepositoryUserList() {
		return repositoryUserList;
	}

	public void setRepositoryUserList(List<RepositoryUser> repositoryUserList) {
		this.repositoryUserList = repositoryUserList;
	}

	public List<Setting> getSettingList() {
		return settingList;
	}

	public void setSettingList(List<Setting> settingList) {
		this.settingList = settingList;
	}

	public List<User> getUserList() {
		return userList;
	}

	public void setUserList(List<User> userList) {
		this.userList = userList;
	}

	public List<WebHook> getWebHookList() {
		return webHookList;
	}

	public void setWebHookList(List<WebHook> webHookList) {
		this.webHookList = webHookList;
	}

}
