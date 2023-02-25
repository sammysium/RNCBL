package com.rncbl;

import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import java.util.List;

public class CouchCrud {
    public WritableArray toWriteableArray(ResultSet resultSet ) {
        /*
        Convert a CB resultset to a writeable array which
        can be consumed by RN. Calling method assures resultSet
        is not null
         */
        WritableArray writableArray = Arguments.createArray();
        for (Result result : resultSet) {
            WritableMap writableMap = Arguments.makeNativeMap(result.toMap());
            writableArray.pushMap(writableMap);
        }

        return writableArray;
    }

}
