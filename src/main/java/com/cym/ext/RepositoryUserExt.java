package com.cym.ext;

import com.cym.model.Repository;
import com.cym.model.RepositoryUser;
import com.cym.model.User;

public class RepositoryUserExt extends RepositoryUser {
	User user;
	Repository repository;

	public Repository getRepository() {
		return repository;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
