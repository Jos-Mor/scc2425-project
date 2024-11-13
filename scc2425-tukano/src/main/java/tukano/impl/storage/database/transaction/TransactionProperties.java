package main.java.tukano.impl.storage.database.transaction;

import java.util.HashMap;

public class TransactionProperties {

    public HashMap<String, Object> props = new HashMap<>();

    public TransactionProperties() {}

    public TransactionProperties(String key, Object value) { props.put(key, value); }

    public TransactionProperties(HashMap<String, Object> map) { props.putAll(map); }

}
