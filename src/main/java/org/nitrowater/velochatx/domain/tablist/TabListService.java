package org.nitrowater.velochatx.domain.tablist;

import org.nitrowater.waterapi.domain.shared.service.ConfigurableService;
import org.nitrowater.waterapi.domain.model.UPlayer;

/**
 * Service interface for tab list customization.
 * <p>
 * Handles tab list header, footer, and player entry formatting.
 * Supports both event-driven and scheduled refresh modes.
 * </p>
 *
 * @since 2.1.0
 * @author Danburen
 * @see TabListConfigKey
 * @see TabListUpdateTask
 */
public interface TabListService extends ConfigurableService<TabListConfigKey> {

    /**
     * Update the tab list header and footer for a player.
     *
     * @param player the player to update
     */
    void updateHeaderAndFooter(UPlayer player);

    /**
     * Update a player's tab list entry display name.
     *
     * @param player the player whose entry to update
     */
    void updateTabListEntry(UPlayer player);

    /**
     * Add a player to another player's tab list.
     *
     * @param sourcePlayer the player to add
     * @param targetPlayer the player whose tab list to modify
     */
    void addPlayerToTabList(UPlayer sourcePlayer, UPlayer targetPlayer);

    /**
     * Remove a player from another player's tab list.
     *
     * @param sourcePlayer the player to remove
     * @param targetPlayer the player whose tab list to modify
     */
    void removePlayerFromTabList(UPlayer sourcePlayer, UPlayer targetPlayer);

    /**
     * Refresh all players' tab lists.
     * This is a full refresh operation, typically used as a fallback
     * to ensure data consistency.
     */
    void refreshAllTabLists();

    /**
     * Queue a tab list update task for processing.
     * <p>
     * The task will be processed either by the scheduled consumer
     * or immediately in event-driven mode.
     * </p>
     *
     * @param task the update task to queue
     */
    void queueUpdate(TabListUpdateTask task);

    /**
     * Start the tab list update consumer.
     * <p>
     * This method starts the scheduled task that processes queued updates.
     * The behavior depends on the configuration:
     * <ul>
     *   <li>interval=0: Event-driven mode with periodic full refresh</li>
     *   <li>interval>0, ratio=0: Fixed frequency refresh</li>
     *   <li>interval>0, ratio>0: Dynamic frequency refresh</li>
     * </ul>
     * </p>
     */
    void startConsumer();

    /**
     * Stop the tab list update consumer.
     * <p>
     * This method stops the scheduled task and clears the update queue.
     * </p>
     */
    void stopConsumer();
}
