package org.nitrowater.velochatx.manager;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.waterwood.plugin.WaterPlugin;
import org.waterwood.utils.Colors;
import net.kyori.adventure.text.Component;
import org.waterwood.plugin.velocity.VelocityPlugin;

@Deprecated
public class MessageManager extends BasicMethods {

    public static String convertString(String original,CommandSource source,CommandSource target){
        if(source instanceof Player sourcePlayer){
            if(target instanceof Player){
                return placeValue(original,sourcePlayer);
            }else{
                return Colors.parseColor(placeValue(original,sourcePlayer));
            }
        }else{
            return convertServer(original,source,target);
        }
    }
    public static String convertMessage(String key,CommandSource source,CommandSource target){
        if(source instanceof Player sourcePlayer){
            if(target instanceof Player){
                return placeValue(WaterPlugin.getInstance().getMessage(key, PlayerManager.getPlayerLangCode(sourcePlayer)),sourcePlayer);
            }else{
                return Colors.parseColor(placeValue(getMessage(key),sourcePlayer));
            }
        }else{
            return convertServer(getMessage(key),source,target);
        }
    }

    public static String convertServer(String original,CommandSource source,CommandSource target){
        String out = original;
        if(source instanceof RegisteredServer server){
            out = ValueToServer(out,server.getServerInfo().getName());
        }else{
            out = ValueToServer(out, PROXY_DISPLAY);
        }
        return target instanceof Player ? out : Colors.parseColor(out);
    }

    public static String getSourceMessage(String key,CommandSource source){
        if(source instanceof Player sourcePlayer) {
            return WaterPlugin.getInstance().getMessage(key, PlayerManager.getPlayerLangCode(sourcePlayer));
        }else{
            return getMessage(key);
        }
    }

    public static String  ValueToServer(String original,String serverName){
        String out = original.toLowerCase();
        if(out.contains("{")){
            String serverDisplay = serverDisplayName.getOrDefault(serverName, serverName);
            if(out.contains("{player}")) {
                out = out.replace("{player}", serverDisplay);
                return out.replaceAll("\\{.*?\\}","");
            }else{
                out = out.replaceAll("\\{.*?\\}","");
                out = serverDisplay + out;
            }
        }
        return out;
    }

    /**
     * Broadcast sub server message
     * @param player Trigger player
     * @param targetServer Trigger server
     * @param isJoin join or leave
     */
    public static void broadcastServerMessage(Player player, RegisteredServer targetServer,boolean isJoin){
        if(ChannelManager.isBroadcastEnabled()) {
            final String message = placeValue(isJoin ? ChannelManager.getJoinMessage() : ChannelManager.getLeaveMessage()
                            ,player,targetServer);
            targetServer.sendMessage(Component.text(
                    isJoin ? ChannelManager.getJoinPrefix() + message: ChannelManager.getLeavePrefix() + message));
            if(ChannelManager.isLogEnable()) {
                getLogger().info(Colors.parseColor(message));
            }
            if(ChannelManager.isSendToAll()){
                for(RegisteredServer registeredServer : proxyServer.getAllServers()){
                    if(registeredServer.equals(targetServer)){
                        continue;
                    }
                    registeredServer.sendMessage(Component.text(message));
                }
            }else{
                if(ChannelManager.isBroadcastInChannel()){
                    for(RegisteredServer registeredServer : proxyServer.getAllServers()){
                        if(registeredServer.equals(targetServer)){
                            continue;
                        }
                        if(ChatManager.canCommunicate(targetServer,registeredServer)){
                            registeredServer.sendMessage(Component.text(message));
                        }
                    }
                }
            }
        }
    }
    /**
     * Broadcast Proxy server message
     * @param player Trigger player
     * @param isJoin join or leave
     */
    public static void broadcastProxyMessage(Player player, boolean isJoin){
        if(ChannelManager.isEnabled()){
            if(ChannelManager.isBroadcastEnabledProxy()) {
                final String message = placeValue(
                        isJoin ? ChannelManager.getFullJoinProxyMessage() :
                                ChannelManager.getFullLeaveProxyMessage(),player);
                ProxyServer proxyServer = VelocityPlugin.getProxyServer();
                if(ChannelManager.isLogProxyEnable()){
                    getLogger().info(Colors.parseColor(message));
                }
                if(ChannelManager.isSendToAllProxy()){
                    proxyServer.getAllServers().forEach(registeredServer ->
                            registeredServer.sendMessage(Component.text(message))
                    );
                }
            }
        }
    }

    public static void broadcastMessage(Player player, String message){
        player.sendMessage(Component.text(placeValue(message,player)));
    }

}
