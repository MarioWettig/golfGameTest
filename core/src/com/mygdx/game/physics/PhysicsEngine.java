package com.mygdx.game.physics;


import com.mygdx.game.physics.NumericalMethods.Derivative;
import com.mygdx.game.physics.parser.Parser;
import sun.jvm.hotspot.types.JFloatField;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PhysicsEngine {
    // note: all values of x correspond to x, however in game y values correspond to z, and z to y

    // things i need
    // initial conditions
    // x, y, z = h(x,y) coordinates
    // initial v velocity, direction of velocity

    // partial derivative solver
    // individual components for the motion of the golf ball
    // ODE solver
    // an instance of the same Parser used for the input function

    // system of equations to evaluate

    private Derivative derivative;
    private Parser parser;
    private String terrainExpression;
    public double time;
    public boolean isAtRest;

    public PhysicsEngine(String terrainExpression) {
        parser = new Parser();
        this.terrainExpression = terrainExpression;
        derivative = new Derivative(terrainExpression);
        time = 0;
        this.isAtRest = false;
    }

    public PhysicsEngine(Parser parser, String terrainExpression) {
        this.parser = parser;
        derivative = new Derivative(parser, terrainExpression);
    }

    public double[] computeNewVectorState(double timeStep, double x, double y, double vX, double vY) {
        time = time + timeStep;
        double[] pos = computeNewPosition(timeStep, x,y,vX,vY);
        double[] newPartial = computePartialDerivativesAt(pos[0], pos[1]);
        double vNextX = computeVelocityFor(timeStep, vX, vY, newPartial[0], newPartial[1]);
        double vNextY = computeVelocityFor(timeStep, vY, vX, newPartial[1], newPartial[0]);
        return new double[]{pos[0], pos[1], vNextX, vNextY};
    }

    public double[] computeFinalVectorState(double timeStep, double x, double y, double vX, double vY) {
        double[] finalState = {x,y,vX,vY};
        while(!isAtRest){
         finalState = computeNewVectorState(timeStep,x,y,vX,vY);
         x= finalState[0];
         y = finalState[1];
         vX = finalState[2];
         vY = finalState[3];
        }
        return finalState;
    }

    // uses RK4 to the solve system
    public double[] computeNewPosition(double timeStep, double x, double y, double vX, double vY) {
        //System.out.println(timeStep + " " + x + " " + y + " " + vX + " " + vY + " starting point");

        isAtRest = false;
        double[] partialDerivative = computePartialDerivativesAt(x,y);
        if(staticFriction(vX, vY, partialDerivative[0], partialDerivative[1])){
            isAtRest = true;
            return new double[]{x, y};
        }

        double k1x = vX;
        double k1y = vY;

        double posNewX = x + k1x * timeStep/2;
        double posNewY = y + k1y * timeStep/2;

        partialDerivative = computePartialDerivativesAt(posNewX, posNewY);
        double k2x = evaluate(vX + k1x/2, k1y, timeStep/2, partialDerivative[0], partialDerivative[1]);
        double k2y = evaluate(vY + k1y/2, k1x,timeStep/2, partialDerivative[1], partialDerivative[0]);
        //System.out.println(k2x + " " + k2y + " k2");

        posNewX = x + k2x * timeStep/2;
        posNewY = y + k2y * timeStep/2;

        partialDerivative = computePartialDerivativesAt(posNewX, posNewY);
        double k3x = evaluate(vX + k2x/2, k2y,timeStep/2, partialDerivative[0], partialDerivative[1]);
        double k3y = evaluate(vY + k2y/2, k2x,timeStep/2, partialDerivative[1], partialDerivative[0]);
        //System.out.println(k3x + " " + k3y + " k3");

        posNewX = x + k3x * timeStep;
        posNewY = y + k3y * timeStep;

        partialDerivative = computePartialDerivativesAt(posNewX, posNewY);
        double k4x = evaluate(vX + k3x, k3y, timeStep, partialDerivative[0], partialDerivative[1]);
        double k4y = evaluate(vY + k3y, k3x, timeStep, partialDerivative[1], partialDerivative[0]);
        //System.out.println(k4x + " " + k4y + " k4");

        double finalX = x + (timeStep/ 6) * (k1x + 2 * k2x + 2 * k3x + k4x);
        double finalY = y + (timeStep/ 6) * (k1y + 2 * k2y + 2 * k3y + k4y);
        //System.out.println(finalX + " new x " + finalY + " new y " + time);

        return new double[]{finalX, finalY};
    }

    public boolean staticFriction(double currentVelocityComponent, double orthogonalVelocityComponent, double partialX, double partialY){
        if (Math.abs(currentVelocityComponent) >= 0.01f || Math.abs(orthogonalVelocityComponent) >= 0.01f){
            return false;
        }
        return PhysicsSettings.STATIC_FRIC_GRASS > Math.sqrt(partialX*partialX + partialY*partialY);
    }

    public double computeVelocityFor(double timeStep,  double vX, double vY ,double partialX, double partialY){
        return evaluate(vX,vY,timeStep, partialX, partialY);
    }

    private double evaluate(double currentVelocityComponent, double timeStep, double partialX, double partialY){
        double gradientMagnitude = Math.sqrt(partialX * partialX + partialY * partialY + 1);

        double gForceAlongSlope = gForceComponent(partialX);
        //System.out.println(gForceAlongSlope + " gForceAlongSlope");
        double normalForce = forceNormal(gradientMagnitude);
        //System.out.println(normalForce + " normalForce");
        double frictionForce = forceFriction(normalForce, currentVelocityComponent, PhysicsSettings.KINETIC_FRIC_GRASS);
        //System.out.println(frictionForce + " frictionForce");

        double acceleration = -gForceAlongSlope - frictionForce;
        return currentVelocityComponent + (acceleration * timeStep);
    }

    private double evaluate(double currentVelocityComponent, double orthogonalVelocityComponent, double timeStep, double partialC, double partialO){
        double gradientMagnitude = Math.sqrt(partialC * partialC + partialO * partialO + 1);

        double gForceAlongSlope = gForceComponent(partialC, gradientMagnitude);
        //System.out.println(gForceAlongSlope + " gForceAlongSlope");
        double normalForce = forceNormal(gradientMagnitude);
        //System.out.println(normalForce + " normalForce");
        double frictionForce = forceFriction(normalForce, currentVelocityComponent, orthogonalVelocityComponent, partialC, partialO);
        //System.out.println(frictionForce + " frictionForce");

        double acceleration = (-gForceAlongSlope - frictionForce);
        //System.out.println((currentVelocityComponent + acceleration * timeStep) + " velocity at");
        return currentVelocityComponent + acceleration * timeStep;
    }



    // g force component parallel to the slope
    private double gForceComponent(double partial){
        double sinTheta =  (partial / Math.sqrt(partial * partial + 1));
        return PhysicsSettings.G * sinTheta;
    }
    private double gForceComponent(double partialC, double gradientMagnitude){
        return PhysicsSettings.G * partialC / (gradientMagnitude * gradientMagnitude);
    }

    // normal force to the surface
    private double forceNormal(double gradient){
        return PhysicsSettings.G/gradient;
    }

    private double forceFriction(double forceNormal, double currentVelocityComponent, double frictionCoefficient) {
    return frictionCoefficient * forceNormal * Math.signum(currentVelocityComponent);
    }
    private double forceFriction(double forceNormal, double currentVelocityComponent, double orthogonalVelocityComponent, double partialX, double partialY) {
        double velocityMagnitudeSquared = (currentVelocityComponent * currentVelocityComponent) + (orthogonalVelocityComponent * orthogonalVelocityComponent);
        double dotProductSquared = (partialX * currentVelocityComponent + partialY * orthogonalVelocityComponent) * (partialX * currentVelocityComponent + partialY * orthogonalVelocityComponent);
        if (dotProductSquared + velocityMagnitudeSquared == 0) return 0;

        return (PhysicsSettings.KINETIC_FRIC_GRASS * forceNormal * currentVelocityComponent / Math.sqrt(velocityMagnitudeSquared + dotProductSquared));
    }

    public double getHeight(double x, double y){
        parser.constructFor(terrainExpression);
        Map<String, Double> map = new HashMap<>();
        map.put("x", x);
        map.put("y", y);
        double height = parser.evaluateAt(map, terrainExpression);
        return height;
    }

    // computes the partial derivative with respect to both x and y and returns an array [dh/dx, dh/dy]
    private double[] computePartialDerivativesAt(double x, double y){
        double partialX = derivative.derivativeAtPoint("x", "y", x, y, 0.0001);
        double partialY = derivative.derivativeAtPoint("y","x", y, x, 0.0001);
        double[] gradient = new double[2];
        gradient[0] =  partialX;
        gradient[1] =  partialY;
        //System.out.println(partialX + " "+ partialY + " partials");
        return gradient;
    }

    public static void main(String[] args) {
        PhysicsEngine physicsEngine = new PhysicsEngine("0.05*(x^2 + y^2)"); //   cos((1/3)*x)-sin((1/3)*y) 0.4*(0.9-e^(-(x^2+y^2)/8))
        double x = 9;
        double y = 9;
        double vx = 0;
        double vy = 0;
        double time = 0.01f;
        //double h = 0;

//        int n =0;
//        while(!physicsEngine.isAtRest){
//            double[] next = physicsEngine.computeNewVectorState(time, x, y, vx, vy);
//            x = next[0];
//            y = next[1];
//            vx = next[2];
//            vy = next[3];
//            h = physicsEngine.getHeight(x,y);
//            System.out.println("(" + next[0] + ", " + next[1] + ", " + h + ")" );
//            n++;
//        }

        System.out.println(Arrays.toString(physicsEngine.computeFinalVectorState(time, x, y, vx, vy))+ " " + physicsEngine.time);

        //physicsEngine.setPartialDerivativeCurrent(x,y);
    }



    protected class PhysicsVector {
        public double x;
        public double y;
        public double xVelocity;
        public double yVelocity;
        public PhysicsVector(double x, double y, double xVelocity, double yVelocity) {
            this.x = x;
            this.y = y;
        }

        public void set(double x, double y, double xVelocity, double yVelocity) {
            this.x = x;
            this.y = y;
        }

        public void setX(double x) {
            this.x = x;
        }

        public void setY(double y) {
            this.y = y;
        }

        public void setXVelocity(double xVelocity){
            this.xVelocity = xVelocity;
        }

        public void setYVelocity(double yVelocity){
            this.yVelocity = yVelocity;
        }
    }

    public class CSVWriter {

        String filePath;

        public CSVWriter(String filePath) {
        this.filePath = filePath;
        }

//        public void makeCSV(double[] array) {
//            try (FileWriter writer = new FileWriter(filePath)) {
//                // Iterate over the array and write each value followed by a newline
//                for (double value : array) {
//                    writer.append(Double.toString(value));
//                    writer.append("\n");  // Write each number on a new line
//                }
//                System.out.println("Data was written successfully to " + filePath);
//            } catch (IOException e) {
//                System.err.println("Error writing to file: " + e.getMessage());
//            }
//        }

    }
}
