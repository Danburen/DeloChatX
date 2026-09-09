package org.nitrowater.velochatx.domain.player;

import org.nitrowater.waterapi.domain.model.UPlayer;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for managing player data and attributes.
 * <p>
 * This service handles player CRUD operations, player attributes (ignore/reject lists),
 * and provides methods for player lookup and management.
 * </p>
 *
 * @since 2.1.0
 * @author Danburen
 * @see PlayerServiceImpl
 * @see PlayerConfigKey
 */
public interface PlayerService {

    /**
     * Store or update player record in database.
     *
     * @param player the player to store or update
     * @return {@code true} if player already existed, {@code false} if newly created
     */
    boolean storeOrUpdatePlayer(UPlayer player);

    /**
     * Get player's attribution (ignore/reject lists, chat offline status).
     *
     * @param uuid the player's UUID
     * @return player attribution, or empty if not found
     */
    Optional<PlayerAttribution> getPlayerAttribution(UUID uuid);

    /**
     * Update player's attribution (ignore/reject lists, chat offline status).
     *
     * @param uuid        the player's UUID
     * @param attribution the new attribution data
     */
    void updatePlayerAttribution(UUID uuid, PlayerAttribution attribution);

    /**
     * Update player's first join time.
     *
     * @param uuid the player's UUID
     * @param time join time in milliseconds
     */
    void updateFirstJoinTime(UUID uuid, long time);

    /**
     * Update player's last leave time.
     *
     * @param uuid the player's UUID
     * @param time leave time in milliseconds
     */
    void updateLeaveTime(UUID uuid, long time);

    /**
     * Get player's display name for chat.
     *
     * @param player the player
     * @return display name (may include prefix/suffix)
     */
    String getDisplayName(UPlayer player);

    /**
     * Get all online players' attributions.
     *
     * @return map of UUID to attribution
     */
    Map<UUID, PlayerAttribution> getOnlinePlayerAttributions();

    /**
     * Get a player attr
     * @param uuid  {@link UUID} player uuid
     * @return {@link PlayerAttribution} player attr
     */
    PlayerAttribution getOrCreateAttrs(UUID uuid);
}
