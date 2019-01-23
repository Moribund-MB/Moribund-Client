package com.github.moribund.objects.playable.players;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.github.moribund.MoribundClient;
import com.github.moribund.graphics.*;
import com.github.moribund.net.packets.combat.ProjectileCollisionPacket;
import com.github.moribund.net.packets.input.KeyPressedPacket;
import com.github.moribund.net.packets.input.KeyUnpressedPacket;
import com.github.moribund.net.packets.input.MouseClickedPacket;
import com.github.moribund.net.packets.items.EquipItemPacket;
import com.github.moribund.net.packets.items.PickupItemPacket;
import com.github.moribund.objects.flags.Flag;
import com.github.moribund.objects.flags.FlagConstants;
import com.github.moribund.objects.nonplayable.items.EquippedItemType;
import com.github.moribund.objects.nonplayable.items.GroundItem;
import com.github.moribund.objects.nonplayable.items.Item;
import com.github.moribund.objects.nonplayable.projectile.Projectile;
import com.github.moribund.objects.nonplayable.projectile.ProjectileType;
import com.github.moribund.objects.playable.players.containers.Equipment;
import com.github.moribund.objects.playable.players.containers.Inventory;
import com.github.moribund.objects.playable.players.ui.LocalHealthBar;
import com.github.moribund.objects.playable.players.ui.Timer;
import com.github.moribund.utils.GLUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

/**
 * The {@code Player} that is being controlled by a client. The {@code Player}
 * is a type of {@link InputProcessor} for it is bound to {@link Player#keyBinds}.
 */
public class Player implements PlayableCharacter {

    private static final int ROTATION_SPEED = 5;
    private static final int MOVEMENT_SPEED = 5;

    @Getter
    private final int gameId;
    /**
     * The unique player ID based on the {@link com.esotericsoftware.kryonet.Connection} of
     * the client to the server.
     */
    @Getter
    private final int playerId;
    @Getter
    private final Inventory inventory;
    private final LocalHealthBar healthBar;
    @Getter
    private final Equipment equipment;
    @Getter
    private final Timer timer;
    @Getter
    private Polygon polygon;
    /**
     * The {@link Sprite} of this {@code Player} that represents the {@code Player}
     * in the live game visually.
     */
    private Sprite sprite;
    /**
     * The respective {@link com.badlogic.gdx.Input.Keys} that are bound to
     * {@link Runnable} methods defined in this class.
     */
    private Int2ObjectMap<PlayerAction> keyBinds;
    /**
     * The currently active {@link Flag}s on the {@code Player}.
     */
    private final ObjectSet<Flag> flags;
    /**
     * The currently active {@link Flag}s on the {@code Player} that will soon be removed. This is cleared whenever
     * the respective {@link Player#flags} have been removed from their {@link ObjectSet}.
     */
    private final ObjectSet<Flag> flagsToRemove;
    @Getter
    @Setter
    private int hitpoints;
    private final String username;
    @Getter
    private int maxHitpoints;
    private SpriteAnimation currentAnimation;

    /**
     * Makes a {@code Player} with its unique player ID generated by
     * the {@link com.esotericsoftware.kryonet.Connection} between the
     * client and the server.
     *
     * @param playerId The unique player ID.
     */
    public Player(int gameId, int playerId, String username, int maxHitpoints) {
        this.gameId = gameId;
        this.playerId = playerId;
        this.username = username;
        this.maxHitpoints = maxHitpoints;
        hitpoints = maxHitpoints;
        flags = new ObjectArraySet<>();
        flagsToRemove = new ObjectArraySet<>();
        sprite = new Sprite(SpriteContainer.getInstance().getSprite(SpriteFile.PLAYER));
        inventory = new Inventory();
        equipment = new Equipment();
        healthBar = new LocalHealthBar(this);
        timer = new Timer();
        polygon = new Polygon(SpriteVertices.PLAYER.getVertices());
        polygon.setOrigin(sprite.getOriginX(), sprite.getOriginY());
    }

    /**
     * todo optimize this
     */
    public void addUIAssets() {
        val assets = MoribundClient.getInstance().getDrawableUIAssets();
        assets.add(inventory);
        assets.add(equipment);
        assets.add(healthBar);
        assets.add(timer);
    }

    private void changeCharacter(SpriteFile spriteFile, SpriteVertices spriteVertices) {
        val sprite = new Sprite(SpriteContainer.getInstance().getSprite(spriteFile));
        sprite.setPosition(this.sprite.getX(), this.sprite.getY());
        sprite.setRotation(this.sprite.getRotation());
        this.sprite = sprite;

        val polygon = new Polygon();
        polygon.setVertices(spriteVertices.getVertices());
        polygon.setOrigin(sprite.getOriginX(), sprite.getOriginY());
        polygon.setPosition(this.polygon.getX(), this.polygon.getY());
        polygon.setRotation(this.polygon.getRotation());
        this.polygon = polygon;
    }

    /**
     * Flags a new {@link Flag} on the player.
     *
     * @param flag The {@link Flag} to flag.
     */
    private void flag(Flag flag) {
        flags.add(flag);
    }

    /**
     * Removes a {@link Flag} on the player.
     *
     * @param flag The {@link Flag} that is not longer active.
     */
    private void flagToRemove(Flag flag) {
        flagsToRemove.add(flag);
    }

    @Override
    public void processFlags() {
        flags.removeAll(flagsToRemove);
        flagsToRemove.clear();
        flags.forEach(flag -> flag.processFlag(this));
    }

    @Override
    public void setRotation(float angle) {
        sprite.setRotation(angle);
        polygon.setRotation(angle);
    }

    @Override
    public float getRotation() {
        return sprite.getRotation();
    }

    @Override
    public void bindKeys() {
        keyBinds.put(Input.Keys.W, new PlayerAction() {
            @Override
            public void keyPressed() {
                flag(FlagConstants.MOVE_FORWARD_FLAG);
            }

            @Override
            public void keyUnpressed() {
                flagToRemove(FlagConstants.MOVE_FORWARD_FLAG);
            }
        });
        keyBinds.put(Input.Keys.S, new PlayerAction() {
            @Override
            public void keyPressed() {
                flag(FlagConstants.MOVE_BACKWARD_FLAG);
            }

            @Override
            public void keyUnpressed() {
                flagToRemove(FlagConstants.MOVE_BACKWARD_FLAG);
            }
        });
        keyBinds.put(Input.Keys.D, new PlayerAction() {
            @Override
            public void keyPressed() {
                flag(FlagConstants.ROTATE_RIGHT_FLAG);
            }

            @Override
            public void keyUnpressed() {
                flagToRemove(FlagConstants.ROTATE_RIGHT_FLAG);
            }
        });
        keyBinds.put(Input.Keys.A, new PlayerAction() {
            @Override
            public void keyPressed() {
                flag(FlagConstants.ROTATE_LEFT_FLAG);
            }

            @Override
            public void keyUnpressed() {
                flagToRemove(FlagConstants.ROTATE_LEFT_FLAG);
            }
        });
        keyBinds.put(Input.Keys.E, new PlayerAction() {
            @Override
            public void keyPressed() {
                val pickableObjectNear = getPickableObjectNearest();
                if (pickableObjectNear != null) {
                    sendPickupItemRequest(pickableObjectNear);
                }
            }

            @Override
            public void keyUnpressed() {

            }
        });
        keyBinds.put(Input.Keys.B, new PlayerAction() {
            @Override
            public void keyPressed() {
                changeCharacter(SpriteFile.PLAYER_WITH_BOW, SpriteVertices.PLAYER_WITH_BOW);
            }

            @Override
            public void keyUnpressed() {

            }
        });
        keyBinds.put(Input.Keys.N, new PlayerAction() {
            @Override
            public void keyPressed() {

            }

            @Override
            public void keyUnpressed() {

            }
        });
    }

    private void sendPickupItemRequest(GroundItem groundItem) {
        val packetDispatcher = MoribundClient.getInstance().getPacketDispatcher();
        val pickupItemPacket = new PickupItemPacket(gameId, playerId, groundItem.getItemType().getId(), groundItem.getX(), groundItem.getY());
        packetDispatcher.sendTCP(pickupItemPacket);
    }

    private GroundItem getPickableObjectNearest() {
        for (GroundItem groundItem : MoribundClient.getInstance().getGroundItems()) {
            if (groundItem.isTouching(sprite.getBoundingRectangle())) {
                return groundItem;
            }
        }
        return null;
    }

    @Override
    public float getX() {
        return sprite.getX();
    }

    @Override
    public float getY() {
        return sprite.getY();
    }

    @Override
    public void setX(float x) {
        sprite.setX(x);
        polygon.setPosition(x, getY());
    }

    @Override
    public void setY(float y) {
        sprite.setY(y);
        polygon.setPosition(getX(), y);
    }

    @Override
    public void draw(SpriteBatch spriteBatch) {
        if (currentAnimation != null) {
            currentAnimation.drawAnimation(spriteBatch, sprite);
            if (currentAnimation.isFinished()) {
                currentAnimation.end();
                currentAnimation = null;
            }
        } else {
            sprite.draw(spriteBatch);
        }
        drawUniversalHealthBar(spriteBatch);
    }

    private void drawUniversalHealthBar(SpriteBatch spriteBatch) {
        val hitpointsPercentage = hitpoints / (double) maxHitpoints;
        val biggestLength = Math.max(sprite.getHeight(), sprite.getWidth());
        spriteBatch.draw(GLUtils.getRedTexture(), getX() + 3, getY() + biggestLength + 10, biggestLength - 20, 10);
        spriteBatch.draw(GLUtils.getGreenTexture(), getX() + 3, getY() + biggestLength + 10, (int) ((biggestLength - 20) * hitpointsPercentage), 10);
    }

    @Override
    public void updateAppearance() {
        for (Item item : equipment.getItems()) {
            val equippedItemType = EquippedItemType.getItemType(item.getItemType().getId());
            if (equippedItemType != null) {
                changeCharacter(equippedItemType.getSpriteFile(), equippedItemType.getSpriteVertices());
                break;
            }
        }
    }

    @Override
    public void animateThenLaunch(Animation animation, ProjectileType projectileType, int movementSpeed) {
        currentAnimation = AnimationContainer.getInstance().getAnimation(animation.getFile());
        val projectile = Projectile.builder()
                .type(projectileType)
                .withMovementSpeed(movementSpeed)
                .ignoring(this)
                .create();
        currentAnimation.whenEnded(() -> {
            projectile.setX(getX());
            projectile.setY(getY());
            projectile.setRotation(getRotation());
            Projectile.launchProjectile(projectile);
        });
    }

    @Override
    public void rotateLeft() {
        sprite.rotate(ROTATION_SPEED);
        polygon.rotate(ROTATION_SPEED);
    }

    @Override
    public void rotateRight() {
        sprite.rotate(-ROTATION_SPEED);
        polygon.rotate(-ROTATION_SPEED);
    }

    @Override
    public void moveForward() {
        val angle = sprite.getRotation();
        val xVelocity = getXVelocity(false, angle);
        val yVelocity = getYVelocity(false, angle);

        sprite.translate(xVelocity, yVelocity);
        polygon.translate(xVelocity, yVelocity);
    }

    @Override
    public void moveBack() {
        val angle = sprite.getRotation();
        val xVelocity = getXVelocity(true, angle);
        val yVelocity = getYVelocity(true, angle);

        sprite.translate(xVelocity, yVelocity);
        polygon.translate(xVelocity, yVelocity);
    }

    private float getXVelocity(boolean back, float angle) {
        val xVelocity = (back ? -1 : 1) * MOVEMENT_SPEED * MathUtils.cosDeg(angle);
        val xBound = SpriteContainer.getInstance().getSprite(SpriteFile.BACKGROUND).getWidth() / 2;
        return getVelocityWithLimitations(xVelocity, getX(), xBound, -xBound);
    }

    private float getYVelocity(boolean back, float angle) {
        val yVelocity = (back ? -1 : 1) * MOVEMENT_SPEED * MathUtils.sinDeg(angle);
        val yBound = SpriteContainer.getInstance().getSprite(SpriteFile.BACKGROUND).getHeight() / 2;
        return getVelocityWithLimitations(yVelocity, getY(), yBound, -yBound);
    }

    private float getVelocityWithLimitations(float velocity, float dependentVariable, float upperBound, float lowerBound) {
        val balancingConstant = 50;
        if (velocity < 0 && dependentVariable <= lowerBound) {
            velocity = 0;
        } else if (velocity > 0 && dependentVariable >= upperBound - balancingConstant) {
            velocity = 0;
        }
        return velocity;
    }

    @Override
    public Int2ObjectMap<PlayerAction> getKeyBinds() {
        if (keyBinds == null) {
            keyBinds = new Int2ObjectOpenHashMap<>();
            bindKeys();
        }
        return keyBinds;
    }

    @Override
    public void keyPressed(int keyPressed) {
        getKeyBinds().get(keyPressed).keyPressed();
    }

    @Override
    public void keyUnpressed(int keyUnpressed) {
        getKeyBinds().get(keyUnpressed).keyUnpressed();
    }

    @Override
    public boolean keyDown(int keycode) {
        if (getKeyBinds().containsKey(keycode)) {
            val player = MoribundClient.getInstance().getPlayer();
            val packetDispatcher = MoribundClient.getInstance().getPacketDispatcher();
            val keyPressedPacket = new KeyPressedPacket(player.getGameId(), player.getPlayerId(), keycode);
            packetDispatcher.sendTCP(keyPressedPacket);
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (getKeyBinds().containsKey(keycode)) {
            val player = MoribundClient.getInstance().getPlayer();
            val packetDispatcher = MoribundClient.getInstance().getPacketDispatcher();
            val keyUnpressedPacket = new KeyUnpressedPacket(player.getGameId(), player.getPlayerId(), keycode);
            packetDispatcher.sendTCP(keyUnpressedPacket);
        }
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (screenX >= 374 && screenX <= 849 && screenY >= 673 && screenY <= 768) {
            inventory.click(this, screenX);
        } else {
            val mouseClickedPacket = new MouseClickedPacket(gameId, playerId);
            MoribundClient.getInstance().getPacketDispatcher().sendTCP(mouseClickedPacket);
        }
        return true;
    }

    @Override
    public void equipItem(int inventorySlot) {
        val equipItemPacket = new EquipItemPacket(gameId, playerId, inventorySlot);
        MoribundClient.getInstance().getPacketDispatcher().sendTCP(equipItemPacket);
    }

    @Override
    public void collide(Projectile projectile) {
        projectile.removeProjectile();
        MoribundClient.getInstance().getPacketDispatcher().sendTCP(
                new ProjectileCollisionPacket(gameId, playerId, projectile.getProjectileType().getId()));
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}