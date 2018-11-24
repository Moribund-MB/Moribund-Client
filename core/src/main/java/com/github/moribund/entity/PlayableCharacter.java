package com.github.moribund.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.Map;

public interface PlayableCharacter {
    void moveUp();
    void moveDown();
    void moveLeft();
    void moveRight();
    Map<Integer, Runnable> getKeyBinds();
    void bindKeys();
    Tile getCurrentTile();
    void setTile(Tile tile);
    void draw(SpriteBatch spriteBatch);
    int getPlayerId();
}