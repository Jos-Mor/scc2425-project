package main.java.tukano.impl.storage.database.transaction;

import org.hibernate.Session;

import java.util.function.Function;

public class HibernateTrans implements Transaction <Session>{
    public HibernateTrans(Session s) {
        session = s;
    }

    private final Session session;
    public <T> T add(Function<Session, T> c) {
        return c.apply(session);
    }

}
