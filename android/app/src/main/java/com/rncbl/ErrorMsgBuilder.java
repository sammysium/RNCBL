package com.rncbl;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

public class ErrorMsgBuilder {
    public static WritableMap ErrorResponse(String action, Exception e){
        /*
        action is what we wanted to do such as deleteDatabase
         */

        WritableMap errorObject  = Arguments.createMap();
        errorObject.putString("errorType", "exception");
        errorObject.putString("action", action);
        errorObject.putString("errorDetail", e.toString());

        return errorObject;
    }

    public static WritableMap ErrorResponse(String action, String e){
        WritableMap errorObject  = Arguments.createMap();
        errorObject.putString("errorType", "logic");
        errorObject.putString("action", action);
        errorObject.putString("errorDetail", e);
        return errorObject;
    }

    public static WritableMap ErrorResponse(String action, Boolean e){
        WritableMap errorObject  = Arguments.createMap();
        errorObject.putString("errorType", "logic");
        errorObject.putString("action", action);
        errorObject.putBoolean("errorDetail", e);
        return errorObject;
    }
}
