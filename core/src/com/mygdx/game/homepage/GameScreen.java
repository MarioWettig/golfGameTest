package com.mygdx.game.homepage;

import com.badlogic.gdx.Screen;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.Gdx;
import com.mygdx.game.GameClass;

public class GameScreen implements Screen {
        private GameClass gameClass;

        public GameScreen() {
            this.gameClass = new GameClass();
            this.gameClass.create();  // Assuming GameClass uses a create method for setup
        }

        @Override
        public void show() {
        }

        @Override
        public void render(float delta) {
            this.gameClass.render();  // Delegate rendering to GameClass
        }

        @Override
        public void resize(int width, int height) {
            this.gameClass.resize(width, height);
        }

        @Override
        public void pause() {
            this.gameClass.pause();
        }

        @Override
        public void resume() {
            this.gameClass.resume();
        }

        @Override
        public void hide() {
            // Optionally clean up resources that are only needed while this screen is active
        }

        @Override
        public void dispose() {
            this.gameClass.dispose();
        }
}
