package org.javabip.verification.visitors;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javabip.verification.ast.*;
import org.javabip.verification.parser.JavaParser;
import org.javabip.verification.parser.JavaParserBaseVisitor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class ExpressionASTBuilder extends JavaParserBaseVisitor {
    protected static final Logger logger = LogManager.getLogger();
    Object parentObject;

    public ExpressionASTBuilder(Object bipComponent) {
        this.parentObject = bipComponent;
    }

    public ParsedJavaExpression build(ParseTree t) {
        return (ParsedJavaExpression) t.accept(this);
    }

    @Override
    public ParsedJavaExpression visitTerminal(TerminalNode node) {
        String text = node.getText();
        switch (text) {
            case "this": {
                return new ThisExpression();
            }
            case "super": {
                return new SuperExpression();
            }
            case "++":
            case "--": {
                return new StringExpression(text);
            }
            default: {
                //TODO error
            }
        }

        return null;
    }

    @Override
    public ParsedJavaExpression visitExpression(JavaParser.ExpressionContext ctx) {
        List<ParseTree> children = ctx.children;
        if (children == null) {
            logger.error("Malformed Expression: " + ctx.getText());
        }

        switch (children.size()) {
            case 1: {
                // cases: primary, methodCall
                return (ParsedJavaExpression) children.get(0).accept(this);
            }
            case 2: {
                //case: prefix and postfix
                return buildPreAndPostfixExpression(children);
            }
            case 3: {
                //case: dot-separated expression
                ParseTree separator = children.get(1);
                if (separator instanceof TerminalNodeImpl && separator.getText().equals(".")) {
                    return buildDotSeparatedExpression(ctx);
                }

                //case: binary expressions
                return buildBinaryExpression(ctx);

            }
            case 4: {
                // case: expression '[' expression ']'
                return buildArrayExpression(children);
            }
            case 5: {
                //case: ternary expression
                //TODO ternary expression
            }
            default: {
                //TODO error
            }
        }

        for (ParseTree ch : children) {
            Object accept = ch.accept(this);
        }
        return null;
        //TODO throw an exception
    }

    @Override
    public ParsedJavaExpression visitPrimary(JavaParser.PrimaryContext ctx) {
        List<ParseTree> children = ctx.children;
        int i = children.size();
        switch (i) {
            case 1: {
                ParseTree parseTree = children.get(0);
                if (parseTree instanceof TerminalNodeImpl) {

                    return visitTerminal((TerminalNodeImpl) parseTree);
                    //TODO keep track that only this and super can be processed. Interface?

                    /*String text = parseTree.getText();
                    switch (text){
                        case "this": {
                            return (ParsedJavaExpression) new ThisExpression();
                        }
                        case "super": {
                            return (ParsedJavaExpression) new SuperExpression();
                        }
                        default: {
                            //TODO error
                        }
                    }*/
                }

                return (ParsedJavaExpression) parseTree.accept(this);
            }
            case 3: {
                ParseTree parseTree = ctx.children.get(1);
                return ((ParsedJavaExpression) parseTree.accept(this));
            }
            default: {
                //TODO Exception
            }
        }


        return null;
    }

    @Override
    public ParsedJavaExpression visitMethodCall(JavaParser.MethodCallContext ctx) {
        //parse methodBase
        ParsedJavaExpression methodBase = build(ctx.children.get(0));
        if (!(methodBase instanceof MethodCallBase)) {
            logger.error("Malformed Expression: " + ctx.getText());
            return null;
        }

        /*//ugly way of saying an identifier that it represents a method name
        if (methodBase instanceof IdentifierExpression){
            ((IdentifierExpression) methodBase).setIsMethodNameToTrue();
        }*/

        //parse arguments if present
        ArrayList<ParsedJavaExpression> arguments = new ArrayList<>();
        JavaParser.ExpressionListContext expressionListContext = ctx.expressionList();
        if (expressionListContext != null) {
            expressionListContext.children.forEach(arg -> arguments.add(build(arg)));
        }
        List<ParsedJavaExpression> argumentsNotNull = arguments.stream().filter(Objects::nonNull).collect(Collectors.toList());

        return new MethodCallExpression((MethodCallBase) methodBase, (ArrayList<org.javabip.verification.ast.ParsedJavaExpression>) argumentsNotNull, parentObject);
    }

    @Override
    public ParsedJavaExpression visitIdentifier(JavaParser.IdentifierContext ctx) {
        TerminalNode identifier = ctx.IDENTIFIER();
        if (identifier != null) {
            String identifierName = identifier.getText();
            Optional<Field> field = checkFieldExists(identifierName);
            Optional<Method> method = checkMethodExists(identifierName);

            if (field.isPresent()) {
                if (method.isPresent())
                    return new IdentifierExpression(identifierName, field.get(), method.get(), parentObject);
                else
                    return new IdentifierExpression(identifierName, field.get(), parentObject);
            } else if (method.isPresent()) {
                return new IdentifierExpression(identifierName, method.get(), parentObject);
            }

            // TODO log that we have encountered an id that does not present in the object as a field or a method
            //  (e.g. could be for array.length or when the expression is malformed)
            return new IdentifierExpression(identifierName, parentObject);
        }

        logger.error("Malformed Expression: " + ctx.getText() + " is not an identifier");
        return null;
    }

    @Override
    public ParsedJavaExpression visitLiteral(JavaParser.LiteralContext ctx) {
        ParseTree literal = ctx.children.get(0);
        if (literal instanceof TerminalNodeImpl) {
            TerminalNode charLiteral = ctx.CHAR_LITERAL();
            if (charLiteral != null) {
                return (ParsedJavaExpression) new StringExpression(charLiteral.toString().replace("'", ""));
            }

            TerminalNode stringLiteral = ctx.STRING_LITERAL();
            if (stringLiteral != null) {


                return (ParsedJavaExpression) new StringExpression(stringLiteral.toString().replace("\"", ""));
            }

            TerminalNode boolLiteral = ctx.BOOL_LITERAL();
            if (boolLiteral != null) {
                return (ParsedJavaExpression) new BooleanExpression(Boolean.parseBoolean(boolLiteral.toString()));
            }


            TerminalNode nullLiteral = ctx.NULL_LITERAL();
            if (nullLiteral != null) {
                return (ParsedJavaExpression) new NullExpression();
            }

            TerminalNode textBlockLiteral = ctx.TEXT_BLOCK();
            if (textBlockLiteral != null) {
                //TODO NOT IMPLEMENTED
            }

            return null;
            //TODO this is incorrect, an error should be thrown here
        } else
            return (ParsedJavaExpression) literal.accept(this);
    }

    @Override
    public ParsedJavaExpression visitIntegerLiteral(JavaParser.IntegerLiteralContext ctx) {
        TerminalNode decInt = ctx.DECIMAL_LITERAL();

        if (decInt != null) {
            return new IntegerExpression(Integer.parseInt(decInt.getText()), IntegerExpression.IntegerType.DECIMAL_LITERAL);
        }

        TerminalNode binInt = ctx.BINARY_LITERAL();
        if (binInt != null) {
            return new IntegerExpression(Integer.parseInt(binInt.getText()), IntegerExpression.IntegerType.BINARY_LITERAL);
        }

        TerminalNode hexInt = ctx.HEX_LITERAL();
        if (hexInt != null) {
            return new IntegerExpression(Integer.parseInt(hexInt.getText()), IntegerExpression.IntegerType.HEX_LITERAL);
        }

        TerminalNode octInt = ctx.OCT_LITERAL();
        if (octInt != null) {
            return new IntegerExpression(Integer.parseInt(octInt.getText()), IntegerExpression.IntegerType.OCT_LITERAL);
        }

        return null;
        //TODO this is incorrect, an error should be thrown here

    }

    @Override
    public ParsedJavaExpression visitFloatLiteral(JavaParser.FloatLiteralContext ctx) {
        TerminalNode floatLiteral = ctx.FLOAT_LITERAL();
        if (floatLiteral != null) {
            return new FloatExpression(Float.parseFloat(floatLiteral.getText()), FloatExpression.FloatType.FLOAT);
        }

        TerminalNode hexFloatLiteral = ctx.HEX_FLOAT_LITERAL();
        if (hexFloatLiteral != null) {
            return new FloatExpression(Float.parseFloat(hexFloatLiteral.getText()), FloatExpression.FloatType.HEX_FLOAT);
        }

        return null;
        //TODO this is incorrect, an error should be thrown here
    }

    @Override
    public ParsedJavaExpression visitExpressionList(JavaParser.ExpressionListContext ctx) {
        return null;
    }

    private ParsedJavaExpression buildBinaryExpression(JavaParser.ExpressionContext ctx) {
        List<ParseTree> children = ctx.children;
        ParsedJavaExpression left = build(children.get(0));
        ParsedJavaExpression right = build(children.get(2));

        ParseTree separator = children.get(1);
        if (separator instanceof TerminalNodeImpl) {
            String value = separator.getText();
            switch (value) {
                case ".": {
                    // case: afterDotExpression
                    if (right instanceof AfterDotExpression) {
                        return new DotSeparatedExpression(left, (AfterDotExpression) right);
                    } else {
                        logger.error("Malformed Expression: " + ctx.getText() + ". The right part is expected to be a field, a method call or this.");
                    }
                    break;
                }
                case "==":
                case "!=": {
                    return new EqualityExpression(left, right, value);
                }
                case "||":
                case "&&": {
                    return new LogicalExpression(left, right, value);
                }
                case "<":
                case ">":
                case "<=":
                case ">=": {
                    return new RelationalExpression(left, right, value);
                }
                case "+":
                case "-": {
                    return new SumExpression(left, right, value);
                }
                case "*":
                case "/":
                case "%": {
                    return new ProductExpression(left, right, value);
                }
                case "|":
                case "^":
                case "&":
                case ">>":
                case ">>>":
                case "<<": {
                    return new BitBinaryExpression(left, right, value);
                }
            }
        } else {
            logger.error("Malformed Expression: " + ctx.getText() + ". Can not parse a separator.");
        }
        return null;
    }

    private ParsedJavaExpression buildPreAndPostfixExpression(List<ParseTree> children) {
        Utils utils = new Utils();
        ParseTree partOne = children.get(0);
        if (partOne instanceof TerminalNodeImpl) { //its prefix
            String value = partOne.getText();
            if (utils.inPrefixes(value)) {
                ParsedJavaExpression partTwo = (ParsedJavaExpression) children.get(1).accept(this);
                return new PrefixExpression(partTwo, value);
            } else {
                //TODO wrong prefix, error
            }
        } else {
            ParseTree partTwo = children.get(1);
            if (partTwo instanceof TerminalNodeImpl) { //its postfix
                String value = partTwo.getText();
                if (utils.inPostfixes(value)) {
                    ParsedJavaExpression partOneExpression = (ParsedJavaExpression) partOne.accept(this);
                    return new PrefixExpression(partOneExpression, value);

                } else {
                    //otherwise it is malformed
                    //TODO error
                }
            }
        }

        //TODO error
        return null;
    }

    private ParsedJavaExpression buildArrayExpression(List<ParseTree> children) {
        ParsedJavaExpression outerExpression = build(children.get(0));
        ParsedJavaExpression innerExpression = build(children.get(2));
        return new ArrayExpression(outerExpression, innerExpression);
    }

    private ParsedJavaExpression buildDotSeparatedExpression(JavaParser.ExpressionContext ctx) {
        List<ParseTree> children = ctx.children;
        ParsedJavaExpression left = build(children.get(0));
        ParsedJavaExpression right = build(children.get(2));
        if (right instanceof AfterDotExpression){
            return new DotSeparatedExpression(left, (AfterDotExpression) right);
        } else {
            logger.error("Malformed Expression: " + ctx.getText() + ". The right part is expected to be a field, a method call or this.");
            return null;
        }
    }

    // TODO good candidates for utils
    private Optional<Field> checkFieldExists(String fieldName) {
        try {
            return Optional.of(parentObject.getClass().getDeclaredField(fieldName));
        } catch (NoSuchFieldException e) {
            //e.printStackTrace();
            return Optional.empty();
        }
    }

    private Optional<Method> checkMethodExists(String methodName) {
        Method[] declaredMethods = parentObject.getClass().getDeclaredMethods();
        return Arrays.stream(declaredMethods).filter(m -> m.getName().equals(methodName)).findFirst();
    }
}
