package com.rncbl;
import com.couchbase.lite.Expression;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ExpressionBuilder {
    /*
     given a readable array (i.e. JSON), create expressions. An alternative is to use Params:

     https://forums.couchbase.com/t/how-do-i-parse-strings-as-an-expression-in-android/29951
     */
    private static final Logger logger = Logger.getLogger(ExpressionBuilder.class.toString());

    Expression exp = null;

    void ExpressionBuilder(){
        this.exp = null;
    }

    private Expression buildCompound(ReadableArray data){
        Expression expression = null;
        int total = data.size();
        int connectWithNext = -1;
        for (int i = 0; i < total ; i++) {
            ReadableMap queryNode = data.getMap(i);
            if(!queryNode.hasKey("compoundComparison")){
                Expression rowExp = Utils.createExpressionComparison(queryNode);
                if(expression == null){
                    expression = rowExp;
                }else{
                    if (connectWithNext == 0){
                        expression = expression.and(rowExp);
                    }else{
                        expression = expression.or(rowExp);
                    }
                }
                connectWithNext = Utils.connectToNextVia(queryNode);
            }else{
                Expression e = buildCompound(queryNode.getArray("compoundComparison"));
                if (expression == null){
                    expression = e;
                }else{
                    if (connectWithNext == 0){
                        expression = expression.and((e));
                    }else{
                        expression = expression.or((e));
                    }
                }
                connectWithNext = Utils.connectToNextVia(queryNode);
            }

        }

        return expression;

    }

    public Expression createExpression(ReadableArray data){
        int total = data.size();
        int connectWithNext = -1;
        for(int counter = 0; counter < total ; counter++){
            ReadableMap queryNode = data.getMap(counter);
            if(!queryNode.hasKey("compoundComparison")){
                Expression rowExp = Utils.createExpressionComparison(queryNode);
                if (this.exp == null){
                    this.exp = rowExp;
                }else{
                    if (connectWithNext == 0){
                        this.exp = this.exp.and(rowExp);
                    }else{
                        this.exp = this.exp.or(rowExp);
                    }
                }
                connectWithNext = Utils.connectToNextVia(queryNode);

            }
            else{
                //we have compound expression
                Expression cpdExpression = buildCompound(queryNode.getArray("compoundComparison"));
                if (this.exp == null){
                    this.exp = cpdExpression;
                }else{
                    if (connectWithNext == 0){
                        this.exp = this.exp.and((cpdExpression));

                    }else {
                        this.exp = this.exp.or((cpdExpression));
                    }
                }
                connectWithNext = Utils.connectToNextVia(queryNode);
            }

        }
        return this.exp;
    }

}
