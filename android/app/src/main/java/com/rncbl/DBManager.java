package com.rncbl;

import androidx.core.app.DialogCompat;

import com.couchbase.lite.CouchbaseLite;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.Dictionary;
import com.couchbase.lite.Document;
import com.couchbase.lite.Expression;
import com.couchbase.lite.From;
import com.couchbase.lite.GroupBy;
import com.couchbase.lite.Having;
import com.couchbase.lite.IndexBuilder;
import com.couchbase.lite.Join;
import com.couchbase.lite.Joins;
import com.couchbase.lite.Limit;
import com.couchbase.lite.ListenerToken;
import com.couchbase.lite.Meta;
import com.couchbase.lite.MutableDictionary;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.OrderBy;
import com.couchbase.lite.Ordering;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.Select;
import com.couchbase.lite.SelectResult;
import com.couchbase.lite.ValueIndex;
import com.couchbase.lite.ValueIndexItem;
import com.couchbase.lite.Where;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;


import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBManager {


    private static Database database;
    private static DBManager instance = null;
    private static final Logger logger = Logger.getLogger(DBManager.class.toString());
    private static Boolean CBLInitialized = false;
    private CouchCrud couchCrud = new CouchCrud();
    //stores instances of databases
    private static Map<String,Database> createdDBS =new HashMap<String, Database>();
    private static Map<String, LiveQueryListener> liveQueryTokens = new HashMap<String, LiveQueryListener>();


    ReactApplicationContext reactContext;

    DBManager(ReactApplicationContext context) {
        if (CBLInitialized.equals(false)){
            CouchbaseLite.init(context);
            this.reactContext = context;
            CBLInitialized = true;
        }

    }

    public Database getDBInstance(String dbName){
        Database db = DBManager.createdDBS.get(dbName);
        if (db!= null && db.getPath().equals(null)){
            return null;
        }
        return db;
    }

    private void removeDBReference(String dbName){
        this.createdDBS.remove(dbName);
    }

    private ValueIndex buildIndexes(ReadableArray indexedColumns){
        if (indexedColumns.size() > 0 ){
            List<ValueIndexItem> indexes = new ArrayList<>();

            int totalIndexedColumns = indexedColumns.size();
            for(int i =0 ; i < totalIndexedColumns; i++){
                String columnName = indexedColumns.getString(i);
                Expression colExpression = Expression.property(columnName);
                indexes.add(ValueIndexItem.expression(colExpression));
            }
            return IndexBuilder.valueIndex(indexes.toArray(new ValueIndexItem[totalIndexedColumns]));
        }
        return null;
    }

    public Boolean createDatabase(String DB_NAME, ReadableArray indexedColumns) {
        DatabaseConfiguration configuration = new DatabaseConfiguration();
        ValueIndex indexes= buildIndexes(indexedColumns);
        try {
            database = new Database(DB_NAME, configuration);
            if (indexes != null){
                database.createIndex(DB_NAME + "Index", indexes);
            }
            this.createdDBS.put(DB_NAME, this.database);
            return true;
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalStateException e){
            return null;
        }
        catch (Exception e){
            return null;
        }
    }

    public boolean closeDB(String dbName) throws CouchbaseLiteException {
        try {
            Database db = this.getDBInstance(dbName);
            if (db != null){
                db.close();
            }
            return true;
        } catch (NullPointerException e) {
            throw e;
        } catch (CouchbaseLiteException e) {
            throw e;
        }catch (IllegalStateException e){
            throw e;
        } catch(Exception e){
            throw e;
        }
    }

    public boolean deleteDB(String dbName) throws CouchbaseLiteException {
        try {
            Database db = this.getDBInstance(dbName);
            if (db!=null){
                db.delete();
                this.removeDBReference(dbName);
            }

            return true;
        } catch (CouchbaseLiteException e) {
            throw e;
        }catch (IllegalStateException e){
            throw e;
        } catch(Exception e){
            throw e;
        }
    }

    public WritableMap dbProperties(String dbName, String path) {
        try {
            Database db = this.getDBInstance(dbName);
            if (db != null){
                Long totalDocs = db.getCount();
                String name = db.getName();
                String dbDirectory = path + "/" + name + ".cblite2";
                WritableMap dbInfoMap = new WritableNativeMap();
                dbInfoMap.putString("name", name);
                dbInfoMap.putString("path", dbDirectory);
                dbInfoMap.putDouble("totalDocs", totalDocs);
                return dbInfoMap;
            }

            throw new IllegalStateException("missingDB");
        } catch (IllegalStateException e){
            throw e;
        } catch(Exception e){
            throw e;
        }
    }


    public Document insertDocument(String dbName, MutableDocument doc) throws CouchbaseLiteException {
        try {
            Database db = this.getDBInstance(dbName);
            if (db != null){
                db.save(doc);
                return doc;
            }
            throw new IllegalStateException("missingDB");
        }catch (CouchbaseLiteException e){
            throw e;
        }catch (IllegalStateException e){
            throw e;
        } catch(Exception e){
            throw e;
        }

    }
    public WritableMap insertDocuments(String dbName, ReadableArray data) throws CouchbaseLiteException {
        try {
            Database db = this.getDBInstance(dbName);
            if (db != null){

                AtomicInteger totalAffectedDocs = new AtomicInteger();
                AtomicInteger totalRejectedDocs = new AtomicInteger();
                try {
                    db.inBatch(() -> {
                        int totalDocs = data.size();
                        for (int counter = 0; counter < totalDocs; counter++) {
                            ReadableMap map = data.getMap(counter);
                            MutableDocument document = Utils.readableToMutableDoc(map);
                            try {
                                database.save(document);
                                totalAffectedDocs.set(totalAffectedDocs.get() + 1);
                            } catch (CouchbaseLiteException e) {
                                totalRejectedDocs.set(totalRejectedDocs.get() + 1);
                            }
                        }

                    });
                    WritableMap map = new WritableNativeMap();
                    map.putInt("totalAffectedDocs", totalAffectedDocs.intValue());
                    map.putInt("totalRejectedDocs", totalRejectedDocs.intValue());
                    return map;
                } catch (CouchbaseLiteException e) {
                    throw e;
                }
            }
            throw new IllegalStateException("missingDB");
        }catch (CouchbaseLiteException e){
            throw e;
        }catch (IllegalStateException e){
            throw e;
        } catch(Exception e){
            throw e;
        }

    }


    public Boolean deleteDocument(String dbName, String docId) throws CouchbaseLiteException {
        try {
            Database db = this.getDBInstance(dbName);
            if (db != null){
                db.purge(docId);
            }
            return true;
        }catch (CouchbaseLiteException e){
            throw e;
        }catch (IllegalStateException e){
            throw e;
        } catch(Exception e){
            throw e;
        }

    }

    public Document updateDocument(String dbName, String docId, ReadableMap data) throws Exception {
        try {
            Database db = this.getDBInstance(dbName);
            if (db != null){
                Document doc = db.getDocument(docId);
                if (doc != null){
                    MutableDocument document = Utils.readableToMutableDoc(data, doc.toMutable());
                    db.save(document);
                    return document;
                }else{
                    throw new Exception("missingDocument");
                }

            }
            throw new Exception("missingDB");
        }catch (CouchbaseLiteException e){
            throw e;
        }catch (IllegalStateException e){
            throw e;
        } catch(Exception e){
            throw e;
        }

    }

    public Document getDocument(String dbName, String docId) throws Exception {
        try {
            Database db = this.getDBInstance(dbName);
            if (db != null){
                Document doc = db.getDocument(docId);

                return doc;

            }
            throw new Exception("missingDB");
        }catch (CouchbaseLiteException e){
            throw e;
        }catch (IllegalStateException e){
            throw e;
        } catch(Exception e){
            throw e;
        }

    }

    private ResultSet selectDocumentIDS(Database db, Expression whereExp){
        /*
          returns documents with the expression
         */
        Query query = null;

        if (whereExp != null){
            query = QueryBuilder
                    .select(
                            SelectResult.expression(Meta.id)
                    )
                    .from(DataSource.database(db)).where(whereExp);
        }else{
            query = QueryBuilder
                    .select(
                            SelectResult.expression(Meta.id)
                    )
                    .from(DataSource.database(db));
        }


        try {
            ResultSet rs = query.execute();
            return rs;
        } catch (CouchbaseLiteException e) {
            return null;
        }

    }

    public WritableMap updateDocuments(String dbName, Expression whereExp, ReadableMap updateWithFields) throws Exception {
        /*
        Update multiple documents at once.
         */
        WritableMap map = new WritableNativeMap();
        try {
            Database db = this.getDBInstance(dbName);
            if (db != null){

                ResultSet documents = this.selectDocumentIDS(db, whereExp);
                // do we have documents?
                if (documents != null){

                    AtomicInteger totalAffectedDocs = new AtomicInteger();
                    AtomicInteger totalRejectedDocs = new AtomicInteger();

                    try {

                        db.inBatch(() -> {
                            for (Result result : documents) {
                                String docId = result.getString("id");
                                MutableDocument doc = db.getDocument(docId).toMutable();

                                MutableDocument updatedDoc = Utils.readableToMutableDoc(updateWithFields, doc);
                                try {
                                    db.save(updatedDoc);
                                    totalAffectedDocs.set(totalAffectedDocs.get() + 1);
                                } catch (CouchbaseLiteException e) {
                                    totalRejectedDocs.set(totalRejectedDocs.get() + 1);
                                };
                            }

                        });



                        map.putInt("totalAffectedDocs", totalAffectedDocs.intValue());
                        map.putInt("totalRejectedDocs", totalRejectedDocs.intValue());
                        return map;
                    } catch (CouchbaseLiteException e) {
                        throw e;
                    }
                }
                map.putInt("totalAffectedDocs", 0);
                map.putInt("totalRejectedDocs", 0);
                return map;
            }
            throw new Exception("missingDB");
        }catch (CouchbaseLiteException e){
            throw e;
        }catch (IllegalStateException e){
            throw e;
        } catch(Exception e){
            throw e;
        }

    }

    public WritableMap deleteDocuments(String dbName, Expression whereExp) throws Exception {
        /*
        delete multiple documents at once.
         */
        WritableMap map = new WritableNativeMap();
        try {
            Database db = this.getDBInstance(dbName);
            if (db != null){

                ResultSet documents = this.selectDocumentIDS(db, whereExp);
                // do we have documents?
                if (documents != null){

                    AtomicInteger totalAffectedDocs = new AtomicInteger();
                    AtomicInteger totalRejectedDocs = new AtomicInteger();

                    try {

                        db.inBatch(() -> {
                            for (Result result : documents) {
                                String docId = result.getString("id");

                                try {
                                    db.purge(docId);
                                    totalAffectedDocs.set(totalAffectedDocs.get() + 1);
                                } catch (CouchbaseLiteException e) {
                                    totalRejectedDocs.set(totalRejectedDocs.get() + 1);
                                };
                            }

                        });


                        map.putInt("totalAffectedDocs", totalAffectedDocs.intValue());
                        map.putInt("totalRejectedDocs", totalRejectedDocs.intValue());
                        return map;
                    } catch (CouchbaseLiteException e) {
                        throw e;
                    }
                }

                map.putInt("totalAffectedDocs", 0);
                map.putInt("totalRejectedDocs", 0);
                return map;
            }
            throw new Exception("missingDB");
        }catch (CouchbaseLiteException e){
            throw e;
        }catch (IllegalStateException e){
            throw e;
        } catch(Exception e){
            throw e;
        }

    }

    private SelectResult selectField(ReadableMap fieldInfo) {
        /*
         given a fieldname, see if we need to return an aggregate on the field or just hte row as it is
         */

        String fieldName = fieldInfo.getString("fieldName");
        String fromTable = fieldInfo.getString("fromTable");
        //see if this is an aggregate or not
        Expression fieldExp = Utils.expressionField(fieldName, fromTable);
        return SelectResult.expression(fieldExp).as(fieldName);

    }

    private Limit queryLimit(ReadableMap limitInfo,From from, Joins joins, Where where, GroupBy groupBy, Having havingExp, OrderBy orderBy){
       /*
        attaches limit to the query. limit should come after whatever is last
        e.g. if where, gropby and orderby are given, limit should be attached to order by
        so order is from=>where => groupby => orderby

        ORDER MATTERS A LOT!
        */
        Limit limitExp = null;

        Expression limitValue = Expression.intValue(limitInfo.getInt("limit"));
        Expression offsetValue = null;
        if (limitInfo.hasKey("offset")){
            offsetValue = Expression.intValue(limitInfo.getInt("offset"));
        }

        if (orderBy != null){
            limitExp = orderBy.limit(limitValue, offsetValue);
        }else if(havingExp != null){
            limitExp = havingExp.limit(limitValue, offsetValue);
        } else if (groupBy!=null){
            limitExp = groupBy.limit(limitValue, offsetValue);
        } else if(where!= null){
            limitExp = where.limit(limitValue, offsetValue);
        }else if (from != null){
            //attach to from
            limitExp = from.limit(limitValue, offsetValue);
        }else if (joins != null){
            limitExp = joins.limit(limitValue, offsetValue);
        }

        return limitExp;
    }

    private String createLiveQueryName(String dbName, String liveQueryName){
        return dbName + "_" + liveQueryName;
    }


    public WritableArray query(String dbName, ReadableMap queryParams, ReadableMap liveQueryOptions) throws Exception {
        Database db = this.getDBInstance(dbName);
        if (db != null){



            ReadableArray wantedFields = queryParams.getArray("select");
            int totalFields = wantedFields.size();

            SelectResult[] selectedFields = new SelectResult[totalFields];

            if(totalFields == 0){
                //perform a select all.
                selectedFields = new SelectResult[1];
                selectedFields[0] = SelectResult.all();
            }else{
                //user specified fields. loop throuhg each now
                for (int counter =0 ; counter < totalFields; counter++){
                    ReadableMap fieldInfo = wantedFields.getMap(counter);


                    selectedFields[counter] = this.selectField(fieldInfo);
                }
            }
            // do we have where?
            Expression whereExp = null;

            if (queryParams.hasKey("where")){
                ExpressionBuilder expBuilder = new ExpressionBuilder();
                whereExp = expBuilder.createExpression(queryParams.getArray("where"));
            }

            Ordering[] ordering = null;
            if (queryParams.hasKey("orderBy")){
                ordering = Utils.buildOrderingExpression(queryParams.getArray("orderBy"));
            };

            Expression[] groupBy = null;
            Expression havingBy = null;
            if (queryParams.hasKey("groupBy")){
               groupBy = Utils.buildGroupBy(queryParams.getArray("groupBy"));
               if (queryParams.hasKey("having")){
                   havingBy = Utils.buildHavingBy(queryParams.getArray("having"));
               }
            }



            Query query = null;

            Select select = QueryBuilder.select(selectedFields);

            //we can do either from or from attached to joins that returns joins
            From from = null;
            Joins joins = null;

            if(queryParams.hasKey("join")){
                JoinBuilder joinBuilder = new JoinBuilder();
                JSONObject joinInfo = joinBuilder.build(db, Objects.requireNonNull(queryParams.getArray("join")));
                from = select.from(DataSource.database(db).as((String) joinInfo.get("primaryTable")));
                Join[] joinedRows = (Join[]) joinInfo.get("joins");

                joins = select.from(DataSource.database(db)).join(joinedRows);

            }else{
                from = select.from(DataSource.database(db));
            }

            Where where = null;
            GroupBy groupByExp = null;
            Having havingExp = null;
            OrderBy orderBy = null;

            query = from;

            if (whereExp != null){
                if (from != null){
                    where = from.where(whereExp);
                }else{
                    where = joins.where(whereExp);
                }

                 query = where;
            }


            Boolean skipOrderBy = false;
            if (groupBy != null){
                if (where != null){
                    groupByExp = where.groupBy(groupBy);
                }else{
                    if (from != null) {
                        groupByExp = from.groupBy(groupBy);
                    }

                }

                if (havingBy !=null){
                    havingExp = groupByExp.having(havingBy);
                }

               if (ordering != null){
                   if (havingExp != null){
                       orderBy = havingExp.orderBy(ordering);
                   }else{
                       orderBy = groupByExp.orderBy(ordering);
                   }

                   query = orderBy;
                   skipOrderBy = true;
               }else{
                   if (havingExp != null){
                       query = havingExp;
                   }else{
                       query = groupByExp;
                   }
               }
            }

            if (skipOrderBy== false && ordering != null){
                // we have attached orderBy to groupBy.
                if (where != null){
                    orderBy = where.orderBy(ordering);
                }else{
                    if (from != null) {
                        orderBy = from.orderBy(ordering);
                    }
                    else{
                        orderBy = joins.orderBy(ordering);
                    }
                }

                query = orderBy;
            }


            if (queryParams.hasKey("limit")){
                query = this.queryLimit(queryParams.getMap("limit"),from, joins, where, groupByExp,havingExp, orderBy);;
            }


            if (liveQueryOptions.hasKey("liveQuery") && liveQueryOptions.getBoolean("liveQuery") == true){

                String liveQueryName = createLiveQueryName(dbName , liveQueryOptions.getString("liveQueryName"));

                WritableMap liveQueryProps = new WritableNativeMap();

                LiveQueryListener listenerInfo = new LiveQueryListener();

          ListenerToken token  = query.addChangeListener(change->{
                    Throwable error = change.getError();

                    if (error != null){
                        liveQueryProps.putBoolean("error", true);
                        liveQueryProps.putString("errorMessage", error.toString());
                    }
                    else{
                        liveQueryProps.putBoolean("error", false);
                        liveQueryProps.putArray("results", this.couchCrud.toWriteableArray(change.getResults()));
                    }
                    this.liveQueryReport(liveQueryName, liveQueryProps);
                });

                query.execute();


                listenerInfo.liveQueryName = liveQueryName;
                listenerInfo.token = token;
                listenerInfo.query = query;
                liveQueryTokens.put(liveQueryName, listenerInfo);
                return null;

            }else{
                // not a live query
                try {
                    ResultSet resultSet = null;
                    resultSet = query.execute();
                    WritableArray results = this.couchCrud.toWriteableArray(resultSet);
                    return results;
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                    throw new Exception(e);
                }

            }

        }else{
            throw new Exception("missingDB");
        }

    }

    private void liveQueryReport(String eventName, WritableMap documents){
        WritableMap payload = Arguments.createMap();
        // Put data to map
        //payload.putString("status", documents);
        payload.putMap("status", documents );

    try{
        this.reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, payload);
    } catch (RuntimeException e) {
        logger.log(Level.INFO, "liveQueryReport", e);
    }
    }

    Boolean stopLiveQuery (String dbName, String queryName){
        /*
         Given a live query name, stop it
         */
        String liveQueryName = createLiveQueryName(dbName , queryName);
        LiveQueryListener info = this.liveQueryTokens.get(liveQueryName);
        if (info != null){
            info.query.removeChangeListener(info.token);
        }

        return true;

    }

    Boolean deleteIndexes(String dbName, ReadableArray indexes){
          Database db = this.getDBInstance(dbName);
          if (db != null){
              int totalIndexes = indexes.size();
              int counter = 0;
              for (counter = 0 ; counter < totalIndexes; counter++){
                  String indexName = indexes.getString(counter);
                  try{
                      db.deleteIndex(indexName);
                  }catch (CouchbaseLiteException e){
                      e.printStackTrace();
                  }

              }
          }
        return true;
    }

    
    int copyDocumentsToDatabase(String fromDBName, String toDBName, ReadableArray docsExpression, ReadableArray omitFields, Boolean overWriteExisting, Boolean keepMetaId){
        final int[] totalMoved = {0};
        Database fromDB = this.getDBInstance(fromDBName);
        Database toDB = this.getDBInstance(toDBName);
        if (fromDB != null && toDB != null){

            int totalFieldsToOmit = omitFields.size();

            ExpressionBuilder expBuilder = new ExpressionBuilder();
            Expression expression =  expBuilder.createExpression(docsExpression);
            Query query = null;
            if (expression == null){
                query = QueryBuilder.select(SelectResult.all(), SelectResult.expression(Meta.id)).from(DataSource.database(fromDB));
            }else {
                query = QueryBuilder.select(SelectResult.all(), SelectResult.expression(Meta.id)).from(DataSource.database(fromDB)).where(expression);
            }

            try {
                ResultSet resultSet = query.execute();
                fromDB.inBatch(()->{

                    for (Result result : resultSet) {
                        Dictionary dic = result.getDictionary(fromDBName);
                        String id = result.getString("id");
                        Map mapDoc = dic.toMap();

                        MutableDocument doc = null;
                        if (!keepMetaId){
                            doc = new MutableDocument(mapDoc);
                        }else{
                            doc = new MutableDocument(id, mapDoc);
                        }


                        for(int counter = 0; counter < totalFieldsToOmit; counter++){
                            String fieldToRemove = omitFields.getString(counter);
                            doc.remove(fieldToRemove);
                        }

                        Boolean copyDocument = false;

                        if (!overWriteExisting){
                            // don't replace existing document
                            Document existingDocument = toDB.getDocument(doc.getId());
                            if (existingDocument == null){
                                copyDocument = true;
                            }
                        }else{
                            copyDocument = true;
                        }

                        if (copyDocument){
                            try{
                                toDB.save(doc);
                                totalMoved[0] = totalMoved[0] + 1;
                            }catch(CouchbaseLiteException e){ }
                        }

                    }
                });


            } catch (CouchbaseLiteException e) { }


        }
        return totalMoved[0];
    }




}