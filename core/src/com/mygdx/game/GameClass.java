package com.mygdx.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.enums.CameraMode;
import com.mygdx.game.shaders.CustomShaderProvider;
import com.mygdx.game.terrains.Terrain;
import com.mygdx.game.terrains.TerrainHeight;
import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;

import javax.swing.*;

public class GameClass extends ApplicationAdapter implements InputProcessor
{
	private SceneManager sceneManager;
	private SceneAsset sceneAsset;
	private Scene playerScene;
	private PerspectiveCamera camera;
	private Cubemap diffuseCubemap;
	private Cubemap environmentCubemap;
	private Cubemap specularCubemap;
	private Texture brdfLUT;
	private float time;
	private SceneSkybox skybox;
	private DirectionalLightEx light;
	private FirstPersonCameraController cameraController;

	//player objects
	private Ball ball;

	//Player Movement
	float speed = 60f;
	float rotationSpeed = 80f;
	private Matrix4 playerTransform = new Matrix4();
	private final Vector3 moveTranslation = new Vector3();
	private final Vector3 currentPosition = new Vector3();

 	// Camera
	private float cameraHeight = 20f;
	private CameraMode cameraMode = CameraMode.BEHIND_PLAYER;
	private float camPitch = Settings.CAMERA_START_PITCH;
	private float distanceFromPlayer = 35f;
	private float angleAroundPlayer = 0f;
	private float angleBehindPlayer = 0f;

	// MouseMovement
	private boolean isDragging;
	private float dragStartX;
	private float dragStartY;

	// Terrain
	private Terrain terrain;
	private Scene terrainScene;

	private boolean startingPosition;
	private boolean isMoving;
	private float startTime;
	private final Vector3 lastPosition = new Vector3();

	@Override
	public void create() {
		ball = new Ball("golfBall/golf_ball_lp.glb");
		// create scene
		sceneAsset = new GLBLoader().load(Gdx.files.internal("golfBall/golf_ball_lp.glb"));
		playerScene = new Scene(sceneAsset.scene);
		startingPosition = true;

		isMoving = false;

		sceneManager = new SceneManager(new CustomShaderProvider(), PBRShaderProvider.createDefaultDepth(36));

		sceneManager.addScene(playerScene);

		// setup camera (The BoomBox model is very small so you may need to adapt camera settings for your scene)
		camera = new PerspectiveCamera(60f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.near = 1f;
		camera.far = 1000;
		sceneManager.setCamera(camera);
		camera.position.set(0,0, 4f);

		//Gdx.input.setCursorCatched(true); causes the game to crash

		cameraController = new FirstPersonCameraController(camera);
		cameraController.setVelocity(100f);

		Gdx.input.setInputProcessor(new InputMultiplexer(this, cameraController));
		//Gdx.input.setInputProcessor(new );

		// setup light
		light = new DirectionalLightEx();
		light.direction.set(1, -3, 1).nor();
		light.color.set(Color.WHITE);
		sceneManager.environment.add(light);

		// setup quick IBL (image based lighting)
		IBLBuilder iblBuilder = IBLBuilder.createOutdoor(light);
		environmentCubemap = iblBuilder.buildEnvMap(1024);
		diffuseCubemap = iblBuilder.buildIrradianceMap(256);
		specularCubemap = iblBuilder.buildRadianceMap(10);
		iblBuilder.dispose();

		// This texture is provided by the library, no need to have it in your assets.
		brdfLUT = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));

		sceneManager.setAmbientLight(1f);
		sceneManager.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
		sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap));
		sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));

		// setup skybox
		skybox = new SceneSkybox(environmentCubemap);
		sceneManager.setSkyBox(skybox);

		// to build boxes
		//buildBoxes();

		createTerrain();


	}

	private void createTerrain() {
		if (terrain != null) {
			terrain.dispose();
			sceneManager.removeScene(terrainScene);
		}

		terrain = new TerrainHeight();
		terrainScene = new Scene(terrain.getModelInstance());
		sceneManager.addScene(terrainScene);
	}

//	private void buildBoxes(){
//		ModelBuilder modelBuilder = new ModelBuilder();
//		modelBuilder.begin();
//
//		for (int x = 0; x < 100; x+= 10) {
//			for (int z = 0; z < 100; z+= 10) {
//				Material material = new Material();
//				material.set(PBRColorAttribute.createBaseColorFactor(Color.RED));
//				MeshPartBuilder builder = modelBuilder.part(x + " " + z, GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, material);
//				BoxShapeBuilder.build(builder, x,0, z, 1f, 1f, 1f);
//			}
//		}
//
//		ModelInstance model  = new ModelInstance(modelBuilder.end());
//		sceneManager.addScene(new Scene(model));
//	}

	@Override
	public void resize(int width, int height) {
		sceneManager.updateViewport(width, height);
	}

	@Override
	public void render() {
		float deltaTime = Gdx.graphics.getDeltaTime();
		time += deltaTime;

		if(time-startTime >= 1) isMoving = false;

		//cameraController.update();
	//	playerScene.modelInstance.transform.rotate(Vector3.Y, 10f * deltaTime);
		processInput(deltaTime);
		updateCamera();


		// render
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);


		sceneManager.update(deltaTime);
		sceneManager.render();
	}

	private void updateCamera(){
		if(cameraMode == CameraMode.FLY_MODE){
			cameraController.update();
			return;
		}

		float horDistance = calculateHorizontalDistance(distanceFromPlayer);
		float vertDistance = calculateVerticalDistance(distanceFromPlayer);

		calculatePitch();
		calculateAngleAroundPlayer();
		calculateCameraPosition(currentPosition, horDistance, vertDistance);

		//camera.position.set(currentPosition.x, cameraHeight, currentPosition.z - camPitch);
		camera.lookAt(currentPosition);
		camera.up.set(Vector3.Y);
		camera.update();
	}

	private void calculateCameraPosition(Vector3 currentPosition, float horDistance, float vertDistance){
		float offsetX = (float) (horDistance * Math.sin(Math.toRadians(angleAroundPlayer)));
		float offsetZ = (float) (horDistance * Math.cos(Math.toRadians(angleAroundPlayer)));

		camera.position.x = currentPosition.x - offsetX;
		camera.position.y = currentPosition.y + vertDistance;
		camera.position.z = currentPosition.z - offsetZ;
	}

	private void calculateAngleAroundPlayer(){
		if (cameraMode == CameraMode.FREE_LOOK){
			if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
				float angleChange = Gdx.input.getDeltaX()/5.0f;
				angleAroundPlayer -= angleChange;
			}
		} else {
			angleAroundPlayer = angleBehindPlayer;
		}
	}

	private void calculatePitch(){
		float pitchChange = -Gdx.input.getDeltaY() * Settings.CAM_PITCH_FACTOR;
		camPitch -= pitchChange;

		if(camPitch < Settings.CAMERA_MIN_PITCH){
			camPitch = Settings.CAMERA_MIN_PITCH;
		} else if (camPitch > Settings.CAMERA_MAX_PITCH){
			camPitch = Settings.CAMERA_MAX_PITCH;
		}
	}

	private float calculateHorizontalDistance(float distanceFromPlayer){
		return (float) (distanceFromPlayer * Math.cos(Math.toRadians(camPitch)));
	}

	private float calculateVerticalDistance(float distanceFromPlayer){
		return (float) (distanceFromPlayer * Math.sin(Math.toRadians(camPitch)));
	}

	private void processInput(float deltaTime){
		// update the playerTransform Matrix
		playerTransform.set(playerScene.modelInstance.transform);


			if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
				Gdx.app.exit();
			}
		if (cameraMode != CameraMode.FLY_MODE) {

			if (Gdx.input.isKeyPressed(Input.Keys.W)) {
				moveTranslation.z += speed * deltaTime;
			}

			if (Gdx.input.isKeyPressed(Input.Keys.S)) {
				moveTranslation.z -= speed * deltaTime;
			}

			if (Gdx.input.isKeyPressed(Input.Keys.A)) {
				playerTransform.rotate(Vector3.Y, rotationSpeed * deltaTime);
				angleBehindPlayer += rotationSpeed * deltaTime;
			}

			if (Gdx.input.isKeyPressed(Input.Keys.D)) {
				playerTransform.rotate(Vector3.Y, -rotationSpeed * deltaTime);
				angleBehindPlayer -= rotationSpeed * deltaTime;
			}

			if (Gdx.input.isKeyPressed(Input.Keys.P)) {
				isMoving = true;
				startTime = time;
			}
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
			switch (cameraMode){
				case FREE_LOOK:
					cameraMode = CameraMode.BEHIND_PLAYER;
					angleAroundPlayer = angleBehindPlayer;
					break;
				case BEHIND_PLAYER:
					cameraMode = CameraMode.FLY_MODE;
					break;
				case FLY_MODE:
					cameraMode = CameraMode.FREE_LOOK;
					break;
			}

		}

		if(isMoving){
			moveTranslation.z += 20 * deltaTime;
		}

		if (outOfBounds()){
			System.out.println();
			moveTranslation.set(0,0,0);
		}

		// Apply the move translation to the transform
		playerTransform.translate(moveTranslation);
		// Set the modified transform
		playerScene.modelInstance.transform.set(playerTransform);
		// Update vector position
		playerScene.modelInstance.transform.getTranslation(currentPosition);
		// height collision detection
		float height = terrain.getHeightAtWorld(currentPosition.x, currentPosition.z);

		if(startingPosition){
			currentPosition.x += 40f;
			currentPosition.z += 40f;
			startingPosition = false;
		}

		currentPosition.y = height + 1.85f;

		setLastPosition();

		playerScene.modelInstance.transform.setTranslation(currentPosition);
		// Clear the move translation out for the next frame
		moveTranslation.set(0,0,0);
	}

	private void setLastPosition(){
		lastPosition.x = currentPosition.x;
		lastPosition.y = currentPosition.y;
		lastPosition.z = currentPosition.z;
	}

	private boolean outOfBounds(){
		return currentPosition.x + moveTranslation.x > 400 || currentPosition.z + moveTranslation.z > 400 || currentPosition.x + moveTranslation.x < 0 || currentPosition.z + moveTranslation.z < 0;
	}

	@Override
	public void dispose() {
		sceneManager.dispose();
		sceneAsset.dispose();
		environmentCubemap.dispose();
		diffuseCubemap.dispose();
		specularCubemap.dispose();
		brdfLUT.dispose();
		skybox.dispose();
	}

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {

		return false;
	}

	@Override
	public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
//		System.out.println("down");
//		if (cameraMode == CameraMode.FREE_LOOK && button == Input.Buttons.LEFT) {
//			isDragging = true;
//			dragStartX = screenX;
//			return true;
//		}
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
//		System.out.println("up");
//		if (button == Input.Buttons.LEFT && isDragging) {
//			isDragging = !isDragging;
//			return true;
//		}
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
//		System.out.println("drag");
//		if(!isDragging){
//			float dragDelta = dragStartX - screenX;
//			angleAroundPlayer -= dragDelta;
//			dragStartX = screenX;
//			return true;
//		}
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		float zoomLevel = amountY * Settings.CAMERA_ZOOM_LEVEL_FACTOR;
		distanceFromPlayer += zoomLevel;
		if(distanceFromPlayer < Settings.CAMER_MIN_DISTANCE_FROM_PLAYER){
			distanceFromPlayer = Settings.CAMER_MIN_DISTANCE_FROM_PLAYER;
		}
		return false;
	}
}
