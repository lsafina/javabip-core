package org.javabip.verification.ast;

abstract class BinaryExpression implements ParsedJavaExpression {
    public ParsedJavaExpression leftExpression;
    public ParsedJavaExpression rightExpression;
    public String separator;

    public BinaryExpression(ParsedJavaExpression leftExpression, ParsedJavaExpression rightExpression, String separator) {
        this.leftExpression = leftExpression;
        this.rightExpression = rightExpression;
        this.separator = separator;
    }

    public String toString(){
        return leftExpression.toString() + separator + rightExpression.toString();
    }
}
