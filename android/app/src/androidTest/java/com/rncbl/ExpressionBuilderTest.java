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

import com.couchbase.lite.Array;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Expression;
import com.couchbase.lite.MutableDocument;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

//RobolectricTestRunner
@RunWith(MockitoJUnitRunner.class)
public class ExpressionBuilderTest {
    private static final Logger logger = Logger.getLogger(ExpressionBuilder.class.toString());



    WritableMap getGenderLimit(){
        WritableMap simpleGenderLimit = new WritableNativeMap();
        simpleGenderLimit.putString("fieldName", "gender");
        simpleGenderLimit.putString("value","M");
        simpleGenderLimit.putInt("comparison", 0);
        return simpleGenderLimit;
    }

    WritableMap getAgeLimit(){
        WritableMap simpleAgeLimit = new WritableNativeMap();
        simpleAgeLimit.putString("fieldName", "age");
        simpleAgeLimit.putInt("value",28);
        simpleAgeLimit.putInt("comparison", 0);
        return simpleAgeLimit;
    }




    @Test
    public void createSimpleExpression(){
       /*
         SELECT * FROM TABLE WHERE gender = M
         */
        ExpressionBuilder expBuilder = new ExpressionBuilder();

        WritableArray jsJson = new WritableNativeArray();

        jsJson.pushMap(getGenderLimit());

        Expression expectedExpression = Expression.property("gender").equalTo(Expression.string("M"));
        Expression generatedExpression = expBuilder.createExpression(jsJson);
        assertThat(TestHelpers.jsonPartOfExpression(generatedExpression).equals(TestHelpers.jsonPartOfExpression(expectedExpression))).isEqualTo(true);

    }

    @Test
    public void createSimpleAndExpression(){
       /*
         SELECT * FROM TABLE WHERE gender = M AND age = 28
         */
        ExpressionBuilder expBuilder = new ExpressionBuilder();

        Expression expectedExpression = Expression.property("gender").equalTo(Expression.string("M")).and(Expression.property("age").equalTo(Expression.intValue(28)));
        WritableArray jsJson = new WritableNativeArray();
        jsJson.pushMap(getGenderLimit());
        jsJson.pushMap(getAgeLimit());

        Expression generatedExpression = expBuilder.createExpression(jsJson);
        assertThat(TestHelpers.jsonPartOfExpression(generatedExpression).equals(TestHelpers.jsonPartOfExpression(expectedExpression))).isEqualTo(true);
    }

    @Test
    public void createSimpleOrExpression(){
       /*
         SELECT * FROM TABLE WHERE gender = M or age = 28
         */
        ExpressionBuilder expBuilder = new ExpressionBuilder();
        WritableMap genderLimit = getGenderLimit();
        genderLimit.putInt("connectWithNextVia", 1);

        Expression expectedExpression = Expression.property("gender").equalTo(Expression.string("M")).or(Expression.property("age").equalTo(Expression.intValue(28)));
        WritableArray jsJson = new WritableNativeArray();
        jsJson.pushMap(genderLimit);
        jsJson.pushMap(getAgeLimit());

        Expression generatedExpression = expBuilder.createExpression(jsJson);
        assertThat(TestHelpers.jsonPartOfExpression(generatedExpression).equals(TestHelpers.jsonPartOfExpression(expectedExpression))).isEqualTo(true);
    }


    @Test
    public void createSimpleAndCompoundExpression(){
       /*
         SELECT * FROM TABLE WHERE gender = M OR (gender=F AND age=28)
         */
        ExpressionBuilder expBuilder = new ExpressionBuilder();
        WritableArray jsJson = new WritableNativeArray();
        WritableMap genderMaleLimit = getGenderLimit();
        genderMaleLimit.putInt("connectWithNextVia", 1);

        WritableMap compoundJson = new WritableNativeMap();
        WritableArray compoundJsonElements = new WritableNativeArray();
        compoundJsonElements.pushMap(getAgeLimit());

        WritableMap genderFemaleLimit = getGenderLimit();
        genderFemaleLimit.putString("value", "F");
        compoundJsonElements.pushMap(genderFemaleLimit);

        compoundJson.putArray("compoundComparison", compoundJsonElements);

        jsJson.pushMap(genderMaleLimit);
        jsJson.pushMap(compoundJson);
        Expression expectedExpression = Expression.property("gender").equalTo(Expression.string("M")).or((Expression.property("age").equalTo(Expression.intValue(28)).and(Expression.property("gender").equalTo(Expression.string("F")))));
        Expression generatedExpression = expBuilder.createExpression(jsJson);
        assertThat(TestHelpers.jsonPartOfExpression(generatedExpression).equals(TestHelpers.jsonPartOfExpression(expectedExpression))).isEqualTo(true);

    }

    @Test
    public void createSimpleDoubleCompoundExpression(){
        /*
         SELECT * FROM TABLE WHERE (gender = M AND age = 23) OR (gender=F AND age=28)
         */
        ExpressionBuilder expBuilder = new ExpressionBuilder();
        WritableArray jsJson = new WritableNativeArray();


        WritableMap ageLimitMan = getAgeLimit();
        ageLimitMan.putInt("value", 23);
        WritableMap compoundJsonMaleAge = new WritableNativeMap();
        WritableArray compoundJsonMaleAgeElements = new WritableNativeArray();
        compoundJsonMaleAgeElements.pushMap(getGenderLimit());
        compoundJsonMaleAgeElements.pushMap(ageLimitMan);

        WritableMap genderFemaleLimit = getGenderLimit();
        genderFemaleLimit.putString("value", "F");
        WritableMap ageLimitFemale = getAgeLimit();
        ageLimitFemale.putInt("value", 28);
        WritableMap compoundJsonFemaleAge = new WritableNativeMap();
        WritableArray compoundJsonFemaleAgeElements = new WritableNativeArray();
        compoundJsonFemaleAgeElements.pushMap(genderFemaleLimit);
        compoundJsonFemaleAgeElements.pushMap(ageLimitFemale);


        compoundJsonMaleAge.putInt("connectWithNextVia", 1);
        compoundJsonMaleAge.putArray("compoundComparison", compoundJsonMaleAgeElements);

        compoundJsonFemaleAge.putArray("compoundComparison", compoundJsonFemaleAgeElements);

        jsJson.pushMap(compoundJsonMaleAge);
        jsJson.pushMap(compoundJsonFemaleAge);

        Expression genderMale = Expression.property("gender").equalTo(Expression.string("M")).and(Expression.property("age").equalTo(Expression.intValue(23)));
        Expression genderFemale = Expression.property("gender").equalTo(Expression.string("F")).and(Expression.property("age").equalTo(Expression.intValue(28)));
        Expression expectedExpression = genderMale.or(genderFemale);
        Expression generatedExpression = expBuilder.createExpression(jsJson);
        assertThat(TestHelpers.jsonPartOfExpression(generatedExpression).equals(TestHelpers.jsonPartOfExpression(expectedExpression))).isEqualTo(true);
    }

    @Test
    public void createComparisonExpression(){
       /*
         SELECT * FROM TABLE WHERE salary > 100 AND startedYearOn >=2021 AND height<171 AND weight<=100 AND age=10 AND prefix LIKE %tekle
                                    AND suffix LIKE junior% AND title LIKE %manager% AND interests IN [gaming,reading]
         */
        ExpressionBuilder expBuilder = new ExpressionBuilder();

        WritableMap salaryLimit = new WritableNativeMap();
        salaryLimit.putString("fieldName", "salary");
        salaryLimit.putInt("value", 100);
        salaryLimit.putInt("comparison", 3);

        WritableMap startedYearOnLimit = new WritableNativeMap();
        startedYearOnLimit.putString("fieldName", "startedYearOn");
        startedYearOnLimit.putInt("value", 2021);
        startedYearOnLimit.putInt("comparison", 1);

        WritableMap heightLimit = new WritableNativeMap();
        heightLimit.putString("fieldName", "height");
        heightLimit.putInt("value", 171);
        heightLimit.putInt("comparison", 4);

        WritableMap weightLimit = new WritableNativeMap();
        weightLimit.putString("fieldName", "weight");
        weightLimit.putInt("value", 100);
        weightLimit.putInt("comparison", 2);

        WritableMap ageLimit = new WritableNativeMap();
        ageLimit.putString("fieldName", "age");
        ageLimit.putInt("value", 10);
        ageLimit.putInt("comparison", 0);

        WritableMap prefixLimit = new WritableNativeMap();
        prefixLimit.putString("fieldName", "prefix");
        prefixLimit.putString("value", "tekle");
        prefixLimit.putInt("comparison", 5);


        WritableMap suffixLimit = new WritableNativeMap();
        suffixLimit.putString("fieldName", "suffix");
        suffixLimit.putString("value", "junior");
        suffixLimit.putInt("comparison", 6);


        WritableMap titleLimit = new WritableNativeMap();
        titleLimit.putString("fieldName", "title");
        titleLimit.putString("value", "manager");
        titleLimit.putInt("comparison", 7);

        WritableMap interestsLimit = new WritableNativeMap();
        WritableArray interests = new WritableNativeArray();
        interests.pushString("gaming");
        interests.pushString("reading");
        interestsLimit.putString("fieldName", "interests");
        interestsLimit.putArray("value", interests);
        interestsLimit.putInt("comparison", 8);

        WritableArray jsJson = new WritableNativeArray();

        jsJson.pushMap(salaryLimit);
        jsJson.pushMap(startedYearOnLimit);
        jsJson.pushMap(heightLimit);
        jsJson.pushMap(weightLimit);
        jsJson.pushMap(ageLimit);
        jsJson.pushMap(prefixLimit);
        jsJson.pushMap(suffixLimit);
        jsJson.pushMap(titleLimit);
        jsJson.pushMap(interestsLimit);

               /*
         SELECT * FROM TABLE WHERE salary > 100 AND startedYearOn >=2021 AND height<171 AND weight<=100 AND age=10 AND prefix LIKE %tekle
                                    AND suffix LIKE junior% AND title LIKE %manager% AND interests IN [gaming,reading]
         */
        Expression[] expInterests = new Expression[2];
        expInterests[0] = Expression.string("gaming");
        expInterests[1] = Expression.string("reading");

        Expression expectedExpression = Expression.property("salary").greaterThan(Expression.intValue(100))
                .and(Expression.property("startedYearOn").greaterThanOrEqualTo(Expression.intValue(2021)))
                .and(Expression.property("height").lessThan(Expression.intValue(171)))
                .and(Expression.property("weight").lessThanOrEqualTo(Expression.intValue(100)))
                .and(Expression.property("age").equalTo(Expression.intValue(10)))
                .and(Expression.property("prefix").like(Expression.value("%" + "tekle")))
                .and(Expression.property("suffix").like(Expression.value("junior" + "%")))
                .and(Expression.property("title").like(Expression.value("%" + "manager" + "%")))
                .and(Expression.property("interests").in(expInterests));
        Expression generatedExpression = expBuilder.createExpression(jsJson);

        assertThat(TestHelpers.jsonPartOfExpression(generatedExpression).equals(TestHelpers.jsonPartOfExpression(expectedExpression))).isEqualTo(true);
    }


    @Test
    public void createAliasedSimpleExpression(){
       /*
         SELECT * FROM TABLE AS employees WHERE gender = M
        */
        ExpressionBuilder expBuilder = new ExpressionBuilder();

        WritableArray jsJson = new WritableNativeArray();
        WritableMap genderMaleLimit = getGenderLimit();
        genderMaleLimit.putString("fromTable", "employees");
        jsJson.pushMap(genderMaleLimit);

        Expression expectedExpression = Expression.property("gender").from("employees").equalTo(Expression.string("M"));
        Expression generatedExpression = expBuilder.createExpression(jsJson);
        assertThat(TestHelpers.jsonPartOfExpression(generatedExpression).equals(TestHelpers.jsonPartOfExpression(expectedExpression))).isEqualTo(true);

    }

}
