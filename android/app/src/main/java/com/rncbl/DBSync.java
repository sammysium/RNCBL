package com.rncbl;


import com.couchbase.lite.BasicAuthenticator;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Dictionary;
import com.couchbase.lite.Endpoint;
import com.couchbase.lite.ListenerToken;
import com.couchbase.lite.MutableDictionary;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.ReplicatedDocument;
import com.couchbase.lite.Replicator;
import com.couchbase.lite.ReplicatorConfiguration;
import com.couchbase.lite.SessionAuthenticator;
import com.couchbase.lite.URLEndpoint;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;

import com.facebook.react.modules.core.DeviceEventManagerModule;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBSync {
    private static final Logger logger = Logger.getLogger(DBSync.class.toString());
    private ReactApplicationContext reactContext;
    private String syncingStatus = "STOPPED";
    Replicator replicator = null;
    private Database db;
    private Boolean syncContinuously = false;
    private Integer syncDirection = 0; //0 PULL, 1 PUSH, 2 PULL PUSH
    private String syncURL = "";
    private Boolean enableTrackingSyncStatus = false;
    private String authSessionID = "";
    private ListenerToken listenerToken;
    private String eventName = "";
    private ReadableMap authOption = null;
    private ReadableMap syncOption = null;
    private ReadableArray configHeaders = null;
    private ReadableArray pullFilter = null;
    private ReadableArray pushFilter = null;
    private ListenerToken listenerTokenDocument;
    private Boolean emitPulledDocuments = false;
    private Boolean emitPushedDocuments = false;



    public DBSync(ReactApplicationContext reactContext){
        this.reactContext = reactContext;
    }

    private void setAdditionalSyncingOptions(ReplicatorConfiguration config){
        if (this.syncOption != null){
           /*
               configure the syncing as desired here such as heart beat, filters, stating channels etc
            */
            logger.log(Level.INFO, "options");
        }
    }

    private ReplicatorConfiguration setPullPushFilter(ReplicatorConfiguration config, String applyFilterOn, ReadableArray filterBy){

        if (applyFilterOn.equals("pull")){
            config.setPullFilter((document, flags) -> {
                return Utils.convertMapToSyncFilters(filterBy, document);
            });
        }else if (applyFilterOn.equals("push")){
            config.setPullFilter((document, flags) -> {
                return Utils.convertMapToSyncFilters(filterBy, document);
            });
        }

        return config;
    }

    public WritableMap startReplication(String dbName, ReadableMap syncParameters) {
        /*
           starts replication. the on-going process of replication is handled via emit but any errors
           before we initiate replication is handled/reported
         */
        DBManager dbManager = new DBManager(reactContext);
        Database db = dbManager.getDBInstance(dbName);
        WritableMap replicationInitStatus = new WritableNativeMap();
        replicationInitStatus.putBoolean("syncingInitiated", false);
        replicationInitStatus.putString("syncingInitiatedStatus", "");
        if (db != null){

            this.db = db;

            this.syncContinuously =  syncParameters.getBoolean("continuous");
            this.syncDirection = syncParameters.getInt("syncDirection");
            this.syncURL =syncParameters.getString("syncURL");
            this.enableTrackingSyncStatus = syncParameters.getBoolean("enableTrackingSyncStatus");
            this.authOption = syncParameters.getMap("authOption");
            this.eventName=   syncParameters.getString("eventName");
            this.syncOption = syncParameters.getMap("syncOption");
            if (syncParameters.hasKey("emitPulledDocuments")){
                this.emitPulledDocuments = syncParameters.getBoolean("emitPulledDocuments");
            }
            if (syncParameters.hasKey("emitPushedDocuments")){
                this.emitPushedDocuments = syncParameters.getBoolean("emitPushedDocuments");
            }
            Boolean resetCheckPoint = false;
            if (syncParameters.hasKey("resetCheckPoint")){
                resetCheckPoint = syncParameters.getBoolean("resetCheckPoint");
            }
            if (syncParameters.hasKey("headers")){
                this.configHeaders = syncParameters.getArray("headers");
            }


            URI uri = null;

            Boolean authIsDefined = false;

            try {
                uri = new URI(this.syncURL);
                //uri set correctly
                Endpoint endpoint = new URLEndpoint(uri);
                ReplicatorConfiguration config = new ReplicatorConfiguration(this.db, endpoint);
                if (this.authOption != null){
                    if (authOption.hasKey("username") && authOption.hasKey("password")){
                        //go with basic auth
                        authIsDefined = true;
                        config.setAuthenticator(new BasicAuthenticator(authOption.getString("username"), authOption.getString("password")));
                    }else if(authOption.hasKey("sessionId")){
                        authIsDefined = true;
                        config.setAuthenticator(new SessionAuthenticator(authOption.getString("sessionId")));
                    }
                }

                // we shouldn't sync without proper auth in place
                if (authIsDefined.equals(true)){
                    if (syncParameters.hasKey("pullFromChannelsOnly") && this.syncDirection != 1){
                        //pull from the given channels only
                        List<String> channels = Utils.readableArrayToList(syncParameters.getArray("pullFromChannelsOnly"));
                        config.setChannels(channels);
                    }

                    Boolean setPullFilter = false;
                    Boolean setPushFilter = false;

                    if (this.syncDirection == 1){
                        config.setReplicatorType(ReplicatorConfiguration.ReplicatorType.PUSH);
                        setPushFilter = true;
                    }else if (this.syncDirection == 2){
                        config.setReplicatorType(ReplicatorConfiguration.ReplicatorType.PUSH_AND_PULL );
                        setPushFilter = true;
                        setPullFilter = true;
                    }else {
                        config.setReplicatorType(ReplicatorConfiguration.ReplicatorType.PULL);
                        setPullFilter = true;
                    }
                    config.setContinuous(this.syncContinuously);

                    if (syncParameters.hasKey("filterPull") && setPullFilter){
                        config = this.setPullPushFilter(config, "pull", syncParameters.getArray("filterPull"));
                    }
                    if (syncParameters.hasKey("filterPush") && setPushFilter){
                        config = this.setPullPushFilter(config, "push", syncParameters.getArray("filterPush"));
                    }


                    if (this.configHeaders != null){
                        //add headers
                        int totalHeaders = this.configHeaders.size();
                        int counter = 0;
                        for (counter=0 ; counter < totalHeaders; counter++){
                            Map<String, String> configMap = Utils.createConfigHeadersFromReadableMap(this.configHeaders.getMap(counter));
                            config.setHeaders(configMap);
                        }
                    }

                    //this.setAdditionalSyncingOptions(config);

                    this.replicator = new Replicator(config);

                    if (this.enableTrackingSyncStatus){
                        this.trackSyncingStatus();
                    }
                    replicationInitStatus.putBoolean("syncingInitiated", true);
                    replicationInitStatus.putString("syncingInitiatedStatus", "");
                    this.emittedSyncedDocuments();
                    this.replicator.start(resetCheckPoint);
                }else{
                    replicationInitStatus.putString("syncingInitiatedStatus", "authenticationNotSet");
                }
            } catch (URISyntaxException e) {
                // this.syncURL contains invalid characters. I think we can encode it....
                replicationInitStatus.putString("syncingInitiatedStatus", "urlContainsInvalidCharacters");
            } catch(IllegalArgumentException e){
                // most probably the given URL is not ws or wss
                replicationInitStatus.putString("syncingInitiatedStatus", "syncURLNotWS");
            }catch(Exception e){
                replicationInitStatus.putString("syncingInitiatedStatus", "unknownError " + e.getMessage());
            }
        }

        else{
            //this is on us totally.
            replicationInitStatus.putString("syncingInitiatedStatus", "dbIsNull");
        }

        return replicationInitStatus;
       
    }

    private void trackSyncingStatus(){
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("status", null);
        jsonResponse.put("affectedDocs", 0);
      this.listenerToken =  this.replicator.addChangeListener(change -> {
            CouchbaseLiteException error = change.getStatus().getError();
            if (error != null){
                jsonResponse.put("status",error.getCode());
                jsonResponse.put("affectedDocs", 0);
            }else{
                jsonResponse.put("status", change.getStatus().getActivityLevel().toString());
                jsonResponse.put("affectedDocs", change.getStatus().getProgress().getCompleted());
            }
            this.syncingStatus = jsonResponse.toJSONString();
            logger.log(Level.INFO, "total syncing status " + syncingStatus);
            this.sendSyncingStatus(this.eventName);
        });

    }

    void emittedSyncedDocuments(){
        /*

         */
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("status", "IDLE");
        jsonResponse.put("affectedDocs", 0);
        jsonResponse.put("isPush", null);
        jsonResponse.put("replicatedDocuments", null);
        if ((this.emitPulledDocuments && this.syncDirection != 1) || (this.emitPushedDocuments && this.syncDirection != 0)){

            this.listenerTokenDocument = this.replicator.addDocumentReplicationListener(replication -> {
                List<ReplicatedDocument> replicatedDocuments = replication.getDocuments();
                Boolean isPush = replication.isPush();
                jsonResponse.put("affectedDocs", 0);
                jsonResponse.put("status", "BUSY");
                jsonResponse.put("isPush", isPush);
                jsonResponse.put("replicatedDocuments", replicatedDocuments);

            });
        }
        this.syncingStatus = jsonResponse.toJSONString();
        this.sendSyncingStatus("documentListener" + this.eventName);
    }

    private void sendSyncingStatus(String eventName){
        WritableMap payload = Arguments.createMap();
        // Put data to map
        payload.putString("status", this.syncingStatus);

        this.reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, payload);
    }



    void stopReplication(){
        if (this.listenerTokenDocument != null){
            this.replicator.removeChangeListener(this.listenerTokenDocument);
        }
        if (this.replicator != null){
            this.replicator.stop();
            this.replicator.removeChangeListener(this.listenerToken);
            this.replicator = null;
        }

    }

}
