package org.javabip.verification.ast;

import org.javabip.annotations.Guard;
import org.javabip.annotations.Pure;
import org.javabip.verification.visitors.PJEEvaluateVisitor;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

public class MethodCallExpression implements ParsedJavaExpression, AfterDotExpression {
    MethodCallBase methodCallBase;
    ArrayList<ParsedJavaExpression> parsedArguments;

    public void setParentObject(Object parentObject) {
        this.parentObject = parentObject;
    }

    private Object parentObject;

    public MethodCallExpression(MethodCallBase methodCallBase, ArrayList<ParsedJavaExpression> parsedArguments, Object parentObject) {
        this.methodCallBase = methodCallBase;
        this.parsedArguments = parsedArguments;
        this.parentObject = parentObject;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(methodCallBase.toString()).append("(");
        parsedArguments.forEach(a -> sb.append(a.toString()).append(", "));
        int length = sb.length();
        if (parsedArguments.size() > 0) sb.delete(length - 2, length);
        sb.append(")");
        return sb.toString();
    }

    @Override
    public Object accept(PJEEvaluateVisitor v) {
        if (methodCallBase instanceof IdentifierExpression) {
            IdentifierExpression id = (IdentifierExpression) this.methodCallBase;
            List<Method> associatedMethods = id.getAssociatedMethods();
            if (!associatedMethods.isEmpty()) {
                Method method = resolveMethod(associatedMethods, parsedArguments);
                if (method == null) return null; //TODO log errror

                Annotation[] annotations = method.getAnnotations();
                Arrays.stream(annotations).filter(annotation -> annotation instanceof Guard);


                //check for pure?
                Optional<Annotation> pureAnnotation = Arrays.stream(annotations).filter(annotation -> annotation instanceof Pure).findFirst();
                if (pureAnnotation.isPresent()) {
                    method.setAccessible(true);
                    try {
                        Object[] evaluatedArguments = parsedArguments.stream().map(argument -> argument.accept(v)).toArray();
                        return method.invoke(parentObject, evaluatedArguments);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
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

    private Method resolveMethod(List<Method> associatedMethods, ArrayList<ParsedJavaExpression> arguments) {
        int argumentsSize = arguments.size();
        List<Method> prunedBySize = associatedMethods.stream().filter(method -> method.getParameters().length == argumentsSize).collect(Collectors.toList());

        int prunedListSize = prunedBySize.size();
        switch (prunedListSize) {
            case 0: {
                return null;
            }
            case 1: {
                return prunedBySize.get(0);
            }
            default: {
                for (Method method : prunedBySize) {
                    if (compareParameters(method.getParameters(), arguments))
                        return method;
                }
                return null;
            }
        }
    }

    private Boolean compareParameters(Parameter[] parameters, ArrayList<ParsedJavaExpression> arguments) {
        Iterator<Parameter> iteratorParameter = Arrays.stream(parameters).iterator();
        Iterator<ParsedJavaExpression> iteratorArgument = arguments.iterator();
        while (iteratorParameter.hasNext()) {
            if (!compareTwoArguments(iteratorParameter.next(), iteratorArgument.next()))
                return false;
        }
        return true;
    }

    private Boolean compareTwoArguments(Parameter parameter, ParsedJavaExpression argument) {
        //TODO
        return true;
    }
}
