package org.nitrowater.velochatx.domain.tablist;

import java.util.UUID;

/**
 * Represents a tab list update task.
 * <p>
 * This class encapsulates the data needed to perform a tab list update,
 * including the player UUID, update type, and timestamp.
 * </p>
 *
 * @since 2.1.0
 * @author Danburen
 */
public class TabListUpdateTask {

    private final UUID playerUuid;
    private final UUID targetPlayerUuid;
    private final UpdateType type;
    private final long timestamp;

    /**
     * Create a new TabListUpdateTask for single player operations.
     *
     * @param playerUuid the UUID of the player to update
     * @param type       the type of update
     * @param timestamp  the timestamp when the task was created
     */
    public TabListUpdateTask(UUID playerUuid, UpdateType type, long timestamp) {
        this(playerUuid, null, type, timestamp);
    }

    /**
     * Create a new TabListUpdateTask for two player operations (ADD/REMOVE).
     *
     * @param playerUuid       the UUID of the source player
     * @param targetPlayerUuid the UUID of the target player (whose tab list to modify)
     * @param type             the type of update
     * @param timestamp        the timestamp when the task was created
     */
    public TabListUpdateTask(UUID playerUuid, UUID targetPlayerUuid, UpdateType type, long timestamp) {
        this.playerUuid = playerUuid;
        this.targetPlayerUuid = targetPlayerUuid;
        this.type = type;
        this.timestamp = timestamp;
    }

    /**
     * Get the player UUID.
     *
     * @return the player UUID
     */
    public UUID getPlayerUuid() {
        return playerUuid;
    }

    /**
     * Get the target player UUID.
     *
     * @return the target player UUID, or null for single player operations
     */
    public UUID getTargetPlayerUuid() {
        return targetPlayerUuid;
    }

    /**
     * Get the update type.
     *
     * @return the update type
     */
    public UpdateType getType() {
        return type;
    }

    /**
     * Get the timestamp.
     *
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Types of tab list updates.
     */
    public enum UpdateType {
        /** Update header and footer for a player */
        HEADER_FOOTER,
        /** Update a player's tab list entry display name */
        ENTRY,
        /** Add a player to another player's tab list */
        ADD,
        /** Remove a player from another player's tab list */
        REMOVE
    }
}
