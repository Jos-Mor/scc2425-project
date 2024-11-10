package main.test.test;

import com.azure.cosmos.models.CosmosBatch;
import main.java.tukano.api.User;
import main.java.tukano.impl.rest.utils.Props;
import main.java.tukano.impl.storage.database.azure.CosmoDB;
import main.java.tukano.impl.storage.database.imp.DataBase;

import java.util.Locale;

import static main.test.test.Test.show;

public class DBTest {

    public static void main(String[] args) {
        System.setProperty(org.slf4j.simple.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "Error");

        try {
            Locale.setDefault(Locale.US);
            DataBase<CosmosBatch> db = new CosmoDB(CosmoDB.Container.USERS);
			/*
			var id1 = "john";
			var user1 = new User(id1, "12345", "john@nova.pt", "John Smith");

			var res1 = db.insertOne( user1);
			System.out.println( res1 );*/


            show(db.getOne("kgallagher", User.class));

			/*

			var id2 = "mary";
			var user2 = new User(id2, "12345", "mary@nova.pt", "Mary Smith");
			var res3 = db.insertOne( user2);
			System.out.println( res3 );

			var res4 = db.getOne(id2, User.class);
			System.out.println( res4 );

			System.out.println( "Get for id = " + id1);
			var res5 = db.query(User.class, String.format("SELECT * FROM users WHERE users.id=\"%s\"", id1));
			System.out.println( res5 );

			System.out.println( "Get for all ids");
			var res6 = db.query(User.class, "SELECT * FROM users");
			System.out.println( res6);
			*/

        } catch( Exception x ) {
            x.printStackTrace();
        }
    }
}
