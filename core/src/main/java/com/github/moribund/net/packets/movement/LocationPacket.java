package com.github.moribund.net.packets.movement;

import com.github.moribund.MoribundClient;
import com.github.moribund.net.packets.IncomingPacket;
import com.github.moribund.net.packets.OutgoingPacket;
import lombok.val;

/**
 * The {@code LocationPacket} sends the location of a given player to update the server.
 * This packet is sent every LibGDX game cycle ({@link com.badlogic.gdx.Screen#render(float)})
 * and is constantly supplying the server where the player currently is for as long as a
 * moving {@link com.github.moribund.objects.flags.Flag} is active. This is an
 * easy-to-manipulate packet should the client be decompiled and abused, however.
 */
public final class LocationPacket implements IncomingPacket, OutgoingPacket {
    /**
     * The player ID of the player that is at the given tile.
     */
    private final int playerId;
    /**
     * The x location of the player.
     */
    private final float x;
    /**
     * The y location of the player.
     */
    private final float y;

    public LocationPacket(int playerId, float x, float y) {
        this.playerId = playerId;
        this.x = x;
        this.y = y;
    }

    LocationPacket() {
        playerId = -1;
        x = 0;
        y = 0;
    }

    @Override
    public void process() {
        val player = MoribundClient.getInstance().getPlayers().get(playerId);
        player.setX(x);
        player.setY(y);
    }
}