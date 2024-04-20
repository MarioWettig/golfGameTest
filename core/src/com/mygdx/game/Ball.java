package com.mygdx.game;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class Ball {


    //libGdx stuff
    private Vector3 position;
    private Scene sceneBall;
    private SceneAsset ballAsset;
    private Matrix4 playerTransform = new Matrix4();
    private ModelBatch ball;
    private float radius = 1.85f;

    //movement related
    private boolean isMoving;

    public Ball(String filePath) {
        ballAsset = new GLBLoader().load(Gdx.files.internal(filePath));
        sceneBall = new Scene(ballAsset.scene);
        position = new Vector3();
        isMoving = false;
    }

    public void setInitialPosition(float x, float y) {
        position.set(position.x, position.y, position.z);
    }

    public void render(){}

    public void update(float delta) {}

    public void dispose(){}

}
