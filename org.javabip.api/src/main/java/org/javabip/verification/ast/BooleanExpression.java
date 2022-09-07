package org.javabip.verification.ast;

import org.javabip.verification.visitors.PJEEvaluateNumericVisitor;
import org.javabip.verification.visitors.PJEEvaluateVisitor;

public class BooleanExpression implements Literal {
    public Boolean value;

    public BooleanExpression(Boolean value) {
        this.value = value;
    }

    public String toString(){
        return value.toString();
    }

    @Override
    public Boolean accept(PJEEvaluateVisitor v) {
        return value;
    }

    @Override
    public Number accept(PJEEvaluateNumericVisitor v) {
        return null;
        //TODO either return null or some "ErrorExpression"
    }
}