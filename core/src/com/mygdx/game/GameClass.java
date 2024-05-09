package com.mygdx.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.CylinderShapeBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.game.enums.CameraMode;
import com.mygdx.game.homepage.HomeScreen;
import com.mygdx.game.physics.NumericalMethods.Derivative;
import com.mygdx.game.physics.PhysicsEngine;
import com.mygdx.game.physics.parser.Parser;
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


import java.util.HashMap;
import java.util.Map;

public class GameClass extends ApplicationAdapter implements InputProcessor {
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
	private InputMultiplexer inputMultiplexer;

	//player objects
	private Ball ball;

	//player Movement
	float speed = 60f;
	float rotationSpeed = 80f;
	private Matrix4 playerTransform = new Matrix4();
	private final Vector3 moveTranslation = new Vector3();
	private final Vector3 currentPosition = new Vector3();

	// flag/hole
	private final Vector3 holePosition = new Vector3();

 	// Camera
	private float cameraHeight = 20f;
	private CameraMode cameraMode = CameraMode.GOLF_MODE;
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
	private String heightfunction = "0.4*(0.9-e^(-(x^2+y^2)/8))";

	private boolean isStartingPosition;
	private boolean isMoving;
	private float startTime;
	private final Vector3 lastPosition = new Vector3();

	// Physics
	private PhysicsEngine physicsEngine;
	private Parser parser;
	private float velocity;
	private Vector2 velocityVector = new Vector2();
	private float size_half = Settings.SIZE/2;
	private Derivative derivative;

	// Shooting
	private Stage stage;
	private Skin skin;
	private Slider powerSlider;
	private Label sliderValueLabel;


	public GameClass(){

	}






	@Override
	public void create() {
		ball = new Ball("golfBall/golf_ball_lp.glb");
		playerScene = ball.getSceneBall();
		isStartingPosition = true;
		velocityVector.x = 0;
		velocityVector.y = 0;

		parser = new Parser(heightfunction);
		derivative = new Derivative(parser, heightfunction);
		physicsEngine = new PhysicsEngine(heightfunction); // might also include parser for efficiency

		isMoving = false;

		sceneManager = new SceneManager(new CustomShaderProvider(), PBRShaderProvider.createDefaultDepth(36));
		sceneManager.addScene(playerScene);

		// setup camera (the model is very small so you may need to adapt camera settings for your scene)
		camera = new PerspectiveCamera(60f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.near = 1f;
		camera.far = 1000f;
		sceneManager.setCamera(camera);
		camera.position.set(0,0, 0);

		cameraController = new FirstPersonCameraController(camera);
		cameraController.setVelocity(100f);

		inputMultiplexer = new InputMultiplexer(this, cameraController);
		//Gdx.input.setInputProcessor(inputMultiplexer);

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

		poleBuilder();
		createTerrain();
		setupSlider();
	}


	private void createTerrain() {
		if (terrain != null) {
			terrain.dispose();
			sceneManager.removeScene(terrainScene);
		}

		terrain = new TerrainHeight(parser, heightfunction);
		terrainScene = new Scene(terrain.getModelInstance());
		sceneManager.addScene(terrainScene);
		Gdx.gl20.glDisable(GL20.GL_CULL_FACE);
	}


	private void poleBuilder() {
		ModelBuilder modelBuilder = new ModelBuilder();
		modelBuilder.begin();

		Material material = new Material();
		material.set(PBRColorAttribute.createBaseColorFactor(new Color(1, 0, 0, 0.5f))); // Red color with 50% transparency
		material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)); // Enable blending for transparency

		// Define the vertex attributes for the cylinder: position and normal are necessary
		int usageCode = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;

		// Create a cylinder part
		MeshPartBuilder builder = modelBuilder.part("cylinder", GL20.GL_TRIANGLES, usageCode, material);

		// Build the cylinder with specified dimensions and properties
		float diameter = 4f; // Diameter of the cylinder
		float height =2000f;   // Height of the cylinder
		int divisions = 16;  // Number of divisions around the cylinder

		// CylinderShapeBuilder requires diameter, height, and divisions
		CylinderShapeBuilder.build(builder, diameter, height, diameter, divisions);

		ModelInstance model  = new ModelInstance(modelBuilder.end());
		model.transform.setToTranslation(0, 0, 0);
		sceneManager.addScene(new Scene(model));
	}

	private void setupSlider() {
		stage = new Stage(new ScreenViewport());
		skin = new Skin(Gdx.files.internal("skins/skin1/uiskin.json")); // Make sure you have this skin file

		powerSlider = new Slider(0, 5f, 0.1f, false, skin);
		powerSlider.setValue(0); // Set a default value
		powerSlider.setTouchable(Touchable.enabled);

		velocity = powerSlider.getValue();
		sliderValueLabel = new Label(String.format("%.1f", powerSlider.getValue()) + " m/s", skin);
		sliderValueLabel.setColor(1, 2, 1, 1);

		powerSlider.getStyle().knob.setMinHeight(20);
		powerSlider.getStyle().knob.setMinWidth(20);
		powerSlider.getStyle().background.setMinHeight(5);

		sliderValueLabel.setFontScale(1.5f);

		Table table = new Table();
		table.setFillParent(true);
		table.add(powerSlider).width(300).padTop(700);
		table.row();
		table.add(sliderValueLabel).padTop(10);

		stage.addActor(table);

		inputMultiplexer.addProcessor(stage);
		Gdx.input.setInputProcessor(inputMultiplexer);

		powerSlider.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				velocity = powerSlider.getValue();
				sliderValueLabel.setText(String.format("%.1f", powerSlider.getValue())+ " m/s");

			}
		});
	}


	@Override
	public void resize(int width, int height) {
		sceneManager.updateViewport(width, height);
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void render() {
		float deltaTime = Gdx.graphics.getDeltaTime();
		time += deltaTime;


		processInput(deltaTime);
		updateCamera();

		// render
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		sceneManager.update(deltaTime);
		sceneManager.render();
		stage.act(deltaTime);
		stage.draw();
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

		if (cameraMode == CameraMode.GOLF_MODE){
			float height = getHorizontalDistance(camera.position.x, camera.position.z);
			camera.position.y = height + 10f;
		}
	}

	private void calculateAngleAroundPlayer(){
		if (cameraMode == CameraMode.FREE_LOOK || cameraMode == CameraMode.GOLF_MODE){
			if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
				float angleChange = Gdx.input.getDeltaX()/5;
				angleAroundPlayer -= angleChange;
				System.out.println(angleBehindPlayer);
			}
		} else {
			angleAroundPlayer = angleBehindPlayer;
		}
	}

	private void calculatePitch(){
		if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && cameraMode != CameraMode.GOLF_MODE) {
			float pitchChange = -Gdx.input.getDeltaY() * Settings.CAM_PITCH_FACTOR;
			camPitch -= pitchChange;
		}
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

	private float getHorizontalDistance(float x, float y){
		float converted_x = (x - Settings.SIZE/2) * Settings.SIZE_RATIO;
		float converted_y = (y - Settings.SIZE/2) * Settings.SIZE_RATIO;
		Map<String, Double> map = new HashMap<>();
		map.put("x", (double) converted_x);
		map.put("y", (double) converted_y);
		float evaluated = (float) parser.evaluateAt(map, heightfunction);
		return evaluated / Settings.SIZE_RATIO;
	}

	private void processInput(float deltaTime){
		// update the playerTransform Matrix
		playerTransform.set(playerScene.modelInstance.transform);
		//System.out.println(angleAroundPlayer);

			if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
				Gdx.app.exit();
			}

		if (cameraMode != CameraMode.FLY_MODE && !isMoving) {

			if (Gdx.input.isKeyPressed(Input.Keys.W)) {
				moveTranslation.z += speed * deltaTime;
				isMoving = true;
			}

			if (Gdx.input.isKeyPressed(Input.Keys.S)) {
				moveTranslation.z -= speed * deltaTime;
				isMoving = true;
			}

			if (Gdx.input.isKeyPressed(Input.Keys.A)) {
				playerTransform.rotate(Vector3.Y, rotationSpeed * deltaTime);
				angleBehindPlayer += rotationSpeed * deltaTime;
			}

			if (Gdx.input.isKeyPressed(Input.Keys.D)) {
				playerTransform.rotate(Vector3.Y, -rotationSpeed * deltaTime);
				angleBehindPlayer -= rotationSpeed * deltaTime;
			}

			if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
				isMoving = true;
				float angleRadians = (float) (angleAroundPlayer * Math.PI/180);
				velocityVector.x = velocity * MathUtils.sin(angleRadians);
				velocityVector.y = velocity * MathUtils.cos(angleRadians);
				//System.out.println(velocityVector.x + " " + velocityVector.y + " " + angleRadians);
			}
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.C)){
			switch (cameraMode){
				case GOLF_MODE:
					cameraMode = CameraMode.BEHIND_PLAYER;
					angleAroundPlayer = angleBehindPlayer;
					break;
				case BEHIND_PLAYER:
					cameraMode = CameraMode.FLY_MODE;
					break;
				case FLY_MODE:
					cameraMode = CameraMode.FREE_LOOK;
					break;
				case FREE_LOOK:
					cameraMode = CameraMode.GOLF_MODE;
					break;
			}
		}

		//System.out.println(isMoving);
		if(isMoving){
			double [] newPos = physicsEngine.computeNewVectorState(deltaTime, (currentPosition.x-size_half)*Settings.SIZE_RATIO, (currentPosition.z-size_half)*Settings.SIZE_RATIO, velocityVector.x, velocityVector.y);
			moveTranslation.x = -currentPosition.x + (float) ((newPos[0]/Settings.SIZE_RATIO) + size_half);
			moveTranslation.z = -currentPosition.z + (float) ((newPos[1]/Settings.SIZE_RATIO) + size_half);
			velocityVector.x = (float) newPos[2];
			velocityVector.y = (float) newPos[3];
			//System.out.println((newPos[0]) + " " + (newPos[1])  + " " + newPos[2] + " " + newPos[3]  +" "+ physicsEngine.isAtRest);
			isMoving = !physicsEngine .isAtRest;
		}

		if (outOfBounds()){
			if (currentPosition.x + moveTranslation.x > Settings.SIZE)
				moveTranslation.set(-1,0,0);
			else if (currentPosition.z + moveTranslation.z > Settings.SIZE){
				moveTranslation.set(0,0,-1);
			} else if (currentPosition.x + moveTranslation.x < 0){
				moveTranslation.set(1,0,0);
			} else if (currentPosition.z + moveTranslation.z < 0) {
				moveTranslation.set(0,0,1);
			}
		}

		//System.out.println((currentPosition.x*0.02-10) + " " + currentPosition.y*0.02 + " " + (currentPosition.z*0.02-10));

		// Apply the move translation to the transform
		playerTransform.translate(moveTranslation);
		// Set the modified transform
		playerScene.modelInstance.transform.set(playerTransform);
		// Update vector position
		playerScene.modelInstance.transform.getTranslation(currentPosition);

		float height = getHorizontalDistance(currentPosition.x, currentPosition.z);

		if(isStartingPosition){
			currentPosition.x += 40f;
			currentPosition.z += 40f;
			isStartingPosition = false;
		}


//		Vector3 normal = normal(currentPosition.x, currentPosition.z);
//		normal.scl(Settings.BALL_RADIUS);
		currentPosition.y = height + Settings.BALL_RADIUS;
		//currentPosition.add(normal);



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
		return currentPosition.x + moveTranslation.x > Settings.SIZE || currentPosition.z + moveTranslation.z > Settings.SIZE || currentPosition.x + moveTranslation.x < 0 || currentPosition.z + moveTranslation.z < 0 || currentPosition.x + moveTranslation.x > Settings.SIZE && currentPosition.z + moveTranslation.z > Settings.SIZE || currentPosition.x + moveTranslation.x < 0 && currentPosition.z + moveTranslation.z < 0;
	}

	private Vector3 normal(float x, float y){
		x = convertToRealUnits(x);
		y = convertToRealUnits(y);
		float partialX = (float) derivative.derivativeAtPoint("x", "y", x, y, 0.01);
		float partialY = (float) derivative.derivativeAtPoint("y", "x", y, x, 0.01);
		Vector3 normal = new Vector3(-partialX, -partialY, 1);
		System.out.println("Normal Vector at (" + x + ", " + y + "): " + normal);
		return normal.nor();
	}

	private float convertToRealUnits(float num){
		return (num - Settings.SIZE/2) * Settings.SIZE_RATIO;
	}

	private float convertToWorldUnits(float num){
		return num/Settings.SIZE_RATIO + Settings.SIZE/2;
	}






	@Override
	public void dispose() {
		sceneManager.dispose();
		//sceneAsset.dispose();
		ball.dispose();
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
		if(distanceFromPlayer < Settings.CAMERA_MIN_DISTANCE_FROM_PLAYER){
			distanceFromPlayer = Settings.CAMERA_MIN_DISTANCE_FROM_PLAYER;
		}
		return false;
	}
}
