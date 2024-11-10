package main.java.tukano.impl;

import static java.lang.String.format;
import static main.java.tukano.api.Result.ErrorCode.*;
import static main.java.tukano.api.Result.error;
import static main.java.tukano.api.Result.errorOrResult;
import static main.java.tukano.api.Result.errorOrValue;
import static main.java.tukano.api.Result.ok;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import com.azure.cosmos.models.CosmosBatch;
import main.java.tukano.api.Result;
import main.java.tukano.api.User;
import main.java.tukano.api.Users;
import main.java.tukano.impl.storage.cache.*;
import main.java.tukano.impl.storage.database.azure.CosmoDB;
import main.java.tukano.impl.storage.database.imp.DataBase;
import main.java.utils.JSON;

public class JavaUsers implements Users {
	
	private static Logger Log = Logger.getLogger(JavaUsers.class.getName());

	private static Users instance;

	//private static final DataBase<Session> DB = new HibernateDB();
	private final DataBase<CosmosBatch> DB = new CosmoDB(CosmoDB.Container.USERS);
	synchronized public static Users getInstance() {
		if( instance == null )
			instance = new JavaUsers();
		return instance;
	}
	
	private JavaUsers() {}
	
	@Override
	public Result<String> createUser(User user) {
		Log.info(() -> format("createUser : %s\n", user));

		if( badUserInfo( user ) )
				return error(BAD_REQUEST);

		return errorOrValue( DB.insertOne( user), user.getUserId() );
	}

	@Override
	public Result<User> getUser(String userId, String pwd) {
		Log.info( () -> format("getUser : userId = %s, pwd = %s\n", userId, pwd));

		if (userId == null)
			return error(BAD_REQUEST);

		var redisRes = RedisCache.doRedis(j -> {
			var user = RedisCache.getRedis(j, userId+pwd, u -> JSON.decode(u, User.class));
			if (user.isOK()) {
				return validatedUserOrError(ok(user.value()), pwd);
			}
			return error(NOT_FOUND);
		});
		if (redisRes.isOK()) return redisRes;

		Result<User> result = validatedUserOrError( DB.getOne( userId, User.class), pwd);

		if (result.isOK()) {
			RedisCache.doRedis(j -> {
				RedisCache.setRedis(j, userId+pwd, result.value(), u -> JSON.encode(u) );
				return ok();
			});
		}

		return result;
	}

	@Override
	public Result<User> updateUser(String userId, String pwd, User other) {
		Log.info(() -> format("updateUser : userId = %s, pwd = %s, user: %s\n", userId, pwd, other));

		if (badUpdateUserInfo(userId, pwd, other))
			return error(BAD_REQUEST);

		return errorOrResult( validatedUserOrError(DB.getOne( userId, User.class), pwd), user -> {
			var result = DB.updateOne( user.updateFrom(other));

			if (result.isOK()) {
				RedisCache.doRedis(j -> {
					RedisCache.setRedis(j, userId+pwd, result.value(), u -> JSON.encode(u) );
					return ok();
				});
			}

			return result;
		});
	}

	@Override
	public Result<User> deleteUser(String userId, String pwd) {
		Log.info(() -> format("deleteUser : userId = %s, pwd = %s\n", userId, pwd));

		if (userId == null || pwd == null )
			return error(BAD_REQUEST);

		Result<User> res = validatedUserOrError( DB.getOne( userId, User.class), pwd);

		return errorOrResult( res, user -> {

			// Delete user shorts and related info asynchronously in a separate thread
			Executors.defaultThreadFactory().newThread( () -> {
				JavaShorts.getInstance().deleteAllShorts(userId, pwd, Token.get(userId));
				JavaBlobs.getInstance().deleteAllBlobs(userId, Token.get(userId));
			}).start();
			
			var result = DB.deleteOne( user);

			if (result.isOK()) {
				RedisCache.doRedis(j -> {
					var user2 = j.get(userId+pwd);
					if (user2 != null){
						j.del(userId+pwd);
					}
					return ok();
				});
			}

			return result;
		});
	}

	@Override
	public Result<List<User>> searchUsers(String pattern) {
		Log.info( () -> format("searchUsers : patterns = %s\n", pattern));

		var query = format("SELECT * FROM User u WHERE UPPER(u.userId) LIKE '%%%s%%'", pattern.toUpperCase());
		var hits = DB.sql(query, User.class)
				.stream()
				.map(User::copyWithoutPassword)
				.toList();

		return ok(hits);
	}

	
	private Result<User> validatedUserOrError( Result<User> res, String pwd ) {
		if( res.isOK())
			return res.value().getPwd().equals( pwd ) ? res : error(FORBIDDEN);
		else
			return res;
	}
	
	private boolean badUserInfo( User user) {
		return (user.userId() == null || user.pwd() == null || user.displayName() == null || user.email() == null);
	}
	
	private boolean badUpdateUserInfo( String userId, String pwd, User info) {
		return (userId == null || pwd == null || info.getUserId() != null && ! userId.equals( info.getUserId()));
	}
}
