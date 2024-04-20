package com.mygdx.game.physics.parser;

import java.util.Stack;

public abstract class Token {
    private TokenType type;
    public boolean isOperator;
    public int priority;

    public Token(TokenType type, boolean isOperator, int priority) {
        this.type = type;
        this.isOperator = isOperator;
        this.priority = priority;

    }

    public double evaluate(Stack<Double> stack){
        return 0;
    }

    public TokenType getType() {
        return type;
    }

    public String toString() {
        return "Token";
    }

    public enum TokenType {
        NUMBER, PLUS, MINUS, MULTIPLY, DIVIDE, POWER, LEFT_PARENTHESIS, RIGHT_PARENTHESIS, EQUALS, VARIABLE, FUNCTION, DERIVATIVE, CONSTANT, NEGATIVE, LOGARITHM, COMMA, ROOT

    }
    public static class NumberToken extends Token {
        double number;
        public NumberToken(Double number, boolean isOperator, int priority) {
            super(TokenType.NUMBER, isOperator, priority);
            this.number = number;
            this.isOperator = isOperator;
            this.priority = priority;
        }


        public String toString() {
            return String.valueOf(number);
        }

        public double evaluate() {
            return 0.0;
        }

    }
    public static class PlusToken extends Token{
        public boolean isOperator;
        public int priority;
        public PlusToken(boolean isOperator, int priority) {
            super(TokenType.PLUS,isOperator, priority);
            this.isOperator = isOperator;
            this.priority = priority;
        }

        public double evaluate(Stack<Double> stack) {
            if (stack.size() < 2) {
                throw new IllegalArgumentException("Insufficient values for operation");
            }
            double left = stack.pop();
            double right = stack.pop();
            return left + right;
        }

        public String toString() {
            return " + ";
        }
    }
    public static class MinusToken extends Token{
        public boolean isOperator;
        public int priority;
        public MinusToken(boolean isOperator, int priority) {
            super(TokenType.MINUS,isOperator, priority);
            this.isOperator = isOperator;
            this.priority = priority;
        }

        public double evaluate(Stack<Double> stack) {
            if (stack.size() < 1) { // Check for sufficient operands
                throw new IllegalArgumentException("Insufficient values for operation");
            }
            double right = stack.pop();
            double left = stack.pop();
            return left - right;
        }

        public String toString() {
            return " - ";
        }
    }
    public static class MultiplyToken extends Token{
        public boolean isOperator;
        public int priority;
        public MultiplyToken(boolean isOperator, int priority) {
            super(TokenType.MULTIPLY,isOperator, priority);
            this.isOperator = isOperator;
            this.priority = priority;
        }

        public double evaluate(Stack<Double> stack) {
            if (stack.size() < 2) {
                throw new IllegalArgumentException("Insufficient values for operation");
            }
            double right = stack.pop();
            double left = stack.pop();
            return left * right;
        }

        public String toString() {
            return " * ";
        }
    }
    public static class DivideToken extends Token{
        public boolean isOperator;
        public int priority;
        public DivideToken(boolean isOperator, int priority) {
            super(TokenType.DIVIDE,isOperator, priority);
            this.isOperator = isOperator;
            this.priority = priority;
        }

        public double evaluate(Stack<Double> stack) {
            if (stack.size() < 2) {
                throw new IllegalArgumentException("Insufficient values for operation");
            }
            double right = stack.pop();
            double left = stack.pop();
            if (right == 0) throw new ArithmeticException("Division by zero.");
            return left/right;
        }

        public String toString() {
            return " / ";
        }
    }
    public static class PowerToken extends Token{
        public int priority;
        public boolean isOperator;
        public PowerToken(boolean isOperator, int priority) {
            super(TokenType.POWER,isOperator, priority);
            this.isOperator = isOperator;
            this.priority = priority;
        }

        public double evaluate(Stack<Double> stack) {
            if (stack.size() < 2) {
                throw new IllegalArgumentException("Insufficient values for operation");
            }
            double right = stack.pop();
            double left = stack.pop();
            return Math.pow(left, right);
        }

        public String toString() {
            return " ^ ";
        }

    }
    public static class VariableToken extends Token {
        public boolean isOperator;
        public
        String var;
        public VariableToken(String var, boolean isOperator, int priority) {
            super(TokenType.VARIABLE,isOperator, priority);
            this.var = var;
            this.isOperator = isOperator;
            this.priority = priority;
        }

        public String toString() {
            return var;
        }

    }
    public static class LeftParenToken extends Token{
        public int priority;
        public boolean isOperator;
        public LeftParenToken(boolean isOperator, int priority) {
            super(TokenType.LEFT_PARENTHESIS,isOperator, priority);
            this.isOperator = isOperator;
            this.priority = priority;
        }

        public String toString() {
            return " ( ";
        }
    }
    public static class RightParenToken extends Token{
        public boolean isOperator;
        public RightParenToken(boolean isOperator, int priority) {
            super(TokenType.RIGHT_PARENTHESIS,isOperator, priority);
            this.isOperator = isOperator;
            this.priority = priority;
        }

        public String toString() {
            return " ) ";
        }
    }
    public static class TrigFunctionToken extends Token {
        public boolean isOperator;
        String function;

        public TrigFunctionToken(String function, boolean isOperator, int priority) {
            super(TokenType.FUNCTION, isOperator, priority);
            this.function = function;
            this.isOperator = isOperator;
            this.priority = priority;
        }

        public double evaluate(Stack<Double> stack) {
            double var = stack.pop();
            switch (function){
                case "sin":
                    return Math.sin(var);
                case "cos":
                    return Math.cos(var);
                case "tan":
                    return Math.tan(var);
            }
            throw new RuntimeException("not possible trig function");
        }

        public String toString() {
            return function;
        }

    }
    public static class NegativeToken extends Token{
            public boolean isOperator;
            public int priority;
            public NegativeToken(boolean isOperator, int priority) {
                super(TokenType.MINUS,isOperator, priority);
                this.isOperator = isOperator;
                this.priority = priority;
            }

            public double evaluate(Stack<Double> stack) {
                return - stack.pop();
            }

            public String toString() {
                return " - ";
            }
        }
    public static class LogarithmToken extends Token{
        public boolean isOperator;
        public int priority;
        public LogarithmToken(boolean isOperator, int priority) {
            super(TokenType.MINUS,isOperator, priority);
            this.isOperator = isOperator;
            this.priority = priority;
        }

        public double evaluate(Stack<Double> stack) {
            if (stack.size() < 2) {
                throw new IllegalArgumentException("Insufficient operands for logarithm operation");
            }
            double base = stack.pop();
            double argument = stack.pop();
            return Math.log(argument) / Math.log(base); // Calculate and return the logarithm
        }

        @Override
        public String toString() {
            return "log";
        }
    }
    public static class CommaToken extends Token {
        public CommaToken() {
            super(TokenType.COMMA, false, 0); // Commas typically don't act as operators and don't have a priority
        }

        @Override
        public double evaluate(Stack<Double> stack) {
            throw new UnsupportedOperationException("Comma cannot be evaluated directly.");
        }

        @Override
        public String toString() {
            return ",";
        }
    }
    public static class ConstantToken extends Token{
        public boolean isOperator;
        String constant;
        public ConstantToken(String constant, boolean isOperator, int priority) {
            super(TokenType.CONSTANT,isOperator, priority);
            this.constant = constant;
            this.isOperator = isOperator;
            this.priority = priority;
        }

        public double evaluate(Stack<Double> stack){
            switch (constant){
                case "e":
                    return Math.E;
                case  "pi":
                    return Math.PI;
            }
            return 0;
        }

        public String toString() {
            return constant;
        }



    }
    public static class SquareRootToken extends Token {
        public SquareRootToken(boolean isOperator, int priority) {
            super(TokenType.ROOT, isOperator, priority);
        }

        @Override
        public double evaluate(Stack<Double> stack) {
            if (stack.isEmpty()) {
                throw new IllegalArgumentException("Insufficient operands for square root operation");
            }
            double operand = stack.pop();
            if (operand < 0) {
                throw new ArithmeticException("Cannot take the square root of a negative number.");
            }
            return Math.sqrt(operand);
        }

        @Override
        public String toString() {
            return "sqrt";
        }
    }
    public static class DerivativeToken extends Token{
        private int order;
        private String var;

        public DerivativeToken(String var, int order, boolean isOperator, int priority) {
            super(TokenType.DERIVATIVE,isOperator, priority);
            this.var = var;
            this.order = order;
            this.isOperator = isOperator;
            this.priority = priority;
        }
        public String toString() {
            return var;
        }
        public int getOrder() {
            return order;
        }

    }

}
