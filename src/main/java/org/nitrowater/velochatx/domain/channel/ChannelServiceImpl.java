package org.nitrowater.velochatx.domain.channel;

import org.nitrowater.velochatx.domain.server.ServerService;
import org.nitrowater.velochatx.domain.shared.VeloChatXMetaInfoType;
import org.nitrowater.waterapi.domain.model.UServer;
import org.nitrowater.waterapi.domain.shared.config.Config;
import org.nitrowater.waterapi.domain.shared.service.EventListener;
import org.nitrowater.waterapi.domain.kernel.application.KernelConfigUpdateEvent;
import org.nitrowater.waterapi.domain.placeholder.PlaceholderService;
import org.nitrowater.waterapi.domain.placeholder.PlaceholdersContributor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of {@link ChannelService}.
 * <p>
 * This service manages channel configuration and provides methods for
 * determining server communication capabilities.
 * </p>
 *
 * <p>Configuration loaded from {@code config.yml} and can be reloaded
 * at runtime via {@link KernelConfigUpdateEvent}.</p>
 *
 * @since 2.1.0
 * @author Danburen
 */
public class ChannelServiceImpl implements ChannelService, EventListener<KernelConfigUpdateEvent>, PlaceholdersContributor {

    private final ServerService serverService;
    private volatile Config config;
    private final Map<String, Channel> channels = new ConcurrentHashMap<>();

    /**
     * Create a new ChannelServiceImpl.
     *
     * @param config        the config to read from
     * @param serverService the server service for server lookups
     */
    public ChannelServiceImpl(Config config, ServerService serverService) {
        this.config = config;
        this.serverService = serverService;
        loadChannels();
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public void onEvent(KernelConfigUpdateEvent event) {
        this.config = event.getConfig();
        loadChannels();
    }

    @Override
    public List<Channel> getAllChannels() {
        return List.copyOf(channels.values());
    }

    @Override
    public Optional<Channel> getChannel(String name) {
        return Optional.ofNullable(channels.get(name));
    }

    @Override
    public Channel getChannelForServer(String serverName) {
        for (Channel channel : channels.values()) {
            for (UServer server : channel.getServers()) {
                if (server.getName().equals(serverName)) {
                    return channel;
                }
            }
        }
        return null;
    }

    @Override
    public boolean canCommunicate(String sourceServerName, String targetServerName) {
        UServer source = serverService.getServer(sourceServerName).orElse(null);
        UServer target = serverService.getServer(targetServerName).orElse(null);
        return canCommunicate(source, target);
    }

    @Override
    public boolean canCommunicate(UServer source, UServer target) {
        if (source == null || target == null) {
            return true;
        }
        if (getConfig().get(ChannelConfigKey.GLOBAL)) {
            return true;
        }
        Set<String> sourceChannels = new HashSet<>();
        for (Channel channel : channels.values()) {
            if (channel.getServers().contains(source)) {
                sourceChannels.add(channel.getName());
            }
        }
        for (Channel channel : channels.values()) {
            if (channel.getServers().contains(target) && sourceChannels.contains(channel.getName())) {
                return true;
            }
        }
        return false;
    }

    private void loadChannels() {
        channels.clear();
        Map<String, Object> channelMap = getConfig().get(ChannelConfigKey.CHANNEL_LIST);
        if (channelMap == null) {
            return;
        }
        channelMap.forEach((key, value) -> {
            String prefix = "channels.channel-list." + key;
            List<UServer> servers = getConfig().<String>getCollection(prefix + ".servers", List.of()).stream()
                    .map(serverService::getServer)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
            Channel channel = new Channel(
                    key,
                    getConfig().get(prefix + ".display", "unknown"),
                    getConfig().get(prefix + ".welcome.enable", false),
                    getConfig().get(prefix + ".welcome.message", ""),
                    servers
            );
            channels.put(key, channel);
            // Set channel info on each server's MetaInfo
            servers.forEach(server -> {
                List<String> channelNames = server.<List<String>>getMeta(VeloChatXMetaInfoType.CHANNELS)
                        .map(ArrayList::new)
                        .orElse(new ArrayList<>());
                if (!channelNames.contains(key)) {
                    channelNames.add(key);
                }
                server.setMeta(VeloChatXMetaInfoType.CHANNELS, channelNames);
            });
        });
    }

    @Override
    public void contributePlaceholders(PlaceholderService service) {
        service.register("channel", ctx -> ctx.getServerName()
                .map(this::getChannelForServer)
                .map(Channel::getDisplayName)
                .orElse(""));
        service.registerDynamic("channel_online", (fullKey, ctx) -> {
            String channelName = fullKey.replace("_channel_online", "");
            return getChannel(channelName)
                    .map(ch -> String.valueOf(ch.getOnlinePlayerCount()))
                    .orElse("0");
        });
    }
}
