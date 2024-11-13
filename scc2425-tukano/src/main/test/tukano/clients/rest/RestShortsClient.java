package main.test.tukano.clients.rest;

import java.util.List;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import main.java.tukano.api.Shorts;
import main.java.tukano.api.Result;
import main.java.tukano.api.TukanoShort;
import main.java.tukano.api.rest.RestShorts;

public class RestShortsClient extends RestClient implements Shorts {

	public RestShortsClient(String serverURI) {
		super(serverURI, RestShorts.PATH);
	}

	public Result<TukanoShort> _createShort(String userId, String password) {
		return super.toJavaResult(
				target
				.path(userId)
				.queryParam(RestShorts.PWD, password )
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.post( Entity.json(null)), TukanoShort.class);
	}

	public Result<Void> _deleteShort(String shortId, String password) {
		return super.toJavaResult(
				target
				.path(shortId)
				.queryParam(RestShorts.PWD, password )
				.request()
				.delete());
	}

	public Result<TukanoShort> _getShort(String shortId) {
		return super.toJavaResult(
				target
				.path(shortId)
				.request()
				.get(), TukanoShort.class);
	}

	public Result<List<String>> _getShorts(String userId) {
		return super.toJavaResult(
				target
				.path(userId)
				.path(RestShorts.SHORTS)
				.request()
				.accept( MediaType.APPLICATION_JSON)
				.get(), new GenericType<List<String>>() {});
	}

	public Result<Void> _follow(String userId1, String userId2, boolean isFollowing, String password) {
		return super.toJavaResult(
				target
				.path(userId1)
				.path(userId2)
				.path(RestShorts.FOLLOWERS)
				.queryParam(RestShorts.PWD, password )
				.request()
				.post( Entity.entity(isFollowing, MediaType.APPLICATION_JSON)));
	}

	public Result<List<String>> _followers(String userId, String password) {
		return super.toJavaResult(
				target
				.path(userId)
				.path(RestShorts.FOLLOWERS)
				.queryParam(RestShorts.PWD, password )
				.request()
				.accept( MediaType.APPLICATION_JSON)
				.get(), new GenericType<List<String>>() {});
	}

	public Result<Void> _like(String shortId, String userId, boolean isLiked, String password) {
		return super.toJavaResult(
				target
				.path(shortId)
				.path(userId)
				.path(RestShorts.LIKES)
				.queryParam(RestShorts.PWD, password )
				.request()
				.post( Entity.entity(isLiked, MediaType.APPLICATION_JSON)));
	}

	public Result<List<String>> _likes(String shortId, String password) {
		return super.toJavaResult(
				target
				.path(shortId)
				.path(RestShorts.LIKES)
				.queryParam(RestShorts.PWD, password )
				.request()
				.accept( MediaType.APPLICATION_JSON)
				.get(), new GenericType<List<String>>() {});
	}

	public Result<List<String>> _getFeed(String userId, String password) {
		return super.toJavaResult(
				target
				.path(userId)
				.path(RestShorts.FEED)
				.queryParam(RestShorts.PWD, password )
				.request()
				.accept( MediaType.APPLICATION_JSON)
				.get(), new GenericType<List<String>>() {});
	}

	public Result<Void> _deleteAllShorts(String userId, String password, String token) {
		return super.toJavaResult(
				target
				.path(userId)
				.path(RestShorts.SHORTS)
				.queryParam(RestShorts.PWD, password )
				.queryParam(RestShorts.TOKEN, token )
				.request()
				.delete());
	}
	
	public Result<Void> _verifyBlobURI(String blobId) {
		return super.toJavaResult(
				target
				.path(blobId)
				.request()
				.get());
	}
		
	@Override
	public Result<TukanoShort> createShort(String userId, String password) {
		return super.reTry( () -> _createShort(userId, password));
	}

	@Override
	public Result<Void> deleteShort(String shortId, String password) {
		return super.reTry( () -> _deleteShort(shortId, password));
	}

	@Override
	public Result<TukanoShort> getShort(String shortId) {
		return super.reTry( () -> _getShort(shortId));
	}

	@Override
	public Result<List<String>> getShorts(String userId) {
		return super.reTry( () -> _getShorts(userId));
	}

	@Override
	public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String password) {
		return super.reTry( () -> _follow(userId1, userId2, isFollowing, password));
	}

	@Override
	public Result<List<String>> followers(String userId, String password) {
		return super.reTry( () -> _followers(userId, password));
	}

	@Override
	public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {
		return super.reTry( () -> _like(shortId, userId, isLiked, password));
	}

	@Override
	public Result<List<String>> likes(String shortId, String password) {
		return super.reTry( () -> _likes(shortId, password));
	}

	@Override
	public Result<List<String>> getFeed(String userId, String password) {
		return super.reTry( () -> _getFeed(userId, password));
	}

	@Override
	public Result<Void> deleteAllShorts(String userId, String password, String token) {
		return super.reTry( () -> _deleteAllShorts(userId, password, token));
	}
}
