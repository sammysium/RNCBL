package com.rncbl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.couchbase.lite.Database;
import com.couchbase.lite.Expression;
import com.facebook.react.bridge.CatalystInstance;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.queue.MessageQueueThreadSpec;
import com.facebook.react.bridge.queue.QueueThreadExceptionHandler;
import com.facebook.react.bridge.queue.ReactQueueConfiguration;
import com.facebook.react.bridge.queue.ReactQueueConfigurationImpl;
import com.facebook.react.bridge.queue.ReactQueueConfigurationSpec;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import java.lang.Math;
public class TestHelpers {

    public static CatalystInstance createMockCatalystInstance() {
        // copied directly from react-native repo and modified
        ReactQueueConfigurationSpec spec =
                ReactQueueConfigurationSpec.builder()
                        .setJSQueueThreadSpec(MessageQueueThreadSpec.mainThreadSpec())
                        .setNativeModulesQueueThreadSpec(MessageQueueThreadSpec.mainThreadSpec())
                        .build();
        ReactQueueConfiguration ReactQueueConfiguration =
                ReactQueueConfigurationImpl.create(
                        spec,
                        new QueueThreadExceptionHandler() {
                            @Override
                            public void handleException(Exception e) {
                                throw new RuntimeException(e);
                            }
                        });

        CatalystInstance reactInstance = mock(CatalystInstance.class);
        when(reactInstance.getReactQueueConfiguration()).thenReturn(ReactQueueConfiguration);
        when(reactInstance.getNativeModule(UIManagerModule.class))
                .thenReturn(mock(UIManagerModule.class));
        when(reactInstance.getNativeModule(DeviceEventManagerModule.class))
                .thenReturn(mock(DeviceEventManagerModule.class));

        return reactInstance;
    }

    public static String jsonPartOfExpression(Expression e){
        String[] exArray = e.toString().split("json");
        return exArray[1];
    }

    public static String generateDBName(String prefix){
        int min = 1;
        int max = 2000000000;
        int b = (int)(Math.random()*(max-min+1)+min);
        return "db_" + prefix + "_" + b;
    }


}
