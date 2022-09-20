package org.javabip.verification.ast;

import org.javabip.verification.visitors.PJEEvaluateVisitor;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

public class IdentifierExpression implements ParsedJavaExpression, AfterDotExpression, MethodCallBase {
    private final String identifierName;
    private Field associatedField;
    private Method associatedMethod;
    private Object parentObject;

    public void setParentObject(Object parentObject) {
        this.parentObject = parentObject;
    }

    public IdentifierExpression(String identifierName, Object component) {
        this.identifierName = identifierName;
        this.parentObject = component;
    }

    public IdentifierExpression(String identifierName, Field field, Object component) {
        this.identifierName = identifierName;
        this.associatedField = field;
        this.parentObject = component;
    }

    public IdentifierExpression(String identifierName, Method method, Object component) {
        this.identifierName = identifierName;
        this.associatedMethod = method;
        this.parentObject = component;
    }

    public IdentifierExpression(String identifierName, Field field, Method method, Object component) {
        this.identifierName = identifierName;
        this.associatedField = field;
        this.associatedMethod = method;
        this.parentObject = component;
    }

    public String toString(){
        return identifierName;
    }

    public Optional<Field> getAssociatedField() {
        if (associatedField != null)
            return Optional.of(associatedField);
        else return Optional.empty();
    }

    public Optional<Method> getAssociatedMethod() {
        if (associatedMethod != null)
            return Optional.of(associatedMethod);
        else return Optional.empty();
    }

    public Object getParentObject() {
        return parentObject;
    }

    @Override
    public Object accept(PJEEvaluateVisitor v) {
        if (associatedField != null){
            associatedField.setAccessible(true);
            try {
                return associatedField.get(parentObject);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            //corner cases: e.g. array length
            if (parentObject.getClass().isArray() && identifierName.equals("length")){
                return Array.getLength(parentObject);
            }
            //TODO other corner cases
        }

        //TODO error log
        return null;
    }
}
