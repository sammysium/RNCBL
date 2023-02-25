package com.rncbl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;

import org.jetbrains.annotations.NotNull;

final class MockPromise implements Promise {

    private Object result;
    private WritableMap errorReply;

    @Override
    public void resolve(@Nullable @org.jetbrains.annotations.Nullable Object value) {
        result = value;
        errorReply= null;
    }

    @Override
    public void reject(String code, String message) {

    }

    @Override
    public void reject(String code, Throwable throwable) {

    }

    @Override
    public void reject(String code, String message, Throwable throwable) {

    }

    @Override
    public void reject(Throwable throwable) {

    }

    @Override
    public void reject(Throwable throwable, WritableMap userInfo) {

    }

    @Override
    public void reject(String code, @NonNull @NotNull WritableMap userInfo) {
         result = false;
         errorReply = userInfo;
    }



    @Override
    public void reject(String code, Throwable throwable, WritableMap userInfo) {

    }

    @Override
    public void reject(String code, String message, @NonNull @NotNull WritableMap userInfo) {

    }

    @Override
    public void reject(String code, String message, Throwable throwable, WritableMap userInfo) {

    }

    @Override
    public void reject(String message) {

    }

    public Object getResult() {
        return result;
    }
    public WritableMap getErrorReply() { return errorReply;}
}
