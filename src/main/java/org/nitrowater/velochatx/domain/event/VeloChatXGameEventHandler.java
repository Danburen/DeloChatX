package org.nitrowater.velochatx.domain.event;

import org.nitrowater.velochatx.domain.event.player.PlayerJoinServerEvent;
import org.nitrowater.waterapi.domain.event.GameEventHandler;

/**
 * VeloChatX game event handler.
 * Extends {@link org.nitrowater.waterapi.domain.event.GameEventHandler}
 * with Velocity-specific events.
 */
public interface VeloChatXGameEventHandler extends GameEventHandler {

    /**
     * Handle player join server event.
     *
     * @param event the join server event
     */
    default void onJoinServer(PlayerJoinServerEvent event) {
        // do nothing by default
    }
}
