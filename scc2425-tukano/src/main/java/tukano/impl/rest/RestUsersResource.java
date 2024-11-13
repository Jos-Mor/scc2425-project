package main.java.tukano.impl.rest;

import java.util.List;

import jakarta.inject.Singleton;
import main.java.tukano.api.TukanoUser;
import main.java.tukano.api.Users;
import main.java.tukano.api.rest.RestUsers;

import main.java.tukano.impl.JavaUsers;

@Singleton
public class RestUsersResource extends RestResource implements RestUsers {

	final Users impl;
	public RestUsersResource() {
		this.impl = JavaUsers.getInstance();
	}
	
	@Override
	public String createUser(TukanoUser user) {
		return super.resultOrThrow( impl.createUser( user));
	}

	@Override
	public TukanoUser getUser(String name, String pwd) {
		var result = super.resultOrThrow( impl.getUser(name, pwd));
		if (result != null) {

		}
		return result;
	}
	
	@Override
	public TukanoUser updateUser(String name, String pwd, TukanoUser user) {
		return super.resultOrThrow( impl.updateUser(name, pwd, user));
	}

	@Override
	public TukanoUser deleteUser(String name, String pwd) {
		return super.resultOrThrow( impl.deleteUser(name, pwd));
	}

	@Override
	public List<TukanoUser> searchUsers(String pattern) {
		return super.resultOrThrow( impl.searchUsers( pattern));
	}
}
