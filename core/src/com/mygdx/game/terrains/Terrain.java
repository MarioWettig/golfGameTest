package com.mygdx.game.terrains;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.utils.Disposable;

public abstract class Terrain implements Disposable {

    protected int size;
    protected int width;
    protected int height;
    protected int magnitude;

    protected ModelInstance modelInstance;

    public ModelInstance getModelInstance() {
        return modelInstance;
    }

    abstract public float getHeightAtWorld(float worldX, float worldY);
}
