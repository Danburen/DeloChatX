package org.nitrowater.velochatx.utils;

import org.nitrowater.velochatx.entity.Channel;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A class served as util to holder broadcast
 * @since 2.0.0
 * @author Danburen
 * @version 1.0.0
 */
public class BroadCastUtil {
    private final String broadcastName;
    private final String prefix;
    private final List<String> messageList;
    private final Set<SubServer> serverSet;

    public BroadCastUtil(String broadcastName, String prefix, List<String> messageList, Set<Channel> channelSet, Set<SubServer> serverSet) {
        this.broadcastName = broadcastName;
        this.prefix = prefix;
        this.messageList = messageList;
        this.serverSet = channelSet.stream()
                .flatMap(channel -> Stream.concat(channel.getServers().stream(),serverSet.stream()))
                .collect(Collectors.toSet());
    }

    public void addMessage(String message) {
        messageList.add(message);
    }

    public void removeMessage(String message) {
        messageList.remove(message);
    }

    public String getName() {
        return broadcastName;
    }

    public String getPrefix() {
        return prefix;
    }

    public List<String> getMessageList() {
        return messageList;
    }

    public Set<SubServer> getServerSet() {
        return serverSet;
    }
}
