package com.mygdx.game;

public class Settings {
    public static final float CAMERA_START_PITCH = 50f; //default starting pitch
    public static final float CAMERA_MIN_PITCH = CAMERA_START_PITCH-5f;
    public static final float CAMERA_MAX_PITCH = CAMERA_START_PITCH+45f;
    public static final float CAM_PITCH_FACTOR = 0.3f;
    public static final float CAMERA_ZOOM_LEVEL_FACTOR = 0.5f;
    public static final float CAMERA_ANGLE_AROUND_PLAYER_FACTOR = 0.2f; // rotation around the player speed
    public static final float CAMERA_MIN_DISTANCE_FROM_PLAYER = 20; // min zoom distance
    public static final float SIZE_RATIO = 0.02f;
    public static final float SIZE = 1000f;
    public static final float BALL_RADIUS = 1.9f;
    public static final float MAX_VELOCITY= 0.5f;

}
