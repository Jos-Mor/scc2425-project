package main.java.tukano.impl;

import static java.lang.String.format;

import static main.java.tukano.api.Result.*;
import static main.java.tukano.api.Result.ErrorCode.*;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import com.azure.cosmos.models.CosmosBatch;
import main.java.tukano.api.Blobs;
import main.java.tukano.api.Result;
import main.java.tukano.api.Shorts;
import main.java.tukano.api.Short;


import main.java.tukano.api.User;
import main.java.tukano.impl.data.Following;
import main.java.tukano.impl.data.Likes;
import main.java.tukano.impl.rest.TukanoRestServer;
import main.java.tukano.impl.storage.database.azure.CosmoDB;
import main.java.tukano.impl.storage.database.imp.DataBase;
import main.java.utils.JSON;

import main.java.tukano.impl.storage.cache.*;

public class JavaShorts implements Shorts {

	private static Logger Log = Logger.getLogger(JavaShorts.class.getName());
	
	private static Shorts instance;


	//private static final DataBase<Session> DB = new HibernateDB();
	private static final DataBase<CosmosBatch> DB = new CosmoDB(CosmoDB.Container.SHORTS);

	synchronized public static Shorts getInstance() {
		if( instance == null )
			instance = new JavaShorts();
		return instance;
	}
	
	private JavaShorts() {}
	
	
	@Override
	public Result<Short> createShort(String userId, String password) {
		Log.info(() -> format("createShort : userId = %s, pwd = %s\n", userId, password));

		return errorOrResult( okUser(userId, password), user -> {
			
			var shortId = format("%s+%s", userId, UUID.randomUUID());
			var blobUrl = format("%s/%s/%s", TukanoRestServer.serverURI, Blobs.NAME, shortId);
			var shrt = new Short(shortId, userId, blobUrl);

			return errorOrValue(DB.insertOne(shrt), (Short s) -> s.copyWithLikes_And_Token(0));
		});
	}

	@Override
	public Result<Short> getShort(String shortId) {

		Log.info(() -> format("getShort : shortId = %s\n", shortId));

		if( shortId == null )
			return error(BAD_REQUEST);


		var redisRes = RedisCache.doRedis( j -> {
			var res1 = RedisCache.getRedis(j, shortId, (res) -> JSON.decode(res, Short.class));
			var res2 = RedisCache.getRedis(j, shortId+"-like", (likes) -> Long.parseLong(likes));
			if (res1.isOK() && res2.isOK()) {
				return ok(res1.value().copyWithLikes_And_Token(res2.value()));
			}
			return error(NOT_FOUND);
		});

		if (redisRes.isOK()) return redisRes;

		/*
		try (var jedis = RedisCache.getCachePool().getResource()) {
			var result = jedis.get(shortId);
			var likes = jedis.get(shortId+"-like");
			if (result != null && likes != null) {
				var decodedShrt = JSON.decode(result, Short.class);
				return ok(decodedShrt.copyWithLikes_And_Token(Long.parseLong(likes)));
			}
		}
		catch (Exception e){
			System.err.println(e);
		}
		*/

		var query = format("SELECT count(*) FROM Likes l WHERE l.shortId = '%s'", shortId);
		var likes = DB.sql(query, Long.class);
		return errorOrValue( DB.getOne(shortId, Short.class),
				(Short shrt) -> {
					Short shrt2 = shrt.copyWithLikes_And_Token(likes.get(0));

					RedisCache.doRedis(j -> {
						RedisCache.setRedis(j, shortId, shrt, (s) -> JSON.encode(shrt));
						RedisCache.setRedis(j, shortId+"-like", likes, (l) -> l.get(0).toString());
						return ok();
					});
					return shrt2;
				}
		);


	}

	
	@Override
	public Result<Void> deleteShort(String shortId, String password) {
		Log.info(() -> format("deleteShort : shortId = %s, pwd = %s\n", shortId, password));

		return errorOrResult( getShort(shortId), (Short shrt) ->
				errorOrResult( okUser( shrt.getOwnerId(), password), user ->
					DB.transaction(trans -> {

						DB.deleteOne(shrt.getShortId(), shrt, trans);
						//hibernate.remove( shrt);

						var query = format("DELETE Likes l WHERE l.shortId = '%s'", shortId);
						//hibernate.createNativeQuery( query, Likes.class).executeUpdate();
						DB.sql(query, Likes.class, trans);

						JavaBlobs.getInstance().delete(shrt.getBlobUrl(), Token.get() );

						RedisCache.doRedis(j -> {
							var result = j.get(shortId);
							if (result != null) {
								j.del(shortId);
								j.del(shortId+"-like");
							}
							return ok();
						});
					})
				)
		);
	}

	@Override
	public Result<List<String>> getShorts(String userId) {
		Log.info(() -> format("getShorts : userId = %s\n", userId));

		var query = format("SELECT s.shortId FROM Short s WHERE s.ownerId = '%s'", userId);
		return errorOrValue( okUser(userId), DB.sql( query, String.class));
	}

	@Override
	public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String password) {
		Log.info(() -> format("follow : userId1 = %s, userId2 = %s, isFollowing = %s, pwd = %s\n", userId1, userId2, isFollowing, password));
	
		
		return errorOrResult( okUser(userId1, password), user -> {
			var f = new Following(userId1, userId2);
			return errorOrVoid( okUser( userId2), isFollowing ? DB.insertOne( f ) : DB.deleteOne( f ));	
		});			
	}

	@Override
	public Result<List<String>> followers(String userId, String password) {
		Log.info(() -> format("followers : userId = %s, pwd = %s\n", userId, password));

		var query = format("SELECT f.follower FROM Following f WHERE f.followee = '%s'", userId);		
		return errorOrValue( okUser(userId, password), DB.sql(query, String.class));
	}

	@Override
	public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {
		Log.info(() -> format("like : shortId = %s, userId = %s, isLiked = %s, pwd = %s\n", shortId, userId, isLiked, password));
		
		return errorOrResult( getShort(shortId), shrt -> {
			var l = new Likes(userId, shortId, shrt.getOwnerId());

			var result = errorOrVoid( okUser( userId, password), isLiked ? DB.insertOne( l ) : DB.deleteOne( l ));

			if (result.isOK()) {
				RedisCache.doRedis(j -> {
					var res = j.get(shrt + "-like");
					if (res != null) {
						if (isLiked)
							j.incr(shrt + "-like");
						else
							j.decr(shrt + "-like");
					}
					return ok();
				});
			}
			return result;
		});
	}

	@Override
	public Result<List<String>> likes(String shortId, String password) {
		Log.info(() -> format("likes : shortId = %s, pwd = %s\n", shortId, password));

		return errorOrResult( getShort(shortId), shrt -> {
			
			var query = format("SELECT l.userId FROM Likes l WHERE l.shortId = '%s'", shortId);					
			
			return errorOrValue( okUser( shrt.getOwnerId(), password ), DB.sql(query, String.class));
		});
	}

	@Override
	public Result<List<String>> getFeed(String userId, String password) {
		Log.info(() -> format("getFeed : userId = %s, pwd = %s\n", userId, password));

		final var QUERY_FMT = """
				SELECT s.shortId, s.timestamp FROM Short s WHERE	s.ownerId = '%s'				
				UNION			
				SELECT s.shortId, s.timestamp FROM Short s, Following f 
					WHERE 
						f.followee = s.ownerId AND f.follower = '%s' 
				ORDER BY s.timestamp DESC""";

		return errorOrValue( okUser( userId, password), DB.sql( format(QUERY_FMT, userId, userId), String.class));		
	}
		
	protected Result<User> okUser(String userId, String pwd) {
		return JavaUsers.getInstance().getUser(userId, pwd);
	}
	
	private Result<Void> okUser( String userId ) {
		var res = okUser( userId, "");
		if( res.error() == FORBIDDEN )
			return ok();
		else
			return error( res.error() );
	}
	
	@Override
	public Result<Void> deleteAllShorts(String userId, String password, String token) {
		Log.info(() -> format("deleteAllShorts : userId = %s, password = %s, token = %s\n", userId, password, token));

		if( ! Token.isValid( token, userId ) )
			return error(FORBIDDEN);

		return DB.transaction( (trans) -> {

			//get shorts
			List<String> list = this.getShorts(userId).value();

			//delete shorts
			var query1 = format("DELETE Short s WHERE s.ownerId = '%s'", userId);
			DB.sql(query1, Short.class, trans);
			//hibernate.createQuery(query1, Short.class).executeUpdate();

			//delete follows
			var query2 = format("DELETE Following f WHERE f.follower = '%s' OR f.followee = '%s'", userId, userId);
			DB.sql(query2, Following.class, trans);
			//hibernate.createQuery(query2, Following.class).executeUpdate();

			//delete likes
			var query3 = format("DELETE Likes l WHERE l.ownerId = '%s' OR l.userId = '%s'", userId, userId);
			DB.sql(query3, Likes.class, trans);
			//hibernate.createQuery(query3, Likes.class).executeUpdate();

			RedisCache.doRedis(j -> {
				list.forEach(shrt -> {
					var result = j.get(shrt);
					if (result != null) {
						j.del(shrt);
						j.del(shrt+"-like");
					}
				});
				return ok();
			});
		});
	}
	
}