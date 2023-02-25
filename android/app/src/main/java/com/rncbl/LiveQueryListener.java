package com.rncbl;

import com.couchbase.lite.ListenerToken;
import com.couchbase.lite.Query;

public class LiveQueryListener {
    public String liveQueryName = "";
    public Query query = null;
    public ListenerToken token = null;

}
