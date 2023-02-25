package com.rncbl;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;

import com.couchbase.lite.Database;
import com.couchbase.lite.Expression;
import com.couchbase.lite.Join;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;

import org.json.simple.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@RunWith(MockitoJUnitRunner.class)
public class JoinBuilderTest {
    private static final Logger logger = Logger.getLogger(JoinBuilderTest.class.toString());
    private static Context ctxt;
    private static DBManager dbManager;
    private static Database db;

    @NonNull
    private static ReactApplicationContext getRNContext() {
        return new ReactApplicationContext(InstrumentationRegistry.getInstrumentation().getTargetContext());
    }


    @BeforeClass
    public static void setUpDB(){
        ctxt = getRNContext();
        ReactApplicationContext reactApplicationContext = new ReactApplicationContext(ctxt);
        WritableArray testDBIndexes = new WritableNativeArray();
        dbManager = new DBManager(reactApplicationContext);
        testDBIndexes.pushString("columnName");
        dbManager.createDatabase("dbName", testDBIndexes);
        db = dbManager.getDBInstance("dbName");
    }


    @Test
    public void buildJoin(){
        WritableArray joinsInfo = new WritableNativeArray();
        WritableMap joinBlockEmployeesBeneficiaries = new WritableNativeMap();
        WritableMap joinBlockEmployeesGrades = new WritableNativeMap();

        joinBlockEmployeesBeneficiaries.putString("firstTable", "employees");
        joinBlockEmployeesBeneficiaries.putString("firstTableField", "gender");
        joinBlockEmployeesBeneficiaries.putString("secondTable", "beneficiaries");
        joinBlockEmployeesBeneficiaries.putString("secondTableField", "gender");
        joinBlockEmployeesBeneficiaries.putInt("comparison", 0);

        joinBlockEmployeesGrades.putString("firstTable", "employees");
        joinBlockEmployeesGrades.putString("firstTableField", "grade");
        joinBlockEmployeesGrades.putString("secondTable", "grades");
        joinBlockEmployeesGrades.putString("secondTableField", "grade");
        joinBlockEmployeesGrades.putInt("comparison", 1);

        joinsInfo.pushMap(joinBlockEmployeesBeneficiaries);
        joinsInfo.pushMap(joinBlockEmployeesGrades);
        JoinBuilder joinBuilder = new JoinBuilder();
        JSONObject reply = joinBuilder.build(db, joinsInfo);
        Join[] joins = (Join[]) reply.get("joins");
        assertThat(joins.length).isEqualTo(2);
        assertThat(reply.get("primaryTable")).isEqualTo("employees");
    }
}
