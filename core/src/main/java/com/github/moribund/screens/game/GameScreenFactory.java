package com.github.moribund.screens.game;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.moribund.images.SpriteContainer;
import com.github.moribund.images.SpriteFile;
import com.github.moribund.screens.ScreenFactory;
import lombok.val;

/**
 * A factory that creates a {@link GameScreen} and its dependencies.
 */
public class GameScreenFactory implements ScreenFactory {
    @Override
    public Screen createScreen() {
        val spriteBatch = createSpriteBatch();
        val camera = createCamera();
        val backgroundSprite = createBackgroundSprite();
        return new GameScreen(spriteBatch, camera, backgroundSprite);
    }

    /**
     * Creates a singular instance of the {@link SpriteFile#BACKGROUND} sprite.
     * @return The background sprite created.
     */
    private Sprite createBackgroundSprite() {
        val backgroundSprite = new Sprite(SpriteContainer.getInstance().getSprite(SpriteFile.BACKGROUND));
        backgroundSprite.setPosition(-(backgroundSprite.getWidth() / 2), -(backgroundSprite.getHeight() / 2));
        return backgroundSprite;
    }

    /**
     * Creates the {@link SpriteBatch} dependency.
     * @return The newly made sprite batch.
     */
    private SpriteBatch createSpriteBatch() {
        return new SpriteBatch();
    }

    /**
     * Creates a {@link OrthographicCamera} dependency.
     * @return The newly made camera.
     */
    private Camera createCamera() {
        val camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        return camera;
    }
}
