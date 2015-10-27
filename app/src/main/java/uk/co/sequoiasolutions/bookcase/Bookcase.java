package uk.co.sequoiasolutions.bookcase;

import android.app.Application;

import org.orman.dbms.Database;
import org.orman.dbms.sqliteandroid.SQLiteAndroid;
import org.orman.mapper.MappingSession;

/**
 * Created by ajs on 23/10/2015.
 */
public class Bookcase extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Database db = new SQLiteAndroid(this, "bookcase.db");
        MappingSession.registerDatabase(db);
        //MappingSession.registerPackage("uk.co.sequoiasolutions.bookcase");
        MappingSession.registerEntity(Ebook.class);
        MappingSession.registerEntity(Author.class);

        MappingSession.start();
    }
}
