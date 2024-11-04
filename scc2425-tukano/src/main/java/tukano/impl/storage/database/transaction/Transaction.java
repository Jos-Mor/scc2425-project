package main.java.tukano.impl.storage.database.transaction;

import java.util.function.Function;

public interface  Transaction <Y> {
    public <T> T add(Function<Y, T> c);
}
