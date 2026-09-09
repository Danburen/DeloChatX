package org.nitrowater.velochatx.domain.server;

import org.nitrowater.waterapi.domain.model.UServer;
import org.nitrowater.waterapi.domain.shared.service.ConfigurableService;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing backend servers connected to the proxy.
 * <p>
 * This service maintains the registry of all servers and provides methods
 * for server lookup, player tracking, and display name management.
 * </p>
 *
 * <p>Configuration accessed via {@link #getConfig()} with {@link ServerConfigKey}:</p>
 * <pre>{@code
 * // Check if display names enabled
 * boolean enabled = serverService.getConfig().get(ServerConfigKey.DISPLAY_ENABLED);
 *
 * // Get proxy display
 * String proxy = serverService.getConfig().get(ServerConfigKey.PROXY_DISPLAY);
 * }</pre>
 *
 * @since 2.1.0
 * @author Danburen
 * @see UServer
 * @see ServerConfigKey
 */
public interface ServerService extends ConfigurableService<ServerConfigKey> {

    /**
     * Get all registered servers.
     *
     * @return immutable list of all servers
     */
    List<UServer> getAllServers();

    /**
     * Get a server by its name.
     *
     * @param name the server name (as configured in Velocity/Bukkit)
     * @return an Optional containing the server if found, empty otherwise
     */
    Optional<UServer> getServer(String name);

    /**
     * Get the display name for a server.
     *
     * @param serverName the server name
     * @return the display name, or the server name if not configured
     */
    String getDisplay(String serverName);

    /**
     * Get the total number of registered servers.
     *
     * @return server count
     */
    int getServerCount();

    /**
     * Get the total number of online players across all servers.
     *
     * @return total player count
     */
    int getTotalPlayerCount();

    /**
     * Get a server or create by name and inner proxy server
     *
     * @param name  {@link  UServer#getName()} target server name
     * @return      {@link UServer} instance
     */
    UServer getOrCreate(String name);
}
