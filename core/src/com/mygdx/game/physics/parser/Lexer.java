package com.mygdx.game.physics.parser;

import java.util.ArrayList;
import java.util.List;

public class Lexer {

    private List<Token> tokens;
    private int position = 0;
    public Lexer(){
        tokens = new ArrayList<>();
    }

    public void tokenise(String equation){
        StringBuilder varBuffer = new StringBuilder();
        StringBuilder numBuffer = new StringBuilder();
        for (int i = 0; i < equation.length(); i++) {
            char c = equation.charAt(i);

            if (Character.isDigit(c) || (c == '.' && numBuffer.length() > 0)) {
                numBuffer.append(c);
                if (equation.length()-i == 1) tokens.add(new Token.NumberToken(Double.parseDouble(numBuffer.toString()),false, 0));
                continue;
            } else if (Character.isLetter(c) || c == '\'') {
                varBuffer.append(c);
                if (equation.length()-i == 1 && c != '\'') finalizeVarBuffer(varBuffer);
                continue;
            } else {
                if (numBuffer.length() > 0) {
                    tokens.add(new Token.NumberToken(Double.parseDouble(numBuffer.toString()),false, 0));
                    numBuffer.setLength(0);
                }
                if (varBuffer.length() > 0) {
                    finalizeVarBuffer(varBuffer);
                    varBuffer.setLength(0);
                }
            }

                switch (c) {
                    case '+':
                        tokens.add(new Token.PlusToken(true, 1));
                        break;
                    case '-':
                        tokens.add(new Token.MinusToken(true, 1));
                        break;
                    case '*':
                        tokens.add(new Token.MultiplyToken(true, 2));
                        break;
                    case '/':
                        tokens.add(new Token.DivideToken(true, 2));
                        break;
                    case '^':
                        tokens.add(new Token.PowerToken(true, 3));
                        break;
                    case '(':
                        tokens.add(new Token.LeftParenToken(true, 4));
                        break;
                    case ')':
                        tokens.add(new Token.RightParenToken(true, 4));
                        break;
                    case ',':
                        tokens.add(new Token.RightParenToken(true, 4));
                        break;
                    default:
                        // Handle unrecognized characters, if necessary
                        break;
                }

        }
    }

    private void finalizeVarBuffer(StringBuilder varBuffer) {
        String identifier = varBuffer.toString();
        switch (identifier) {
            case "sin":
            case "cos":
            case "tan":
                tokens.add(new Token.TrigFunctionToken(identifier, true, 3));
                break;
            case "e":
            case "pi":
                tokens.add(new Token.ConstantToken(identifier, false, 0));
                break;
            case "log":
                tokens.add(new Token.LogarithmToken(true, 3));
                break;
            case "sqrt":
                tokens.add(new Token.SquareRootToken(true, 3));
                break;
            default:
                tokens.add(new Token.VariableToken(identifier, false, 0));
                break;
        }
    }

    public Token peek() {
        // Return the next token without advancing the position.
        return position < tokens.size() ? tokens.get(position) : null;
    }

    public Token next() {
        // Return the current token and advance the position.
        return position < tokens.size() ? tokens.get(position++) : null;
    }

    public List<Token> getTokens(){
        return tokens;
    }

    public void clearLexer(){
        tokens.clear();
    }

    public void sometest(){
//        for (int i = 0; i < tokens.size(); i++) {
//            System.out.println(tokens.get(i).toString());
//        }
    }
}




