package main.java.utils;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;


import main.java.tukano.api.Result;
import main.java.tukano.api.Result.ErrorCode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.exception.ConstraintViolationException;

import static main.java.tukano.impl.rest.TukanoRestServer.Log;

/**
 * A helper class to perform POJO (Plain Old Java Objects) persistence, using
 * Hibernate and a backing relational database.
 * 
 * <Session>
 */
public class Hibernate {
//	private static Logger Log = Logger.getLogger(Hibernate.class.getName());

	private static final String HIBERNATE_CFG_FILE = "hibernate.cfg.xml";
	private SessionFactory sessionFactory;
	private static Hibernate instance;

	private Hibernate() {
		try {
			Log.info("hibernate init");
			sessionFactory = new Configuration().configure().buildSessionFactory();
			Log.info(sessionFactory.toString());
			Log.info("hibernate finish init");


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the Hibernate instance, initializing if necessary. Requires a
	 * configuration file (hibernate.cfg.xml)
	 * 
	 * @return
	 */
	synchronized public static Hibernate getInstance() {
		if (instance == null)
			instance = new Hibernate();
		return instance;
	}

	public Result<Void> persistOne(Object  obj) {
		return execute( (hibernate) -> {
			hibernate.persist( obj );
		});
	}

	public Result<Void> persistOne(Object  obj, Session session) {
		session.persist( obj );
		return Result.ok();
	}

	public <T> Result<T> updateOne(T obj) {
		return execute( hibernate -> {
			var res = hibernate.merge( obj );
			if( res == null)
				return Result.error( ErrorCode.NOT_FOUND );
			
			return Result.ok( res );
		});
	}

	public <T> Result<T> updateOne(T obj, Session session) {
		var res = session.merge( obj );
		if( res == null)
			return Result.error( ErrorCode.NOT_FOUND );

		return Result.ok( res );

	}
	
	public <T> Result<T> deleteOne(T obj) {
		return execute( hibernate -> {
			hibernate.remove( obj );
			return Result.ok( obj );
		});
	}

	public <T> Result<T> deleteOne(T obj, Session session) {
		session.remove( obj );
		return Result.ok( obj );
	}
		
	public <T> Result<T> getOne(Object id, Class<T> clazz) {
		try (var session = sessionFactory.openSession()) {
			var res = session.find(clazz, id);
			if (res == null)
				return Result.error(ErrorCode.NOT_FOUND);
			else
				return Result.ok(res);
		} catch (Exception e) {
			throw e;
		}
	}

	public <T> Result<T> getOne(Object id, Class<T> clazz, Session session) {
		try {
			var res = session.find(clazz, id);
			if (res == null)
				return Result.error(ErrorCode.NOT_FOUND);
			else
				return Result.ok(res);
		} catch (Exception e) {
			throw e;
		}
	}
	
	public <T> List<T> sql(String sqlStatement, Class<T> clazz) {
		try (var session = sessionFactory.openSession()) {
			var query = session.createNativeQuery(sqlStatement, clazz);
			return query.list();
		} catch (Exception e) {
			throw e;
		}
	}

	public <T> List<T> sql(String sqlStatement, Class<T> clazz, Session session) {
		try {
			var query = session.createNativeQuery(sqlStatement, clazz);
			return query.list();
		} catch (Exception e) {
			throw e;
		}
	}

	public <T> void sqlUpdate(String sqlStatement, Class<T> clazz, Session session) {
		try {
			session.createNativeQuery(sqlStatement, clazz).executeUpdate();
		} catch (Exception e) {
			throw e;
		}
	}
	
	public <T> Result<T> execute(Consumer<Session> proc) {
		return execute( (hibernate) -> {
			proc.accept( hibernate);
			return Result.ok();
		});
	}
	
	public <T> Result<T> execute(Function<Session, Result<T>> func) {
		Transaction tx = null;
		try (var session = sessionFactory.openSession()) {
			tx = session.beginTransaction();
			var res = func.apply( session );
			Log.info("function finished");
			session.flush();
			tx.commit();
			Log.info("transaction commited");
			return res;
		}
		catch (ConstraintViolationException __) {	
			return Result.error(ErrorCode.CONFLICT);
		}  
		catch (Exception e) {
			e.printStackTrace();
			if( tx != null ) {
				tx.rollback();
			}

			throw e;
		}
	}
}