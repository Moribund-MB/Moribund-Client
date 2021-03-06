package com.github.moribund.screens.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.github.moribund.MoribundClient;
import com.github.moribund.graphics.drawables.DrawableGameAsset;
import com.github.moribund.graphics.sprites.SpriteContainer;
import com.github.moribund.graphics.sprites.SpriteFile;
import com.github.moribund.objects.attributes.Flaggable;
import com.github.moribund.objects.playable.players.PlayableCharacter;
import com.github.moribund.utils.GLUtils;
import lombok.val;

/**
 * The {@code GameScreen} is the screen of the main game.
 */
class GameScreen implements Screen {

    private final Batch uiBatch;
    /**
     * The sprite batch to display sprites.
     */
    private final Batch gameBatch;
    /**
     * The camera to show the game on.
     */
    private final Camera camera;
    /**
     * The sprite that represents the background image.
     */
    private final Sprite background;

    /**
     * The previous screen X coordinate registered.
     */
    private int previousX;

    /**
     * The previous screen Y coordinate registered.
     */
    private int previousY;

    /**
     * Constructor that provides the {@code GameScreen} its dependencies.
     * @param uiBatch The sprite batch to display the UI on.
     * @param gameSpritebatch The sprite batch to display the game sprites on.
     * @param camera The camera to show the game on.
     */
    GameScreen(Batch uiBatch, Batch gameSpritebatch, Camera camera, Sprite background) {
        this.uiBatch = uiBatch;
        this.gameBatch = gameSpritebatch;
        this.camera = camera;
        this.background = background;
    }

    /**
     * The equivalent of {@link com.badlogic.gdx.Game#create()} where this
     * is run as soon as this screen is displayed.
     */
    @Override
    public void show() {
    }

    /**
     * Renders the {@link Screen} by clearing the GL and drawing the sprites.
     * Essentially, this is the heart of the game's {@link com.badlogic.gdx.graphics.g2d.Sprite}s.
     *
     * @param delta As of now, unclassified usage.
     */
    @Override
    public void render(float delta) {
        processFlags();
        GLUtils.clearGL();
        drawGameSpriteBatch(this::drawBackground, this::drawVisibleEntities);
        drawUISpriteBatch(this::drawUI);
        cameraFollowPlayer();
        playerFollowMouse();

        val player = MoribundClient.getInstance().getPlayer();
        if (player != null) {
            player.updateRotation();
            player.updateLocation();
        }
    }

    /**
     * Makes the player face the location of the mouse, with the mouse's location given in world space coordinates.
     * The {@link PlayableCharacter#faceLocation(Vector3)} method is only called if the mouse cursor is on a unique
     * location since the last render.
     * @see Camera#unproject(Vector3)
     * @see PlayableCharacter#faceLocation(Vector3)
     */
    private void playerFollowMouse() {
        PlayableCharacter player = MoribundClient.getInstance().getPlayer();
        if (player != null) {
            if (Gdx.input.getX() == previousX && Gdx.input.getY() == previousY) {
                return;
            }

            Vector3 mouseLocation = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            player.faceLocation(mouseLocation);

            previousX = Gdx.input.getX();
            previousY = Gdx.input.getY();
        }
    }

    private void drawUI() {
        MoribundClient.getInstance().getDrawableUIAssets().forEach(drawable -> drawable.draw(uiBatch));
    }

    /**
     * Draws the {@link SpriteFile#BACKGROUND} {@link Sprite}.
     */
    private void drawBackground() {
        background.draw(gameBatch);
    }

    /**
     * Follows the player using the {@link GameScreen#getCameraPositionX(PlayableCharacter)} and
     * {@link GameScreen#getCameraPositionY(PlayableCharacter)} coordinates. The camera follows the player
     * until they are at an extreme coordinate in the map, to which the camera is static over that area.
     */
    private void cameraFollowPlayer() {
        val player = MoribundClient.getInstance().getPlayer();
        if (player != null) {
            camera.position.set(getCameraPositionX(player), getCameraPositionY(player), 0);
            camera.update();
        }
    }

    /**
     * Gets the optimal x-position for the camera given the {@link GameScreen#background} and {@link MoribundClient#player}.
     * @param player The {@link MoribundClient#player}.
     * @return The optimal x-position for the camera.
     */
    private float getCameraPositionX(PlayableCharacter player) {
        val playerX = player.getX();
        val balancingConstant = 500;
        val furthestLeftBound = -(background.getWidth() / 2) + balancingConstant;
        val furthestRightBound = background.getWidth() / 2 - balancingConstant;

        if (playerX >= furthestRightBound) {
            return furthestRightBound;
        } else if (playerX <= furthestLeftBound) {
            return furthestLeftBound;
        }
        return playerX;
    }

    /**
     * Gets the optimal y-position for the camera given the {@link GameScreen#background} and {@link MoribundClient#player}.
     * @param player The {@link MoribundClient#player}.
     * @return The optimal y-position for the camera.
     */
    private float getCameraPositionY(PlayableCharacter player) {
        val playerY = player.getY();
        val balancingConstant = 350;
        val furthestLowerBound = -(background.getHeight() / 2) + balancingConstant;
        val furthestUpperBound = background.getHeight() / 2 - balancingConstant;

        if (playerY >= furthestUpperBound) {
            return furthestUpperBound;
        } else if (playerY <= furthestLowerBound) {
            return furthestLowerBound;
        }
        return playerY;
    }

    /**
     * Draws all the {@link DrawableGameAsset}'s
     * {@link com.badlogic.gdx.graphics.g2d.Sprite}s.
     */
    private void drawVisibleEntities() {
        MoribundClient.getInstance().getDrawableGameAssets().forEach(drawable -> drawable.draw(gameBatch));
    }

    /**
     * Processes all flaggables.
     */
    private void processFlags() {
        MoribundClient.getInstance().getFlaggables().forEach(Flaggable::processFlags);
    }

    /**
     * Draws the {@link SpriteBatch} by enabling it for drawing and taking in
     * the drawing actions of drawing {@link com.badlogic.gdx.graphics.g2d.Sprite}s.
     * It then closes the {@link SpriteBatch}.
     *
     * @param drawing The {@link Runnable} drawing actions (typically using
     *                {@link SpriteContainer} to draw them) executed just before the
     *                {@link SpriteBatch} ends.
     */
    private void drawGameSpriteBatch(Runnable background, Runnable drawing) {
        gameBatch.setProjectionMatrix(camera.combined);
        gameBatch.begin();
        gameBatch.disableBlending();
        background.run();
        gameBatch.enableBlending();
        drawing.run();
        gameBatch.end();
    }

    private void drawUISpriteBatch(Runnable drawing) {
        uiBatch.begin();
        drawing.run();
        uiBatch.end();
    }

    @Override
    public void resize(int width, int height) {

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
        uiBatch.dispose();
        gameBatch.dispose();

        MoribundClient.getInstance().getFlaggables().clear();
        MoribundClient.getInstance().getPlayers().clear();
        MoribundClient.getInstance().getDrawableGameAssets().clear();
        MoribundClient.getInstance().getDrawableUIAssets().clear();
    }
}