package main.java.tukano.impl.data;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Likes {

	String id;

	@Id

	String userId;

	@Id

	String shortId;

	String ownerId;


	public Likes() {}

	public Likes(String userId, String shortId, String ownerId) {
		this.userId = userId;
		this.shortId = shortId;
		this.ownerId = ownerId;
		this.id = "l-" + this.userId + "-" + this.ownerId + "-" + this.shortId;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId; this.id = "l-" + this.userId + "-" + this.ownerId + "-" + this.shortId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId; this.id = "l-" + this.userId + "-" + this.ownerId + "-" + this.shortId;
	}

	public String getShortId() {
		return shortId;
	}

	public void setShortId(String shortId) {
		this.shortId = shortId; this.id = "l-" + this.userId + "-" + this.ownerId + "-" + this.shortId;
	}

	public String id() {
		return id;
	}
	public String userId() {
		return userId;
	}

	public String shortId() {
		return shortId;
	}

	public String ownerId() {
		return ownerId;
	}

	@Override
	public String toString() {
		return "Likes [userId=" + userId + ", shortId=" + shortId + ", ownerId=" + ownerId + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(ownerId, shortId, userId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Likes other = (Likes) obj;
		return Objects.equals(ownerId, other.ownerId) && Objects.equals(shortId, other.shortId)
				&& Objects.equals(userId, other.userId);
	}
	
	
}
