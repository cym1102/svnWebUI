package com.cym.ext;

import com.cym.model.Group;
import com.cym.model.RepositoryGroup;
import com.cym.model.RepositoryUser;

public class RepositoryGroupExt extends RepositoryGroup {
	
	Group group;

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}
	
	
}
