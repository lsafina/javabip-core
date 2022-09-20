package org.javabip.verification.ast;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javabip.verification.visitors.PJEEvaluateVisitor;

public class DotSeparatedExpression implements ParsedJavaExpression {
    final ParsedJavaExpression left;
    final AfterDotExpression right;
    protected static final Logger logger = LogManager.getLogger();

    public DotSeparatedExpression(ParsedJavaExpression left, AfterDotExpression right) {
        this.left = left;
        this.right = right;
    }

    public String toString(){
        return left.toString() + "." + right.toString();
    }

    @Override
    public Object accept(PJEEvaluateVisitor v) {
        //basic case when the parent object is a bip component
        if (left instanceof ThisExpression){
            return right.accept(v);
        }
        if (left instanceof SuperExpression){
            //TODO communicate with the parent class
        }
        //nested dot expression where the parent object is the evaluation result of all left expressions before the dot
        else {
            Object leftEvaluated = left.accept(v);
            if (right instanceof IdentifierExpression){
                IdentifierExpression rightAsId = (IdentifierExpression) this.right;
                rightAsId.setParentObject(leftEvaluated);
                return rightAsId.accept(v);
            }
            if (right instanceof MethodCallExpression){
                MethodCallExpression rightAsMethodCall = (MethodCallExpression) this.right;
                rightAsMethodCall.setParentObject(leftEvaluated);
                return rightAsMethodCall.accept(v);
            }
            //java grammar also allows "this" to be as a rightmost element, however I do not see any cases where it can work
        }

        logger.error("Malformed expression: " + this);
        return null;
    }

}
