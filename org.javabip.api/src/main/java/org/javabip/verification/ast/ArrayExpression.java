package org.javabip.verification.ast;

import org.javabip.verification.visitors.PJEEvaluateVisitor;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

// for expressions like expression[expression]
public class ArrayExpression implements ParsedJavaExpression {
    private final ParsedJavaExpression outerExpression;
    private final ParsedJavaExpression innerExpression;

    public ArrayExpression(ParsedJavaExpression outerExpression, ParsedJavaExpression innerExpression) {
        this.outerExpression = outerExpression;
        this.innerExpression = innerExpression;
    }

    public String toString(){
        return outerExpression.toString() + "[" + innerExpression.toString() + "]";
    }

    @Override
    public Object accept(PJEEvaluateVisitor v) {
        if (outerExpression instanceof IdentifierExpression && innerExpression instanceof IntegerExpression){
            IdentifierExpression outer = (IdentifierExpression) this.outerExpression;
            //TODO this can return null
            Field outerField = outer.getAssociatedField().get();
            outerField.setAccessible(true);
            try {
                Object arrayInstance = outerField.get(outer.getParentObject());
                Integer index = ((IntegerExpression) innerExpression).value;
                return Array.get(arrayInstance, index);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            //TODO exception
        }

        return null;
    }
}
