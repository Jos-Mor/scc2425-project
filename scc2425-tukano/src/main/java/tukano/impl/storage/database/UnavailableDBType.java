package main.java.tukano.impl.storage.database;

public class UnavailableDBType extends Exception {
    public UnavailableDBType() {}
    public UnavailableDBType(String type)
    {
        super(type + " not available.");
    }
}

