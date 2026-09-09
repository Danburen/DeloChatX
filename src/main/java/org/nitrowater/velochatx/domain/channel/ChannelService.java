package org.nitrowater.velochatx.domain.channel;

import org.nitrowater.waterapi.domain.model.UServer;
import org.nitrowater.waterapi.domain.shared.service.ConfigurableService;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing communication channels.
 * <p>
 * Channels group servers together to control cross-server message broadcasting.
 * This service provides CRUD operations for channels and handles the relationship
 * between channels and servers.
 * </p>
 *
 * <p>Configuration accessed via {@link #getConfig()} with {@link ChannelConfigKey}:</p>
 * <pre>{@code
 * // Check if enabled
 * boolean enabled = channelService.getConfig().get(ChannelConfigKey.BROADCAST_ENABLED);
 *
 * // Check if global mode
 * boolean global = channelService.getConfig().get(ChannelConfigKey.GLOBAL);
 * }</pre>
 *
 * @since 2.1.0
 * @author Danburen
 * @see Channel
 * @see UServer
 * @see ChannelConfigKey
 */
public interface ChannelService extends ConfigurableService<ChannelConfigKey> {

    /**
     * Get all registered channels.
     *
     * @return immutable list of all channels
     */
    List<Channel> getAllChannels();

    /**
     * Get a channel by its unique name.
     *
     * @param name the channel name/key
     * @return an Optional containing the channel if found, empty otherwise
     */
    Optional<Channel> getChannel(String name);

    /**
     * Get the channel that a server belongs to.
     * <p>
     * If a server belongs to multiple channels, returns the source (primary) channel.
     * </p>
     *
     * @param serverName the server name
     * @return the channel, or {@code null} if the server has no channel
     */
    Channel getChannelForServer(String serverName);

    /**
     * Check if two servers can communicate with each other.
     * <p>
     * Returns {@code true} if: global mode enabled, or both servers share
     * at least one common channel.
     * </p>
     *
     * @param sourceServerName the source server name
     * @param targetServerName the target server name
     * @return {@code true} if communication is allowed
     */
    boolean canCommunicate(String sourceServerName, String targetServerName);

    /**
     * Check if two servers can communicate with each other.
     *
     * @param source the source server
     * @param target the target server
     * @return {@code true} if communication is allowed
     */
    boolean canCommunicate(UServer source, UServer target);
}
