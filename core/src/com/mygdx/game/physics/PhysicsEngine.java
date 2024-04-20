package com.mygdx.game.physics;


import com.mygdx.game.physics.NumericalMethods.Derivative;
import com.mygdx.game.physics.parser.Parser;
import sun.jvm.hotspot.types.JFloatField;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;

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
    private  float currentPartialX;
    private float currentPartialY;
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

    public float[] computeNewVectorState(float timeStep, float x, float y, float vX, float vY) {
        time = time + timeStep;
        float[] pos = computeNewPosition(timeStep, x,y,vX,vY);
        float[] newPartial = computePartialDerivativesAt(pos[0], pos[1]);
        float vNextX = computeVelocityFor(timeStep, vX, vY, newPartial[0], newPartial[1]);
        float vNextY = computeVelocityFor(timeStep, vY, vX, newPartial[1], newPartial[0]);
        return new float[]{pos[0], pos[1], vNextX,vNextY};
    }

    // uses RK4 to the solve system
    public float[] computeNewPosition(float timeStep, float x, float y, float vX, float vY) {
        //System.out.println(timeStep + " " + x + " " + y + " " + vX + " " + vY + " starting point");

        isAtRest = false;
        float[] partialDerivative = computePartialDerivativesAt(x,y);
        if(staticFriction(vX, vY, partialDerivative[0], partialDerivative[1])){
            isAtRest = true;
            return new float[]{x, y};
        }

        float k1x = vX;
        float k1y = vY;

        float posNewX = x + k1x * timeStep/2;
        float posNewY = y + k1y * timeStep/2;
        partialDerivative = computePartialDerivativesAt(posNewX, posNewY);
        float k2x = evaluate(vX + (k1x * timeStep)/2, k1y, timeStep/2, partialDerivative[0], partialDerivative[1]);
        float k2y = evaluate(vY + (k1y * timeStep)/2, k1x,timeStep/2, partialDerivative[1], partialDerivative[0]);
        //System.out.println(k2x + " " + k2y + " k2");

        posNewX = x + k2x * timeStep/2;
        posNewY = y + k2y * timeStep/2;
        partialDerivative = computePartialDerivativesAt(posNewX, posNewY);
        float k3x = evaluate(vX + (k2x * timeStep)/2, k2y,timeStep/2, partialDerivative[0], partialDerivative[1]);
        float k3y = evaluate(vY + (k2y * timeStep)/2, k2x,timeStep/2, partialDerivative[1], partialDerivative[0]);
        //System.out.println(k3x + " " + k3y + " k3");

        posNewX = x + k3x * timeStep;
        posNewY = y + k3y * timeStep;
        partialDerivative = computePartialDerivativesAt(posNewX, posNewY);
        float k4x = evaluate(vX + k3x * timeStep, k3y, timeStep, partialDerivative[0], partialDerivative[1]);
        float k4y = evaluate(vY + k3y * timeStep, k3x, timeStep, partialDerivative[1], partialDerivative[0]);
        //System.out.println(k4x + " " + k4y + " k4");

        float finalX = x + (timeStep/ 6) * (k1x + 2 * k2x + 2 * k3x + k4x);
        float finalY = y + (timeStep/ 6) * (k1y + 2 * k2y + 2 * k3y + k4y);
        System.out.println(finalX + " new x " + finalY + " new y " + time);

        return new float[]{finalX, finalY};
    }

    public boolean staticFriction(float currentVelocityComponent, float orthogonalVelocityComponent, float partialX, float partialY){
        if (Math.abs(currentVelocityComponent) >= 0.01f && Math.abs(orthogonalVelocityComponent) >= 0.01f){
            return false;
        }
        return PhysicsSettings.STATIC_FRIC_GRASS > Math.sqrt(partialX*partialX + partialY*partialY);
    }

    public float computeVelocityFor(float timeStep,  float vX, float vY ,float partialX, float partialY){
        return evaluate(vX,vY,timeStep, partialX, partialY);
    }


    private float evaluate(float currentVelocityComponent, float timeStep, float partialX, float partialY){
        float gradientMagnitude = (float) Math.sqrt(partialX * partialX + partialY * partialY + 1);

        float gForceAlongSlope = gForceComponent(partialX);
        //System.out.println(gForceAlongSlope + " gForceAlongSlope");
        float normalForce = forceNormal(gradientMagnitude);
        //System.out.println(normalForce + " normalForce");
        float frictionForce = forceFriction(normalForce, currentVelocityComponent, PhysicsSettings.KINETIC_FRIC_GRASS);
        //System.out.println(frictionForce + " frictionForce");

        float acceleration = -gForceAlongSlope - frictionForce;
        return currentVelocityComponent + (acceleration * timeStep);
    }

    private float evaluate(float currentVelocityComponent, float orthogonalVelocityComponent, float timeStep, float partialC, float partialO){
        float gradientMagnitude = (float) Math.sqrt(partialC * partialC + partialO * partialO + 1);

        float gForceAlongSlope = gForceComponent(partialC);
        //System.out.println(gForceAlongSlope + " gForceAlongSlope");
        float normalForce = forceNormal(gradientMagnitude);
        //System.out.println(normalForce + " normalForce");
        float frictionForce = forceFriction(normalForce, currentVelocityComponent, orthogonalVelocityComponent, partialC, partialO);
        //System.out.println(frictionForce + " frictionForce");

        float acceleration = (-gForceAlongSlope - frictionForce);
        //System.out.println((currentVelocityComponent + acceleration * timeStep) + " velocity at");
        return currentVelocityComponent + acceleration * timeStep;
    }




    // g force component parallel to the slope
    private float gForceComponent(float partial){
        float sinTheta = (float) (partial / Math.sqrt(partial * partial + 1));
        return PhysicsSettings.G * sinTheta;
    }
    private float gForceComponent(float partialC, float gradientMagnitude){
        return PhysicsSettings.G * partialC / (gradientMagnitude * gradientMagnitude);
    }

    // normal force to the surface
    private float forceNormal(float gradient){
        return PhysicsSettings.G/gradient;
    }

    private float forceFriction(float forceNormal, float currentVelocityComponent, float frictionCoefficient) {
    return frictionCoefficient * forceNormal * Math.signum(currentVelocityComponent);
    }
    private float forceFriction(float forceNormal, float currentVelocityComponent, float orthogonalVelocityComponent, float partialX, float partialY) {
        double velocityMagnitudeSquared = (currentVelocityComponent * currentVelocityComponent) + (orthogonalVelocityComponent * orthogonalVelocityComponent);
        double dotProductSquared = (partialX * currentVelocityComponent + partialY * orthogonalVelocityComponent) * (partialX * currentVelocityComponent + partialY * orthogonalVelocityComponent);
        if (dotProductSquared == 0) return 0;

        return (float) (PhysicsSettings.KINETIC_FRIC_GRASS * forceNormal * currentVelocityComponent / Math.sqrt(velocityMagnitudeSquared + dotProductSquared));
    }

    // computes the partial derivative with respect to both x and y and returns an array [dh/dx, dh/dy]
    private float[] computePartialDerivativesAt(double x, double y){
        double partialX = derivative.derivativeAtPoint("x", "y", x, y, 0.0001);
        double partialY = derivative.derivativeAtPoint("y","x", y, x, 0.0001);
        float[] gradient = new float[2];
        gradient[0] = (float) partialX;
        gradient[1] = (float) partialY;
        //System.out.println(partialX + " "+ partialY + " partials");
        return gradient;
    }


    public static void main(String[] args) {
        PhysicsEngine physicsEngine = new PhysicsEngine("0.05*(x^2+y^2)");
        float x = 10;
        float y = 10;
        float vx = 0;
        float vy = 0;
        float time = 0.01f;

        while(!physicsEngine.isAtRest){
            float[] next = physicsEngine.computeNewVectorState(time, x, y, vx, vy);
            x = next[0];
            y = next[1];
            vx = next[2];
            vy = next[3];
            System.out.println(Arrays.toString(next) + " " + physicsEngine.time);
        }

        //System.out.println(Arrays.toString(physicsEngine.computeNewVectorState(time, x, y, vx, vy))+ " " + physicsEngine.time);

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

        public void makeCSV(double[] array) {
            try (FileWriter writer = new FileWriter(filePath)) {
                // Iterate over the array and write each value followed by a newline
                for (double value : array) {
                    writer.append(Double.toString(value));
                    writer.append("\n");  // Write each number on a new line
                }
                System.out.println("Data was written successfully to " + filePath);
            } catch (IOException e) {
                System.err.println("Error writing to file: " + e.getMessage());
            }
        }

    }
}
