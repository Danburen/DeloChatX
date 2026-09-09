package org.nitrowater.velochatx.domain.broadcast;

import lombok.Getter;
import org.nitrowater.velochatx.domain.channel.Channel;
import org.nitrowater.waterapi.domain.model.UServer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a broadcast configuration entry.
 * <p>
 * This class holds broadcast messages, prefix, and target servers/channels.
 * It is used by {@link BroadcastService} to manage scheduled broadcasts.
 * </p>
 *
 * @since 2.1.0
 * @author Danburen
 * @see BroadcastService
 * @see BroadcastConfigKey
 */
@Getter
public final class BroadcastModel {

    private final String name;
    private final String prefix;
    private final List<String> messages;
    private final List<UServer> servers;

    /**
     * Create a new BroadcastModel.
     *
     * @param name     the broadcast name/identifier
     * @param prefix   the broadcast prefix for chat output
     * @param messages the list of broadcast messages
     * @param channels the channels to broadcast to (servers will be resolved)
     * @param servers  the direct servers to broadcast to
     */
    public BroadcastModel(String name, String prefix, List<String> messages, List<Channel> channels, List<UServer> servers) {
        this.name = name;
        this.prefix = prefix;
        this.messages = new ArrayList<>(messages);
        this.servers = resolveServers(channels, servers);
    }

    private static List<UServer> resolveServers(List<Channel> channels, List<UServer> servers) {
        return Stream.concat(
                channels.stream().flatMap(ch -> ch.getServers().stream()),
                servers.stream()
        ).distinct().collect(Collectors.toList());
    }

    public List<String> getMessages() {
        return List.copyOf(messages);
    }

    public void addMessage(String message) {
        messages.add(message);
    }

    public void removeMessage(String message) {
        messages.remove(message);
    }

    public List<UServer> getServers() {
        return List.copyOf(servers);
    }

    public boolean containsServer(UServer server) {
        return servers.contains(server);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BroadcastModel that = (BroadcastModel) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "BroadcastModel{" + name + "}";
    }
}
