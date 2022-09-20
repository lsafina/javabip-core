package org.javabip.verification.ast;

import org.javabip.verification.visitors.PJEEvaluateVisitor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Optional;

public class MethodCallExpression implements ParsedJavaExpression, AfterDotExpression {
    MethodCallBase methodCallBase;
    ArrayList<ParsedJavaExpression> arguments;

    public void setParentObject(Object parentObject) {
        this.parentObject = parentObject;
    }

    private Object parentObject;

    public MethodCallExpression(MethodCallBase methodCallBase, ArrayList<ParsedJavaExpression> arguments, Object parentObject) {
        this.methodCallBase = methodCallBase;
        this.arguments = arguments;
        this.parentObject = parentObject;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(methodCallBase.toString()).append("(");
        arguments.forEach(a -> sb.append(a.toString()).append(", "));
        int length = sb.length();
        if (arguments.size() > 0) sb.delete(length - 2, length);
        sb.append(")");
        return sb.toString();
    }

    @Override
    public Object accept(PJEEvaluateVisitor v) {
        if (methodCallBase instanceof IdentifierExpression) {
            IdentifierExpression id = (IdentifierExpression) this.methodCallBase;
            Optional<Method> associatedMethod = id.getAssociatedMethod();
            if (associatedMethod.isPresent()) {
                Method method = associatedMethod.get();
                method.setAccessible(true);
                try {
                    return method.invoke(parentObject, arguments.toArray());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

        }
        if (methodCallBase instanceof ThisExpression) {
            // TODO
        }
        if (methodCallBase instanceof SuperExpression) {
            // TODO
        }

        // TODO log error
        return null;
    }

    /*public Object evaluateId(Object parentObject){
        return evaluate(parentObject);
    }
    private Object evaluate(Object parentObject){}*/
}
