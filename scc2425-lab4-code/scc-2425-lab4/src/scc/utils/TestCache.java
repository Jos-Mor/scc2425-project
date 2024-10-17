package scc.utils;

import java.util.Locale;

import scc.cache.RedisCache;
import scc.data.User;
import scc.data.UserDAO;

/**
 * Standalone program for accessing the database
 *
 */
public class TestCache {
	
	private static final String MOST_RECENT_USERS_LIST = "MostRecentUsers";
	private static final String MUM_USERS_COUNTER = "NumUsers";

	public static void main(String[] args) {
		System.setProperty(org.slf4j.simple.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "Error");

		try {
			Locale.setDefault(Locale.US);
			
			var id1 = "john-" + System.currentTimeMillis() ;			
			var user1 = new User(id1, "12345", "john@nova.pt", "John Smith");
			
			
			try (var jedis = RedisCache.getCachePool().getResource()) {
				
				var key = "user:" + user1.getId();
				var value = JSON.encode( user1 );
				
			    jedis.set( key, value );
			    
			    
			    var user2 = JSON.decode( jedis.get(key), User.class);
			    System.out.println( user2 );
			    			    
			    var user3 = JSON.decode( jedis.get(key), UserDAO.class);
			    System.out.println( user3 );

			    var cnt = jedis.lpush(MOST_RECENT_USERS_LIST, value );
			    if (cnt > 5)
			        jedis.ltrim(MOST_RECENT_USERS_LIST, 0, 4);
			    
			    var list = jedis.lrange(MOST_RECENT_USERS_LIST, 0, -1);
			    
		    	System.out.println(MOST_RECENT_USERS_LIST);
		    	
			    for( String s : list)
			    	System.out.println(JSON.decode(s, User.class));
			    
			    cnt = jedis.incr(MUM_USERS_COUNTER);
			    System.out.println( "Num users : " + cnt);
			    
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}


