package main.java.utils.database;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import main.java.tukano.api.Result;
import main.java.utils.Hibernate;
import org.hibernate.Session;

public class DB implements DataBase{

	public DB() {

	}

	public <T> List<T> sql(String query, Class<T> clazz) {
		return Hibernate.getInstance().sql(query, clazz);
	}
	
	public <T> List<T> sql(Class<T> clazz, String fmt, Object ... args) {
		return Hibernate.getInstance().sql(String.format(fmt, args), clazz);
	}
	
	public <T> Result<T> getOne(String id, Class<T> clazz) {
		return Hibernate.getInstance().getOne(id, clazz);
	}
	
	public <T> Result<T> deleteOne(T obj) {
		return Hibernate.getInstance().deleteOne(obj);
	}
	
	public <T> Result<T> updateOne(T obj) {
		return Hibernate.getInstance().updateOne(obj);
	}
	
	public <T> Result<T> insertOne( T obj) {
		return Result.errorOrValue(Hibernate.getInstance().persistOne(obj), obj);
	}
	
	public <T> Result<T> transaction( Consumer<Session> c) {
		return Hibernate.getInstance().execute( c::accept );
	}
	
	public <T> Result<T> transaction( Function<Session, Result<T>> func) {
		return Hibernate.getInstance().execute( func );
	}
}
