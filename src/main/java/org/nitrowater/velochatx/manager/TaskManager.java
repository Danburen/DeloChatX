package org.nitrowater.velochatx.manager;

import net.kyori.adventure.text.Component;
import org.nitrowater.waterapi.domain.shared.service.SchedulerService;
import org.nitrowater.waterapi.plugin.velocity.util.ScheduledManager;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * A Class to holder Tasks
 * @since 2.0.0
 * @author Danburen
 */
@Deprecated
public class TaskManager extends BasicMethods {
    private static final Random random;
    static{
        random = new Random();
    }
    public static void initTask(SchedulerService scheduler){
        ScheduledManager scheduledManager = new ScheduledManager(scheduler);
        scheduledManager.cancelAllTasks();

        // Player's tab list update task
        if(TabListManager.isTabListEnable()) tabListUpdateTask(scheduledManager);
        // Broadcast task
        if(ChannelManager.isEnabled() && (ChannelManager.isGlobalEnable() || ChannelManager.isLocalEnable())) {
            broadcastMessageTask(scheduledManager);
        }
    }

    private static void tabListUpdateTask(ScheduledManager scheduledManager){
        scheduledManager.addRepeatTask("update-tab-list", () ->
                        proxyServer.getAllPlayers().forEach(TabListManager::updateHeadAndFooter),
                getConfigs().get("tab-list.interval",1000L),
                TimeUnit.MILLISECONDS);
        scheduledManager.addRepeatTask("update-tab-list-utils",() ->{
            proxyServer.getAllPlayers().forEach(TabListManager::updateTabListUtils);
        },5L,TimeUnit.SECONDS);
    }
    /**
     * Broadcast Single message to the player which in <b>target server</b>
     * <p></p>
     * @param message <b>raw</b> message to broadcast
     * @param targetServers target servers which allow to broadcast
     */
    public static void broadcastMessage(String message, Set<String> targetServers){
        proxyServer.getAllPlayers().forEach(player -> {
            String playerServerName = player.getCurrentServer()
                    .map(serverConnection -> serverConnection.getServerInfo().getName())
                    .orElse("");
            if(targetServers.contains(playerServerName)){
                player.sendMessage(Component.text(message));
            }
        });
    }

    private static void broadcastMessageTask(ScheduledManager scheduledManager){
        scheduledManager.addRepeatTask("broadcast", ()->{
            proxyServer.getAllServers().forEach(server -> {
                String serverName = server.getServerInfo().getName();
                List<String> msgList = ChannelManager.getMessages(serverName);
                if(msgList == null || msgList.isEmpty()) return;
                String message = placeServerValue(new StringBuilder(msgList
                        .get(random.nextInt( ChannelManager.getMessageCount(serverName)))),serverName).toString();
                server.sendMessage(Component.text(ChannelManager.getMsgPrefix(serverName) + message));
            });
        }, ChannelManager.getBroadcastConfigs().get("interval",60L), TimeUnit.SECONDS);
    }
}
