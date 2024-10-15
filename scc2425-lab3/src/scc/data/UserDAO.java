package scc.data;

/**
 * Represents a User, as stored in the database
 */
public class UserDAO extends User {
	private String _rid;
	public String get_rid() {
		return _rid;
	}


	public void set_rid(String _rid) {
		this._rid = _rid;
	}


	public String get_ts() {
		return _ts;
	}


	public void set_ts(String _ts) {
		this._ts = _ts;
	}


	private String _ts;

	public UserDAO() {
	}
	
	
	@Override
	public String toString() {
		return "UserDAO [_rid=" + _rid + ", _ts=" + _ts + ", user=" + super.toString();
	}
}
