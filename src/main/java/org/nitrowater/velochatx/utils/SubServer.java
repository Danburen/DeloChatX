package org.nitrowater.velochatx.utils;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.nitrowater.velochatx.entity.Channel;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Util to temporary store server data
 * one SubServer corresponds to a RegisteredServer
 * Include ServerName,DisplayName,{@link Channel}
 * @since 2.0.0
 */
public class SubServer{
    private final String serverName;
    private final String serverDisplayName;
    private final RegisteredServer server;
    private Channel sourceChannel;
    private final List<Player> players = new ArrayList<>();
    private final Set<Channel> channels = new HashSet<>();

    public SubServer(String serverName, String serverDisplayName,RegisteredServer server) {
        this.serverName = serverName;
        this.serverDisplayName = serverDisplayName;
        this.server = server;
    }

    public @NonNull String getServerName() {
        return serverName;
    }

    public @NonNull String getServerDisplayName() {
        return serverDisplayName;
    }

    public @NonNull Set<Channel> getChannels() {
        return channels;
    }

    public @Nullable Channel getSourceChannel() {
        return sourceChannel;
    }

    public @Nullable RegisteredServer getRegisteredServer() {
        return server;
    }

    public void addChannel(Channel channel) {
        if(sourceChannel == null) {
            sourceChannel = channel;
        }
        channels.add(channel);
    }

    public void setSourceChannel(Channel sourceChannel) {
        this.sourceChannel = sourceChannel;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public int getPlayerCount() {
        return players.size();
    }

    public void setPlayers(List<Player> players) {
        this.players.clear();
        this.players.addAll(players);
    }

    public void addPlayer(Player player) {
        players.add(player);
        channels.forEach(channel -> channel.getPlayers().add(player));
    }

    public void removePlayer(Player player) {
        players.remove(player);
        channels.forEach(channel -> {
            channel.getPlayers().remove(player);
        });
    }
}
