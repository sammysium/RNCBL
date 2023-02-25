package com.rncbl;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Expression;
import com.couchbase.lite.MutableDocument;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableNativeMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;
import com.facebook.react.modules.core.DeviceEventManagerModule;

//RobolectricTestRunner
@RunWith(MockitoJUnitRunner.class)
public class DBManagerTest {
    private static final Logger logger = Logger.getLogger(DBManagerTest.class.toString());
    private static DBManager dbManager;

    private static WritableArray testDBIndexes = new WritableNativeArray();
    private static String dbName = "db";

    private static Boolean dbCreated;
    private static Database db;


    private static Context ctxt;

    @NonNull
    private static ReactApplicationContext getRNContext() {
        return new ReactApplicationContext(InstrumentationRegistry.getInstrumentation().getTargetContext());
    }


    @BeforeClass
    public static void setUpDB(){
        ctxt = getRNContext();
        ReactApplicationContext reactApplicationContext = new ReactApplicationContext(ctxt);

        dbManager = new DBManager(reactApplicationContext);
        testDBIndexes.pushString("columnName");
        dbCreated = dbManager.createDatabase(dbName, testDBIndexes);
        db = dbManager.getDBInstance(dbName);
    }

    @AfterClass
    public static void removeDB() throws CouchbaseLiteException {
        assertThat(dbManager.deleteDB(dbName)).isEqualTo(true);
        assertThat(db.getPath()).isEqualTo(null);
    }


    @Test
    public void getDBInstance(){
        Boolean dbData = dbManager.createDatabase("sampleName", testDBIndexes);
        Database dbSample = dbManager.getDBInstance("sampleName");
        assertThat(dbSample).isInstanceOf(Database.class);
        assertThat(dbSample.getName()).isEqualTo("sampleName");
        Database dbDummy = dbManager.getDBInstance("dbDummy");
        assertThat(dbDummy).isEqualTo(null);
    }


    @Test
    public void initialize(){
        assertThat(db).isInstanceOf(Database.class);
        assertThat(db.getName()).isEqualTo(dbName);
    }

    @Test
    public void deleteDBNull() throws CouchbaseLiteException {
        assertThat(dbManager.deleteDB(null)).isEqualTo(true);
    }

    @Test
    public void closeDB() throws CouchbaseLiteException {
        assertThat(dbManager.closeDB(dbName)).isEqualTo(true);
        dbManager.createDatabase(dbName, testDBIndexes);
    }


    @Test
    public void closeDBNull() throws CouchbaseLiteException {
        assertThat(dbManager.closeDB(null)).isEqualTo(true);
    }

    @Test
    public void dbProperties() throws CouchbaseLiteException {
        String absolutePath = ctxt.getFilesDir().getAbsolutePath();
        WritableMap dbInfoMap = new WritableNativeMap();
        dbInfoMap.putString("name", db.getName());
        dbInfoMap.putString("path", absolutePath + "/" + db.getName() + ".cblite2");
        dbInfoMap.putDouble("totalDocs", db.getCount());

        WritableMap reply = dbManager.dbProperties(dbName, absolutePath);
        assertThat(reply.getString("name")).isEqualTo(dbInfoMap.getString("name"));
        assertThat(reply.getString("path")).isEqualTo(dbInfoMap.getString("path"));
        //TODO: Should be isEqualTo.
        assertThat(reply.getDouble("totalDocs")).isAtLeast(dbInfoMap.getDouble("totalDocs"));
    }

    @Test (expected = IllegalStateException.class)
    public void dbPropertiesNull() throws CouchbaseLiteException {
        dbManager.dbProperties(null, "path");
    }

    @Test
    public void insertDocument() throws CouchbaseLiteException {
        MutableDocument doc = new MutableDocument();
        doc.setString("hello", "world");
        doc.setBoolean("pinkRocks", true);
        Document newlyAddedDoc = dbManager.insertDocument(dbName, doc);
        assertThat(newlyAddedDoc).isEqualTo(doc);
    }

    @Test
    public void insertDocumentID() throws CouchbaseLiteException {
        String docId = "12345-jondoe";
        MutableDocument doc = new MutableDocument(docId);
        doc.setString("hello", "world");
        doc.setBoolean("rocks", true);
        Document newlyAddedDoc = dbManager.insertDocument(dbName, doc);
        Document savedDocument = db.getDocument(docId);
        assertThat(newlyAddedDoc).isEqualTo(doc);
        assertThat(savedDocument.getId()).isEqualTo(docId);
    }

    @Test (expected = IllegalStateException.class)
    public void insertDocumentNull() throws CouchbaseLiteException {
        MutableDocument doc = new MutableDocument();
        dbManager.insertDocument(null, doc);
    }

    @Test
    public void insertDocuments() throws CouchbaseLiteException {
        WritableMap map = new WritableNativeMap();

        WritableMap semira = new WritableNativeMap();
        semira.putString("name", "semira");
        semira.putInt("age", 30);

        WritableMap feez = new WritableNativeMap();
        feez.putString("name", "feez");
        feez.putInt("age", 5);


        WritableArray data = new WritableNativeArray();
        data.pushMap(semira);
        data.pushMap(feez);
        WritableMap reply = dbManager.insertDocuments(dbName, data);
        assertThat(reply.getInt("totalAffectedDocs")).isEqualTo(2);
        assertThat(reply.getInt("totalRejectedDocs")).isEqualTo(0);
    }



    @Test
    public void deleteDocument() throws CouchbaseLiteException {
        String docId = "123-456-789";
        MutableDocument doc = new MutableDocument(docId);
        doc.setString("hello", "world");
        dbManager.insertDocument(dbName, doc);
        Boolean newlyAddedDoc = dbManager.deleteDocument(dbName, docId);
        assertThat(newlyAddedDoc).isEqualTo(true);
    }

    @Test
    public void updateDocument() throws Exception {
        String docId = "123-456-789";
        MutableDocument doc = new MutableDocument(docId);
        doc.setString("hello", "world");
        dbManager.insertDocument(dbName, doc);
        WritableMap newInfo = new WritableNativeMap();
        newInfo.putString("hello", "selam");
        Document modifiedDocument = dbManager.updateDocument(dbName, docId, newInfo);
        assertThat(modifiedDocument.getId()).isEqualTo(docId);
        assertThat(modifiedDocument.getString("hello")).isEqualTo("selam");
    }

    @Test
    public void getDocument() throws Exception {
        String docId = "123-456-00-1-789";
        MutableDocument doc = new MutableDocument(docId);
        doc.setString("hello", "world");
        dbManager.insertDocument(dbName, doc);
        Document getDoc = dbManager.getDocument(dbName, docId);
        assertThat(getDoc.getId()).isEqualTo(docId);
        assertThat(getDoc.getString("hello")).isEqualTo("world");

        Document getDocNull = dbManager.getDocument(dbName, "nonexistingdocumentIDGoeshere");
        assertThat(getDocNull).isEqualTo(null);
    }

    @Test
    public void updateDocuments() throws Exception {
        String docId1 = "123-456-789";
        MutableDocument doc = new MutableDocument(docId1);
        doc.setString("playedOn", "Monday");
        doc.setString("kind", "soccer");
        doc.setBoolean("is_open", true);
        doc.setString("type","sport");
        dbManager.insertDocument(dbName, doc);
        String docId2 = "123-456-788";
        MutableDocument doc2 = new MutableDocument(docId2);
        doc2.setString("playedOn", "Friday");
        doc2.setString("kind", "volleyball");
        doc2.setBoolean("is_open", true);
        doc2.setString("type","sport");
        dbManager.insertDocument(dbName, doc2);
        String docId3 = "123-456-787";
        MutableDocument doc3 = new MutableDocument(docId3);
        doc3.setString("hostedOn", "Friday");
        doc3.setBoolean("is_open", true);
        doc3.setString("type","hobby");
        dbManager.insertDocument(dbName, doc3);
        WritableMap newInfo = new WritableNativeMap();
        newInfo.putBoolean("is_open", false);
        Expression docsOfType = Expression.property("type").equalTo(Expression.string("sport"));
        WritableMap updatedDocsResult = dbManager.updateDocuments(dbName, docsOfType, newInfo);
        assertThat(updatedDocsResult.getInt("totalAffectedDocs")).isEqualTo(2);
        assertThat(updatedDocsResult.getInt("totalRejectedDocs")).isEqualTo(0);

    }

    @Test
    public void deleteDocuments() throws Exception {
        String docId1 = "100-456-789";
        MutableDocument doc = new MutableDocument(docId1);
        doc.setString("status", "Failed");
        doc.setString("type","operation");
        dbManager.insertDocument(dbName, doc);
        String docId2 = "100-456-788";
        MutableDocument doc2 = new MutableDocument(docId2);
        doc2.setString("status", "Failed");
        doc2.setString("type","operation");
        dbManager.insertDocument(dbName, doc2);
        String docId3 = "100-456-787";
        MutableDocument doc3 = new MutableDocument(docId3);
        doc3.setString("status", "Passed");
        doc3.setString("type","operation");
        dbManager.insertDocument(dbName, doc3);
        // delete all operations have Failed
        Expression docsOfType = Expression.property("type").equalTo(Expression.string("operation"))
                .and(Expression.property("status").equalTo(Expression.string("Failed")));
        WritableMap deletedDocsResult = dbManager.deleteDocuments(dbName, docsOfType);
        assertThat(deletedDocsResult.getInt("totalAffectedDocs")).isEqualTo(2);
        assertThat(deletedDocsResult.getInt("totalRejectedDocs")).isEqualTo(0);
    }

    @Test
    public void query() throws Exception {
        WritableMap liveQueryOptions = new WritableNativeMap();
        liveQueryOptions.putBoolean("liveQuery", false);

        String docId1 = "08100-4561";
        MutableDocument doc = new MutableDocument(docId1);
        doc.setString("name","Daniel");
        doc.setBoolean("isAlive", false);
        doc.setString("type","human");
        doc.setInt("age", 10);
        dbManager.insertDocument(dbName, doc);
        String docId2 = "08100-4562";
        MutableDocument doc2 = new MutableDocument(docId2);
        doc2.setString("name","Elim");
        doc2.setBoolean("isAlive", true);
        doc2.setString("type","human");
        doc2.setInt("age", 30);
        dbManager.insertDocument(dbName, doc2);
        String docId3 = "08100-4563";
        MutableDocument doc3 = new MutableDocument(docId3);
        doc3.setString("name","Selam");
        doc3.setBoolean("isAlive", true);
        doc3.setString("type","human");
        doc3.setInt("age", 5);
        dbManager.insertDocument(dbName, doc3);
        String docId4 = "08100-4564";
        MutableDocument doc4 = new MutableDocument(docId4);
        doc4.setString("name","Doggo");
        doc4.setBoolean("isAlive", true);
        doc4.setString("type","dog");
        doc4.setInt("age", 1);
        dbManager.insertDocument(dbName, doc4);
        String docId5 = "08100-4565";
        MutableDocument doc5 = new MutableDocument(docId5);
        doc5.setString("name","Black");
        doc5.setBoolean("isAlive", false);
        doc5.setString("type","dog");
        doc5.setInt("age", 15);
        dbManager.insertDocument(dbName, doc5);

        // prepare fields to select
        WritableArray selectedFields = new WritableNativeArray();
        selectedFields.pushString("type");
        selectedFields.pushString("isAlive");

        WritableMap parameters = new WritableNativeMap();
        parameters.putArray("select", selectedFields);

        WritableArray results = dbManager.query(dbName, parameters, liveQueryOptions);
        int totalResultsFound = results.size();
        assertThat(totalResultsFound).isEqualTo(5);
        ReadableMap record = results.getMap(0);
        assertThat(record.getBoolean("isAlive")).isEqualTo(false);
        assertThat(record.getString("type")).isEqualTo("human");
        assertThat(results.getMap(1).getBoolean("isAlive")).isEqualTo(true);
        assertThat(results.getMap(1).getString("type")).isEqualTo("human");
        assertThat(results.getMap(2).getBoolean("isAlive")).isEqualTo(true);
        assertThat(results.getMap(2).getString("type")).isEqualTo("human");
        assertThat(results.getMap(3).getBoolean("isAlive")).isEqualTo(true);
        assertThat(results.getMap(3).getString("type")).isEqualTo("dog");
        assertThat(results.getMap(4).getBoolean("isAlive")).isEqualTo(false);
        assertThat(results.getMap(4).getString("type")).isEqualTo("dog");

        assertThat(results.getMap(2).getString("unselectedField")).isEqualTo(null);

        // selectAll. as a reminder, select.all() returns dbName: {....}, dbName: {....}
        WritableArray allFields = new WritableNativeArray();
        WritableMap allFieldsParameters = new WritableNativeMap();
        allFieldsParameters.putArray("select", allFields);
        WritableArray resultsAllFields = dbManager.query(dbName, allFieldsParameters, liveQueryOptions);
        int totalResultsFoundAllFields = resultsAllFields.size();
        assertThat(totalResultsFoundAllFields).isEqualTo(5);

        assertThat(resultsAllFields.getMap(0).getMap(dbName)).isInstanceOf(ReadableNativeMap.class);
        ReadableMap firstRecord = resultsAllFields.getMap(0).getMap(dbName);
        assertThat(firstRecord.getBoolean("isAlive")).isEqualTo(false);
        assertThat(firstRecord.getString("type")).isEqualTo("human");
        assertThat(firstRecord.getInt("age")).isEqualTo(10);
        assertThat(firstRecord.getString("name")).isEqualTo("Daniel");

    }

    //Java wiz needs to take a look at why the below test cant be part of the above query!
    @Test
    public void queryWhereLimit() throws Exception {
        WritableMap liveQueryOptions = new WritableNativeMap();
        liveQueryOptions.putBoolean("liveQuery", false);
        WritableArray selectedFields = new WritableNativeArray();
        //only queryTest
        WritableArray whereLimit = new WritableNativeArray();
        WritableMap simpleLimit = new WritableNativeMap();
        simpleLimit.putString("fieldName", "gender");
        simpleLimit.putString("value","M");
        simpleLimit.putInt("comparison", 0);
        whereLimit.pushMap(simpleLimit);

        WritableMap parametersWhere = new WritableNativeMap();
        parametersWhere.putArray("select", selectedFields);
        parametersWhere.putArray("where", whereLimit);

        WritableArray resultsNoFound = dbManager.query(dbName, parametersWhere, liveQueryOptions);
        assertThat(resultsNoFound.size()).isEqualTo(0);

        String docId1 = "081100-4561";
        MutableDocument doc = new MutableDocument(docId1);
        doc.setString("status", "Failed");
        doc.setString("type","queryTest");
        dbManager.insertDocument(dbName, doc);
        String docId2 = "081100-4562";
        MutableDocument doc2 = new MutableDocument(docId2);
        doc2.setString("status", "Passed");
        doc2.setString("type","queryTest");
        dbManager.insertDocument(dbName, doc2);

        // quries that failed
        WritableArray allFields = new WritableNativeArray();
        WritableArray whereLimitFailedQueries = new WritableNativeArray();
        WritableMap failedQueries = new WritableNativeMap();
        failedQueries.putString("fieldName", "status");
        failedQueries.putString("value","Failed");
        failedQueries.putInt("comparison", 0);
        whereLimitFailedQueries.pushMap(failedQueries);

        WritableMap parametersWhereFailed = new WritableNativeMap();
        parametersWhereFailed.putArray("select", allFields);
        parametersWhereFailed.putArray("where", whereLimitFailedQueries);

        WritableArray resultsFound = dbManager.query(dbName, parametersWhereFailed, liveQueryOptions);
        assertThat(resultsFound.size()).isEqualTo(1);
    }

    @Test
    public void queryAggregates() throws Exception {
        WritableMap liveQueryOptions = new WritableNativeMap();
        liveQueryOptions.putBoolean("liveQuery", false);
        String docId1 = "08331100-4561";
        MutableDocument doc = new MutableDocument(docId1);
        doc.setInt("age", 9);
        doc.setString("type","queryTest");
        dbManager.insertDocument(dbName, doc);
        String docId2 = "08331100-4562";
        MutableDocument doc2 = new MutableDocument(docId2);
        doc2.setInt("age", 10);
        doc2.setString("type","queryTest");
        dbManager.insertDocument(dbName, doc2);

        WritableArray selectedFields = new WritableNativeArray();

        selectedFields.pushString("type");
        selectedFields.pushString("sum___age");
        selectedFields.pushString("count___type");
        WritableMap parameters = new WritableNativeMap();
        parameters.putArray("select", selectedFields);

        WritableArray results = dbManager.query(dbName, parameters, liveQueryOptions);
        int totalResultsFound = results.size();
        assertThat(totalResultsFound).isEqualTo(1);
        ReadableMap record = results.getMap(0);
        assertThat(record.getInt("sum___age")).isEqualTo(19);
        assertThat(record.getInt("count___type")).isEqualTo(2);

    }
    @Test
    public void queryGroupBy() throws Exception {
        WritableMap liveQueryOptions = new WritableNativeMap();
        liveQueryOptions.putBoolean("liveQuery", false);
        String docId1 = "08331100-61";
        MutableDocument doc = new MutableDocument(docId1);
        doc.setInt("age", 9);
        doc.setString("gender", "M");
        doc.setString("type","queryTest");
        dbManager.insertDocument(dbName, doc);
        String docId2 = "08331100-62";
        MutableDocument doc2 = new MutableDocument(docId2);
        doc2.setInt("age", 10);
        doc2.setString("gender", "M");
        doc2.setString("type","queryTest");
        dbManager.insertDocument(dbName, doc2);

        String docId3 = "0833110011-62";
        MutableDocument doc3 = new MutableDocument(docId3);
        doc3.setInt("age", 20);
        doc3.setString("gender", "F");
        doc3.setString("type","queryTest");
        dbManager.insertDocument(dbName, doc3);

        WritableArray selectedFields = new WritableNativeArray();
        WritableArray groupByFields = new WritableNativeArray();
        groupByFields.pushString("gender");

        selectedFields.pushString("gender");
        selectedFields.pushString("sum___age");
        selectedFields.pushString("count___type");
        WritableMap parameters = new WritableNativeMap();
        parameters.putArray("select", selectedFields);
        parameters.putArray("groupBy", groupByFields);

        WritableArray results = dbManager.query(dbName, parameters, liveQueryOptions);
        int totalResultsFound = results.size();
        assertThat(totalResultsFound).isEqualTo(2);
        ReadableMap malesRecord = results.getMap(0);
        assertThat(malesRecord.getString("gender")).isEqualTo("F");
        assertThat(malesRecord.getInt("sum___age")).isEqualTo(20);
        assertThat(malesRecord.getInt("count___type")).isEqualTo(1);
        ReadableMap femalesRecord = results.getMap(1);
        assertThat(femalesRecord.getInt("sum___age")).isEqualTo(19);
        assertThat(femalesRecord.getInt("count___type")).isEqualTo(2);

    }

    @Test
    public void queryLimit() throws Exception {
        WritableMap liveQueryOptions = new WritableNativeMap();
        liveQueryOptions.putBoolean("liveQuery", false);
        String docId1 = "082100-4561";
        MutableDocument doc = new MutableDocument(docId1);
        doc.setString("name","Daniel");
        doc.setBoolean("isAlive", false);
        doc.setString("type","human");
        doc.setInt("age", 10);
        dbManager.insertDocument(dbName, doc);
        String docId2 = "082100-4562";
        MutableDocument doc2 = new MutableDocument(docId2);
        doc2.setString("name","Elim");
        doc2.setBoolean("isAlive", true);
        doc2.setString("type","human");
        doc2.setInt("age", 30);
        dbManager.insertDocument(dbName, doc2);
        String docId3 = "082100-4563";
        MutableDocument doc3 = new MutableDocument(docId3);
        doc3.setString("name","Selam");
        doc3.setBoolean("isAlive", true);
        doc3.setString("type","human");
        doc3.setInt("age", 5);
        dbManager.insertDocument(dbName, doc3);

        // prepare fields to select
        WritableArray selectedFields = new WritableNativeArray();
        WritableMap limit = new WritableNativeMap();

        limit.putInt("limit", 1);

        WritableMap parameters = new WritableNativeMap();
        parameters.putArray("select", selectedFields);
        parameters.putMap("limit", limit);

        WritableArray results = dbManager.query(dbName, parameters, liveQueryOptions);
        int totalResultsFound = results.size();
        assertThat(totalResultsFound).isEqualTo(1);


    }

    @Test
    public void queryOrderByAsc() throws Exception {
        WritableMap liveQueryOptions = new WritableNativeMap();
        liveQueryOptions.putBoolean("liveQuery", false);
        String docId1 = "083100-4561";
        MutableDocument doc = new MutableDocument(docId1);
        doc.setString("name","Daniel");
        doc.setBoolean("isAlive", false);
        doc.setString("type","human");
        doc.setInt("age", 10);
        dbManager.insertDocument(dbName, doc);
        String docId2 = "083100-4562";
        MutableDocument doc2 = new MutableDocument(docId2);
        doc2.setString("name","Elim");
        doc2.setBoolean("isAlive", true);
        doc2.setString("type","human");
        doc2.setInt("age", 30);
        dbManager.insertDocument(dbName, doc2);
        String docId3 = "083100-4563";
        MutableDocument doc3 = new MutableDocument(docId3);
        doc3.setString("name","Selam");
        doc3.setBoolean("isAlive", true);
        doc3.setString("type","human");
        doc3.setInt("age", 5);
        dbManager.insertDocument(dbName, doc3);


        // prepare fields to select
        WritableArray selectedFields = new WritableNativeArray();
        selectedFields.pushString("name");
        selectedFields.pushString("age");

        WritableArray orderBy = new WritableNativeArray();
        WritableMap orderByInfo = new WritableNativeMap();
        orderByInfo.putInt("orderBy", 0);
        orderByInfo.putString("fieldName", "age");
        orderBy.pushMap(orderByInfo);

        WritableMap parameters = new WritableNativeMap();
        parameters.putArray("select", selectedFields);
        parameters.putArray("orderBy", orderBy);

        WritableArray results = dbManager.query(dbName, parameters, liveQueryOptions);

        assertThat(results.getMap(0).getInt("age")).isEqualTo(5);
        assertThat(results.getMap(0).getString("name")).isEqualTo("Selam");
        assertThat(results.getMap(1).getInt("age")).isEqualTo(10);
        assertThat(results.getMap(1).getString("name")).isEqualTo("Daniel");
        assertThat(results.getMap(2).getInt("age")).isEqualTo(30);
        assertThat(results.getMap(2).getString("name")).isEqualTo("Elim");

    }

    @Test
    public void queryLive() throws Exception {
        WritableMap liveQueryOptions = new WritableNativeMap();
        liveQueryOptions.putBoolean("liveQuery", true);
        liveQueryOptions.putString("liveQueryName", "queryName");
        String docId1 = "083100-4561-12345";
        MutableDocument doc = new MutableDocument(docId1);
        doc.setString("name","Daniel");
        doc.setBoolean("isAlive", false);
        doc.setString("type","human");
        doc.setInt("age", 10);
        dbManager.insertDocument(dbName, doc);
        String docId2 = "083100-4562-12345";
        MutableDocument doc2 = new MutableDocument(docId2);
        doc2.setString("name","Elim");
        doc2.setBoolean("isAlive", true);
        doc2.setString("type","human");
        doc2.setInt("age", 30);
        dbManager.insertDocument(dbName, doc2);

        // prepare fields to select
        WritableArray selectedFields = new WritableNativeArray();

        WritableMap parameters = new WritableNativeMap();


        WritableArray whereLimit = new WritableNativeArray();
        WritableMap simpleLimit = new WritableNativeMap();
        simpleLimit.putString("fieldName", "name");
        simpleLimit.putString("value","Daniel");
        simpleLimit.putInt("comparison", 0);
        whereLimit.pushMap(simpleLimit);

        parameters.putArray("select", selectedFields);
        parameters.putArray("where", whereLimit);

        WritableArray results = dbManager.query(dbName, parameters, liveQueryOptions);
        assertThat(results).isEqualTo(null);

        WritableMap updateDaniel = new WritableNativeMap();
        updateDaniel.putString("name", "Daniel Zagreb");

        dbManager.updateDocument(dbName, docId1, updateDaniel);

    }

    @Test
    public void stopQueryLive() throws Exception {
        assertThat(dbManager.stopLiveQuery("dbName", "queryName")).isEqualTo(true);
    }

    @Test
    public void deleteIndexes() throws CouchbaseLiteException {

        WritableArray indexNames = new WritableNativeArray();
        indexNames.pushString("name");
        indexNames.pushString("type");
        Boolean reply = dbManager.deleteIndexes(dbName, indexNames);
        assertThat(reply).isEqualTo(true);

    }

    @Test
    public void copyDocumentsToDatabase() throws Exception {
        String destDb = TestHelpers.generateDBName("dest");
        String sourceDb = TestHelpers.generateDBName("source");

        dbManager.createDatabase(sourceDb, testDBIndexes);
        dbManager.createDatabase(destDb, testDBIndexes);
        Database destDatabase = dbManager.getDBInstance(destDb);

        String docId1 = "08100-4561";
        MutableDocument doc = new MutableDocument(docId1);
        doc.setString("name","Daniel");
        doc.setBoolean("isAlive", false);
        doc.setString("type","human");
        doc.setInt("age", 10);
        dbManager.insertDocument(sourceDb, doc);
        String docId2 = "08100-4562";
        MutableDocument doc2 = new MutableDocument(docId2);
        doc2.setString("name","Elim");
        doc2.setBoolean("isAlive", true);
        doc2.setString("type","human");
        doc2.setInt("age", 30);
        dbManager.insertDocument(sourceDb, doc2);

        WritableArray expressions = new WritableNativeArray();
        WritableNativeArray omitFields = new WritableNativeArray();
        int dbTotalMovedDocuments = dbManager.copyDocumentsToDatabase(sourceDb, destDb, expressions, omitFields, false, true);
        assertThat(dbTotalMovedDocuments).isEqualTo(2);
        assertThat(destDatabase.getCount()).isEqualTo(2);

    }

    @Test
    public void copyDocumentsToDatabaseWithFieldOmit() throws Exception {
        String destDb = TestHelpers.generateDBName("destW");
        String sourceDb = TestHelpers.generateDBName("sourceW");

        dbManager.createDatabase(sourceDb, testDBIndexes);
        dbManager.createDatabase(destDb, testDBIndexes);
        Database destDatabase = dbManager.getDBInstance(destDb);

        String docId1 = "08100-4561333";
        MutableDocument doc = new MutableDocument(docId1);
        doc.setString("name","Daniel");
        doc.setBoolean("isAlive", false);
        doc.setString("type","human");
        doc.setInt("age", 10);
        dbManager.insertDocument(sourceDb, doc);
        String docId2 = "08100-4562333";
        MutableDocument doc2 = new MutableDocument(docId2);
        doc2.setString("name","Elim");
        doc2.setBoolean("isAlive", true);
        doc2.setString("type","human");
        doc2.setInt("age", 30);
        dbManager.insertDocument(sourceDb, doc2);

        WritableArray expressions = new WritableNativeArray();
        WritableNativeArray omitFields = new WritableNativeArray();
        omitFields.pushString("name");
        int dbTotalMovedDocuments = dbManager.copyDocumentsToDatabase(sourceDb, destDb, expressions, omitFields, false, true );
        assertThat(dbTotalMovedDocuments).isEqualTo(2);
        assertThat(destDatabase.getCount()).isEqualTo(2);
        Document document = destDatabase.getDocument(docId1);

        assertThat(document.contains("name")).isEqualTo(false);
        assertThat(document.getString("type")).isEqualTo("human");

    }

    @Test
    public void copyDocumentsToDatabaseReplaceId() throws Exception {
        String destDb = TestHelpers.generateDBName("destRid");
        String sourceDb = TestHelpers.generateDBName("sourceRid");

        dbManager.createDatabase(sourceDb, testDBIndexes);
        dbManager.createDatabase(destDb, testDBIndexes);
        Database destDatabase = dbManager.getDBInstance(destDb);

        String docId1 = "08100-4561";
        MutableDocument doc = new MutableDocument(docId1);
        doc.setString("name","Daniel");
        doc.setBoolean("isAlive", false);
        doc.setString("type","human");
        doc.setInt("age", 10);
        dbManager.insertDocument(sourceDb, doc);
        String docId2 = "08100-4562";
        MutableDocument doc2 = new MutableDocument(docId2);
        doc2.setString("name","Elim");
        doc2.setBoolean("isAlive", true);
        doc2.setString("type","human");
        doc2.setInt("age", 30);
        dbManager.insertDocument(sourceDb, doc2);

        WritableArray expressions = new WritableNativeArray();
        WritableNativeArray omitFields = new WritableNativeArray();
        int dbTotalMovedDocuments = dbManager.copyDocumentsToDatabase(sourceDb, destDb, expressions, omitFields, false, false);
        assertThat(dbTotalMovedDocuments).isEqualTo(2);
        Document document = destDatabase.getDocument(docId1);
        assertThat(document).isEqualTo(null);

    }
}
