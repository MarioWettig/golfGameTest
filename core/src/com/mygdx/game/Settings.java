package com.mygdx.game;

public class Settings {
    public static final float CAMERA_START_PITCH = 20f; //default starting pitch
    public static final float CAMERA_MIN_PITCH = CAMERA_START_PITCH-20f;
    public static final float CAMERA_MAX_PITCH = CAMERA_START_PITCH+20f;
    public static final float CAM_PITCH_FACTOR = 0.3f;
    public static final float CAMERA_ZOOM_LEVEL_FACTOR = 0.5f;
    public static final float CAMERA_ANGLE_AROUND_PLAYER_FACTOR = 0.2f; // rotation around the player speed
    public static final float CAMER_MIN_DISTANCE_FROM_PLAYER = 4; // min zoom distance
}
