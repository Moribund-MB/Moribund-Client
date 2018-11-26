package com.github.moribund.net.packets;

import lombok.Getter;

/**
 * The {@link com.badlogic.gdx.Input.Keys} value pressed response back from
 * the server  to enact what to do when the key is released.
 */
public class KeyUnpressedResponsePacket implements Packet {
    /**
     * The unique player ID of who pressed the key.
     */
    @Getter
    private int playerId;
    /**
     * The {@link com.badlogic.gdx.Input.Keys} value pressed.
     */
    @Getter
    private int keyUnpressed;

    /**
     * A private constructor to ensure the client cannot unexpectedly send this
     * request to the server.
     */
    private KeyUnpressedResponsePacket() { }
}