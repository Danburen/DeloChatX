package org.nitrowater.velochatx.domain.broadcast;

import org.nitrowater.velochatx.domain.channel.Channel;
import org.nitrowater.velochatx.domain.channel.ChannelService;
import org.nitrowater.velochatx.domain.server.ServerService;
import org.nitrowater.waterapi.domain.model.UServer;
import org.nitrowater.waterapi.domain.model.UPlayer;
import org.nitrowater.waterapi.domain.model.WProxyServer;
import org.nitrowater.waterapi.domain.placeholder.PlaceholderContext;
import org.nitrowater.waterapi.domain.placeholder.PlaceholderService;
import org.nitrowater.waterapi.domain.spi.PlayerActionService;
import org.nitrowater.waterapi.domain.shared.config.Config;
import org.nitrowater.waterapi.domain.shared.service.EventListener;
import org.nitrowater.waterapi.domain.kernel.application.KernelConfigUpdateEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementation of {@link BroadcastService}.
 * <p>
 * This service manages broadcast messages using {@link BroadcastModel}.
 * It reads configuration from {@code broadcast.yml}.
 * </p>
 *
 * @since 2.1.0
 * @author Danburen
 */
public class BroadcastServiceImpl implements BroadcastService, EventListener<KernelConfigUpdateEvent> {

    private final Config broadcastConfig;
    private final ServerService serverService;
    private final ChannelService channelService;
    private final WProxyServer wProxyServer;
    private final PlaceholderService placeholderService;
    private final PlayerActionService actionService;

    private volatile BroadcastModel globalBroadcast;
    private final List<BroadcastModel> localeBroadcasts = new ArrayList<>();
    private final Map<String, List<BroadcastModel>> serverBroadcasts = new ConcurrentHashMap<>();

    /**
     * Create a new BroadcastServiceImpl.
     *
     * @param broadcastConfig  the broadcast config (from broadcast.yml)
     * @param serverService    the server service for server lookups
     * @param channelService   the channel service for channel lookups
     * @param wProxyServer     the proxy server wrapper
     * @param placeholderService the placeholder service for message resolution
     * @param actionService    the player action service for sending messages
     */
    public BroadcastServiceImpl(Config broadcastConfig, ServerService serverService,
                                ChannelService channelService, WProxyServer wProxyServer,
                                PlaceholderService placeholderService, PlayerActionService actionService) {
        this.broadcastConfig = broadcastConfig;
        this.serverService = serverService;
        this.channelService = channelService;
        this.wProxyServer = wProxyServer;
        this.placeholderService = placeholderService;
        this.actionService = actionService;
        loadBroadcasts();
    }

    @Override
    public Config getConfig() {
        return broadcastConfig;
    }

    @Override
    public void onEvent(KernelConfigUpdateEvent event) {
        loadBroadcasts();
    }

    @Override
    public Optional<BroadcastModel> getGlobalBroadcast() {
        return Optional.ofNullable(globalBroadcast);
    }

    @Override
    public List<BroadcastModel> getLocaleBroadcasts() {
        return List.copyOf(localeBroadcasts);
    }

    @Override
    public Optional<BroadcastModel> getLocaleBroadcast(String name) {
        return localeBroadcasts.stream()
                .filter(bct -> bct.getName().equals(name))
                .findFirst();
    }

    @Override
    public List<BroadcastModel> getBroadcastsForServer(String serverName) {
        return serverBroadcasts.getOrDefault(serverName, List.of());
    }

    @Override
    public List<String> getMessages(String serverName) {
        return serverBroadcasts.getOrDefault(serverName, List.of()).stream()
                .flatMap(bct -> bct.getMessages().stream())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public String getPrefix(String serverName) {
        List<BroadcastModel> broadcasts = serverBroadcasts.getOrDefault(serverName, List.of());
        return broadcasts.isEmpty() ? "" : broadcasts.get(0).getPrefix();
    }

    @Override
    public int getMessageCount(String serverName) {
        return getMessages(serverName).size();
    }

    @Override
    public void broadcastPlayerJoin(UPlayer player, UServer currentServer) {
        broadcastPlayerJoin(player, currentServer, null);
    }

    @Override
    public void broadcastPlayerJoin(UPlayer player, UServer currentServer, UServer previousServer) {
        boolean isProxyJoin = (previousServer == null);
        String currentChannelName = channelService.getChannelForServer(currentServer.getName()).getName();
        String previousChannelName = previousServer != null ?
                channelService.getChannelForServer(previousServer.getName()).getName() : null;
        boolean isChannelSwitch = previousServer != null && !currentChannelName.equals(previousChannelName);

        boolean overlay = getConfig().getBoolean(BroadcastConfigKey.ENTRY_HIERARCHICAL_OVERLAY);

        // Level 1 (highest): Player joins proxy
        if (isProxyJoin) {
            if (!getConfig().getBoolean(BroadcastConfigKey.PROXY_ENABLED)) return;
            String message = getConfig().get(BroadcastConfigKey.PROXY_JOIN_MESSAGE);
            String resolved = resolveMessage(message, player);
            logToConsole(resolved);
            broadcastByScope(resolved, getConfig().get(BroadcastConfigKey.PROXY_SCOPE), player, null);
            if (overlay) return;
        }
        // Level 2 (middle): Player switches channel
        if (isChannelSwitch) {
            if (!getConfig().getBoolean(BroadcastConfigKey.CHANNEL_ENABLED)) return;
            String message = getConfig().get(BroadcastConfigKey.CHANNEL_JOIN_MESSAGE);
            String resolved = resolveMessage(message, player);
            logToConsole(resolved);
            broadcastByScope(resolved, getConfig().get(BroadcastConfigKey.CHANNEL_SCOPE), player, previousChannelName);
            if (overlay) return;
        }
        // Level 3 (lowest): Player joins backend
        if (!getConfig().getBoolean(BroadcastConfigKey.BACKEND_ENABLED)) return;
        String message = getConfig().get(BroadcastConfigKey.BACKEND_JOIN_MESSAGE);
        String resolved = resolveMessage(message, player);
        logToConsole(resolved);
        broadcastByScope(resolved, getConfig().get(BroadcastConfigKey.BACKEND_SCOPE), player, previousChannelName);
    }

    @Override
    public void broadcastPlayerProxyLeave(UPlayer player) {
        broadcastProxyLeave(player);
    }

    @Override
    public void broadcastPlayerLeave(UPlayer player, UServer previousServer) {
        String currentServerName = player.getCurrentServer().map(UServer::getName).orElse(null);
        boolean isProxyLeave = (currentServerName == null);
        String previousChannelName = channelService.getChannelForServer(previousServer.getName()).getName();
        String currentChannelName = currentServerName != null ?
                channelService.getChannelForServer(currentServerName).getName() : null;
        boolean isChannelSwitch = currentServerName != null && !previousChannelName.equals(currentChannelName);

        boolean overlay = getConfig().getBoolean(BroadcastConfigKey.ENTRY_HIERARCHICAL_OVERLAY);
        // Level 1 (highest): Player leaves proxy
        if (isProxyLeave) {
            broadcastProxyLeave(player);
            if (overlay) return;
        }
        // Level 2 (middle): Player switches channel
        if (isChannelSwitch) {
            if (!getConfig().getBoolean(BroadcastConfigKey.CHANNEL_ENABLED)) return;
            String message = getConfig().get(BroadcastConfigKey.CHANNEL_LEAVE_MESSAGE);
            String resolved = resolveMessage(message, player);
            logToConsole(resolved);
            broadcastByScope(resolved, getConfig().get(BroadcastConfigKey.CHANNEL_SCOPE), player, previousChannelName);
            if (overlay) return;
        }
        // Level 3 (lowest): Player leaves backend
        if (!getConfig().getBoolean(BroadcastConfigKey.BACKEND_ENABLED)) return;
        String message = getConfig().get(BroadcastConfigKey.BACKEND_LEAVE_MESSAGE);
        String resolved = resolveMessage(message, player);
        logToConsole(resolved);
        broadcastByScope(resolved, getConfig().get(BroadcastConfigKey.BACKEND_SCOPE), player, previousChannelName);
    }

    private void broadcastProxyLeave(UPlayer player) {
        if (!getConfig().getBoolean(BroadcastConfigKey.PROXY_ENABLED)) return;
        String message = getConfig().get(BroadcastConfigKey.PROXY_LEAVE_MESSAGE);
        String resolved = resolveMessage(message, player);
        logToConsole(resolved);
        broadcastByScope(resolved, getConfig().get(BroadcastConfigKey.PROXY_SCOPE), player, null);
    }

    private void broadcastByScope(String message, String scope, UPlayer player, String channelName) {
        switch (scope) {
            case "proxy":
                actionService.broadcastToAll(message);
                break;
            case "channel":
                if (channelName != null) {
                    broadcastToChannel(channelName, message);
                } else {
                    // Fallback: broadcast to current server's channel
                    player.getCurrentServer().ifPresent(server -> {
                        String channel = channelService.getChannelForServer(server.getName()).getName();
                        broadcastToChannel(channel, message);
                    });
                }
                break;
            case "server":
            default:
                // Broadcast to current server only
                player.getCurrentServer().ifPresent(server -> {
                    actionService.broadcastToServer(server, message);
                });
                break;
        }
    }

    private void broadcastToChannel(String channelName, String message) {
        channelService.getChannel(channelName).ifPresent(channel -> {
            channel.getServers().forEach(server -> {
                actionService.broadcastToServer(server, message);
            });
        });
    }

    private String resolveMessage(String template, UPlayer player) {
        PlaceholderContext context = PlaceholderContext.builder()
                .player(player)
                .build();
        return placeholderService.resolve(template, context);
    }

    private void logToConsole(String message) {
        if (getConfig().get(BroadcastConfigKey.ENTRY_LOG_TO_CONSOLE)) {
            System.out.println("[Broadcast] " + message);
        }
    }

    private void loadBroadcasts() {
        globalBroadcast = null;
        localeBroadcasts.clear();
        serverBroadcasts.clear();

        boolean globalEnabled = broadcastConfig.get(BroadcastConfigKey.GLOBAL_ENABLED);
        boolean localeEnabled = broadcastConfig.get(BroadcastConfigKey.LOCALE_ENABLED);

        if (globalEnabled) {
            String globalPrefix = broadcastConfig.get(BroadcastConfigKey.GLOBAL_PREFIX);
            List<String> globalMessages = broadcastConfig.get("global.message-list", List.of());

            globalBroadcast = new BroadcastModel(
                    "global",
                    globalPrefix,
                    globalMessages,
                    channelService.getAllChannels(),
                    serverService.getAllServers()
            );
        }

        if (localeEnabled) {
            Map<String, Object> broadcastMap = broadcastConfig.get("locale.broadcast-list", Map.of());
            broadcastMap.forEach((key, value) -> {
                String pathPrefix = "locale.broadcast-list." + key;
                boolean enabled = broadcastConfig.get(pathPrefix + ".enable", false);
                if (enabled) {
                    String prefix = broadcastConfig.get(pathPrefix + ".prefix", "");
                    List<String> messageList = broadcastConfig.get(pathPrefix + ".message-list", List.of());
                    List<String> serverNames = broadcastConfig.get(pathPrefix + ".server-list", List.of());
                    List<String> channelNames = broadcastConfig.get(pathPrefix + ".channel-list", List.of());

                    List<UServer> servers = serverNames.stream()
                            .map(serverService::getServer)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());

                    List<Channel> channels = channelNames.stream()
                            .map(channelService::getChannel)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());

                    BroadcastModel localeBct = new BroadcastModel(key, prefix, messageList, channels, servers);
                    localeBroadcasts.add(localeBct);
                }
            });
        }
        buildServerBroadcastsMap();
    }

    private void buildServerBroadcastsMap() {
        serverService.getAllServers().forEach(server ->
                serverBroadcasts.put(server.getName(), new ArrayList<>())
        );
        if (globalBroadcast != null) {
            serverService.getAllServers().forEach(server ->
                    serverBroadcasts.get(server.getName()).add(globalBroadcast)
            );
        }
        localeBroadcasts.forEach(bct ->
                bct.getServers().forEach(server ->
                        serverBroadcasts.computeIfAbsent(server.getName(), k -> new ArrayList<>())
                                .add(bct)
                )
        );
    }
}
