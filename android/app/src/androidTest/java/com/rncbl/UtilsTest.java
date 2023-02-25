package com.rncbl;


import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.junit.MockitoJUnitRunner;

import com.couchbase.lite.Array;

import com.couchbase.lite.Document;
import com.couchbase.lite.Expression;

import com.couchbase.lite.Function;
import com.couchbase.lite.GroupBy;
import com.couchbase.lite.Having;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Ordering;

import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import static com.google.common.truth.Truth.assertThat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

//RobolectricTestRunner
@RunWith(MockitoJUnitRunner.class)
public class UtilsTest {
    private static final Logger logger = Logger.getLogger(DBManagerTest.class.toString());

    @Test
    public void jsDocToMutableDocument(){
      //  ReadableMap map = new ReadableNativeMap();
        WritableMap map = new WritableNativeMap();

        String name = "elim";
        String id = "!23456-95";
        int age = 40;

        WritableMap mapName = new WritableNativeMap();
        mapName.putString("fieldType", "string");
        mapName.putString("value", name);

        WritableMap mapAge = new WritableNativeMap();
        mapAge.putString("fieldType", "int");
        mapAge.putInt("value", age);

        WritableMap mapId = new WritableNativeMap();
        mapId.putString("fieldType", "string");
        mapId.putString("value", id);

        map.putMap("name", mapName);
        map.putMap("age", mapAge);
        map.putMap("id", mapId);

        MutableDocument doc = Utils.jsDocToMutableDocument(map);
        assertThat(doc.getId()).isEqualTo(id);
        assertThat(doc.getString("name")).isEqualTo(name);
        assertThat(doc.getInt("age")).isEqualTo(age);


    }

    @Test
    public void readableToMutableDoc(){
        WritableMap map = new WritableNativeMap();
        String firstName = "Semira";
        Double salary = 45.5;
        String id = "!23456-95";
        int age = 40;

        WritableMap spouseInfo = new WritableNativeMap();
        spouseInfo.putString("name", "Ghettu");
        spouseInfo.putInt("age", 35);
        WritableMap spouseJob = new WritableNativeMap();
        spouseJob.putString("name", "businessman");
        spouseJob.putBoolean("current", true);
        spouseInfo.putMap("job", spouseJob);

        WritableMap recordDetail = new WritableNativeMap();
        recordDetail.putString("name", "dRo");


        WritableArray records = new WritableNativeArray();
        records.pushString("data");
        records.pushDouble(12.4);
        records.pushMap(recordDetail);

        map.putString("firstName", firstName);
        map.putInt("age", age);
        map.putString("id", id);
        map.putDouble("salary", salary);
        map.putBoolean("isAwesome", true);
        map.putMap("spouseInfo", spouseInfo);
        map.putArray("records", records);

        MutableDocument doc = Utils.readableToMutableDoc(map);
        assertThat(doc.getId()).isEqualTo(id);
        assertThat(doc.getString("firstName")).isEqualTo(firstName);
        assertThat(doc.getInt("age")).isEqualTo(age);
        assertThat(doc.getDouble("salary")).isEqualTo(salary);
        assertThat(doc.getBoolean("isAwesome")).isEqualTo(true);
        //assertThat(doc.getDictionary("spouseInfo")).isEqualTo(spouseInfo);

    }

    @Test
    public void expressionField(){
        Expression exp = Utils.expressionField("age", "");
        //this comparison wont work since each Expression sends a different id. shallow copy perhaps?
        assertThat(exp).isInstanceOf(Expression.class);

        // same as
        Expression expectedSum = Function.sum(Expression.property("age"));
        Expression returnedExpSum = Utils.expressionField("sum___age", "");
        assertThat(returnedExpSum).isInstanceOf(Expression.class);
        assertThat(expectedSum.toString().split("json")[1]).isEqualTo(returnedExpSum.toString().split("json")[1]);

        Expression expectedCount = Function.count(Expression.property("id"));
        Expression returnedExpCount = Utils.expressionField("count___id", "");
        assertThat(returnedExpCount).isInstanceOf(Expression.class);
        assertThat(expectedCount.toString().split("json")[1]).isEqualTo(returnedExpCount.toString().split("json")[1]);

        Expression expectedAvg = Function.avg(Expression.property("salary"));
        Expression returnedExpAvg = Utils.expressionField("avg___salary", "");
        assertThat(returnedExpAvg).isInstanceOf(Expression.class);
        assertThat(expectedAvg.toString().split("json")[1]).isEqualTo(returnedExpAvg.toString().split("json")[1]);

        Expression expectedMax = Function.max(Expression.property("salary"));
        Expression returnedExpMax = Utils.expressionField("max___salary", "");
        assertThat(returnedExpMax).isInstanceOf(Expression.class);
        assertThat(expectedMax.toString().split("json")[1]).isEqualTo(returnedExpMax.toString().split("json")[1]);

        Expression expectedMin = Function.min(Expression.property("salary"));
        Expression returnedExpMin = Utils.expressionField("min___salary", "");
        assertThat(returnedExpMin).isInstanceOf(Expression.class);
        assertThat(expectedMin.toString().split("json")[1]).isEqualTo(returnedExpMin.toString().split("json")[1]);

        Expression expFrom = Expression.property("age").from("employee");
        Expression returnedExpFrom = Utils.expressionField("age", "employee");
        assertThat(returnedExpFrom).isInstanceOf(Expression.class);
        assertThat(expFrom.toString().split("json")[1]).isEqualTo(returnedExpFrom.toString().split("json")[1]);

    }

    @Test
    public void buildOrderingExpression(){
        WritableArray orderBy = new WritableNativeArray();
        WritableMap orderByGenderAsc = new WritableNativeMap();
        orderByGenderAsc.putString("fieldName", "gender");
        orderByGenderAsc.putInt("orderBy", 0);

        WritableMap orderByAgeDesc = new WritableNativeMap();
        orderByAgeDesc.putString("fieldName", "age");
        orderByAgeDesc.putString("fromTable", "students");
        orderByAgeDesc.putInt("orderBy", 1);
        orderBy.pushMap(orderByGenderAsc);
        orderBy.pushMap(orderByAgeDesc);

        Ordering[] ordering = Utils.buildOrderingExpression(orderBy);
        assertThat(ordering.length).isEqualTo(2);
    }

    @Test
    public void buildGroupBy(){
        WritableArray groupBy = new WritableNativeArray();
        WritableMap genderField = new WritableNativeMap();
        genderField.putString("fieldName", "gender");

        WritableMap ageField = new WritableNativeMap();
        ageField.putString("fieldName", "age");
        ageField.putString("fromTable", "students");

        groupBy.pushMap(genderField);
        groupBy.pushMap(ageField);
        Expression[] exp = Utils.buildGroupBy(groupBy);
        assertThat(exp.length).isEqualTo(2);
    }


    @Test
    public void buildHavingBy(){
        WritableArray havingBy = new WritableNativeArray();
        WritableMap genderField = new WritableNativeMap();
        genderField.putString("fieldName", "gender");
        genderField.putInt("comparison", 0);
        genderField.putString("value", "M");

        WritableMap ageField = new WritableNativeMap();
        ageField.putString("fieldName", "age");
        ageField.putInt("comparison", 0);
        ageField.putInt("value", 10);
        ageField.putInt("connectWithNextVia", 1);

        WritableMap jobField = new WritableNativeMap();
        jobField.putString("fieldName", "job");
        jobField.putString("value", "admin");
        jobField.putInt("comparison", 0);

        havingBy.pushMap(genderField);
        havingBy.pushMap(ageField);
        havingBy.pushMap(jobField);
        Expression generatedExpression = Utils.buildHavingBy(havingBy);
        Expression expectedExpression = Expression.property("gender").equalTo(Expression.string("M"))
                .and(Expression.property("age").equalTo(Expression.intValue(10)))
                .or(Expression.property("job").equalTo(Expression.string("admin")));

        assertThat(TestHelpers.jsonPartOfExpression(generatedExpression).equals(TestHelpers.jsonPartOfExpression(expectedExpression))).isEqualTo(true);
    }



    @Test
    public void createConfigHeadersFromReadableMap(){
        WritableMap header = new WritableNativeMap();
        header.putString("User-Agent", "123");
        Map<String, String> headerMap = Utils.createConfigHeadersFromReadableMap(header);
        assertThat(headerMap.get("User-Agent")).isEqualTo("123");
    }

    @Test
    public void readableArrayToList(){
        WritableArray data = new WritableNativeArray();
        data.pushString("abc");
        List<String> list = Utils.readableArrayToList(data);

        assertThat(list.get(0)).isEqualTo("abc");
    }



    @Test
    public void convertMapToSyncFilters(){
        MutableDocument document = new MutableDocument();
        document.setInt("age", 10);
        document.setString("name","Simon Fraser");
        document.setString("groupName", null);
        document.setNumber("groupId", null);
        document.setDouble("height", 1.71);
        document.setString("startDate", "2021-01-01");


        WritableArray singleBlock = new WritableNativeArray();
        WritableMap filterOptionsStringEquals = new WritableNativeMap();
        filterOptionsStringEquals.putString("fieldName", "name");
        filterOptionsStringEquals.putString("value", "Simon Fraser");
        filterOptionsStringEquals.putInt("comparison", 10);
        singleBlock.pushMap(filterOptionsStringEquals);
        assertThat(Utils.convertMapToSyncFilters(singleBlock, document )).isEqualTo(true);

        WritableArray andBlock = new WritableNativeArray();

        WritableMap filterOptionsStringStartsWith = new WritableNativeMap();
        filterOptionsStringStartsWith.putString("fieldName", "name");
        filterOptionsStringStartsWith.putString("value", "Sim");
        filterOptionsStringStartsWith.putInt("comparison", 5);
        andBlock.pushMap(filterOptionsStringStartsWith);

        WritableMap filterOptionsStringEndsWith = new WritableNativeMap();
        filterOptionsStringEndsWith.putString("fieldName", "name");
        filterOptionsStringEndsWith.putString("value", "ser");
        filterOptionsStringEndsWith.putInt("comparison", 6);
        andBlock.pushMap(filterOptionsStringEndsWith);
        assertThat(Utils.convertMapToSyncFilters(andBlock, document )).isEqualTo(true);

        WritableArray orBlock = new WritableNativeArray();

        WritableMap filterOptionsStringContains = new WritableNativeMap();
        filterOptionsStringContains.putString("fieldName", "name");
        filterOptionsStringContains.putString("value", "Fra");
        filterOptionsStringContains.putInt("comparison", 7);
        filterOptionsStringContains.putInt("connectWithNextVia", 1);
        orBlock.pushMap(filterOptionsStringContains);

        WritableMap filterOptionsIntEquals = new WritableNativeMap();
        filterOptionsIntEquals.putString("fieldName", "age");
        filterOptionsIntEquals.putInt("value", 100);
        filterOptionsIntEquals.putInt("comparison", 10);
        orBlock.pushMap(filterOptionsIntEquals);
        assertThat(Utils.convertMapToSyncFilters(orBlock, document )).isEqualTo(true);

        WritableArray allBlock = new WritableNativeArray();

        WritableMap filterOptionsIntGreaterOrEquals = new WritableNativeMap();
        filterOptionsIntGreaterOrEquals.putString("fieldName", "age");
        filterOptionsIntGreaterOrEquals.putInt("value", 10);
        filterOptionsIntGreaterOrEquals.putInt("comparison", 1);
        allBlock.pushMap(filterOptionsIntGreaterOrEquals);

        WritableMap filterOptionsIntGreaterThan = new WritableNativeMap();
        filterOptionsIntGreaterThan.putString("fieldName", "age");
        filterOptionsIntGreaterThan.putInt("value", 5);
        filterOptionsIntGreaterThan.putInt("comparison", 3);
       allBlock.pushMap(filterOptionsIntGreaterThan);

        WritableMap filterOptionsIntSmallerOrEqual = new WritableNativeMap();
        filterOptionsIntSmallerOrEqual.putString("fieldName", "age");
        filterOptionsIntSmallerOrEqual.putInt("value", 10);
        filterOptionsIntSmallerOrEqual.putInt("comparison", 2);
        allBlock.pushMap(filterOptionsIntSmallerOrEqual);

        WritableMap filterOptionsIntSmaller = new WritableNativeMap();
        filterOptionsIntSmaller.putString("fieldName", "age");
        filterOptionsIntSmaller.putInt("value", 15);
        filterOptionsIntSmaller.putInt("comparison", 4);
        allBlock.pushMap(filterOptionsIntSmaller);

        WritableMap filterOptionsDoubleEquals = new WritableNativeMap();
        filterOptionsDoubleEquals.putString("fieldName", "height");
        filterOptionsDoubleEquals.putDouble("value", 1.71);
        filterOptionsDoubleEquals.putInt("comparison", 0);
        allBlock.pushMap(filterOptionsDoubleEquals);

        WritableMap filterOptionsDoubleGreaterThan = new WritableNativeMap();
        filterOptionsDoubleGreaterThan.putString("fieldName", "height");
        filterOptionsDoubleGreaterThan.putDouble("value", 1.70);
        filterOptionsDoubleGreaterThan.putInt("comparison", 3);
        allBlock.pushMap(filterOptionsDoubleGreaterThan);

        WritableMap filterOptionsNull = new WritableNativeMap();
        filterOptionsNull.putString("fieldName", "groupId");
        filterOptionsNull.putNull("value");
        filterOptionsNull.putInt("comparison", 3);
        allBlock.pushMap(filterOptionsNull);

        WritableMap filterOptionsStringNull = new WritableNativeMap();
        filterOptionsStringNull.putString("fieldName", "groupName");
        filterOptionsStringNull.putNull("value");
        filterOptionsStringNull.putInt("comparison", 3);
        allBlock.pushMap(filterOptionsStringNull);


        WritableMap filterOptionsDateEqualsTo = new WritableNativeMap();
        filterOptionsDateEqualsTo.putString("fieldName", "startDate");
        filterOptionsDateEqualsTo.putString("value", "2021-01-01");
        filterOptionsDateEqualsTo.putInt("comparison", 0);
        filterOptionsDateEqualsTo.putBoolean("valueIsDateString", true);
        allBlock.pushMap(filterOptionsDateEqualsTo);

        WritableMap filterOptionsDateEqualsToAfter = new WritableNativeMap();
        filterOptionsDateEqualsToAfter.putString("fieldName", "startDate");
        filterOptionsDateEqualsToAfter.putString("value", "2020-01-01");
        filterOptionsDateEqualsToAfter.putInt("comparison", 1);
        filterOptionsDateEqualsToAfter.putBoolean("valueIsDateString", true);
        allBlock.pushMap(filterOptionsDateEqualsToAfter);

        WritableMap filterOptionsDateAfter = new WritableNativeMap();
        filterOptionsDateAfter.putString("fieldName", "startDate");
        filterOptionsDateAfter.putString("value", "2020-01-01");
        filterOptionsDateAfter.putInt("comparison", 3);
        filterOptionsDateAfter.putBoolean("valueIsDateString", true);
        allBlock.pushMap(filterOptionsDateAfter);

        WritableMap filterOptionsDateEqualsToBefore = new WritableNativeMap();
        filterOptionsDateEqualsToBefore.putString("fieldName", "startDate");
        filterOptionsDateEqualsToBefore.putString("value", "2021-02-01");
        filterOptionsDateEqualsToBefore.putInt("comparison", 2);
        filterOptionsDateEqualsToBefore.putBoolean("valueIsDateString", true);
        allBlock.pushMap(filterOptionsDateEqualsToBefore);

        WritableMap filterOptionsDateBefore = new WritableNativeMap();
        filterOptionsDateBefore.putString("fieldName", "startDate");
        filterOptionsDateBefore.putString("value", "2021-02-01");
        filterOptionsDateBefore.putInt("comparison", 4);
        filterOptionsDateBefore.putBoolean("valueIsDateString", true);
        allBlock.pushMap(filterOptionsDateBefore);

    }

    @Test
    public void connectToNextVia(){
        WritableMap infoConnectViaAnd = new WritableNativeMap();
        infoConnectViaAnd.putInt("connectToNextVia", 0);
        assertThat(Utils.connectToNextVia(infoConnectViaAnd)).isEqualTo(0);

        WritableMap infoConnectViaOr = new WritableNativeMap();
        infoConnectViaOr.putInt("connectWithNextVia", 1);
        assertThat(Utils.connectToNextVia(infoConnectViaOr)).isEqualTo(1);

        WritableMap infoConnectViaInvalid = new WritableNativeMap();
        infoConnectViaInvalid.putInt("connectToNextVia", 45);
        assertThat(Utils.connectToNextVia(infoConnectViaInvalid)).isEqualTo(0);

        WritableMap infoConnectViaInvalidField = new WritableNativeMap();
        infoConnectViaInvalidField.putInt("invalidConnectionField", 0);
        assertThat(Utils.connectToNextVia(infoConnectViaInvalidField)).isEqualTo(0);
    }

    @Test
    public void readableDataToObjectValue(){
        WritableMap mapString = new WritableNativeMap();
        mapString.putString("value", "name");
        assertThat(Utils.readableDataToObjectValue(mapString, "value")).isEqualTo("name");

        WritableMap mapBool = new WritableNativeMap();
        mapBool.putBoolean("value", true);
        assertThat(Utils.readableDataToObjectValue(mapBool, "value")).isEqualTo(true);

        WritableMap mapInt = new WritableNativeMap();
        mapInt.putInt("value", 10);
        assertThat(Utils.readableDataToObjectValue(mapInt, "value")).isEqualTo(10);

        WritableMap mapDouble = new WritableNativeMap();
        mapDouble.putDouble("value", 11.4);
        assertThat(Utils.readableDataToObjectValue(mapDouble, "value")).isEqualTo(11.4);

        WritableMap mapNull = new WritableNativeMap();
        mapNull.putNull("value");
        assertThat(Utils.readableDataToObjectValue(mapNull, "value")).isEqualTo(null);

        WritableMap mapArray = new WritableNativeMap();
        mapArray.putArray("value", readableDataToObjectValueArray());
        assertThat(Utils.readableDataToObjectValue(mapArray, "value")).isEqualTo(readableDataToObjectValueArray());
    }

    //to avoid array already consumed error
    private WritableArray readableDataToObjectValueArray(){
        WritableArray mapArrayItems = new WritableNativeArray();
        mapArrayItems.pushString("old");
        mapArrayItems.pushString("days");
        return mapArrayItems;
    }


    @Test
    public void createExpressionComparison(){

        WritableMap equalTo = new WritableNativeMap();
        equalTo.putString("fieldName", "gender");
        equalTo.putInt("comparison", 0);
        equalTo.putString("value", "M");
        Expression expectedEqualTo = Expression.property("gender").equalTo(Expression.string("M"));
        Expression returnedExp = Utils.createExpressionComparison(equalTo);
        assertThat(TestHelpers.jsonPartOfExpression(expectedEqualTo).equals(TestHelpers.jsonPartOfExpression(returnedExp))).isEqualTo(true);

        WritableMap equalToDefault = new WritableNativeMap();
        equalToDefault.putString("fieldName", "gender");
        equalToDefault.putString("value", "M");
        Expression expectedEqualToDefault = Expression.property("gender").equalTo(Expression.string("M"));
        Expression returnedExpDefault = Utils.createExpressionComparison(equalToDefault);
        assertThat(TestHelpers.jsonPartOfExpression(expectedEqualToDefault).equals(TestHelpers.jsonPartOfExpression(returnedExpDefault))).isEqualTo(true);


        WritableMap startsWith = new WritableNativeMap();
        startsWith.putString("fieldName", "name");
        startsWith.putInt("comparison", 5);
        startsWith.putString("value", "jo");
        Expression expectedStartsWith = Expression.property("name").like(Expression.value( "%jo"));
        Expression returnedExpStartsWith = Utils.createExpressionComparison(startsWith);
        assertThat(TestHelpers.jsonPartOfExpression(expectedStartsWith).equals(TestHelpers.jsonPartOfExpression(returnedExpStartsWith))).isEqualTo(true);

        WritableMap endsWith = new WritableNativeMap();
        endsWith.putString("fieldName", "name");
        endsWith.putInt("comparison", 6);
        endsWith.putString("value", "jo");
        Expression expectedEndsWith = Expression.property("name").like(Expression.value( "jo%"));
        Expression returnedExpEndsWith = Utils.createExpressionComparison(endsWith);
        assertThat(TestHelpers.jsonPartOfExpression(expectedEndsWith).equals(TestHelpers.jsonPartOfExpression(returnedExpEndsWith))).isEqualTo(true);


        WritableMap contains = new WritableNativeMap();
        contains.putString("fieldName", "name");
        contains.putInt("comparison", 7);
        contains.putString("value", "jo");
        Expression expectedContains = Expression.property("name").like(Expression.value( "%jo%"));
        Expression returnedExpContains = Utils.createExpressionComparison(contains);
        assertThat(TestHelpers.jsonPartOfExpression(expectedContains).equals(TestHelpers.jsonPartOfExpression(returnedExpContains))).isEqualTo(true);

        WritableMap greaterThan = new WritableNativeMap();
        greaterThan.putString("fieldName", "age");
        greaterThan.putInt("comparison", 3);
        greaterThan.putInt("value", 34);
        Expression expectedGreaterThan = Expression.property("age").greaterThan(Expression.intValue(34));
        Expression returnedExpGreaterThan = Utils.createExpressionComparison(greaterThan);
        assertThat(TestHelpers.jsonPartOfExpression(expectedGreaterThan).equals(TestHelpers.jsonPartOfExpression(returnedExpGreaterThan))).isEqualTo(true);

        WritableMap greaterThanOrEqual = new WritableNativeMap();
        greaterThanOrEqual.putString("fieldName", "age");
        greaterThanOrEqual.putInt("comparison", 1);
        greaterThanOrEqual.putInt("value", 34);
        Expression expectedGreaterThanOrEqual = Expression.property("age").greaterThanOrEqualTo(Expression.intValue(34));
        Expression returnedExpGreaterThanOrEqual = Utils.createExpressionComparison(greaterThanOrEqual);
        assertThat(TestHelpers.jsonPartOfExpression(expectedGreaterThanOrEqual).equals(TestHelpers.jsonPartOfExpression(returnedExpGreaterThanOrEqual))).isEqualTo(true);


        WritableMap smallerThan = new WritableNativeMap();
        smallerThan.putString("fieldName", "age");
        smallerThan.putInt("comparison", 4);
        smallerThan.putInt("value", 34);
        Expression expectedSmallerThan = Expression.property("age").lessThan(Expression.intValue(34));
        Expression returnedExpSmallerThan = Utils.createExpressionComparison(smallerThan);
        assertThat(TestHelpers.jsonPartOfExpression(expectedSmallerThan).equals(TestHelpers.jsonPartOfExpression(returnedExpSmallerThan))).isEqualTo(true);

        WritableMap smallerThanOrEqual = new WritableNativeMap();
        smallerThanOrEqual.putString("fieldName", "age");
        smallerThanOrEqual.putInt("comparison", 2);
        smallerThanOrEqual.putInt("value", 34);
        Expression expectedSmallerThanOrEqual = Expression.property("age").lessThanOrEqualTo(Expression.intValue(34));
        Expression returnedExpSmallerThanOrEqual = Utils.createExpressionComparison(smallerThanOrEqual);
        assertThat(TestHelpers.jsonPartOfExpression(expectedSmallerThanOrEqual).equals(TestHelpers.jsonPartOfExpression(returnedExpSmallerThanOrEqual))).isEqualTo(true);

        WritableMap between = new WritableNativeMap();
        between.putString("fieldName", "age");
        between.putInt("comparison", 9);
        between.putInt("value", 34);
        between.putInt("value2", 38);
        Expression expectedBetween = Expression.property("age").between(Expression.intValue(34), Expression.intValue(38));
        Expression returnedExpBetween = Utils.createExpressionComparison(between);
        assertThat(TestHelpers.jsonPartOfExpression(expectedBetween).equals(TestHelpers.jsonPartOfExpression(returnedExpBetween))).isEqualTo(true);

        WritableMap in = new WritableNativeMap();
        WritableArray inValues = new WritableNativeArray();
        inValues.pushInt(34);
        inValues.pushInt(38);
        in.putString("fieldName", "age");
        in.putInt("comparison", 8);
        in.putArray("value", inValues);

        Expression expectedIn = Expression.property("age").in(Expression.intValue(34), Expression.intValue(38));
        Expression returnedExpIn = Utils.createExpressionComparison(in);
        assertThat(TestHelpers.jsonPartOfExpression(expectedIn).equals(TestHelpers.jsonPartOfExpression(returnedExpIn))).isEqualTo(true);
    }

}
