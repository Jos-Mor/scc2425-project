package main.java.tukano.impl.storage.database.azure;

import main.java.tukano.impl.storage.database.Container;
import main.java.tukano.impl.storage.database.UnavailableDBType;
import main.java.tukano.impl.storage.database.imp.DataBase;
import main.java.tukano.impl.storage.database.imp.HibernateDB;

import static java.lang.String.format;
import static main.java.tukano.impl.rest.TukanoRestServer.Log;

public class DBPicker {

    private static final String type = System.getenv("DB_TYPE");

    public static <T> DataBase<T> chooseDB(Container container) throws UnavailableDBType {
        DataBase<?> db;
        if (isSQL()) {
            db = new HibernateDB();
        } else {
            db = new NoSQLCosmoDB(container);
        }
        return (DataBase<T>) db;
    }

    public static boolean isSQL() throws UnavailableDBType {
        if (type.equalsIgnoreCase("SQL")) {
            return true;
        } else if (type.equalsIgnoreCase("noSQL")) {
            return false;
        } else {
            throw new UnavailableDBType(type);
        }
    }

    public static void runSQLOrNot(Runnable SQL, Runnable noSQL) {
        try {
            if (DBPicker.isSQL()) {
                SQL.run();
            } else {
                noSQL.run();
            }
        } catch (UnavailableDBType e) {
            Log.info("Should be impossible");
        }
    }

}
