package main.java.tukano.impl.storage.database.imp;

import main.java.tukano.api.Result;
import main.java.tukano.impl.storage.database.transaction.Transaction;
import main.java.tukano.impl.storage.database.transaction.TransactionProperties;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public interface DataBase <Z> {
    public <T> List<T> sql(String query, Class<T> clazz);

    public <T> List<T> sql(String query, Class<T> clazz, Transaction<Z> trans);

    public <T> void sqlupdate(String query, Class<T> clazz, Transaction<Z> trans);

    public <T> Result<T> getOne(String id, Class<T> clazz);

    public <T> Result<T> getOne(String id, Class<T> clazz, Transaction<Z> trans);

    public <T> Result<T> deleteOne(T obj);

    public <T> Result<?> deleteOne(String id, T obj, Transaction<Z> trans);

    public <T> Result<T> updateOne(T obj);

    public <T> Result<T> updateOne(T obj, Transaction<Z> trans);

    public <T> Result<T> insertOne( T obj);

    public <T> Result<T> insertOne( T obj, Transaction<Z> trans);

    public <T> Result<T> transaction( Consumer<Transaction<Z>> c);

    public <T> Result<T> transaction( Consumer<Transaction<Z>> c, TransactionProperties properties);

}
