package main.java.utils.database;

import main.java.tukano.api.Result;
import org.hibernate.Session;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public interface DataBase {
    public <T> List<T> sql(String query, Class<T> clazz);

    public <T> List<T> sql(Class<T> clazz, String fmt, Object ... args);

    public <T> Result<T> getOne(String id, Class<T> clazz);

    public <T> Result<T> deleteOne(T obj);

    public <T> Result<T> updateOne(T obj);

    public <T> Result<T> insertOne( T obj);

    public <T> Result<T> transaction( Consumer<Session> c);

    public <T> Result<T> transaction( Function<Session, Result<T>> func);
}
