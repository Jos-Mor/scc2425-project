package main.java.tukano.impl;

import static java.lang.String.format;

import static main.java.tukano.api.Result.*;
import static main.java.tukano.api.Result.ErrorCode.*;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import main.java.tukano.api.Blobs;
import main.java.tukano.api.Result;
import main.java.tukano.api.Shorts;
import main.java.tukano.api.TukanoShort;


import main.java.tukano.api.TukanoUser;
import main.java.tukano.impl.data.FeedResponse;
import main.java.tukano.impl.data.Following;
import main.java.tukano.impl.data.Likes;
import main.java.tukano.impl.rest.TukanoRestServer;
import main.java.tukano.impl.storage.database.Container;
import main.java.tukano.impl.storage.database.azure.*;
import main.java.tukano.impl.storage.database.UnavailableDBType;
import main.java.tukano.impl.storage.database.imp.DataBase;
import main.java.tukano.impl.storage.database.transaction.TransactionProperties;
import main.java.utils.JSON;

import main.java.tukano.impl.storage.cache.*;

public  class JavaShorts <T> implements Shorts {

	private static Logger Log = Logger.getLogger(JavaShorts.class.getName());
	
	private static Shorts instance;


	//private static final DataBase<Session> DB = new HibernateDB();
	private final DataBase<T> DB = DBPicker.chooseDB(Container.SHORTS);

	synchronized public static Shorts getInstance() {
		if( instance == null ) {
			try {
				instance = new JavaShorts();
			} catch (UnavailableDBType e) {
				throw new RuntimeException(e);
			}
		}
		return instance;
	}
	
	private JavaShorts() throws UnavailableDBType {}
	
	
	@Override
	public Result<TukanoShort> createShort(String userId, String password) {
		Log.info(() -> format("createShort : userId = %s, pwd = %s\n", userId, password));

		return errorOrResult( okUser(userId, password), user -> {
			Log.info("obtained user");
			
			var shortId = format("%s+%s", userId, UUID.randomUUID());
			var blobUrl = format("%s/%s/%s", TukanoRestServer.serverURI, Blobs.NAME, shortId);
			var shrt = new TukanoShort(shortId, userId, blobUrl);

			return errorOrValue(DB.insertOne(shrt), (TukanoShort s) -> s.copyWithLikes_And_Token(0));
		});
	}

	@Override
	public Result<TukanoShort> getShort(String shortId) {

		Log.info(() -> format("getShort : shortId = %s\n", shortId));

		if( shortId == null )
			return error(BAD_REQUEST);

		var redisRes = RedisCache.doRedis( j -> {
			var res1 = RedisCache.getRedis(j, shortId, (res) -> JSON.decode(res, TukanoShort.class));
			var res2 = RedisCache.getRedis(j, shortId+"-like", (likes) -> Long.parseLong(likes));
			if (res1.isOK() && res2.isOK()) {
				return ok(res1.value().copyWithLikes_And_Token(res2.value()));
			}
			return error(NOT_FOUND);
		});

		if (redisRes.isOK()) return redisRes;

		Log.info("done redis...");

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

		final String[] query = new String[1];
		DBPicker.runSQLOrNot(
				() -> {
					query[0] = format("SELECT count(l) FROM Likes l WHERE l.shortId = '%s'", shortId);
				}, () -> {
					query[0] = format("SELECT value count(l) FROM Likes l WHERE l.shortId = '%s' and l.userId != null", shortId);
				});

		var likes = DB.sql(query[0], Long.class);
		Log.info("obtaining short...");
		return errorOrValue( DB.getOne(shortId, TukanoShort.class),
				(TukanoShort shrt) -> {
					Log.info("short obtained: " + shrt.toString());
					TukanoShort shrt2 = shrt.copyWithLikes_And_Token(likes.get(0));
					Log.info("short copied: " + shrt2.toString());

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

		return errorOrResult( getShort(shortId), (TukanoShort shrt) ->
				errorOrResult( okUser( shrt.getOwnerId(), password), user ->
					DB.transaction(trans -> {
						Log.info("in transaction...");

						DB.deleteOne(shrt.getShortId(), shrt, trans);
						//hibernate.remove( shrt);
						Log.info("deleted one");

						DBPicker.runSQLOrNot(
								() -> {
									var query = format("DELETE from Likes l WHERE l.shortId = '%s'", shortId);
									DB.sqlupdate(query, Likes.class, trans);
								}, () -> {
									var query = format("""
											SELECT value {
											    userId: l.userId,
											    shortId: l.shortId,
											    ownerId: l.ownerId
											    } FROM Likes l WHERE l.shortId = '%s'
											    and l.userId != null and l.shortId != null and l.ownerId != null
											    """, shortId);

									List<Likes> list = DB.sql(query, Likes.class);
									list.forEach(DB::deleteOne);
								});

						Log.info("deleted likes");

						JavaBlobs.getInstance().delete(shrt.getBlobUrl(), Token.get() );

						Log.info("deleted blob");

						RedisCache.doRedis(j -> {
							var result = j.get(shortId);
							if (result != null) {
								j.del(shortId);
								j.del(shortId+"-like");
							}
							return ok();
						});
						Log.info("redis done");

					}, new TransactionProperties("partition_key", shortId))
				)
		);
	}

	@Override
	public Result<List<String>> getShorts(String userId) {
		Log.info(() -> format("getShorts : userId = %s\n", userId));

		final String[] query = new String[1];
		DBPicker.runSQLOrNot(
				() -> {
					query[0] = format("SELECT s.shortId FROM TukanoShort s WHERE s.ownerId = '%s'", userId);
				}, () -> {
					query[0] = format("SELECT value s.shortId FROM TukanoShort s WHERE s.ownerId = '%s' and s.timestamp != null", userId);
				});
		return errorOrValue( okUser(userId), DB.sql( query[0], String.class));
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

		final String[] query = new String[1];
		DBPicker.runSQLOrNot(
			() -> {
				query[0] = format("SELECT f.follower FROM Following f WHERE f.followee = '%s'", userId);
			}, () -> {
				query[0] = format("SELECT value f.follower FROM Following f WHERE f.followee = '%s'", userId);
			});
		return errorOrValue( okUser(userId, password), DB.sql(query[0], String.class));
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

			final String[] query = new String[1];
			DBPicker.runSQLOrNot(
					() -> {
						query[0] = format("SELECT l.userId FROM Likes l WHERE l.shortId = '%s'", shortId);
					}, () -> {
						query[0] = format("SELECT value l.userId FROM Likes l WHERE l.shortId = '%s' and l.userId != null and l.ownerId != null", shortId);
					});

			return errorOrValue( okUser( shrt.getOwnerId(), password ), DB.sql(query[0], String.class));
		});
	}

	@Override
	public Result<List<String>> getFeed(String userId, String password) {
		Log.info(() -> format("getFeed : userId = %s, pwd = %s\n", userId, password));



		List<String> list = new java.util.ArrayList<>(List.of());

		DBPicker.runSQLOrNot(
				() -> {
					final var QUERY_FMT = """
						(SELECT s.shortId, s.timestamp FROM TukanoShort s WHERE	s.ownerId = '%s')				
						UNION			
						(SELECT s.shortId, s.timestamp FROM TukanoShort s, Following f 
							WHERE 
								f.followee = s.ownerId AND f.follower = '%s') 
						ORDER BY timestamp DESC""";
					list.addAll(DB.sql( format(QUERY_FMT, userId, userId), String.class));

				}, () -> {
					List<FeedResponse> list2 = new java.util.ArrayList<>(List.of());
					var query1 = format("SELECT s.shortId, s.timestamp FROM TukanoShort s WHERE s.ownerId = '%s' and s.timestamp != null", userId);

					list2.addAll(DB.sql( query1, FeedResponse.class));

					var query2 = format("SELECT value l.followee from Following l where l.follower = '%s'", userId);

					DB.sql( query2, String.class).forEach((s) -> {
						var query3 = format("SELECT s.shortId, s.timestamp FROM TukanoShort s WHERE s.ownerId = '%s' and s.timestamp != null and s.shortId != null", s);
						list2.addAll(DB.sql(query3, FeedResponse.class));
					});

					list.addAll(list2.stream().map(FeedResponse::toString).toList());

				});

		return errorOrValue( okUser( userId, password), list);
	}
		
	protected Result<TukanoUser> okUser(String userId, String pwd) {
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

			DBPicker.runSQLOrNot(
				() -> {
					//delete shorts
					var query1 = format("DELETE from TukanoShort s WHERE s.ownerId = '%s'", userId);
					DB.sqlupdate(query1, TukanoShort.class, trans);
					//hibernate.createQuery(query1, Short.class).executeUpdate();

					//delete follows
					var query2 = format("DELETE from Following f WHERE f.follower = '%s' OR f.followee = '%s'", userId, userId);
					DB.sqlupdate(query2, Following.class, trans);
					//hibernate.createQuery(query2, Following.class).executeUpdate();

					//delete likes
					var query3 = format("DELETE from Likes l WHERE l.ownerId = '%s' OR l.userId = '%s'", userId, userId);
					DB.sqlupdate(query3, Likes.class, trans);
					//hibernate.createQuery(query3, Likes.class).executeUpdate();

				}, () -> {

					var query1 = format("SELECT * from TukanoShort s WHERE s.ownerId = '%s' and s.timestamp != null", userId);
					DB.sql(query1, Short.class).forEach(DB::deleteOne);

					var query2 = format("SELECT * from Following f WHERE f.follower = '%s' OR f.followee = '%s'", userId, userId);
					DB.sql(query2, Following.class).forEach(DB::deleteOne);

					var query3 = format("SELECT * from Likes l WHERE (l.ownerId = '%s' OR l.userId = '%s') and l.userId != null", userId, userId);
					DB.sql(query3, Likes.class).forEach(DB::deleteOne);

					});

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
		}, new TransactionProperties("partition_key", userId));
	}
	
}