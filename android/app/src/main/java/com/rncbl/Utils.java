package com.rncbl;
import android.content.Context;

import com.couchbase.lite.Array;
import com.couchbase.lite.Document;
import com.couchbase.lite.Expression;
import com.couchbase.lite.Function;
import com.couchbase.lite.MutableArray;
import com.couchbase.lite.MutableDictionary;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Ordering;
import com.couchbase.lite.SelectResult;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;


import java.io.File;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import kotlin.RequiresOptIn;

public class Utils {

    private static final Logger logger = Logger.getLogger(Utils.class.toString());

    public static WritableMap MapToJSON(Map data ) {
        /*
        Given Map, convert it to writemap or json
         */
        WritableMap writableMap = Arguments.makeNativeMap(data);

        return writableMap;
    }

    public static Map createConfigHeadersFromReadableMap(ReadableMap header){
        ReadableMapKeySetIterator iterator = header.keySetIterator();
        Map<String, String> configHeaderMap = new HashMap<String, String>();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            String value = header.getString(key);
            configHeaderMap.put(key, value);
        }
        return configHeaderMap;
    }

    public static List readableArrayToList(ReadableArray ar){
        int totalItems = ar.size();
        int counter = 0;
        List<String> k = new ArrayList<String>();
        for (counter = 0; counter < totalItems; counter++){
            k.add(ar.getString(counter));
        }
       // logger.log(Level.INFO, "tupac " + k);
        return k;
    }



        public static MutableDocument jsDocToMutableDocument(ReadableMap readableMap) {
        /*
        INspired by the awesome http://www.java2s.com/example/java-src/pkg/com/ibatimesheet/rnjsonutils-47574.html

        readableMap is json doc from Document.ts, which is a map:

         */
            MutableDocument doc;

                if (readableMap.getMap("id") != null) {
                    ReadableMap idMap = readableMap.getMap("id");
                    doc = new MutableDocument(idMap.getString("value"));
                } else {
                    doc = new MutableDocument();
                }


        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            ReadableMap record = readableMap.getMap(key);
            String fieldType = record.getString("fieldType");
            if (key != "id"){
                if (fieldType.equals("string")){
                    doc.setString(key, record.getString("value"));
                } else if (fieldType.equals("number")){
                    doc.setNumber(key, record.getDouble("value"));
                }else if (fieldType.equals("date")){
                    doc.setString(key, record.getString("value"));
                }else if (fieldType.equals("array")){
                    doc.setArray(key, (Array) record.getArray("value"));
                }else if (fieldType.equals("dic")){
                    doc.setDictionary(key, (com.couchbase.lite.Dictionary) record.getMap("value"));
                }else if (fieldType.equals("dic")){
                    doc.setDictionary(key, (com.couchbase.lite.Dictionary) record.getMap("value"));
                }else if (fieldType.equals("bool")){
                    doc.setBoolean(key,  record.getBoolean("value"));
                }else if (fieldType.equals("int")){
                    doc.setInt(key,  record.getInt("value"));
                }else if (fieldType.equals("float")){
                    doc.setDouble(key,  record.getDouble("value"));
                }
            }


        }
        return doc;
    }

    public static String numberType(Double number){
        if ((number % 1) == 0){
            // we have an integer
            return "int";
        }else{
            return "double";
        }
    }

    public static MutableDictionary convertMapToDictionary(ReadableMap readableMap) {
        MutableDictionary dic = new MutableDictionary();
        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            switch (readableMap.getType(key)) {
                case Null:
                    dic.setValue(key, null);
                    break;
                case Boolean:
                    dic.setBoolean(key, readableMap.getBoolean(key));
                    break;
                case Number:
                    String numType = numberType(readableMap.getDouble(key));
                    if (numType.equals("int")){
                        dic.setNumber(key, readableMap.getInt(key));
                    }else{
                        dic.setNumber(key, readableMap.getDouble(key));
                    }

                    break;
                case String:
                    dic.setString(key, readableMap.getString(key));
                    break;
                case Map:
                    dic.setDictionary(key, convertMapToDictionary(readableMap.getMap(key)));
                    break;
                case Array:
                    dic.setArray(key, (Array) readableMap.getArray(key));
                    //object.put(key, convertArrayToJson(readableMap.getArray(key)));
                    break;
            }
        }
        return dic;
    }

    private static Boolean docDateCompare(Document document,String value, String fieldName, int comparison){
        try{
            Date docDate = document.getDate(fieldName);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            Date passedDate = formatter.parse(value);

            if (comparison == 1){
                return docDate.after(passedDate) && docDate.equals(passedDate);
            } else if (comparison == 2){
                return docDate.before(passedDate) && docDate.equals(passedDate);
            } else if (comparison == 3){
                return docDate.after(passedDate);
            }else if (comparison == 4){
                return docDate.before(passedDate);
            }
            return docDate.equals(passedDate);
        }catch(Exception e){
            logger.log(Level.INFO, "Coolie err ");
            e.printStackTrace();
            return true;
        }
    }



    public static Boolean convertMapToSyncFilters(ReadableArray array, Document document) {

        Boolean result = null;

        int totalElements = array.size();
        int counter = 0;
        int connectWithNextVia = 0; //and
        Boolean valueIsDateString = false;
        for(counter =0; counter < totalElements; counter++){
            ReadableMap block = array.getMap(counter);
            String fieldName = block.getString("fieldName");
            int comparison = block.getInt("comparison");

            if (block.hasKey("valueIsDateString")){
                valueIsDateString = block.getBoolean("valueIsDateString");
            }
            Boolean blockStatus = false;
            switch (block.getType("value")){
                case Null:
                    blockStatus = document.getString(fieldName) == null;
                    break;
                case Boolean:
                    blockStatus = block.getBoolean("value") == document.getBoolean(fieldName);
                    break;
                case String:
                    String value = block.getString("value");
                    String docStrValue = document.getString(fieldName);
                    if (!valueIsDateString){
                        if (comparison == 5 ){
                            blockStatus = docStrValue.startsWith(value);
                        } else if (comparison == 6 ){
                            blockStatus = docStrValue.endsWith(docStrValue);
                        }else if (comparison == 7 ){
                            blockStatus = docStrValue.contains(value);
                        }else{
                            // default to equals
                            blockStatus = docStrValue.equals(value);
                        }

                    }else{
                        // it is a date string.
                        Boolean validDateComparison = Utils.docDateCompare(document, value, fieldName, comparison);
                        if (validDateComparison != null){
                            blockStatus = validDateComparison;
                        }
                    }

                    break;
                case Number:
                    String numType = numberType(block.getDouble("value"));
                    if (numType.equals("int")){

                        int intValue = block.getInt("value");
                        int docIntValue = document.getInt(fieldName);
                        if (comparison == 1){
                            blockStatus = docIntValue >= intValue;
                        } else if (comparison == 2){
                            blockStatus = docIntValue <= intValue ;
                        } else if (comparison == 3){
                            blockStatus = docIntValue > intValue;
                        }else if (comparison == 4){
                            blockStatus = docIntValue < intValue;
                        }else{
                            blockStatus = intValue == docIntValue;
                        }


                    }else{
                        Double dblValue = block.getDouble("value");
                        Double docDoubleValue = document.getDouble(fieldName);

                        if (comparison == 1){
                            blockStatus = docDoubleValue >= dblValue;
                        } else if (comparison == 2){
                            blockStatus = docDoubleValue <= dblValue;
                        } else if (comparison == 3){
                            blockStatus = docDoubleValue > dblValue;
                        }else if (comparison == 4){
                            blockStatus = docDoubleValue < dblValue;
                        }else{
                            blockStatus = docDoubleValue.equals(dblValue);
                        }

                    }
                    break;
                default:
                    blockStatus = false;
            }//end switch

            if (counter == 0){
                result = blockStatus;
            }else{
                if (connectWithNextVia == 0){
                    result = result && blockStatus;
                }else{
                    result = result || blockStatus;
                }
            }


            if (block.hasKey("connectWithNextVia")){
                //ensure we dont cross line
                int providedConnector = block.getInt("connectWithNextVia");
                if ( providedConnector == 0 || providedConnector ==1 ){
                    connectWithNextVia = providedConnector;
                }else{
                    connectWithNextVia =0;
                }
            }else{
                connectWithNextVia =0;
            }
        } //end for loop

        return result;
    }

    public static MutableArray convertReadableArrayToDocArray(ReadableArray readableArray) {
        MutableArray ar = new MutableArray();

        for (int i = 0; i < readableArray.size(); i++) {
            switch (readableArray.getType(i)) {
                case Null:
                    break;
                case Boolean:
                    ar.addBoolean(readableArray.getBoolean(i));
                    break;
                case Number:
                    String numType = numberType(readableArray.getDouble(i));
                    if (numType.equals("int")){
                        ar.addInt(readableArray.getInt(i));
                    }else{
                        ar.addDouble(readableArray.getDouble(i));
                    }
                    break;
                case String:
                    ar.addString(readableArray.getString(i));
                    break;
                case Map:
                    ar.addDictionary(convertMapToDictionary(readableArray.getMap(i)));
                    //               ar.pushMap(readableArray.getMap(i));
                    break;
                case Array:
                    ar.addArray((Array) readableArray.getArray(i));
                    break;
            }
        }
        return ar;
    }


    private static MutableDocument readableMapToDocument(MutableDocument doc, ReadableMap readableMap){
        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            switch (readableMap.getType(key)) {
                case Null:
                    doc.setValue(key, null);
                    break;
                case Boolean:
                    doc.setBoolean(key,readableMap.getBoolean(key) );
                    break;
                case Number:
                    String numType = numberType(readableMap.getDouble(key));
                    if (numType.equals("int")){
                        // we have an integer
                        doc.setNumber(key, readableMap.getInt(key));
                    }else{
                        doc.setNumber(key, readableMap.getDouble(key));
                    }
                    break;
                case String:
                    doc.setString(key, readableMap.getString(key));
                    break;
                case Map:
                    MutableDictionary dic = convertMapToDictionary(readableMap.getMap(key));
                    doc.setDictionary(key, dic);
                    break;
                case Array:
                    doc.setArray(key, convertReadableArrayToDocArray(readableMap.getArray(key)));
                    break;

            }
        }
        return doc;
    }

    public static MutableDocument readableToMutableDoc(ReadableMap data) {
        MutableDocument doc; //new MutableDocument();
        String docId = data.getString("id");
        if (docId != null){
            doc = new MutableDocument(docId);
        }else{
            doc = new MutableDocument();
        }

        return readableMapToDocument(doc, data);
    }

    public static MutableDocument readableToMutableDoc(ReadableMap data, MutableDocument doc) {
        return readableMapToDocument(doc, data);
    }


    public static boolean deleteDirectory(File directory) {
        if(directory.exists()){
            File[] files = directory.listFiles();
            if(null!=files){
                for(int i=0; i<files.length; i++) {
                    if(files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    }
                    else {
                        files[i].delete();
                    }
                }
            }
        }
        return(directory.delete());
    }

    public static String[] splitIntoWords(String sentence){
        /*
        Given a space, separate it into words by space. delete subsequent spaces that follow
        a space
         */
        return sentence.split("\\s+");
    }

    public boolean deleteDatabase(String dbName,  Context context) {
        /*
        Deletes the given database. Context is the current instance of the app
        We delete the directory of the database. We assume syncing and writting operations
        are already paused
         */
        File path = context.getFilesDir();

        String dbDirectory = path.getAbsolutePath() + "/" + dbName + ".cblite2";
        File dir = new File(dbDirectory);
        if ( dir.exists()){
            return deleteDirectory(dir);
        }
        return true;
    }

    public static Expression expressionField(String fieldName, String fromTable) {
        /*
         given a fieldname, see if we need to return an aggregate on the field or just hte row as it is
         */
        String[] fieldProperty = fieldName.split("___");
        String aggregateName = fieldProperty[0];
        Expression propertyExpression = null;
        logger.log(Level.INFO, "winnie " + fieldProperty + " => " + fieldProperty.length);

        if (fieldProperty.length > 1) {

            if (fromTable.equals("")){
                propertyExpression = Expression.property(fieldProperty[1]);
            }else{
                propertyExpression = Expression.property(fieldProperty[1]).from(fromTable);
            }
            if (aggregateName.equals("sum")) {
                return Function.sum(propertyExpression);
            } else if (aggregateName.equals("count")) {
                return Function.count(propertyExpression);
            } else if (aggregateName.equals("avg")) {
                return Function.avg(propertyExpression);
            } else if (aggregateName.equals("max")){
                return Function.max(propertyExpression);
            }else if (aggregateName.equals("min")){
                return Function.min(propertyExpression);
            }
        }
        if (fromTable.equals("")){
            return Expression.property(fieldName);
        }
        return Expression.property(fieldName).from(fromTable);
        // default return fildName as it is.

    }


    public static Ordering[] buildOrderingExpression(ReadableArray orderByOptions){
        int totalOrderByFields = orderByOptions.size();
        Ordering[] ordering = new Ordering[totalOrderByFields];
        for (int i =0; i < totalOrderByFields; i++){
            ReadableMap map = orderByOptions.getMap(i);
            String fieldName = map.getString("fieldName");
            String fromTable = "";
            if (map.hasKey("fromTable")){
                fromTable = map.getString("fromTable");
            }
            int orderBy = map.getInt("orderBy");

            Expression fieldExp = Utils.expressionField(fieldName, fromTable);
            if (orderBy == 1){
                ordering[i] = Ordering.expression(fieldExp).descending();
            }else{
                ordering[i] = Ordering.expression(fieldExp).ascending();
            }
        }

        return ordering;
    }

    public static Expression[] buildGroupBy(ReadableArray groupByOptions){
        int totalGroupByFields = groupByOptions.size();
        Expression[] exp = new Expression[totalGroupByFields];
        for (int i =0; i < totalGroupByFields; i++){
            ReadableMap info = groupByOptions.getMap(i);
            String fieldName = info.getString("fieldName");
            String fromTable = "";
            if (info.hasKey("fromTable")){
                fromTable = info.getString("fromTable");
            }
            exp[i] = Utils.expressionField(fieldName, fromTable);
        }
        return exp;
    }

    public static Expression buildHavingBy(ReadableArray havingByOptions){
        int totalHavingFields = havingByOptions.size();
        Expression exp = null;
        int connectWithNextVia = 0;
        for (int i =0; i < totalHavingFields; i++){
            ReadableMap info = havingByOptions.getMap(i);

            Expression expRow = Utils.createExpressionComparison(info);
            if (exp == null){
                exp = expRow;
            }else{
                if (connectWithNextVia == 0){
                    exp = exp.and(expRow);
                }else{
                    exp = exp.or(expRow);
                }
            }
            connectWithNextVia = Utils.connectToNextVia(info);

        }
        return exp;
    }

    static int connectToNextVia(ReadableMap row){

        int connectWithNextVia = 0; // 0 = AND, 1 = or; e.g. gender ="F" AND  age = 89; // gender connects with age via AND
        if (row.hasKey("connectWithNextVia")){

            connectWithNextVia = row.getInt("connectWithNextVia");
            if (connectWithNextVia != 0 && connectWithNextVia != 1){
                connectWithNextVia = 0;
            }
        }
        return connectWithNextVia;
    }
    static Object readableDataToObjectValue(ReadableMap data, String key){
        /*
         Correctly reads the value key of an object.
         {fieldName:'gender', value:'M'...}
         */
        Object result = null;
        if (data.hasKey(key)){
            switch (data.getType(key)){
                case Null:
                    result = null;
                    break;
                case Boolean:
                    result = data.getBoolean(key);
                    break;
                case Number:
                    String numType = Utils.numberType(data.getDouble(key));
                    if (numType.equals("int")) {
                        result = data.getInt(key);
                    } else {
                        result = data.getDouble(key);
                    }
                    break;
                case String:
                    result = data.getString(key);
                    break;
                case Map:
                    //d
                    //MutableDictionary dic = Utils.convertMapToDictionary(data.getMap(key));
                    break;
                case Array:
                    result = data.getArray(key);
                    break;
            }
        }

        return result;
    }
    static Expression[] valueToExpression(ReadableArray items){

        int totalItems = items.size();
        Expression[] expValues = new Expression[totalItems];
        int i = 0;
        for (i = 0; i < totalItems; i++){
            ReadableType valueItem = items.getType(i);
            switch (valueItem){
                case Boolean:
                    expValues[i] = Expression.booleanValue(items.getBoolean(i));
                    break;
                case String:
                    expValues[i] = Expression.string(items.getString(i));
                    break;
                case Number:
                    String numType = Utils.numberType(items.getDouble(i));
                    if (numType.equals("int")) {
                        expValues[i] = Expression.intValue(items.getInt(i));
                    } else {
                        expValues[i] = Expression.doubleValue(items.getDouble(i));
                    }
                    break;
                case Array:
                    expValues[i] = Expression.value(items.getArray(i));
                    break;
            }
        }

        return expValues;
    }
    static Expression createExpressionComparison(ReadableMap record){
      /*
      Given a single comparison record as follows
            {"comparison":0,"value":"M","fieldName":"gender"}
       build an expression
       */
        Object value = Utils.readableDataToObjectValue(record, "value");
        String fromTable = ""; //alias
        int comparison = 0; //default equals to
        if (record.hasKey("comparison")){
            comparison  =  record.getInt("comparison");
        }

        if (record.hasKey("fromTable")){
            fromTable = record.getString("fromTable");
        }

        String fieldName = record.getString("fieldName");

        Expression exp = Expression.property(fieldName);
        if (!fromTable.equals("")){
            exp = Expression.property(fieldName).from(fromTable);
        }


        if (comparison == 1){
            exp = exp.greaterThanOrEqualTo(Expression.value(value));
        }else if (comparison == 2){
            exp =  exp.lessThanOrEqualTo(Expression.value(value));
        }else if (comparison == 3){
            exp =  exp.greaterThan(Expression.value(value));
        }else if(comparison == 4){
            exp =   exp.lessThan(Expression.value(value));
        }else if(comparison == 5){
            exp =   exp.like(Expression.value("%" + value));
        }else if(comparison == 6){
            exp =   exp.like(Expression.value(value + "%"));
        }else if(comparison == 7){
            exp =   exp.like(Expression.value("%" + value + "%"));
        }else if(comparison == 8){

            ReadableArray items = (ReadableArray) value;
            Expression[] expValues = Utils.valueToExpression(items);
            exp = exp.in(expValues);
        }else if(comparison == 9){
            // between
            Object value2 = Utils.readableDataToObjectValue(record, "value2");
            exp = exp.between(Expression.value(value), Expression.value(value2));
        }else{
            // assume equal to
            exp = exp.equalTo(Expression.value(value));
        }

        return exp;
    }



    }
