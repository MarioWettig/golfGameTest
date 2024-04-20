package com.mygdx.game.physics.parser;
import java.util.*;

public class Order {

    public Order() {
    }

    public int checkOrder(String equation) {
        int orderCount = 0;
        int repeat = 0;
        int count = 0;
        while (equation.charAt(count) != '=') {
            if (equation.charAt(count) == '\'') {
                repeat++;
                if (repeat > orderCount) {
                    orderCount = repeat;
                }
            } else {
                repeat = 0;
            }
            count++;
        }
        return orderCount;
    }


    public String getAfterEquals(String equation) {
        int equalsIndex = equation.indexOf("=");
        if (equalsIndex != -1) {
            return equation.substring(equalsIndex + 1).trim();
        } else {
            return "";
        }
    }



    public static String[] generateYAndDerivatives(int highestOrder) {
        if (highestOrder < 0) {
            throw new IllegalArgumentException("Highest order must be non-negative.");
        }
        List<String> variables = new ArrayList<>();
        // Add 'y' itself for order 0
        variables.add("y");

        // Generate derivatives of 'y' up to the highest order
        for (int i = 1; i <= highestOrder; i++) {
            StringBuilder derivative = new StringBuilder("y");
            for (int j = 0; j < i; j++) {
                derivative.append("'");
            }
            variables.add(derivative.toString());
        }
        return variables.toArray(new String[0]);
    }



    public static void main(String[] args) {
        Order order = new Order();
        String equation = "5y'' + 3x'' - z' + 4 = 0";
        String function = "y'' = y' + x*z";

    }


}
