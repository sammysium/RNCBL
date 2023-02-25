package com.rncbl;

import android.content.Context;
import android.provider.ContactsContract;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
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
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

//RobolectricTestRunner
@RunWith(MockitoJUnitRunner.class)
public class RNCBLTest {
    private static final Logger logger = Logger.getLogger(RNCBLTest.class.toString());

    private static String dbName = "test";


    @Mock
    private DBManager mockedManager;

    @Mock
    private DBSync dbSync;


    private ReactApplicationContext context = new ReactApplicationContext(InstrumentationRegistry.getInstrumentation().getTargetContext());


    //@InjectMocks // sets above mocks to outgoing
    private RNCBL rncbl;

    private WritableArray testDBIndexes = new WritableNativeArray();

//    @BeforeClass
//    public void setUp(){
//
//    }

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        rncbl = new RNCBL(context, mockedManager, dbSync);

    }


//    @Before
//    public void setUp(){
//        dbManager = new DBManager(context);
//        mockedManager = mock(DBManager.class);
//        mockedRNCBL = new RNCBL(context, mockedManager);
//        rncbl = new RNCBL(context, dbManager);
//        testDBIndexes.pushString("columnName");
//        MockPromise cb = new MockPromise();
//        rncbl.initialize(dbName, testDBIndexes, cb);
//        assertThat(cb.getResult()).isEqualTo(true);
//        WritableMap errorInfo = cb.getErrorReply();
//        assertThat(errorInfo).isEqualTo(null);
//    }

//    public void tearDown(){
//        MockPromise cb = new MockPromise();
//        rncbl.deleteDB(dbName, false, cb);
//        assertThat(cb.getResult()).isEqualTo(true);
//        //delete a non-existing db
//        rncbl.deleteDB(dbName, false, cb);
//        assertThat(cb.getResult()).isEqualTo(true);
//    }

//    @After
//    public void recreateMocks(){
//        reset(mockedManager);
//        mockedManager = mock(DBManager.class);
//        mockedRNCBL = new RNCBL(context, mockedManager);
//    }


    @Test
    public void getName() {
        String name = rncbl.getName();
        assertThat(name).isEqualTo("RNCBL");
    }

    @Test
    public void initialize() {
        IllegalStateException ex = new IllegalStateException("CBL Exception");
        MockPromise cb = new MockPromise();
        doThrow(ex).
                doReturn(true)
                .when(mockedManager).createDatabase(dbName, testDBIndexes);
        rncbl.initialize(dbName,testDBIndexes, cb);
        WritableMap errorInfo = cb.getErrorReply();

        assertThat(errorInfo.getString("errorType")).isEqualTo("logic");
        assertThat(errorInfo.getString("action")).isEqualTo("dbInit");
        assertThat(errorInfo.getBoolean("errorDetail")).isEqualTo(false);
        rncbl.initialize(dbName,testDBIndexes, cb);
        assertThat(cb.getResult()).isEqualTo(true);
    }


    @Test
    public void closeDB() throws CouchbaseLiteException {
        IllegalStateException ex = new IllegalStateException("CBL Exception");
        MockPromise cb = new MockPromise();
        doThrow(ex).
                doReturn(true)
                .when(mockedManager).closeDB(dbName);
        rncbl.closeDB(dbName, cb);
        WritableMap errorInfo = cb.getErrorReply();

        assertThat(errorInfo.getString("errorType")).isEqualTo("exception");
        assertThat(errorInfo.getString("action")).isEqualTo("closeDB");
        assertThat(errorInfo.getString("errorDetail")).isEqualTo(ex.toString());
        rncbl.closeDB(dbName, cb);
        assertThat(cb.getResult()).isEqualTo(true);
    }

    @Test
    public void deleteDB() throws CouchbaseLiteException {
        IllegalStateException ex = new IllegalStateException("Exception");
        MockPromise cb = new MockPromise();
        doThrow(ex).
                doReturn(true)
                .when(mockedManager).deleteDB(dbName);
        rncbl.deleteDB(dbName, false, cb);
        WritableMap errorInfo = cb.getErrorReply();

        assertThat(errorInfo.getString("errorType")).isEqualTo("exception");
        assertThat(errorInfo.getString("action")).isEqualTo("deleteDB");
        assertThat(errorInfo.getString("errorDetail")).isEqualTo(ex.toString());
        rncbl.deleteDB(dbName, false, cb);
        assertThat(cb.getResult()).isEqualTo(true);
    }

    @Test
    public void dbProperties() {
        IllegalStateException ex = new IllegalStateException("Exception");
        MockPromise cb = new MockPromise();
        String path = context.getFilesDir().getAbsolutePath();
        WritableMap dbInfoMap = new WritableNativeMap();
        dbInfoMap.putString("name", dbName);
        dbInfoMap.putString("path", "");
        dbInfoMap.putDouble("totalDocs", 0);

        doThrow(ex).
                doReturn(dbInfoMap)
                .when(mockedManager).dbProperties(dbName, path);

        rncbl.dbProperties(dbName, cb);
        WritableMap errorInfo = cb.getErrorReply();

        assertThat(errorInfo.getString("errorType")).isEqualTo("exception");
        assertThat(errorInfo.getString("action")).isEqualTo("getDbProperties");
        assertThat(errorInfo.getString("errorDetail")).isEqualTo(ex.toString());
        rncbl.dbProperties(dbName,  cb);
        assertThat(cb.getResult()).isEqualTo(dbInfoMap);
    }



    @Test
    public void insertDocument() throws CouchbaseLiteException {
        IllegalStateException ex = new IllegalStateException("Exception");
        MockPromise cb = new MockPromise();
        WritableMap map = new WritableNativeMap();
        String docId = "1234";
        map.putString("id", docId);
        map.putString("name", "name");

        MutableDocument doc = Utils.readableToMutableDoc(map);

        doThrow(ex).
                doReturn(doc)
                .when(mockedManager).insertDocument(dbName, doc);

        rncbl.insertDocument(dbName, map, cb);
        WritableMap errorInfo = cb.getErrorReply();

        assertThat(errorInfo.getString("errorType")).isEqualTo("exception");
        assertThat(errorInfo.getString("action")).isEqualTo("insertDocument");
        assertThat(errorInfo.getString("errorDetail")).isEqualTo(ex.toString());
        rncbl.insertDocument(dbName, map, cb);
        assertThat(cb.getResult()).isEqualTo(doc.getId());
    }

    @Test
    public void insertDocuments() throws CouchbaseLiteException {
        IllegalStateException ex = new IllegalStateException("Exception");
        MockPromise cb = new MockPromise();
        WritableMap map = new WritableNativeMap();
        String docId = "1234";
        map.putString("id", docId);
        map.putString("name", "name");

        WritableArray ar = new WritableNativeArray();
        ar.pushMap(map);


        WritableMap reply = new WritableNativeMap();
        reply.putInt("totalAffectedDocs", 10);
        reply.putInt("totalRejectedDocs", 1);

        doThrow(ex).
                doReturn(reply)
                .when(mockedManager).insertDocuments(dbName, ar);

        rncbl.insertDocuments(dbName, ar, cb);
        WritableMap errorInfo = cb.getErrorReply();

        assertThat(errorInfo.getString("errorType")).isEqualTo("exception");
        assertThat(errorInfo.getString("action")).isEqualTo("insertDocuments");
        assertThat(errorInfo.getString("errorDetail")).isEqualTo(ex.toString());
        rncbl.insertDocuments(dbName, ar, cb);
        assertThat(cb.getResult()).isEqualTo(reply);
    }




    @Test
    public void deleteDocument() throws CouchbaseLiteException {
        IllegalStateException ex = new IllegalStateException("Exception");
        MockPromise cb = new MockPromise();
        String docId = "1234";
        doThrow(ex).
                doReturn(true)
                .when(mockedManager).deleteDocument(dbName, docId);
        rncbl.deleteDocument(dbName, docId, cb);
        WritableMap errorInfo = cb.getErrorReply();

        assertThat(errorInfo.getString("errorType")).isEqualTo("exception");
        assertThat(errorInfo.getString("action")).isEqualTo("deleteDocument");
        assertThat(errorInfo.getString("errorDetail")).isEqualTo(ex.toString());
        rncbl.deleteDocument(dbName, docId, cb);
        assertThat(cb.getResult()).isEqualTo(true);
    }

    @Test
    public void updateDocument() throws Exception {
        IllegalStateException ex = new IllegalStateException("Exception");
        WritableMap updateData = new WritableNativeMap();
        updateData.putString("name", "newName");
        MockPromise cb = new MockPromise();
        String docId = "1234";
        MutableDocument doc = new MutableDocument();
        doc.setString("name", "newName");
        doThrow(ex).
                doReturn(doc)
                .when(mockedManager).updateDocument(dbName, docId, updateData);
        rncbl.updateDocument(dbName, docId, updateData, cb);
        WritableMap errorInfo = cb.getErrorReply();

        assertThat(errorInfo.getString("errorType")).isEqualTo("exception");
        assertThat(errorInfo.getString("action")).isEqualTo("updateDocument");
        assertThat(errorInfo.getString("errorDetail")).isEqualTo(ex.toString());
        rncbl.updateDocument(dbName, docId, updateData, cb);
        assertThat(cb.getResult()).isEqualTo(true);
    }

    @Test
    public void updateDocuments() throws Exception {
        IllegalStateException ex = new IllegalStateException("Exception");
        WritableMap updateData = new WritableNativeMap();
        updateData.putString("name", "newName");
        WritableArray whereExp = new WritableNativeArray();
        WritableMap limit = new WritableNativeMap();
        limit.putString("fieldName", "gender");
        limit.putInt("comparison", 0);
        limit.putString("value", "M");
        whereExp.pushMap(limit);
  
        MockPromise cb = new MockPromise();
        String docId = "1234";
        WritableMap mockReply = new WritableNativeMap();
        mockReply.putInt("totalAffectedDocs", 1);
        mockReply.putInt("totalRejectedDocs", 0);

        //any() is necessary here because ExpressionBuilder returns a unique ID expression each time
        // thus the above and the expression in update documents have different ids, making the call
        // to mockedManager different
        doThrow(ex).
                doReturn(mockReply)
                .when(mockedManager).updateDocuments(eq(dbName), any(), eq(updateData));
        rncbl.updateDocuments(dbName, whereExp, updateData, cb);
        WritableMap errorInfo = cb.getErrorReply();

        assertThat(errorInfo.getString("errorType")).isEqualTo("exception");
        assertThat(errorInfo.getString("action")).isEqualTo("updateDocuments");
        assertThat(errorInfo.getString("errorDetail")).isEqualTo(ex.toString());
        rncbl.updateDocuments(dbName, whereExp, updateData, cb);
        assertThat(cb.getResult()).isEqualTo(mockReply);
    }

    @Test
    public void deleteDocuments() throws Exception {
        IllegalStateException ex = new IllegalStateException("Exception");
        WritableArray whereExp = new WritableNativeArray();
        WritableMap limit = new WritableNativeMap();
        //delete all MEN :p
        limit.putString("fieldName", "gender");
        limit.putInt("comparison", 0);
        limit.putString("value", "M");
        whereExp.pushMap(limit);   
        MockPromise cb = new MockPromise();

        WritableMap mockReply = new WritableNativeMap();
        mockReply.putInt("totalAffectedDocs", 1);
        mockReply.putInt("totalRejectedDocs", 0);

        //any() is necessary here because ExpressionBuilder returns a unique ID expression each time
        // thus the above and the expression in update documents have different ids, making the call
        // to mockedManager different
        doThrow(ex).
                doReturn(mockReply)
                .when(mockedManager).deleteDocuments(eq(dbName), any());
        rncbl.deleteDocuments(dbName, whereExp, cb);
        WritableMap errorInfo = cb.getErrorReply();

        assertThat(errorInfo.getString("errorType")).isEqualTo("exception");
        assertThat(errorInfo.getString("action")).isEqualTo("deleteDocuments");
        assertThat(errorInfo.getString("errorDetail")).isEqualTo(ex.toString());
        rncbl.deleteDocuments(dbName, whereExp, cb);
        assertThat(cb.getResult()).isEqualTo(mockReply);
    }

    @Test
    public void syncDatabaseDbNotInitialized() throws Exception {
        WritableMap authOption = new WritableNativeMap();
        authOption.putString("username","username");
        authOption.putString("password", "password");
        WritableMap syncParameters = new WritableNativeMap();
        syncParameters.putString("syncURL", "wss://abc.net");
        syncParameters.putBoolean("continuous", true);
        syncParameters.putInt("syncDirection", 0);
        syncParameters.putBoolean("enableTrackingSyncStatus", true);
        syncParameters.putMap("authOption", authOption);
        syncParameters.putString("eventName","eventName");
        syncParameters.putMap("syncOption", null);

        MockPromise cb = new MockPromise();

        WritableMap mockReply = new WritableNativeMap();
        mockReply.putBoolean("syncingInitiated", false);
        mockReply.putString("syncingInitiatedStatus", "dbIsNull");

        doReturn(mockReply).when(dbSync).startReplication(dbName, syncParameters);
        rncbl.syncDatabase(dbName, syncParameters, cb);
        assertThat(cb.getResult()).isEqualTo(mockReply);
        verify(dbSync).startReplication(dbName, syncParameters);
    }


    @Test
    public void syncDatabaseDbInitialized() throws Exception {
        WritableMap authOption = new WritableNativeMap();
        authOption.putString("username","username");
        authOption.putString("password", "password");
        WritableMap syncParameters = new WritableNativeMap();
        syncParameters.putString("syncURL", "wss://abc.net");
        syncParameters.putBoolean("continuous", true);
        syncParameters.putInt("syncDirection", 0);
        syncParameters.putBoolean("enableTrackingSyncStatus", true);
        syncParameters.putMap("authOption", authOption);
        syncParameters.putString("eventName","eventName");
        syncParameters.putMap("syncOption", null);
        WritableArray testDBIndexes = new WritableNativeArray();
        testDBIndexes.pushString("columnName");
        MockPromise cb = new MockPromise();

        WritableMap mockReply = new WritableNativeMap();
        mockReply.putBoolean("syncingInitiated", true);
        mockReply.putString("syncingInitiatedStatus", "");

        doReturn(mockReply).when(dbSync).startReplication(dbName, syncParameters);

        rncbl.syncDatabase(dbName, syncParameters, cb);
        assertThat(cb.getResult()).isEqualTo(mockReply);
        verify(dbSync).startReplication(dbName, syncParameters);
    }

    @Test
    public void stopSyncing() throws Exception {

        MockPromise cb = new MockPromise();

        doNothing().when(dbSync).stopReplication();

        rncbl.stopSyncing(dbName, cb);
        assertThat(cb.getResult()).isEqualTo(true);
        verify(dbSync).stopReplication();
    }


    @Test
    public void query() throws Exception {
        IllegalStateException ex = new IllegalStateException("Exception");

        WritableMap liveQueryOptions = new WritableNativeMap();
        liveQueryOptions.putBoolean("liveQuery", false);

        WritableArray selectedFields = new WritableNativeArray();
        selectedFields.pushString("gender");
        selectedFields.pushString("name");

        WritableMap parameters = new WritableNativeMap();
        parameters.putArray("select", selectedFields);

        MockPromise cb = new MockPromise();

        WritableArray queryResults = new WritableNativeArray();


        doThrow(ex).
                doReturn(queryResults)
                .when(mockedManager).query(dbName, parameters, liveQueryOptions);
        rncbl.query(dbName, parameters,liveQueryOptions, cb);
        WritableMap errorInfo = cb.getErrorReply();

        assertThat(errorInfo.getString("errorType")).isEqualTo("exception");
        assertThat(errorInfo.getString("action")).isEqualTo("query");
        assertThat(errorInfo.getString("errorDetail")).isEqualTo(ex.toString());
        rncbl.query(dbName, parameters, liveQueryOptions, cb);
        assertThat(cb.getResult()).isEqualTo(queryResults);
    }

    @Test
    public void stopLiveQuery() throws Exception {
        IllegalStateException ex = new IllegalStateException("Exception");

        String liveQueryName = "liveQueryName";
        MockPromise cb = new MockPromise();

        doReturn(true)
                .when(mockedManager).stopLiveQuery("dbName", liveQueryName);
        rncbl.stopLiveQuery("dbName", liveQueryName, cb);
        assertThat(cb.getResult()).isEqualTo(true);
    }

    @Test
    public void getDocument() throws Exception {
        IllegalStateException ex = new IllegalStateException("Exception");

       MutableDocument document = new MutableDocument();
       document.setString("name", "Daniel");
       document.setString("nationality", "Ethiopian");
       document.setString("job", "Gospel Singer");

        MockPromise cb = new MockPromise();

        String docId = "123";

        doThrow(ex).
                doReturn(document)
                .when(mockedManager).getDocument(dbName, docId);
        rncbl.getDocument(dbName, docId, cb);
        WritableMap errorInfo = cb.getErrorReply();

        assertThat(errorInfo.getString("errorType")).isEqualTo("exception");
        assertThat(errorInfo.getString("action")).isEqualTo("getDocument");
        assertThat(errorInfo.getString("errorDetail")).isEqualTo(ex.toString());
        rncbl.getDocument(dbName, docId, cb);
        assertThat(cb.getResult()).isEqualTo(Arguments.makeNativeMap(document.toMap()));
    }

    @Test
    public void deleteIndexes() throws Exception {
        WritableArray indexNames = new WritableNativeArray();
        indexNames.pushString("name");
        indexNames.pushString("type");
        MockPromise cb = new MockPromise();

        doReturn(true)
                .when(mockedManager).deleteIndexes(dbName, indexNames);

        rncbl.deleteIndexes(dbName, indexNames, cb);
        assertThat(cb.getResult()).isEqualTo(true);
        verify(mockedManager).deleteIndexes(dbName, indexNames);
    }

    @Test
    public void copyDocumentsToDatabase() throws Exception {
        WritableArray copyDocumentsWhereExp = new WritableNativeArray();
        WritableArray omitFieldsFromBeingCopied = new WritableNativeArray();

        MockPromise cb = new MockPromise();

        doReturn(1)
                .when(mockedManager).copyDocumentsToDatabase("fromDB","toDB",  copyDocumentsWhereExp, omitFieldsFromBeingCopied, false, false );

        rncbl.copyDocumentsToDatabase("fromDB","toDB",  copyDocumentsWhereExp, omitFieldsFromBeingCopied, false, false, cb);
        assertThat(cb.getResult()).isEqualTo(1);
        verify(mockedManager).copyDocumentsToDatabase("fromDB","toDB",  copyDocumentsWhereExp, omitFieldsFromBeingCopied, false, false);
    }

    /*
    @Test
    public void initialize() {
        MockPromise cb = new MockPromise();
     //   DatabaseConfiguration configuration = new DatabaseConfiguration();
        try {
            Database database = new Database(dbName, configuration);

            when(mockedManager.createDatabase(dbName)).thenReturn(database);
            rncbl.initialize(dbName, testDBIndexes, cb);
            assertThat(cb.getResult()).isEqualTo(true);
            WritableMap errorInfo = cb.getErrorReply();
            assertThat(errorInfo).isEqualTo(null);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void initializeError(){

        MockPromise cb = new MockPromise();
        when(mockedManager.createDatabase(dbName)).thenReturn(null);
        mockedRNCBL.initialize(dbName, testDBIndexes, cb);
        WritableMap errorInfo = cb.getErrorReply();
        assertThat(errorInfo.getString("errorType")).isEqualTo("logic");
        assertThat(errorInfo.getString("action")).isEqualTo("dbInit");
        assertThat(errorInfo.getBoolean("errorDetail")).isEqualTo(false);

    }




    @Test
    public void closeDBError() throws CouchbaseLiteException {
        MockPromise cb = new MockPromise();
        CouchbaseLiteException ex = new CouchbaseLiteException("CBL Exception");
        when(mockedManager.closeDB(any()))
                .thenThrow(ex);
        mockedRNCBL.closeDB(dbName, cb);
        WritableMap errorInfo = cb.getErrorReply();
        assertThat(errorInfo.getString("errorType")).isEqualTo("exception");
        assertThat(errorInfo.getString("action")).isEqualTo("closeDB");
        assertThat(errorInfo.getString("errorDetail")).isEqualTo(ex.toString());
    }


    @Test
    public void deleteDBError() throws CouchbaseLiteException {
        MockPromise cb = new MockPromise();
        CouchbaseLiteException ex = new CouchbaseLiteException("CBL Exception");
        when(mockedManager.deleteDB(any()))
                .thenThrow(ex);
        mockedRNCBL.deleteDB(dbName, false, cb);
        WritableMap errorInfo = cb.getErrorReply();
        assertThat(errorInfo.getString("errorType")).isEqualTo("exception");
        assertThat(errorInfo.getString("action")).isEqualTo("deleteDB");
        assertThat(errorInfo.getString("errorDetail")).isEqualTo(ex.toString());

    }



    @Test
    public void deleteDBRecreate() throws CouchbaseLiteException {
        MockPromise cb = new MockPromise();
        when(mockedManager.deleteDB(any())).thenAnswer(invocation -> true);
        mockedRNCBL.deleteDB(dbName, true, cb);
        Boolean result = cb.getResult();
        assertThat(result).isEqualTo(true);

    }

    @Test
    public void dbProperties(){
        this.initialize();
        MockPromise cb = new MockPromise();
        rncbl.dbProperties(dbName, cb);
        WritableMap dbInfoMap = new WritableNativeMap();
        dbInfoMap.putString("name", dbName);
        dbInfoMap.putString("path", context.getFilesDir().getPath().concat("/" + dbName + ".cblite2"));
        dbInfoMap.putDouble("totalDocs", 0);
        assertThat(cb.getResult()).isEqualTo(true);
    }
    */
}