package com.mygdx.game.physics.NumericalMethods;


import com.mygdx.game.physics.parser.Parser;
import java.util.*;

public class RungeKutta4 implements ODESolver {

        double initialTime;
        double finalTime;
        double stepSize;
        double[] varValues;
        String[] equations;
        double[][] sol;
        HashMap<String, double[]> variablesValues;
        HashMap<String, String> equationsToVar;
        Parser parser;
        String x;
        int numSteps;
        String[] variables;

        public RungeKutta4(double initialTime, double finalTime, double stepSize, double[] varValues, String[] variables, String[] equations, String x) {
            this.varValues = varValues;
            try {
                numSteps = (int) ((finalTime-initialTime)/stepSize) + 1;
                sol = new double[variables.length][numSteps]; // number of columns = number of var and number of rows = number of steps (including initial condition)
            } catch(ArithmeticException e){
                System.out.println("The step size can't be 0");
            } catch (Exception e ){
                System.out.println("The time interval should be positive");
            }
            this.x = x;
            this.initialTime = initialTime;
            this.finalTime = finalTime;
            this.stepSize = stepSize;
            this.variables = variables;
            this.equations = equations;
            createValueMap(variables);
            createEquaMap(variables);
            parser = new Parser();
            parser.constructFor(equations);
        }

        public void setStepSize(double stepSize){
            this.stepSize = stepSize;
        }

        public void createValueMap(String[] variables) {
            variablesValues = new HashMap<>();
            System.out.println(numSteps);
            double[] s = new double[numSteps];
            s[0] = initialTime;
            variablesValues.put(x, s);
            for (int i = 0; i < varValues.length; i++) {
                double[] sols = new double[numSteps];
                sols[0] = varValues[i];
                variablesValues.put(variables[i], sols);
            }
        }

        public void createEquaMap(String[] variables) {
            equationsToVar = new HashMap<>();
            for (int i = 0; i < equations.length; i++) {
                equationsToVar.put( equations[i], variables[i]);
            }
        }

        public void calculate() {
            int numVariables = variables.length;

            double t = initialTime;
            double[] ys = new double[numVariables];

            for (int i = 0; i < numVariables; i++) {
                ys[i] = variablesValues.get(variables[i])[0];
            }

            for (int i = 0; i < numSteps-1; i++) {

                double[] ysNext = new double[numVariables];

                for (int j = 0; j < equations.length; j++) {
                    String equation = equations[j];
                    double k1 = evaluateEquation(t, ys[j], equation, i);
                    double k2 = evaluateEquation(t + stepSize / 2, (ys[j] + (k1/2) * stepSize), equation, i);
                    double k3 = evaluateEquation(t + stepSize / 2, (ys[j] + (k2/2) * stepSize), equation, i);
                    double k4 = evaluateEquation(t + stepSize, (ys[j] + k3 * stepSize), equation, i);
                    //System.out.println(k1 + " " + k2 + " " + k3 + " " + k4);

                    ysNext[j] = ys[j] + (stepSize / 6) * (k1 + 2 * k2 + 2 * k3 + k4);
                }

                ys = ysNext.clone();

                t += stepSize;

                variablesValues.get(x)[i+1] = t;
                for (int j = 0; j < variables.length; j++) {
                    variablesValues.get(variables[j])[i+1] = ys[j];
                }
            }
            constructSol();
        }


    private double evaluateEquation(double t, double ys, String equation, int index) {
        Map<String, Double> valMap = new HashMap<>();
       // System.out.println(t + " " + ys);
        valMap.put(x, t);
        String depNew = equationsToVar.get(equation);
        valMap.put(depNew, ys);
        for (int i = 0; i < variables.length; i++) {
            if (!variables[i].equals(depNew) && !variables[i].equals(x)){
                valMap.put(variables[i], variablesValues.get(variables[i])[index]);
            }
        }
        double result = parser.evaluateAt(valMap, equation);
        return result;
    }



        public void constructSol(){
            for (int i = 0; i < variables.length; i++) {
                sol[i] = variablesValues.get(variables[i]);
            }
        }

        public double getSol(String c){
            return variablesValues.get(c)[numSteps];
        }

        public double[][] getMatrix() {
            return sol;
        }

        public double[] testError(double initialTime, double finalTime, double[] stepSizes, double[] varV, String[] variables ,String[] equations, double[] expValue, String x){
            double[] error = new double[stepSizes.length];

            for(int i = 0; i< stepSizes.length; i++){
                double err = 0;
                RungeKutta4 test = new RungeKutta4(initialTime, finalTime, stepSizes[i], varV ,variables, equations, x);
                test.calculate();
                int j = 0;
                for (String c: test.variablesValues.keySet()){
                    //System.out.println(Arrays.toString(test.variablesValues.get(c)));
                    if (!c.equals(x)) {
                        double observed = test.sol[j][test.sol[0].length-1];
                        err += Math.abs(observed - expValue[j]);
                        System.out.println(observed + "- " + expValue[j]+ " = " + err);
                        j++;
                    }
                }
                error[i] = err/variables.length;

            }

            return error;
        }


    public static void main(String[] args) {
        String eq1 = "10 * (y-x)";
        String eq2 = "x*(28-z)-y";
        String eq3 = "x*y-2.66*z";
            String[] equations = {eq1, eq2, eq3};
            String[] variables = {"x", "y", "z"};
            double[] initialValues = {0.1, 0.5, 0.2 };
//            HashMap<String, String> equationMap = createEqualMap(equations, variables);
//            HashMap<String, ArrayList<Double>> initialValueMap = createValueMap(initialValues, variables);


        RungeKutta4 solver = new RungeKutta4(0, 0.5, 0.0001, initialValues, variables, equations, "t");
        solver.calculate();
        System.out.println(Arrays.deepToString(solver.getMatrix()));



//           double[] expVal = {2601};
//           double [] steps = {0.1,0.01,0.001,0.0001,0.00001,0.000001};
//        double [] time = new double [steps.length];
//
//        int j = 0;
//           for (double i : steps){
//               double start = System.nanoTime();
//               RungeKutta4 solver = new RungeKutta4(1, 1.5, i, initialValues, variables, equations, "x");
//               solver.calculate();
//               double end = System.nanoTime();
//               time[j] = Math.log10(end-start);
//               j++;
//           }
//           System.out.println(Arrays.toString(time));

            //System.out.println(Arrays.toString(solver.testError(0, 51, steps, initialValues, variables, equations, expVal,"x")));
        }




    }


