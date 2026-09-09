package org.nitrowater.velochatx.entity;

import com.velocitypowered.api.proxy.Player;
import org.nitrowater.velochatx.utils.SubServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A class to holder server's message communicate
 * <p></p>
 * a server can be in several channels
 * a channel can contain several {@link SubServer}
 * @since 2.0.0
 * @author Danburen
 */
public class Channel {
    private final String channelName;
    private final String channelDisplayName;
    private final boolean welcomeEnable;
    private final String welcomeMessage;
    private final Set<SubServer> servers;
    private final List<Player> players = new ArrayList<>();
    public Channel(String channelName, String channelDisplayName, Set<SubServer> servers,boolean welcomeEnable, String welcomeMessage) {
        this.channelName = channelName;
        this.channelDisplayName = channelDisplayName;
        this.servers = servers;
        this.welcomeEnable = welcomeEnable;
        this.welcomeMessage = welcomeMessage;
    }

    public void addServer(SubServer server) {
        servers.add(server);
    }

    public int getServerCount() {
        return servers.size();
    }

    public String getChannelName() {
        return channelName;
    }

    public String getChannelDisplayName() {
        return channelDisplayName;
    }

    public Set<SubServer> getServers() {
        return servers;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public boolean isWelcomeEnable() {
        return welcomeEnable;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public int getOnlinePlayerCount() {
        AtomicInteger count = new AtomicInteger();
        servers.forEach(server->{
            count.addAndGet(server.getPlayers().size());
        });
        return count.get();
    }
}
