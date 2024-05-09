package com.mygdx.game.physics.NumericalMethods;

import com.mygdx.game.physics.parser.Parser;

import java.util.HashMap;
import java.util.Map;

public class Derivative {

    private Parser parser;
    private String expression;

    public Derivative() {
        parser = new Parser();
    }

    public Derivative(String expression) {
        parser = new Parser();
        this.expression = expression;
        parser.constructFor(expression);
    }

    public Derivative(Parser parser, String expression) {
        this.parser = parser;
        this.expression = expression;
    }

    public void evaluateExpression(String expression) {
        this.expression = expression;
        parser.constructFor(expression);
    }

    public double derivativeAtPoint(String varX, String varY, double x, double y, double stepSize) {
        // Create first map for x + h
        Map<String, Double> mapPlus = new HashMap<>();
        mapPlus.put(varX, x + stepSize);
        mapPlus.put(varY, y);

        // Create the second map for x - h
        Map<String, Double> mapMinus = new HashMap<>();
        mapMinus.put(varX, x - stepSize);
        mapMinus.put(varY, y);

        // Evaluate the function at x + h and x - h and compute the derivative
        double functionPlus = parser.evaluateAt(mapPlus, expression);
        double functionMinus = parser.evaluateAt(mapMinus, expression);

        return (functionPlus - functionMinus) / (2 * stepSize);
    }

    public static void main(String[] args) {
        String expression = " 0.05 * (x^2 + y^2)- sin(x)";
        Map<String, Double> values = new HashMap<>();
        values.put("x", 10.0);
        values.put("y", 10.0);

        Derivative derivative = new Derivative(expression);
        double dub = derivative.derivativeAtPoint("x", "y", 10, 10, 0.0001);
        System.out.println(dub);
    }

}
