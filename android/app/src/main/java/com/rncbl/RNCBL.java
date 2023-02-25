package  com.rncbl;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Expression;
import com.couchbase.lite.Function;
import com.couchbase.lite.MutableDictionary;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Replicator;
import com.couchbase.lite.SelectResult;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;



import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

class RNCBL extends ReactContextBaseJavaModule {
    private static final Logger logger = Logger.getLogger(RNCBL.class.toString());
    private CouchCrud couchCrud = new CouchCrud();
    private String databaseName;
    private ReadableArray indexedFields;
    private static ReactApplicationContext context;
    private static ErrorMsgBuilder errorMsgBuilder;
    private DBManager dbManager;
    private static String errorTitle = "Error";
    private Map<String, Replicator> replicators = new HashMap<String, Replicator>();
    private DBSync dbSync;



    RNCBL(ReactApplicationContext reactContext, DBManager manager, DBSync dbSyncer) {
        super(reactContext);
        context = reactContext;
        dbManager = manager;
        dbSync = dbSyncer;
    }

    @Override
    public String getName() {
        return "RNCBL";
    }


    private boolean createDB(String dbName, ReadableArray indexedColumns){

        this.databaseName  = dbName;
        this.indexedFields = indexedColumns;

        try {
            return dbManager.createDatabase(dbName, indexedColumns);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return false;
        }
    }


    @ReactMethod
    public void initialize(String dbName, ReadableArray indexedColumns, Promise promise){

      boolean connected = this.createDB(dbName, indexedColumns);
      if (connected){
          promise.resolve(true);
      }else{
          WritableMap errorResponse = ErrorMsgBuilder.ErrorResponse("dbInit", false);
          promise.reject(errorTitle, errorResponse);

      }
    }

    @ReactMethod
    public  void closeDB(String dbName, Promise promise) {
        String action = "closeDB";

            try{
                dbManager.closeDB(dbName);
                promise.resolve(true);
            }catch (Exception e){
             WritableMap errorResponse = ErrorMsgBuilder.ErrorResponse(action, e);
             promise.reject(errorTitle, errorResponse);
            }

    }



    @ReactMethod
    public void deleteDB(String dbName, Boolean recreateDB, Promise promise) {
        String action = "deleteDB";

        try {

                dbManager.deleteDB(dbName);
                if (recreateDB.equals(true)){
                    boolean connected = this.createDB(dbName, this.indexedFields);
                    promise.resolve(connected);
                }else{
                    promise.resolve(true);
                }

        } catch (Exception e){

            WritableMap errorResponse = ErrorMsgBuilder.ErrorResponse(action, e);
            promise.reject(errorTitle, errorResponse);
        }
    }

    @ReactMethod
    public  void dbProperties(String dbname, Promise promise){

        File path = context.getFilesDir();
        try {
            WritableMap dbInfoMap = dbManager.dbProperties(dbname, path.getAbsolutePath());
            promise.resolve(dbInfoMap);
        }
        catch (NullPointerException e){
            // context is not being set
            WritableMap errorResponse = ErrorMsgBuilder.ErrorResponse("getDbProperties", e);
            promise.reject(errorTitle, errorResponse);
        }
        catch (Exception e){
            WritableMap errorResponse = ErrorMsgBuilder.ErrorResponse("getDbProperties", e);
            promise.reject(errorTitle, errorResponse);
        }
    }

    @ReactMethod
    public void insertDocument(String dbname, ReadableMap data, Promise promise){
        /*
           insert document created via the MutableDocument class of JS. It is converted to
           JSON in data and we re-read it back
         */
        String action = "insertDocument";
        try{
            MutableDocument document = Utils.readableToMutableDoc(data);
            Document savedDocument = dbManager.insertDocument(dbname, document);
            promise.resolve(savedDocument.getId());
        }catch (Exception e){
            WritableMap errorResponse = ErrorMsgBuilder.ErrorResponse(action, e);
            promise.reject(errorTitle, errorResponse);
        }

    }

    @ReactMethod
    public void insertDocuments(String dbName, ReadableArray data, Promise promise){
        String action = "insertDocuments";
        try{
            WritableMap map = dbManager.insertDocuments(dbName, data);
            promise.resolve(map);
        }catch(Exception e){
            WritableMap errorResponse = ErrorMsgBuilder.ErrorResponse(action, e);
            promise.reject(errorTitle, errorResponse);
        }
    }


    @ReactMethod
    public void deleteDocument(String dbName, String docId, Promise promise){
        String action = "deleteDocument";
        try{
            Boolean result = dbManager.deleteDocument(dbName, docId);
            promise.resolve(result);
        }catch(Exception e){
            WritableMap errorResponse = ErrorMsgBuilder.ErrorResponse(action, e);
            promise.reject(errorTitle, errorResponse);
        }
    }

    @ReactMethod
    public void updateDocument(String dbName, String docId, ReadableMap data, Promise promise){
        String action = "updateDocument";
        try{
            dbManager.updateDocument(dbName, docId, data);

            promise.resolve(true);
        }catch(Exception e){
            WritableMap errorResponse = ErrorMsgBuilder.ErrorResponse(action, e);
            promise.reject(errorTitle, errorResponse);
        }
    }

    @ReactMethod
    public void updateDocuments(String dbName, ReadableArray docsLimitExpression, ReadableMap updateWithData, Promise promise) {
        /*
          update multiple documents at once. docsLimit is documents properties that need to be updated
        */

        ExpressionBuilder expBuilder = new ExpressionBuilder();
        Expression whereExp = expBuilder.createExpression(docsLimitExpression);

        String action = "updateDocuments";
        try{
            WritableMap result = dbManager.updateDocuments(dbName, whereExp, updateWithData);
            promise.resolve(result);
        }catch(Exception e){
            WritableMap errorResponse = ErrorMsgBuilder.ErrorResponse(action, e);
            promise.reject(errorTitle, errorResponse);
        }


    }

    @ReactMethod
    public void deleteDocuments(String dbName, ReadableArray docsLimitExpression, Promise promise) {
        /*
          delete multiple documents at once. docsLimitExpression is documents properties that need to be deleted
        */

        ExpressionBuilder expBuilder = new ExpressionBuilder();
        Expression whereExp = expBuilder.createExpression(docsLimitExpression);

        String action = "deleteDocuments";
        try{
            WritableMap result = dbManager.deleteDocuments(dbName, whereExp);
            promise.resolve(result);
        }catch(Exception e){
            WritableMap errorResponse = ErrorMsgBuilder.ErrorResponse(action, e);
            promise.reject(errorTitle, errorResponse);
        }


    }

    @ReactMethod
    public void syncDatabase(String dbName, ReadableMap syncParameters, Promise promise){

        WritableMap result =  dbSync.startReplication(dbName, syncParameters);

        promise.resolve(result);

    }

    @ReactMethod
    public void stopSyncing(String dbName, Promise promise){
        if (dbSync != null){
            dbSync.stopReplication();
        }
        promise.resolve(true);
    }



    @ReactMethod
    public void query(String dbName, ReadableMap queryParams, ReadableMap liveQueryOptions, Promise promise) throws CouchbaseLiteException {
        String action = "query";

        try{
            WritableArray results =  dbManager.query(dbName, queryParams, liveQueryOptions);
            promise.resolve(results);
        }catch(CouchbaseLiteException e){
            WritableMap errorResponse = ErrorMsgBuilder.ErrorResponse(action, e);
            promise.reject(errorTitle, errorResponse);
        }catch(Exception e){
            WritableMap errorResponse = ErrorMsgBuilder.ErrorResponse(action, e);
            promise.reject(errorTitle, errorResponse);
        }

    }

    @ReactMethod
    public void stopLiveQuery(String dbName, String liveQueryName, Promise promise){
          dbManager.stopLiveQuery(dbName, liveQueryName);
          promise.resolve(true);
    }

    @ReactMethod
    public  void getDocument(String dbName, String docId, Promise promise){
        String action = "getDocument";
        try{
            Document document = dbManager.getDocument(dbName, docId);

            if (document != null){
                WritableMap doc = Arguments.makeNativeMap(document.toMap());
                promise.resolve(doc);
            }else{
                promise.resolve(null);
            }

        }catch(CouchbaseLiteException e){
            logger.log(Level.INFO, "livy e1 " + e);
            WritableMap errorResponse = ErrorMsgBuilder.ErrorResponse(action, e);
            promise.reject(errorTitle, errorResponse);
        }catch(Exception e){
            logger.log(Level.INFO, "livy e2 " + e);
            WritableMap errorResponse = ErrorMsgBuilder.ErrorResponse(action, e);
            promise.reject(errorTitle, errorResponse);
        }

    }

    @ReactMethod
    public void deleteIndexes(String dbName, ReadableArray indexes, Promise promise){
        Boolean reply = dbManager.deleteIndexes(dbName, indexes);
        promise.resolve(reply);
    }

    @ReactMethod
    public void copyDocumentsToDatabase(String fromDBName, String toDBName, ReadableArray docsExpression, ReadableArray omitFields, Boolean overWriteExisting, Boolean keepMetaId, Promise promise){
        /*
        copy records fromDB to toDB that satisfy docsExpression expression.
         */
        int totalCopiedDocuments = dbManager.copyDocumentsToDatabase(fromDBName, toDBName, docsExpression, omitFields, overWriteExisting, keepMetaId);
        promise.resolve(totalCopiedDocuments);

    }

    }