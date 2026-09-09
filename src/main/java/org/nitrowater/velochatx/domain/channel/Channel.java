package org.nitrowater.velochatx.domain.channel;

import lombok.Getter;
import org.nitrowater.waterapi.domain.model.UPlayer;
import org.nitrowater.waterapi.domain.model.UServer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a communication channel that groups servers together.
 * <p>
 * Channels define which servers can communicate with each other. When a player
 * sends a message in a server, it will only be broadcast to other servers in
 * the same channel(s) (unless global broadcast is enabled).
 * </p>
 *
 * @since 2.0.0
 * @author Danburen
 * @see ChannelService
 */
@Getter
public final class Channel {

    private final String name;
    private final String displayName;
    private final boolean welcomeEnabled;
    private final String welcomeMessage;
    private final List<UServer> servers;
    private final List<UPlayer> players = new ArrayList<>();

    public Channel(String name, String displayName, boolean welcomeEnabled, String welcomeMessage, List<UServer> servers) {
        this.name = name;
        this.displayName = displayName;
        this.servers = new ArrayList<>(servers);
        this.welcomeEnabled = welcomeEnabled;
        this.welcomeMessage = welcomeMessage;
    }

    public List<UServer> getServers() {
        return List.copyOf(servers);
    }

    public void addServer(UServer server) {
        if (!servers.contains(server)) {
            servers.add(server);
        }
    }

    public void removeServer(UServer server) {
        servers.remove(server);
    }

    public int getServerCount() {
        return servers.size();
    }

    public void addPlayer(UPlayer player) {
        if (!players.contains(player)) {
            players.add(player);
        }
    }

    public void removePlayer(UPlayer player) {
        players.remove(player);
    }

    public int getOnlinePlayerCount() {
        AtomicInteger count = new AtomicInteger();
        servers.forEach(server -> count.addAndGet(server.getPlayers().size()));
        return count.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Channel channel = (Channel) o;
        return name.equals(channel.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "Channel{" + name + "}";
    }
}
