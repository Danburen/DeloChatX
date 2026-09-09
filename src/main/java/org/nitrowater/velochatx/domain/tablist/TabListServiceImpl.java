package org.nitrowater.velochatx.domain.tablist;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.nitrowater.waterapi.domain.shared.config.Config;
import org.nitrowater.waterapi.domain.shared.service.EventListener;
import org.nitrowater.waterapi.domain.shared.service.SchedulerService;
import org.nitrowater.waterapi.domain.kernel.application.KernelConfigUpdateEvent;
import org.nitrowater.waterapi.domain.model.UPlayer;
import org.nitrowater.waterapi.domain.model.WProxyServer;
import org.nitrowater.waterapi.domain.placeholder.PlaceholderContext;
import org.nitrowater.waterapi.domain.placeholder.PlaceholderService;
import org.nitrowater.waterapi.domain.spi.TabListProvider;
import org.nitrowater.waterapi.utils.StringUtil;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of {@link TabListService}.
 * <p>
 * This service handles tab list header, footer, and player entry formatting
 * using the platform-agnostic {@link TabListProvider} interface.
 * </p>
 * <p>
 * Supports three modes of operation:
 * <ul>
 *   <li>Event-driven (interval=0): Updates are processed immediately, with periodic full refresh as fallback</li>
 *   <li>Fixed frequency (interval>0, ratio=0): Updates are processed at a fixed interval</li>
 *   <li>Dynamic frequency (interval>0, ratio>0): Refresh interval adjusts based on player count</li>
 * </ul>
 * </p>
 *
 * <p>
 * We use producer-consumer model to process tab-list update to decrease the number of updates sent to the client.
 * The update queue is a concurrent queue that stores the update tasks.
 * The consumer task processes the queue at a configured interval, applying only the latest update for each player
 * to avoid redundant updates.
 * </p>
 *
 * @since 2.1.0
 * @author Danburen
 */
public class TabListServiceImpl implements TabListService, EventListener<KernelConfigUpdateEvent> {

    private volatile Config config;
    private final WProxyServer wProxyServer;
    private final PlaceholderService placeholderService;
    private final TabListProvider tabListProvider;
    private final SchedulerService scheduler;

    private final Queue<TabListUpdateTask> updateQueue = new ConcurrentLinkedQueue<>();
    private volatile SchedulerService.TaskHandle consumerHandle;
    private volatile SchedulerService.TaskHandle fullRefreshHandle;
    @Getter
    private volatile long currentRefreshInterval;
    private volatile boolean consumerRunning = false;

    /**
     * Create a new TabListServiceImpl.
     *
     * @param config             the config to read from
     * @param wProxyServer       the WProxyServer wrapper
     * @param placeholderService the placeholder service for template resolution
     * @param tabListProvider    the platform-specific TabList provider
     * @param scheduler          the scheduler service for task scheduling
     */
    public TabListServiceImpl(Config config, WProxyServer wProxyServer, PlaceholderService placeholderService,
                              TabListProvider tabListProvider, SchedulerService scheduler) {
        this.config = config;
        this.wProxyServer = wProxyServer;
        this.placeholderService = placeholderService;
        this.tabListProvider = tabListProvider;
        this.scheduler = scheduler;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public void onEvent(KernelConfigUpdateEvent event) {
        this.config = event.getConfig();
        // Restart consumer with new config
        if (consumerRunning) {
            stopConsumer();
            startConsumer();
        }
    }

    @Override
    public void updateHeaderAndFooter(UPlayer player) {
        if (!isEnabled()) return;
        String headerFormat = getConfig().get(TabListConfigKey.HEADER);
        String footerFormat = getConfig().get(TabListConfigKey.FOOTER);
        if (StringUtil.anyIsBlank(headerFormat, footerFormat)) return;

        PlaceholderContext context = PlaceholderContext.builder()
                .player(player)
                .build();
        Component header = Component.text(placeholderService.resolve(headerFormat, context));
        Component footer = Component.text(placeholderService.resolve(footerFormat, context));
        tabListProvider.sendHeaderAndFooter(player, header, footer);
    }

    @Override
    public void updateTabListEntry(UPlayer player) {
        if (!isEnabled()) return;

        String format = getConfig().get(TabListConfigKey.FORMAT);
        PlaceholderContext context = PlaceholderContext.builder()
                .player(player)
                .build();

        Component displayName = Component.text(placeholderService.resolve(format, context));

        tabListProvider.updateDisplayName(player, displayName);
    }

    @Override
    public void addPlayerToTabList(UPlayer sourcePlayer, UPlayer targetPlayer) {
        if (!isEnabled()) return;

        String format = getConfig().get(TabListConfigKey.FORMAT);
        PlaceholderContext context = PlaceholderContext.builder()
                .player(sourcePlayer)
                .build();

        Component displayName = Component.text(placeholderService.resolve(format, context));

        tabListProvider.addEntry(sourcePlayer, targetPlayer, displayName);
    }

    @Override
    public void removePlayerFromTabList(UPlayer sourcePlayer, UPlayer targetPlayer) {
        if (!isEnabled()) return;

        tabListProvider.removeEntry(sourcePlayer, targetPlayer);
    }

    @Override
    public void refreshAllTabLists() {
        if (!isEnabled()) return;

        wProxyServer.getAllOnlinePlayers().forEach(player -> {
            updateHeaderAndFooter(player);
            updateTabListEntry(player);
        });
    }
    @Override
    public void queueUpdate(TabListUpdateTask task) {
        // offsetting the opposite operation and retaining only the final state
        UUID playerUuid = task.getPlayerUuid();
        if (task.getType() == TabListUpdateTask.UpdateType.REMOVE) {
            updateQueue.removeIf(t -> 
                t.getPlayerUuid().equals(playerUuid) && 
                t.getType() == TabListUpdateTask.UpdateType.ADD
            );
        } else if (task.getType() == TabListUpdateTask.UpdateType.ADD) {
            updateQueue.removeIf(t -> 
                t.getPlayerUuid().equals(playerUuid) && 
                t.getType() == TabListUpdateTask.UpdateType.REMOVE
            );
        } else {
            updateQueue.removeIf(t -> 
                t.getPlayerUuid().equals(playerUuid) && 
                t.getType() == task.getType()
            );
        }
        updateQueue.offer(task);
        // In event-driven mode (interval=0), process immediately
        long interval = getConfig().get(TabListConfigKey.INTERVAL);
        if (interval == 0) {
            processUpdates();
        }
    }

    @Override
    public void startConsumer() {
        if (consumerRunning) return;
        long interval = getConfig().get(TabListConfigKey.INTERVAL);
        double ratio = Math.max(0, Math.min(1, getConfig().get(TabListConfigKey.REFRESH_RATIO)));
        if (interval == 0) {
            // event-driven mode: only start periodic full refresh as fallback
            startFullRefreshTask();
            consumerRunning = true;
            return;
        }
        // start consumer task
        consumerHandle = adjustRefreshInterval(interval, ratio);
        if (ratio > 0) {
            scheduler.scheduleAtFixedRate(
                    () -> adjustRefreshInterval(interval, ratio),
                    10,
                    60,
                    TimeUnit.SECONDS);
        }
        startFullRefreshTask();
        consumerRunning = true;
    }

    @Override
    public void stopConsumer() {
        if (!consumerRunning) return;
        if (consumerHandle != null) {
            consumerHandle.cancel();
            consumerHandle = null;
        }
        if (fullRefreshHandle != null) {
            fullRefreshHandle.cancel();
            fullRefreshHandle = null;
        }
        updateQueue.clear();
        consumerRunning = false;
    }

    /**
     * Adjust the refresh interval based on current player count.
     *
     * @param baseInterval the base interval from config
     * @param ratio        the dynamic ratio from config
     */
    private SchedulerService.TaskHandle adjustRefreshInterval(long baseInterval, double ratio) {
        int playerCount = wProxyServer.getAllOnlinePlayers().size();
        long newInterval = (long) (baseInterval * (1 + (playerCount / 20.0) * ratio));
        if (newInterval != currentRefreshInterval) {
            currentRefreshInterval = newInterval;
            // Restart consumer with new interval
            if (consumerHandle != null) {
                consumerHandle.cancel();
            }
            consumerHandle = scheduler.scheduleAtFixedRate(
                    this::processUpdates,
                    0,
                    currentRefreshInterval,
                    TimeUnit.MILLISECONDS)
            ;
        }
        return consumerHandle;
    }

    /**
     * Start the periodic full refresh task (fallback for event-driven mode).
     */
    private void startFullRefreshTask() {
        // Full refresh every 5 minutes (300 seconds)
        fullRefreshHandle = scheduler.scheduleAtFixedRate(
                this::refreshAllTabLists,
                300,
                300,
                TimeUnit.SECONDS
        );
    }

    /**
     * Process all queued updates.
     */
    private void processUpdates() {
        if (updateQueue.isEmpty()) return;
        // Collect all updates
        List<TabListUpdateTask> tasks = new ArrayList<>();
        while (!updateQueue.isEmpty()) {
            tasks.add(updateQueue.poll());
        }
        // Deduplicate: keep only the latest update for each player
        Map<UUID, TabListUpdateTask> latestTasks = new LinkedHashMap<>();
        for (TabListUpdateTask task : tasks) {
            latestTasks.put(task.getPlayerUuid(), task);
        }
        // Apply updates
        latestTasks.values().forEach(this::applyUpdate);
    }

    /**
     * Apply a single update task.
     *
     * @param task the update task to apply
     */
    private void applyUpdate(TabListUpdateTask task) {
        wProxyServer.getPlayer(task.getPlayerUuid()).ifPresent(player -> {
            switch (task.getType()) {
                case HEADER_FOOTER:
                    updateHeaderAndFooter(player);
                    break;
                case ENTRY:
                    updateTabListEntry(player);
                    break;
                case ADD:
                    if (task.getTargetPlayerUuid() != null) {
                        wProxyServer.getPlayer(task.getTargetPlayerUuid()).ifPresent(target -> {
                            addPlayerToTabList(player, target);
                        });
                    }
                    break;
                case REMOVE:
                    if (task.getTargetPlayerUuid() != null) {
                        wProxyServer.getPlayer(task.getTargetPlayerUuid()).ifPresent(target -> {
                            removePlayerFromTabList(player, target);
                        });
                    }
                    break;
            }
        });
    }

    /**
     * Check if tab list is enabled in config.
     *
     * @return true if enabled
     */
    private boolean isEnabled() {
        return getConfig().get(TabListConfigKey.ENABLED);
    }
}
