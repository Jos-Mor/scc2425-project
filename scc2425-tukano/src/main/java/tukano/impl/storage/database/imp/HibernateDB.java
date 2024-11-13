package main.java.tukano.impl.storage.database.imp;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import main.java.tukano.api.Result;
import main.java.tukano.impl.storage.database.transaction.TransactionProperties;
import main.java.utils.Hibernate;
import main.java.tukano.impl.storage.database.transaction.HibernateTrans;
import main.java.tukano.impl.storage.database.transaction.Transaction;
import org.hibernate.Session;

import static main.java.tukano.impl.rest.TukanoRestServer.Log;

public class HibernateDB implements DataBase<Session> {

	public HibernateDB() {

	}

	public <T> List<T> sql(String query, Class<T> clazz) {
		return Hibernate.getInstance().sql(query, clazz);
	}

	public <T> List<T> sql(String query, Class<T> clazz, Transaction<Session> trans) {
		return trans.add(session -> Hibernate.getInstance().sql(query, clazz, session));
	}

	@Override
	public <T> void sqlupdate(String query, Class<T> clazz, Transaction<Session> trans) {
		trans.add(session -> {Hibernate.getInstance().sqlUpdate(query, clazz, session); return null;});
	}

	public <T> Result<T> getOne(String id, Class<T> clazz) {
		return Hibernate.getInstance().getOne(id, clazz);
	}

	public <T> Result<T> getOne(String id, Class<T> clazz, Transaction<Session> trans) {
		return trans.add(session -> Hibernate.getInstance().getOne(id, clazz, session));
	}

	public <T> Result<T> deleteOne(T obj) {
		return Hibernate.getInstance().deleteOne(obj);
	}

	public <T> Result<T> deleteOne(String id, T obj, Transaction<Session> trans) {
		return trans.add(session -> Hibernate.getInstance().deleteOne(obj, session));
	}

	public <T> Result<T> updateOne(T obj) {
		return Hibernate.getInstance().updateOne(obj);
	}

	public <T> Result<T> updateOne(T obj, Transaction<Session> trans) {
		return trans.add(session -> Hibernate.getInstance().updateOne(obj, session));
	}

	public <T> Result<T> insertOne( T obj) {
		return Result.errorOrValue(Hibernate.getInstance().persistOne(obj), obj);
	}

	public <T> Result<T> insertOne(T obj, Transaction<Session> trans) {
		return trans.add(session ->	Result.errorOrValue(Hibernate.getInstance().persistOne(obj, session), obj));
	}

	@Override
	public <T> Result<T> transaction(Consumer<Transaction<Session>> c) {
		return Hibernate.getInstance().execute(t -> {
			Log.info("in transaction hibernate");
			var trans = t.beginTransaction();
			try {
				HibernateTrans hibtrans = new HibernateTrans(t);
				c.accept(hibtrans);
				trans.commit();
			} catch (Exception e) {
				trans.rollback();
				throw e;
			}
		});
	}

	@Override
	public <T> Result<T> transaction(Consumer<Transaction<Session>> c, TransactionProperties props) {
		return Hibernate.getInstance().execute(t -> {
			HibernateTrans hibtrans = new HibernateTrans(t);
			c.accept(hibtrans);
		});
	}

	/*
	public <T> Result<T> transaction( Consumer<Session> c) {
		return Hibernate.getInstance().execute(t -> {
			c.accept(t);
		});
	}
	*/

	public <T> Result<T> transaction( Function<Session, Result<T>> func) {
		return Hibernate.getInstance().execute( func );
	}
}
