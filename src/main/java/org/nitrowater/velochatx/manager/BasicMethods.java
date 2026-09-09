package org.nitrowater.velochatx.manager;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.nitrowater.velochatx.VeloChatX;
import org.nitrowater.velochatx.entity.Channel;
import org.nitrowater.velochatx.utils.SubServer;
import org.waterwood.plugin.velocity.util.MethodBase;
import org.waterwood.plugin.velocity.VelocityPlugin;

import java.util.*;
import java.util.regex.Pattern;

import static org.waterwood.hock.LuckPermsHock.*;

@Deprecated
public abstract class BasicMethods extends MethodBase {
    protected static Map<String,String> serverDisplayName;
    protected static Map<String, Channel> serverSourceChannel;
    protected static Map<String, SubServer> serverInfoMap;

    protected static ProxyServer proxyServer;
    protected static Object pluginInstance;

    private static final Pattern PERCENT_PATTERN = Pattern.compile("%([^%]+)%");


    protected static String PROXY_DISPLAY;
    static {
        serverDisplayName = new HashMap<>();
        serverSourceChannel = new HashMap<>();
        serverInfoMap = new HashMap<>();
    }


    public static void load(){
        BasicMethods.proxyServer = VeloChatX.getProxyServer();
        BasicMethods.pluginInstance = VeloChatX.getInstance();
        if(getConfigs().getBoolean("server-display.enable",false)){
            getAndSetServer();
        }
        LogManager.initialize();
        TabListManager.initialize();
        ChannelManager.initialize();
        PlayerManager.initialize();
        ChatManager.initialize();
        TaskManager.initTask();

        if(! ChannelManager.isEnabled()){
            VeloChatX.getInstance().logMsg(getPluginMessage("broadcast-disable-message"));
        }
        if(! ChatManager.isCrossingChatEnabled()){
            VeloChatX.getInstance().logMsg(getPluginMessage("crossing-chat-disable-message"));
        }
    }

    // Get and set server display name
    private static void getAndSetServer(){
        Map<String,String> displayMap = getConfigs().get("server-display.display",new HashMap<String,String>());
        serverInfoMap.put("unknown",new SubServer("unknown","UNKNOWN",null));
        for(Map.Entry<String,String> entry : displayMap.entrySet()){
            String k = entry.getKey();String v = entry.getValue();
            // display contains proxy key then band it to proxy display.
            if(k.equalsIgnoreCase("proxy")){
                PROXY_DISPLAY = v;
                continue;
            }

            // sub server class correspond with register server in order to ensure the integrity of server data.
            proxyServer.getAllServers().forEach(regServer -> {
                if(regServer.getServerInfo().getName().equals(k)){
                    serverInfoMap.put(k, new SubServer(k, v, regServer));
                    serverDisplayName.put(k,v);
                }
            });
        }
        if(PROXY_DISPLAY == null) PROXY_DISPLAY = getConfigs().getString("server-display.proxy","[Proxy]");
    }



    private static void replacePlaceholder(StringBuilder builder, String placeholder, String value) {
        int index = 0;
        while ((index = builder.indexOf(placeholder, index)) != -1) {
            builder.replace(index, index + placeholder.length(), value);
            index += value.length();
        }
    }

    public static String placeValue(String origin,Player player,RegisteredServer targetServer){
        StringBuilder out = new StringBuilder(
                PERCENT_PATTERN.matcher(origin).replaceAll("{$1}").toLowerCase());
        placePlayerValue(out, player);
        placeNoTargetValue(out);
        return placeServerValue(out,targetServer.getServerInfo().getName()).toString();
    }

    /**
     * Place value with unsafe getting player's server
     * @param origin original string
     * @param player value to place from player
     * @return value placed
     */
    public static String placeValue(String origin, Player player){
        StringBuilder out = new StringBuilder(
                PERCENT_PATTERN.matcher(origin).replaceAll("{$1}").toLowerCase());
        placePlayerValue(out,player);
        String serverName = player.getCurrentServer().map(sc ->
               sc.getServerInfo().getName()).orElse("unknown");
        placeServerValue(out, serverName);
        placeNoTargetValue(out);
        return out.toString();
    }

    /**
     * Place server placeholder value
     * @param original string builder of original
     * @param serverName target server name
     * @return value placed string builder
     */
    public static StringBuilder placeServerValue(StringBuilder original,String serverName){
        replacePlaceholder(original, "{channel}", Optional.ofNullable(serverSourceChannel.get(serverName)).map(Channel::getChannelDisplayName).orElse(""));
        replacePlaceholder(original, "{server}",serverDisplayName.getOrDefault(serverName, serverName));
        replacePlaceholder(original, "{proxy}", PROXY_DISPLAY);
        replacePlaceholder(original,"{online}", String.valueOf(
                Optional.ofNullable(serverInfoMap.get( serverName )).map(SubServer::getPlayerCount).orElse(0)));
        replacePlaceholder(original,"{total_online}", String.valueOf(VelocityPlugin.getPlayerCount()));
        return original;
    }

    public static void placePlayerValue(StringBuilder origin, Player player){
        String playerName = player.getUsername();
        String prefix = getPlayerPrefix(playerName);
        String suffix = getPlayerSuffix(playerName);
        String groupDisplayName = getPlayerGroupDisplay(playerName);

        replacePlaceholder(origin, "{player}", playerName);
        replacePlaceholder(origin, "{prefix}", prefix);
        replacePlaceholder(origin, "{suffix}", suffix);
        replacePlaceholder(origin, "{group}", groupDisplayName);
        replacePlaceholder(origin, "{ping}", String.valueOf(player.getPing()));
        replacePlaceholder(origin, "&", "§");
    }

    public static void placeNoTargetValue(StringBuilder original){
        ChannelManager.getChannels().forEach(
                (channelKey,channel) ->
                        replacePlaceholder(original, "{%s_channel_online}".formatted(channelKey),
                        String.valueOf(channel.getOnlinePlayerCount())));
        serverInfoMap.forEach(
                (id,display) ->
                        replacePlaceholder(original, "{%s_server_online}".formatted(id),
                                String.valueOf(Optional.ofNullable(serverInfoMap.get(id))
                                        .map(SubServer::getPlayerCount).orElse(0))
        ));

    }
    public static SubServer getSubServer(String serverName){
        return Optional.ofNullable(serverInfoMap.get(serverName)).orElse(serverInfoMap.get("unknown"));
    }

    public static SubServer getSubServer(RegisteredServer registeredServer){
        return getSubServer(registeredServer.getServerInfo().getName());
    }

    public static Map<String,SubServer> getSubServerMap(){
        return serverInfoMap;
    }
}
