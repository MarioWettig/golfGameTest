package com.mygdx.game.homepage;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.game.GameMain;

public class HomeScreen implements Screen {
    private Stage stage;
    private Table table;
    private Skin skin;
    private GameMain game;
    private String heightFunction;

    public HomeScreen(GameMain game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("skins/skin3/uiskin.json"));

        this.table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        createButtons();
    }

    private void createButtons(){
        TextButton startGameButton = new TextButton("Start Game", skin);
        TextButton inputButton = new TextButton("Inputs ", skin);
        TextButton howToButton = new TextButton("Guide", skin);

        startGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.startGame(); // Start the game
            }
        });

        inputButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new InputScreen());
            }
        });

        howToButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new HowToScreen());
            }
        });

        float buttonWidth = 150f;

        table.add(inputButton).uniform().fillX().minWidth(buttonWidth).pad(10);
        table.row();
        table.add(howToButton).uniform().fillX().minWidth(buttonWidth).pad(10);
        table.row();
        table.add(startGameButton).uniform().fillX().minWidth(buttonWidth).pad(10);

        table.center();

    }

    private BitmapFont generateFont(float scale) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/MyFont.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = (int) (16 * scale);  // Base size is 16, scale as needed
        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();
        return font;
    }


    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        stage.act();
        adjustButtonSizes(width, height);
    }

    private void adjustButtonSizes(int width, int height) {
        float buttonWidth = Math.max(width * 0.3f, 150);
        for (Cell cell : table.getCells()) {
            cell.width(buttonWidth);
        }
        table.invalidateHierarchy();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
