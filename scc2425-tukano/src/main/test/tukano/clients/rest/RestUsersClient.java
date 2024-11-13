package main.test.tukano.clients.rest;

import java.util.List;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import main.java.tukano.api.Result;
import main.java.tukano.api.TukanoUser;
import main.java.tukano.api.Users;
import main.java.tukano.api.rest.RestUsers;


public class RestUsersClient extends RestClient implements Users {

	public RestUsersClient( String serverURI ) {
		super( serverURI, RestUsers.PATH );
	}
		
	private Result<String> _createUser(TukanoUser user) {
		return super.toJavaResult( 
			target.request()
			.accept(MediaType.APPLICATION_JSON)
			.post(Entity.entity(user, MediaType.APPLICATION_JSON)), String.class );
	}

	private Result<TukanoUser> _getUser(String userId, String pwd) {
		return super.toJavaResult(
				target.path( userId )
				.queryParam(RestUsers.PWD, pwd).request()
				.accept(MediaType.APPLICATION_JSON)
				.get(), TukanoUser.class);
	}
	
	public Result<TukanoUser> _updateUser(String userId, String password, TukanoUser user) {
		return super.toJavaResult(
				target
				.path( userId )
				.queryParam(RestUsers.PWD, password)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.put(Entity.entity(user, MediaType.APPLICATION_JSON)), TukanoUser.class);
	}

	public Result<TukanoUser> _deleteUser(String userId, String password) {
		return super.toJavaResult(
				target
				.path( userId )
				.queryParam(RestUsers.PWD, password)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.delete(), TukanoUser.class);
	}

	public Result<List<TukanoUser>> _searchUsers(String pattern) {
		return super.toJavaResult(
				target
				.queryParam(RestUsers.QUERY, pattern)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get(), new GenericType<List<TukanoUser>>() {});
	}

	@Override
	public Result<String> createUser(TukanoUser user) {
		return super.reTry( () -> _createUser(user));
	}

	@Override
	public Result<TukanoUser> getUser(String userId, String pwd) {
		return super.reTry( () -> _getUser(userId, pwd));
	}

	@Override
	public Result<TukanoUser> updateUser(String userId, String pwd, TukanoUser user) {
		return super.reTry( () -> _updateUser(userId, pwd, user));
	}

	@Override
	public Result<TukanoUser> deleteUser(String userId, String pwd) {
		return super.reTry( () -> _deleteUser(userId, pwd));
	}

	@Override
	public Result<List<TukanoUser>> searchUsers(String pattern) {
		return super.reTry( () -> _searchUsers(pattern));
	}
}
