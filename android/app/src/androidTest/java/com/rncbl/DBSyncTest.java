package com.rncbl;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
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
import com.couchbase.lite.URLEndpoint;
import com.facebook.react.bridge.CatalystInstance;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import android.content.Context;
import android.content.SyncAdapterType;

import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import com.facebook.react.bridge.CatalystInstance;
import com.facebook.react.bridge.queue.MessageQueueThreadSpec;
import com.facebook.react.bridge.queue.QueueThreadExceptionHandler;
import com.facebook.react.bridge.queue.ReactQueueConfiguration;
import com.facebook.react.bridge.queue.ReactQueueConfigurationImpl;
import com.facebook.react.bridge.queue.ReactQueueConfigurationSpec;
import com.facebook.react.uimanager.UIManagerModule;


@RunWith(MockitoJUnitRunner.class)
public class DBSyncTest {
    private static final Logger logger = Logger.getLogger(DBSyncTest.class.toString());

    private static String dbName = "syncTest";
    private WritableArray testDBIndexes = new WritableNativeArray();



    private static ReactApplicationContext context;


    private WritableMap createBasicAuth(){
         WritableMap authOptionBasic = new WritableNativeMap();
         authOptionBasic.putString("username","test");
         authOptionBasic.putString("password","password");
         return authOptionBasic;
    }

    private WritableMap createSessionAuth(){
        WritableMap authOptionSession = new WritableNativeMap();
        authOptionSession.putString("sessionId", "sessionId");
        return authOptionSession;
    }



    private WritableMap createSyncParameters(){
        WritableMap syncParameters = new WritableNativeMap();
        syncParameters.putString("syncURL", "wss://abc.net");
        syncParameters.putBoolean("continuous", true);
        syncParameters.putInt("syncDirection", 0);
        syncParameters.putBoolean("enableTrackingSyncStatus", true);
        syncParameters.putString("eventName","eventName");
        syncParameters.putMap("syncOption", null);
        syncParameters.putMap("authOption", createBasicAuth());
        return syncParameters;
    }


    @NonNull
    private static ReactApplicationContext getRNContext() {
        return new ReactApplicationContext(InstrumentationRegistry.getInstrumentation().getTargetContext());
    }


    @Before
    public void setUp() {
        context = getRNContext();
        MockitoAnnotations.openMocks(this);

        DBManager dbManager = new DBManager(this.context);
        dbManager.createDatabase(dbName,testDBIndexes );

    }

    @Test
    public void startReplicationInvalidURL(){
        //not wss or ws
        DBSync dbSync = new DBSync(context);
        WritableMap syncParameters = createSyncParameters();
        syncParameters.putString("syncURL", "http://abc.net");
        WritableMap map = dbSync.startReplication(dbName, syncParameters);
        assertThat(map.getBoolean("syncingInitiated")).isEqualTo(false);
        assertThat(map.getString("syncingInitiatedStatus")).isEqualTo("syncURLNotWS");
    }


    @Test
    public void startReplicationMessedUpURL(){
        DBSync dbSync = new DBSync(context);
        WritableMap syncParameters = createSyncParameters();
        syncParameters.putString("syncURL", "http://finance.yahoo.com/q/h?s=^IXIC");
        WritableMap map = dbSync.startReplication(dbName, syncParameters);
        assertThat(map.getBoolean("syncingInitiated")).isEqualTo(false);
        assertThat(map.getString("syncingInitiatedStatus")).isEqualTo("urlContainsInvalidCharacters");
    }

    @Test
    public void startReplicationAuthNotSet(){
        DBSync dbSync = new DBSync(context);
        WritableMap syncParameters = createSyncParameters();
        syncParameters.putString("syncURL", "wss://fieldtech.net:899");
        syncParameters.putMap("authOption", null);
        WritableMap map = dbSync.startReplication(dbName, syncParameters);
        assertThat(map.getBoolean("syncingInitiated")).isEqualTo(false);
        assertThat(map.getString("syncingInitiatedStatus")).isEqualTo("authenticationNotSet");
    }

    @Test
    public void startReplicationDBNull(){
        DBSync dbSync = new DBSync(context);
        WritableMap syncParameters = createSyncParameters();
        WritableMap map = dbSync.startReplication("aDBThatWeDidnotCreateYet", syncParameters);
        assertThat(map.getBoolean("syncingInitiated")).isEqualTo(false);
        assertThat(map.getString("syncingInitiatedStatus")).isEqualTo("dbIsNull");
    }

    @Test
    public void startReplicationBasicAuthNoTrack(){
        DBSync dbSync = new DBSync(context);
        WritableMap syncParameters = createSyncParameters();
        syncParameters.putBoolean("enableTrackingSyncStatus", false);
        WritableMap map = dbSync.startReplication(dbName, syncParameters);
        assertThat(map.getBoolean("syncingInitiated")).isEqualTo(true);
        assertThat(map.getString("syncingInitiatedStatus")).isEqualTo("");

    }


    @Test
    public void startReplicationSessionNoTrack(){
        DBSync dbSync = new DBSync(context);
        WritableMap syncParameters = createSyncParameters();
        WritableMap authOptionSession = createSessionAuth();
        syncParameters.putMap("authOption", authOptionSession);
        syncParameters.putBoolean("enableTrackingSyncStatus", false);
        WritableMap map = dbSync.startReplication(dbName, syncParameters);
        assertThat(map.getBoolean("syncingInitiated")).isEqualTo(true);
        assertThat(map.getString("syncingInitiatedStatus")).isEqualTo("");
    }


    //FIXIT: react JSModule not loaded
    /*
    @Test
    public void startReplicationBasicAuthTracked(){

        ReactApplicationContext k = new ReactApplicationContext(ApplicationProvider.getApplicationContext());
        CatalystInstance dude = TestHelpers.createMockCatalystInstance();
        k.initializeWithInstance(dude);
        DBSync dbSync = new DBSync(context);
        WritableMap map = dbSync.startReplication(dbName, this.syncParameters);
        assertThat(map.getBoolean("syncingInitiated")).isEqualTo(true);
        assertThat(map.getString("syncingInitiatedStatus")).isEqualTo("");

    }
*/



}
