package org.nitrowater.velochatx.domain.event.player;

import lombok.Getter;
import org.nitrowater.waterapi.domain.event.GameEvent;
import org.nitrowater.waterapi.domain.model.UPlayer;

import java.util.Optional;

/**
 * Player join server event.
 * Fired when a player connects to a backend server.
 * <p>
 * This is a Velocity-specific event.
 * </p>
 */
@Getter
public final class PlayerJoinServerEvent implements GameEvent {

    private final UPlayer player;
    private final String serverName;
    private final String previousServerName;

    public PlayerJoinServerEvent(UPlayer player, String serverName, String previousServerName) {
        this.player = player;
        this.serverName = serverName;
        this.previousServerName = previousServerName;
    }
}
