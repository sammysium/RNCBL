package com.rncbl;


import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.Expression;
import com.couchbase.lite.Join;
import com.couchbase.lite.PropertyExpression;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JoinBuilder {

    JSONObject build(Database database, ReadableArray joinedTablesInfo){
        int totalFields = joinedTablesInfo.size();
        int counter = 0;
        JSONObject reply = new JSONObject();
        reply.put("primaryTable", "");

        Join[] joins = new Join[totalFields];

        for (counter = 0 ; counter < totalFields; counter++){

            ReadableMap row = joinedTablesInfo.getMap(counter);
            String firstTable = row.getString("firstTable");
            String firstTableField = row.getString("firstTableField");
            String secondoTable = row.getString("secondTable");
            String secondTableField = row.getString("secondTableField");
            int comparison = row.getInt("comparison");
            if (counter == 0 ){
                reply.put("primaryTable", firstTable);
            }
            
            PropertyExpression firstProperty = Expression.property(firstTableField);
            PropertyExpression secondProperty = Expression.property(secondTableField);

            Join join = null;

            if (comparison == 1){
                join = Join.join(DataSource.database(database).as(secondoTable))
                        .on(firstProperty.from(firstTable)
                                .greaterThanOrEqualTo(secondProperty.from(secondoTable)));
            }else if (comparison == 2){
                join = Join.join(DataSource.database(database).as(secondoTable))
                        .on(firstProperty.from(firstTable)
                                .lessThanOrEqualTo(secondProperty.from(secondoTable)));
            }else if (comparison == 3){
                join = Join.join(DataSource.database(database).as(secondoTable))
                        .on(firstProperty.from(firstTable)
                                .greaterThan(secondProperty.from(secondoTable)));
            }else if (comparison == 4){
                join = Join.join(DataSource.database(database).as(secondoTable))
                        .on(firstProperty.from(firstTable)
                                .lessThan(secondProperty.from(secondoTable)));
            } else{
                join = Join.join(DataSource.database(database).as(secondoTable))
                        .on(firstProperty.from(firstTable)
                                .equalTo(secondProperty.from(secondoTable)));
            }

            joins[counter] = join;


        }
        reply.put("joins", joins);

        return reply;
    }
}
