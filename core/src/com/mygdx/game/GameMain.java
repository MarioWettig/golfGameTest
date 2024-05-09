package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.mygdx.game.homepage.GameScreen;
import com.mygdx.game.homepage.HomeScreen;

public class GameMain extends Game {
    private HomeScreen homeScreen;

    @Override
    public void create() {
        homeScreen = new HomeScreen(this);
        this.setScreen(homeScreen);
    }

    public void startGame() {
        this.setScreen(new GameScreen());
    }
}
