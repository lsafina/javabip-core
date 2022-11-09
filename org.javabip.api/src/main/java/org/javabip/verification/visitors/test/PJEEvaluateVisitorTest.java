package org.javabip.verification.visitors.test;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.javabip.verification.ast.*;
import org.javabip.verification.parser.JavaLexer;
import org.javabip.verification.parser.JavaParser;
import org.javabip.verification.visitors.ExpressionASTBuilder;
import org.javabip.verification.visitors.PJEEvaluateVisitor;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PJEEvaluateVisitorTest {
    private boolean go(String testString) {
        JavaLexer lexer = new JavaLexer(CharStreams.fromString(testString));
        JavaParser parser = new JavaParser(new CommonTokenStream(lexer));
        JavaParser.ExpressionContext expression = parser.expression();
        FakeSpecification f = new FakeSpecification(2, 1, 1, "1", true, new Double[]{1.0, 2.0});
        ExpressionASTBuilder v = new ExpressionASTBuilder(f);
        ParsedJavaExpression invariantParsedExpression = v.build(expression);

        FakeInvariantImpl invariant = new FakeInvariantImpl(testString, invariantParsedExpression);
        Boolean result = invariant.evaluateInvariant();

        System.out.println(testString);
        return result;
    }

    static class FakeInvariantImpl {
        private final String expression;
        private final ParsedJavaExpression parsedExpression;

        public FakeInvariantImpl(String expression, ParsedJavaExpression invariantParsedExpression) {
            this.expression = expression;
            this.parsedExpression = invariantParsedExpression;
        }

        public String expr() {
            return expression;
        }

        public String toString() {
            return "Invariant=(expr = " + expr() + ")";
        }

        public Boolean evaluateInvariant() {
            return (Boolean) parsedExpression.accept(new PJEEvaluateVisitor());
        }
    }

    //region Basic Types Tests
    @Test
    public void testBooleanExpressionFalse() {
        String test = "true && false";
        assertFalse(go(test));
    }

    @Test
    public void testBooleanExpressionTrue() {
        String test = "true || false";
        assertTrue(go(test));
    }

    @Test
    public void testBooleanExpressionParent() {
        String test = "(true || false) && true";
        assertTrue(go(test));
    }

    @Test
    public void testNumberExpression() {
        String test = "5 > 1.0";
        assertTrue(go(test));
    }

    @Test
    public void testEqualityExpressionString() {
        String test = "\"apple\" != \"pear\"";
        assertTrue(go(test));
    }

    @Test
    public void testEqualityExpressionChar() {
        String test = "'a' == 'a'";
        assertTrue(go(test));
    }

    @Test
    public void testEqualityExpressionNumber() {
        String test = "1 == 1.0";
        assertTrue(go(test));
    }

    @Test
    public void testNullExpression() {
        String test = "\"null\" != null";
        assertTrue(go(test));
    }

    @Test
    public void testProductExpression() {
        String test = "1 * 1 == 1";
        assertTrue(go(test));
    }

    @Test
    public void testProductExpressionFail() {
        String test = "1 / 0 == null";
        assertTrue(go(test));
    }

    @Test
    public void testSumExpressionNumber() {
        String test = "1 + 1 == 2";
        assertTrue(go(test));
    }

    @Test
    public void testSumExpressionString() {
        String test = "\"1\" + \"1\" == \"11\"";
        assertTrue(go(test));
    }

    @Test
    public void testTernaryExpression() {
        //TODO ternary implementation does not work
        String test = "1 + 1 == 2 ? true : false";
        assertTrue(go(test));
    }

    @Test
    public void testBitBinaryExpression() { /*TODO to be implemented*/ }
    //endregion

    //region Field Accessing Tests
    @Test
    public void testIdentifierExpression() {
        String test = "bet == 2";
        assertTrue(go(test));
    }

    @Test
    public void testSuperExpression() {
        //TODO
        //in fact should not be supported
    }

    @Test
    public void testThisExpressionField() {
        // in fact "this.value" is just a syntactic sugar for "value"
        String test = "this.win";
        assertTrue(go(test));
    }

    @Test
    public void testThisExpressionMethodCall() {
        // in fact "this.value" is just a syntactic sugar for "value"
        String test = "this.win()";
        assertTrue(go(test));
    }

    @Test
    public void testThisExpressionMethodCallArguments() {
        // in fact "this.value" is just a syntactic sugar for "value"
        String test = "this.win(10)";
        assertTrue(go(test));
    }

    @Test
    public void testThisExpressionMethodCallAsArgument() {
        // in fact "this.value" is just a syntactic sugar for "value"
        String test = "this.win(lose())";
        assertTrue(go(test));
    }

    @Test
    public void testThisExpressionMethodCallEvaluateArguments() {
        // in fact "this.value" is just a syntactic sugar for "value"
        String test = "this.win(1+1)";
        assertTrue(go(test));
    }

    @Test
    public void testThisExpressionArrayField() {
        String test = "this.values.length == 2";
        assertTrue(go(test));
    }

    @Test
    public void testArrayExpression() {
        String test = "values[0] == 1.0";
        assertTrue(go(test));
    }

    //endregion
}
