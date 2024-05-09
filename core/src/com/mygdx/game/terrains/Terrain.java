package com.mygdx.game.terrains;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.utils.Disposable;

public abstract class Terrain implements Disposable {

    protected float sizeRatio;
    protected int size;
    protected int resolution;
    protected Mesh mesh;

    protected ModelInstance modelInstance;

    public ModelInstance getModelInstance() {
        return modelInstance;
    }

    abstract public float getHeight(float x, float y);

}
