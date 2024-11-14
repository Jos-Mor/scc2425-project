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

import main.java.tukano.api.Result;
import main.java.tukano.api.TukanoUser;
import main.java.tukano.api.Users;
import main.java.tukano.impl.storage.cache.*;
import main.java.tukano.impl.storage.database.azure.*;
import main.java.tukano.impl.storage.database.UnavailableDBType;
import main.java.tukano.impl.storage.database.Container;
import main.java.tukano.impl.storage.database.imp.DataBase;
import main.java.utils.JSON;

public class JavaUsers <T> implements Users {
	
	private static Logger Log = Logger.getLogger(JavaUsers.class.getName());

	private static Users instance;

	//private static final DataBase<Session> DB = new HibernateDB();
	//private final DataBase<CosmosBatch> DB = new NoSQLCosmoDB(NoSQLCosmoDB.Container.USERS);
	private final DataBase<T> DB = DBPicker.chooseDB(Container.USERS);


	synchronized public static Users getInstance() {
		if( instance == null )
			try {
				instance = new JavaUsers();
			} catch (UnavailableDBType e) {
				throw new RuntimeException(e);
			}
		return instance;
	}
	
	private JavaUsers() throws UnavailableDBType {}
	
	@Override
	public Result<String> createUser(TukanoUser user) {
		Log.info(() -> format("createUser : %s\n", user));

		if( badUserInfo( user ) )
				return error(BAD_REQUEST);

		return errorOrValue( DB.insertOne( user), user.getUserId() );
	}

	@Override
	public Result<TukanoUser> getUser(String userId, String pwd) {
		Log.info( () -> format("getUser : userId = %s, pwd = %s\n", userId, pwd));

		if (userId == null)
			return error(BAD_REQUEST);

		var redisRes = RedisCache.doRedis(j -> {
			var user = RedisCache.getRedis(j, userId+pwd, u -> JSON.decode(u, TukanoUser.class));
			if (user.isOK()) {
				var result = validatedUserOrError(ok(user.value()), pwd);
				return result;
			}
			return error(NOT_FOUND);
		});
		if (redisRes.isOK()) return redisRes;

		Result<TukanoUser> result = validatedUserOrError( DB.getOne( userId, TukanoUser.class), pwd);

		if (result.isOK()) {
			RedisCache.doRedis(j -> {
				RedisCache.setRedis(j, userId+pwd, result.value(), u -> JSON.encode(u) );
				return ok();
			});
		}

		return result;
	}

	@Override
	public Result<TukanoUser> updateUser(String userId, String pwd, TukanoUser other) {
		Log.info(() -> format("updateUser : userId = %s, pwd = %s, user: %s\n", userId, pwd, other));

		if (badUpdateUserInfo(userId, pwd, other))
			return error(BAD_REQUEST);

		return errorOrResult( validatedUserOrError(DB.getOne( userId, TukanoUser.class), pwd), user -> {
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
	public Result<TukanoUser> deleteUser(String userId, String pwd) {
		Log.info(() -> format("deleteUser : userId = %s, pwd = %s\n", userId, pwd));

		if (userId == null || pwd == null )
			return error(BAD_REQUEST);

		Result<TukanoUser> res = validatedUserOrError( DB.getOne( userId, TukanoUser.class), pwd);

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
	public Result<List<TukanoUser>> searchUsers(String pattern) {
		Log.info( () -> format("searchUsers : patterns = %s\n", pattern));

		var query = format("SELECT * FROM TukanoUser u WHERE UPPER(u.userId) LIKE '%%%s%%'", pattern.toUpperCase());
		var hits = DB.sql(query, TukanoUser.class)
				.stream()
				.map(TukanoUser::copyWithoutPassword)
				.toList();

		return ok(hits);
	}

	
	private Result<TukanoUser> validatedUserOrError(Result<TukanoUser> res, String pwd ) {
		if( res.isOK())
			return res.value().getPwd().equals( pwd ) ? res : error(FORBIDDEN);
		else
			return res;
	}
	
	private boolean badUserInfo( TukanoUser user) {
		return (user.userId() == null || user.pwd() == null || user.displayName() == null || user.email() == null);
	}
	
	private boolean badUpdateUserInfo( String userId, String pwd, TukanoUser info) {
		return (userId == null || pwd == null || info.getUserId() != null && ! userId.equals( info.getUserId()));
	}
}
