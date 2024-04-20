package com.mygdx.game.physics.parser;

import java.util.*;

public class Parser {

    private Lexer lexer;
    private Map<String, Double> valueMap;
    private Stack<Token> opStack;
    private Queue<Token>[] queueExpression;
    private List<Token> tokensList;
    private Map<String, Double> variableMap;
    private Map<String, Queue> equationMap;

    public Parser(){
        lexer = new Lexer();
    }

    public void numOfEquations(int n){
     queueExpression = new Queue[n];
     opStack = new Stack<>();
        for (int i = 0; i < n; i++) {
            queueExpression[i] = new LinkedList<>();
        }
    }

    public void constructFor(String[] equations){
        opStack = new Stack<>();
        equationMap = new HashMap<>();
        for (int i = 0; i < equations.length; i++) {
            Queue<Token> queue = new LinkedList<>();
            constructRPN(equations[i], queue, opStack);
            equationMap.put(equations[i], queue);
            lexer.clearLexer();
        }
    }

    public void constructFor(String equation){
        opStack = new Stack<>();
        equationMap = new HashMap<>();
        Queue<Token> queue = new LinkedList<>();
        constructRPN(equation, queue, opStack);
        equationMap.put(equation, queue);
        lexer.clearLexer();
    }

    public Queue<Token> constructRPN(String equation, Queue<Token> queue, Stack<Token> stack){
        lexer.tokenise(equation);
        tokensList = lexer.getTokens();
        boolean lastLeftBracket = false;
        for (Token token : tokensList) {
            if (!token.isOperator) {
                queue.add(token);
                lastLeftBracket = false;
            } else if (token instanceof Token.LeftParenToken) { // if right bracket is encountered
                stack.push(token);
                lastLeftBracket = true;
            } else if (token instanceof Token.RightParenToken) {
                while (!stack.isEmpty() && !(stack.peek() instanceof Token.LeftParenToken)) { // handle correct order for operations within brackets
                    queue.add(stack.pop());
                }
                if (!stack.isEmpty()) stack.pop();
                lastLeftBracket = false;
            } else if (!stack.isEmpty() &&  lastLeftBracket) {
               stack.push(new Token.NegativeToken(true, 2)); // to recognise negative numbers (-4)
                lastLeftBracket = false;
            } else if (token instanceof Token.CommaToken) {  // functions with multiple inputs like logarithm
                while (!stack.isEmpty() && !(stack.peek() instanceof Token.LeftParenToken)) {
                    queue.add(stack.pop());
                }
                lastLeftBracket = false;
            } else {
                while (!stack.isEmpty() && stack.peek().priority >= token.priority && !(stack.peek() instanceof Token.LeftParenToken)) {
                    queue.add(stack.pop());
                }
                stack.push(token);
                lastLeftBracket = false;
            }
        }
        while (!stack.isEmpty()) {
            queue.add(stack.pop());
        }
//        while (!queue.isEmpty()){
//            System.out.println(queue.poll());
//        }
        return queue;
    }

    public double evaluateAt(Map<String, Double> varMap, String equation){
        return evaluate(varMap, equationMap.get(equation));
    }


    public double evaluate(Map<String, Double> varMap, Queue<Token> rpnQueue) { // needs input of map for the value of unknowns ex. x, y, z...
        Stack<Double> stack = new Stack<>();
        Queue<Token> rpnQueueCopy = new LinkedList<>(rpnQueue);
        while (!rpnQueueCopy.isEmpty()) {
            Token token = rpnQueueCopy.poll();
            if (token instanceof Token.NumberToken) {
                stack.push(((Token.NumberToken) token).number);
            } else if (token instanceof Token.VariableToken) {
                Double varValue = varMap.get(((Token.VariableToken) token).var);
                if (varValue == null) throw new IllegalArgumentException("Unsupported variable: " + token.toString());
                stack.push(varValue);
            } else if (token instanceof Token.ConstantToken) {
                stack.push(token.evaluate(stack));
            } else if (token.isOperator) {
                double result = token.evaluate(stack); // each operator token class has an evaluation method that evaluates for a specific operation
                //System.out.println(result);
                stack.push(result);
            }
        }
        return stack.isEmpty() ? 0 : stack.pop();
    }

    public static void main(String[] args) {
        String expression = " 0.05 * (x^2 + y^2)";
        Map<String, Double> values = new HashMap<>();
        values.put("x", 10.0);
        values.put("y", 10.0);

        Parser parser = new Parser();
        String[] exp = {expression};
        parser.constructFor(expression);
        System.out.println(parser.evaluateAt(values, expression));

    }

}
