package com.rncbl;

import android.content.Context;

import com.couchbase.lite.CouchbaseLite;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.ResultSet;

import java.util.logging.Logger;

public class CBLWrapper {

    private static Database database;
    private static DBManager instance = null;
    private static final Logger logger = Logger.getLogger(DBManager.class.toString());

    CBLWrapper(Context context) {
        CouchbaseLite.init(context);
    }

    public Database createDatabase(String DB_NAME) {
        DatabaseConfiguration configuration = new DatabaseConfiguration();
        try {
            database = new Database(DB_NAME, configuration);
            return database;
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalStateException e){
            return null;
        }catch (Exception e){
            return null;
        }
    }

    public ResultSet runQuery(Database db){
        return null;
    }

}
