package org.nitrowater.velochatx.manager;

import org.nitrowater.velochatx.VeloChatX;
import org.nitrowater.velochatx.utils.BroadCastUtil;
import org.nitrowater.velochatx.entity.Channel;
import org.nitrowater.velochatx.utils.SubServer;
import org.waterwood.io.FileConfiguration;

import java.util.*;
import java.util.stream.Collectors;
@Deprecated
public class ChannelManager extends BasicMethods {
    public static boolean channelGlobal;
    private static Map<String,Channel> channels;
    private static Map<String,List<String>> messages;
    private static Map<String,String> msgPrefix;

    protected static FileConfiguration broadCastConfig;
    private static BroadCastUtil globalBCT;
    private static Set<BroadCastUtil> localBCT;
    private static Set<SubServer> remainServer;

    private static boolean isEnabled = false;
    private static boolean isRandom = false;
    private static boolean WelcomeEnabled = false;
    private static boolean WelcomeJoinFirst = false;
    private static boolean GlobalEnable = false;
    private static boolean LocalEnable = false;

    private static String WELCOME_MESSAGE;

    private static boolean BROADCAST_ENABLED;
    private static boolean BROADCAST_PROXY_ENABLED;
    private static boolean LOG_ENABLE;
    private static boolean LOG_PROXY_ENABLE;
    private static boolean SEND_TO_ALL;
    private static boolean SEND_TO_ALL_PROXY;
    private static boolean BROADCAST_IN_CHANNEL;

    private static String JOIN_MESSAGE;
    private static String LEAVE_MESSAGE;
    private static String JOIN_PROXY_MESSAGE;
    private static String LEAVE_PROXY_MESSAGE;

    private static String JOIN_PREFIX;
    private static String LEAVE_PREFIX;
    private static String JOIN_PROXY_PREFIX;
    private static String LEAVE_PROXY_PREFIX;

    public static void initialize() {
        broadCastConfig = VeloChatX.getInstance().loadFile("broadcast.yml");
        loadData();
        loadChannels();
        loadBroadcastUtils();
        loadMessages();
    }

    /**
     * Add local Broadcast
     * @param name name of the broadcast
     * @param prefix prefix of broadcasting
     * @param msgSet message list of broadcast
     * @param channelSet channel list of broadcast
     */
    public static void addBCT(String name,String prefix,List<String> msgSet,Set<String> channelSet,Set<String> serverSet){
        if(localBCT == null) { localBCT = new HashSet<>(); }
        if(localBCT.stream().map(BroadCastUtil::getName).toList().contains(name)) {
            return;
        }
        localBCT.add(
                new BroadCastUtil(name,prefix,msgSet,channelSet.stream()
                        .map(ChannelManager::getChannel)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()),
                        serverSet.stream()
                        .map(BasicMethods::getSubServer)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()))
        );
    }

    private static void loadLocalBCTs(){
        Map<String,Object> BCTMap = broadCastConfig.get("locale.broadcast-list",new HashMap<>());
        BCTMap.forEach((key, value) ->{
            String PATH_PREFIX = "locale.broadcast-list." + key + ".";
            if(broadCastConfig.getBoolean(PATH_PREFIX + "enable",false)){
                addBCT(key, broadCastConfig.get(PATH_PREFIX + "prefix", "[§6BroadCast§r]"),
                        broadCastConfig.getStringList(PATH_PREFIX + "message-list",List.of()),
                        new HashSet<>(broadCastConfig.getStringList(PATH_PREFIX + "channel-list", List.of()).stream()
                                        .filter(channel-> channels.containsKey(channel)).collect(Collectors.toSet())),
                        new HashSet<>(broadCastConfig.getStringList(PATH_PREFIX + "server-list", List.of()).stream()
                                .filter(server-> serverInfoMap.containsKey(server)).collect(Collectors.toSet())));
            }
        });
    }

    public static void loadData(){
        isEnabled = getConfigs().getBoolean("broadcast-enable",false);
        isRandom = broadCastConfig.getBoolean("random",true);
        GlobalEnable = broadCastConfig.get("global.enable",true);
        LocalEnable = broadCastConfig.get("locale.enable",false);
        channelGlobal = getConfigs().getBoolean("channels.global",true);

        WelcomeEnabled = broadCastConfig.getBoolean("welcome-broadcast.enable",false);
        WelcomeJoinFirst = broadCastConfig.getBoolean("welcome-broadcast.only-first-join",false);

        WELCOME_MESSAGE = broadCastConfig.getString("welcome-broadcast.message","");

        BROADCAST_ENABLED = broadCastConfig.getBoolean("join-leave-broadcast.enable",true);
        LOG_ENABLE = broadCastConfig.getBoolean("join-leave-broadcast.log-to-console",true);
        SEND_TO_ALL = broadCastConfig.getBoolean("join-leave-broadcast.send-to-all-subServer",false);
        BROADCAST_PROXY_ENABLED = broadCastConfig.getBoolean("join-leave-proxy-broadcast.enable",true);
        LOG_PROXY_ENABLE = broadCastConfig.getBoolean("join-leave-proxy-broadcast.log-to-console",true);
        SEND_TO_ALL_PROXY = broadCastConfig.getBoolean("join-leave-proxy-broadcast.send-to-all-subServer",true);
        BROADCAST_IN_CHANNEL = broadCastConfig.getBoolean("join-leave-broadcast.broadcast-in-channel",true);

        JOIN_PREFIX = broadCastConfig.getString("join-leave-broadcast.join-prefix","§a(+)§r");
        LEAVE_PREFIX = broadCastConfig.getString("join-leave-broadcast.leave-prefix","§c(-)§r");
        JOIN_PROXY_PREFIX = broadCastConfig.getString("join-leave-proxy-broadcast.join-prefix","§a[+]§r");
        LEAVE_PROXY_PREFIX = broadCastConfig.getString("join-leave-proxy-broadcast.leave-prefix","§c[-]§r");

        JOIN_MESSAGE = broadCastConfig.getString("join-leave-broadcast.player-join-message","{Group}{Prefix}{Player}{Suffix} has Joined the {Server}");
        LEAVE_MESSAGE = broadCastConfig.getString("join-leave-broadcast.player-leave-message","{Group}{Prefix}{Player}{Suffix} has left the {Server}");
        JOIN_PROXY_MESSAGE = broadCastConfig.getString("join-leave-proxy-broadcast.player-join-message","{Group}{Prefix}{Player}{Suffix} has Joined the {Server}");
        LEAVE_PROXY_MESSAGE = broadCastConfig.getString("join-leave-proxy-broadcast.player-leave-message","{Group}{Prefix}{Player}{Suffix} has left the {Server}");

    }

    private static void loadGlobalBCT(){
        globalBCT = new BroadCastUtil("global",
                broadCastConfig.get("global.prefix", "[§6Server§r]"),
                broadCastConfig.getStringList("global.message-list", List.of()),
                new HashSet<>(channels.values()),
                new HashSet<>(serverInfoMap.values()));
    }

    // Get and set channels & server's channel
    public static void loadChannels(){
        channels = new HashMap<>();
        Map<String,Object> channelMap = getConfigs().get("channels.channel-list");
        if (channelMap != null) {
            channelMap.forEach( (k,v) ->{
                List<String> serverList = getConfigs().get("channels.channel-list." + k + ".servers",new ArrayList<>());
                String PATH_PREFIX = "channels.channel-list." + k;
                //add new channel
                Channel serverChannel = new Channel(
                        k, getConfigs().get(PATH_PREFIX + ".display","unknown")
                        ,serverList.stream().filter(serverName-> serverDisplayName.containsKey(serverName))
                            .map(serverInfoMap::get).collect(Collectors.toSet())
                        ,getConfigs().get(PATH_PREFIX + ".welcome.enable",false)
                        ,getConfigs().get(PATH_PREFIX + ".welcome.message","")
                        );
                channels.put(k,serverChannel);
                // set server's channel
                for(SubServer server : serverInfoMap.values()){
                    if(serverList.contains(server.getServerName())){
                        server.addChannel(serverChannel);
                    }
                }
            });
            // Retrieve source channel name base on server name
            serverInfoMap.forEach( (k,v) ->{
                serverSourceChannel.put(k,v.getSourceChannel());
            });
        }
    }

    public static void loadBroadcastUtils(){
        remainServer = new HashSet<>();
        if(GlobalEnable) {
            loadGlobalBCT();
            remainServer.addAll(globalBCT.getServerSet());
        };
        if(LocalEnable) {
            loadLocalBCTs();
            // record the remaining server
            Set<SubServer> toRemoveServers = localBCT.stream()
                    .flatMap(bct-> bct.getServerSet().stream())
                    .collect(Collectors.toSet());
            remainServer.removeAll(toRemoveServers);
        }
    }

    public static void loadMessages(){
        messages = new HashMap<>();
        msgPrefix = new HashMap<>();
        if(GlobalEnable) {
            serverInfoMap.keySet().forEach(server-> {
                messages.put(server,globalBCT.getMessageList());
                msgPrefix.put(server, globalBCT.getPrefix());
            });
        }
        if(LocalEnable) {
            localBCT.forEach(bct->{
                bct.getServerSet().stream()
                        .map(SubServer::getServerName)
                        .collect(Collectors.toSet()).forEach(server->{
                            if (messages.containsKey(server)) {
                                messages.get(server).addAll(bct.getMessageList());
                            }else{
                                messages.put(server,bct.getMessageList());
                            }
                            msgPrefix.put(server, bct.getPrefix());
                        });
            });
        }
        // distinct the message
        messages.forEach((server,msgList)->
                messages.put(server, new ArrayList<>(new LinkedHashSet<>(msgList))));
    }

    public static boolean isEnabled() {
        return isEnabled;
    }
    public static boolean isRandom() {
        return isRandom;
    }
    public static boolean isGlobalEnable() {
        return GlobalEnable;
    }
    public static boolean isLocalEnable() {
        return LocalEnable;
    }
    public static Set<BroadCastUtil> getLocalBCT() {
        return localBCT;
    }
    public static BroadCastUtil getGlobalBCT() {
        return globalBCT;
    }

    public static boolean isBroadcastInChannel() { return BROADCAST_IN_CHANNEL; }
    public static boolean isBroadcastEnabled() { return BROADCAST_ENABLED; }
    public static boolean isBroadcastEnabledProxy() { return BROADCAST_PROXY_ENABLED; }
    public static boolean isLogEnable() { return LOG_ENABLE; }
    public static boolean isLogProxyEnable() { return LOG_PROXY_ENABLE; }
    public static boolean isSendToAll() { return SEND_TO_ALL; }
    public static boolean isSendToAllProxy() { return SEND_TO_ALL_PROXY; }
    public static String getJoinMessage() { return JOIN_MESSAGE; }
    public static String getLeaveMessage() { return LEAVE_MESSAGE; }
    public static String getJoinProxyMessage() { return JOIN_PROXY_MESSAGE; }
    public static String getLeaveProxyMessage() {
        return LEAVE_PROXY_MESSAGE;
    }
    public static String getJoinPrefix() {
        return JOIN_PREFIX;
    }
    public static String getLeavePrefix() {
        return LEAVE_PREFIX;
    }
    public static String getJoinProxyPrefix() {
        return JOIN_PROXY_PREFIX;
    }
    public static String getLeaveProxyPrefix() {
        return LEAVE_PROXY_PREFIX;
    }
    public static boolean isWelcomeJoinFirst() {
        return WelcomeJoinFirst;
    }
    public static String getWelcomeMessage() {
        return WELCOME_MESSAGE;
    }
    public static boolean isWelcomeEnabled() { return WelcomeEnabled; }

    public static String getFullJoinProxyMessage(){
        return getJoinProxyPrefix() + getJoinProxyMessage();
    }
    public static String getFullLeaveProxyMessage(){
        return getLeaveProxyPrefix() + getLeaveProxyMessage();
    }

    public static Channel getChannel(String serverName){ return channels.get(serverName); }

    public static Map<String,Channel> getChannels(){ return channels; }

    public static boolean isChannelGlobal(){
        return channelGlobal;
    }

    public static FileConfiguration getBroadcastConfigs() {
        return broadCastConfig;
    }

    /**
     * Get the processed message to broadcast from broadcast util
     * @param serverName target server name
     * @return string list of broadcast message
     */
    public static List<String> getMessages(String serverName) {
        return messages.get(serverName);
    }

    public static int getMessageCount(String serverName) {
        return messages.get(serverName).size();
    }

    public static String getMsgPrefix(String serverName) {
        return msgPrefix.get(serverName);
    }
    /**
     * Return the remaining server local broadcast left from global
     * @return set of SubServer
     */
    public static Set<SubServer> getRemainServer() {
        return remainServer;
    }
}
