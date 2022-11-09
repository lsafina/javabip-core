package org.javabip.verification.ast;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javabip.verification.visitors.PJEEvaluateVisitor;

public class TernaryExpression implements ParsedJavaExpression {
    protected static final Logger logger = LogManager.getLogger();

    final ParsedJavaExpression condition;
    final ParsedJavaExpression trueExpression;
    final ParsedJavaExpression falseExpression;

    public TernaryExpression(ParsedJavaExpression condition, ParsedJavaExpression trueExpression, ParsedJavaExpression falseExpression) {
        this.condition = condition;
        this.trueExpression = trueExpression;
        this.falseExpression = falseExpression;
    }

    public String toString(){
        return condition.toString() +
                " ? " +
                trueExpression.toString() +
                " : " +
                falseExpression.toString();
    }

    @Override
    public Object accept(PJEEvaluateVisitor v) {
        Object accept = condition.accept(v);
        if (accept instanceof Boolean){
            if ((Boolean) accept)
                return trueExpression.accept(v);
            else
                return falseExpression.accept(v);
        }

        logger.error("Malformed Expression: " + this.toString());
        return null;
    }
}
