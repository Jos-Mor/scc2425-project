package main.java.tukano.api;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import main.java.tukano.impl.Token;

/**
 * Represents a Short video uploaded by an user.
 * 
 * A short has an unique shortId and is owned by a given user; 
 * Comprises of a short video, stored as a binary blob at some bloburl;.
 * A post also has a number of likes, which can increase or decrease over time. It is the only piece of information that is mutable.
 * A short is timestamped when it is created.
 *
 */
@Entity
public class TukanoShort {
	
	@Id
	String id;
	String shortId;
	String ownerId;
	String blobUrl;
	long timestamp;
	int totalLikes;

	public TukanoShort() {}
	
	public TukanoShort(String shortId, String ownerId, String blobUrl, long timestamp, int totalLikes) {
		super();
		this.shortId = shortId;
		this.id = this.shortId;
		this.ownerId = ownerId;
		this.blobUrl = blobUrl;
		this.timestamp = timestamp;
		this.totalLikes = totalLikes;
	}

	public TukanoShort(String shortId, String ownerId, String blobUrl) {
		this( shortId, ownerId, blobUrl, System.currentTimeMillis(), 0);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	public String getShortId() {
		return shortId;
	}

	public void setShortId(String shortId) {
		this.shortId = shortId; this.id = this.shortId;
	}

	public String getOwnerId() { return ownerId; }

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public String getBlobUrl() {
		return blobUrl;
	}

	public void setBlobUrl(String blobUrl) {
		this.blobUrl = blobUrl;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public int getTotalLikes() {
		return totalLikes;
	}

	public void setTotalLikes(int totalLikes) {
		this.totalLikes = totalLikes;
	}

	@Override
	public String toString() {
		return "Short [shortId=" + shortId + ", ownerId=" + ownerId + ", blobUrl=" + blobUrl + ", timestamp="
				+ timestamp + ", totalLikes=" + totalLikes + "]";
	}
	
	public TukanoShort copyWithLikes_And_Token(long totLikes) {
		var urlWithToken = String.format("%s?token=%s", blobUrl, Token.get(blobUrl));
		return new TukanoShort( shortId, ownerId, urlWithToken, timestamp, (int)totLikes);
	}	
}