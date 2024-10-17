package scc.data;

public class User {
	private String id;
	private String pwd;
	private String email;	
	private String displayName;

	public User() {}
	
	public User(String id, String pwd, String email, String displayName) {		
		this.id = id;
		this.pwd = pwd;
		this.email = email;
		this.displayName = displayName;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
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
		return "User [id=" + id + ", pwd=" + pwd + ", email=" + email + ", displayName=" + displayName + "]";
	}	
}
