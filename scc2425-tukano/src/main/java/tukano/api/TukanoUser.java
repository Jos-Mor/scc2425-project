package main.java.tukano.api;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class TukanoUser {
	
	@Id
	private String id;
	private String userId;
	private String pwd;
	private String email;	
	private String displayName;

	public TukanoUser() {}
	
	public TukanoUser(String userId, String pwd, String email, String displayName) {
		this.pwd = pwd;
		this.email = email;
		this.userId = userId;
		this.id = userId;
		this.displayName = displayName;
	}

	public String getId() {
		return id;
	}
	public void setId(String userId) {
		this.userId = userId; this.id = userId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId; this.id = userId;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String id() {
		return id;
	}
	public String userId() {
		return userId;
	}
	
	public String pwd() {
		return pwd;
	}
	
	public String email() {
		return email;
	}
	
	public String displayName() {
		return displayName;
	}
	
	@Override
	public String toString() {
		return "User [userId=" + userId + ", pwd=" + pwd + ", email=" + email + ", displayName=" + displayName + "]";
	}
	
	public TukanoUser copyWithoutPassword() {
		return new TukanoUser(userId, "", email, displayName);
	}
	
	public TukanoUser updateFrom(TukanoUser other ) {
		return new TukanoUser( userId,
				other.pwd != null ? other.pwd : pwd,
				other.email != null ? other.email : email, 
				other.displayName != null ? other.displayName : displayName);
	}
}
