package com.mygdx.game.physics.NumericalMethods;

import com.mygdx.game.physics.parser.Order;
import com.mygdx.game.physics.parser.Parser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class EulerMethod implements ODESolver {

    double t0;
    double tFinal;
    double[] y0;
    String[] depVars;
    String t;
    double stepSize;
    String[] equations;
    double [][] sol;  // first column should be the x the independent variables and the indep the other columns
    double[] tVal;
    Parser parser;
    Map<String, Double> variables;
    int[] equationOrders;


    public EulerMethod(double[] dep0, double[] indepVars0, String[] equations, String dep, String[] depVars){
        this.t0 = dep0[0];
        this.tFinal = dep0[1];
        this.y0 = indepVars0;
        this.depVars = depVars;
        this.t = dep;
        this.stepSize = dep0[2];
        this.equations = equations;
        processOrder();
        mapVariables();
        fillInitialValues();
        parser = new Parser();
        parser.constructFor(this.equations);
    }


    private void fillInitialValues(){
        try{
            int cols = (int) ((tFinal - t0)/stepSize);
            tVal = new double[cols+1];
            sol = new double[y0.length][cols+1]; ///
        } catch(ArithmeticException e){
            System.out.println("The step size can't be 0");
        } catch (Exception e ){
            System.out.println("The time interval should be positive");
        }
        this.tVal[0] = t0;
        for (int i = 0; i < y0.length; i++) { // if this stays this means that all the variables need to be placed in order
            sol[i][0] = y0[i];
        }

    }

    private void mapVariables(){
        variables = new HashMap<>();
        for (int i = 0; i < depVars.length; i++) {
            variables.put(depVars[i], y0[i]);
            //System.out.println(depVars[i] + " " + y0[i]);
        }
        variables.put(t, t0);
    }

    public void mapVars(){
        Map<String, double[]> solutionSet = new HashMap<>();
        int size = (int)((tFinal-t0)/stepSize);
        for (int i = 0; i < depVars.length; i++) {
            double[] sols = new double[size];
            sols[0] = y0[i];
            solutionSet.put(depVars[i], sols);
        }
        double[] sols = new double[size];
        sols[0] = t0;
        solutionSet.put(t, sols);
    }

    Map<String, Integer> orderMap;

    public void processOrder(){
        Order order = new Order();
        orderMap = new HashMap<>();
        equationOrders = new int[equations.length];
        for (int i = 0; i < equations.length; i++) {
            equationOrders[i] = order.checkOrder(equations[i]);
            equations[i] = order.getAfterEquals(equations[i]);
        }
    }

    public void calculate(){
        for (int i = 0; i < sol[0].length-1; i++) {
            tVal[i+1] = tVal[i] + stepSize;
            int count = 0;
            for (int j = 0; j < equations.length; j++) {
                int eqOrder = equationOrders[j];
                for (int k = 1; k <= eqOrder; k++) {
                    if (k < eqOrder ){
                        sol[count][i+1] = sol[count][i] + stepSize * sol[count+1][i];
                    } else {
                        double dy_dx = parser.evaluateAt(variables, equations[j]);
                        sol[count][i+1] = sol[count][i] + stepSize * dy_dx;
                    }
                    count++;
                }
            }
            variables.replace(t, tVal[i+1]);
            for (int j = 0; j < depVars.length; j++) {
                variables.replace(depVars[j], sol[j][i+1]);
            }
        }
    }
    // with 2 maps

    public Double getValue (){
        return sol[sol.length-1][1];
    }

    public double[][] getMatrix(){
        return sol;
    }

    public double[] returnIndep(){
        return tVal;
    }

    public static void main(String [] args){
        String eq1 = "x' = 10*(y-x)";
        String eq2 = "y' = x*(28-z)-y";
        String eq3 = "z' = x*y-2.66*z";

        //String eq4 = "x' = x";
        String[] equations = {eq1, eq2, eq3};
        String[] vars = {"x", "y", "z"};
        String indep = "t";
        double[] initialValues = {0.1, 0.5, 0.2};
        double[] start = {0,0.5,0.0001}; // intial values of indep, final value and step size
        EulerMethod euler = new EulerMethod(start, initialValues, equations, indep, vars);
        euler.calculate();
        System.out.println(Arrays.deepToString(euler.getMatrix()));
       // System.out.println(Arrays.toString(euler.returnIndep()));
    }
}
