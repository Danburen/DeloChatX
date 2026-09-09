package org.nitrowater.velochatx.events;


import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.waterwood.velochatx.VeloChatX;
import me.waterwood.velochatx.manager.*;
import org.waterwood.plugin.velocity.util.MethodBase;
import me.waterwood.velochatx.entity.Channel;
import me.waterwood.velochatx.utils.SubServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import me.waterwood.velochatx.utils.PlayerAttribution;
import me.waterwood.velochatx.manager.BasicMethods;
import org.waterwood.plugin.velocity.VelocityPlugin;
import org.waterwood.utils.Colors;
import org.waterwood.utils.MessageConverter;

import java.util.*;

public class PlayerEvents extends MethodBase {
    private final ProxyServer proxyServer = VelocityPlugin.getProxyServer();
    private static final Map<UUID,PlayerAttribution> playerAttrs;

    static{
        playerAttrs = new HashMap<>();
    }

    public static Map<UUID, PlayerAttribution> getPlayerAttrs() {
        return playerAttrs;
    }

    @Subscribe(priority = 0)
    public void onPlayChat(PlayerChatEvent evt){
        String message = evt.getMessage();
        Player source = evt.getPlayer();
        UUID sourceUuid = source.getUniqueId();

        PlayerAttribution sourceAttrs;
        if(playerAttrs.containsKey(sourceUuid)){
            sourceAttrs = playerAttrs.get(sourceUuid);
        }else{
            sourceAttrs = new PlayerAttribution();
            playerAttrs.put(sourceUuid, sourceAttrs);
        }

        // source player chat offline
        if(sourceAttrs.isChatOffLine()){
            return;
        }
        if(ChatManager.isBanWorldEnable()) {
            if (ChatManager.hasBanWords(message)) {
                source.sendMessage(Component.text(MessageManager.getMessage("ban-words-message"), NamedTextColor.RED));
                if (ChatManager.isBanWordLog()) {
                    getLogger().info(Colors.parseColor(ChatManager.placeChatValue(MessageManager.getPluginMessage("ban-words-log-message"), message, source)));
                }
                return;
            }
        }
        String finalMessage = ChatManager.placeChatValue(message,source);
        if(LogManager.isLogToConsole()) {
            getLogger().info(Colors.parseColor(finalMessage,! LogManager.isLogColorConvert()));
        }
        if (! ChatManager.isCrossingChatEnabled()) {
            return;
        }

        String sourceServerName = source.getCurrentServer()
                .map(serverConnection -> serverConnection.getServerInfo().getName())
                .orElse("unknown");

        proxyServer.getAllPlayers().forEach(player -> {
            UUID uuid = player.getUniqueId();
            PlayerAttribution attrs = playerAttrs.get(uuid);
            String playerServerName = player.getCurrentServer()
                    .map(serverConnection -> serverConnection.getServerInfo().getName())
                    .orElse("unknown");

            if(attrs ==null){
               attrs = new PlayerAttribution();
               playerAttrs.put(uuid, attrs);
            }
//            getLogger().info("Cross-server chat enabled: " + ChatManager.isCrossingChatEnabled());
//            getLogger().info("Source server: " + sourceServerName);
//            getLogger().info("Channel is global: " + ChannelManager.isChannelGlobal());

            if(attrs.isChatOffLine()) return;
            // black list
            if(attrs.getIgnorePlayers().contains(sourceUuid)) return;
            // same server
            if(playerServerName.equals(sourceServerName)) return;
            // same channel communicate
            if(!ChatManager.canCommunicate(playerServerName,sourceServerName) && ! ChannelManager.isChannelGlobal()) return;

            player.sendMessage(MessageConverter.convert(ChatManager.getCHATFORMAT_MODEL(), finalMessage));
        });
    }

    @Subscribe(priority = 1)
    public void onConnectServer(ServerConnectedEvent evt){
        Player player = evt.getPlayer();
        RegisteredServer server = evt.getServer();
        try {
            VeloChatX.getInstance().loadLocale(PlayerManager.getPlayerLangCode(player));
        }catch(NullPointerException e){
            getLogger().info(getPluginMessage("cant-load-message"));
        }finally {
            // player join backhand server broadcast
            evt.getPreviousServer().ifPresent(preServer -> {
                        MessageManager.broadcastServerMessage(player,preServer,false);
                        BasicMethods.getSubServer(preServer.getServerInfo().getName()).removePlayer(player);
                    });
            MessageManager.broadcastServerMessage(player,server,true);

            // welcome message broadcast
            SubServer subServer = BasicMethods.getSubServer(server.getServerInfo().getName());
            subServer.addPlayer(player);
            Channel channel = subServer.getSourceChannel();
            if (channel != null && channel.isWelcomeEnable()) {
                MessageManager.broadcastMessage(player,channel.getWelcomeMessage());
            }

            // tab list update
            if(TabListManager.isTabListEnable()) {
                proxyServer.getAllPlayers().forEach(p ->{
                    TabListManager.tabListAddPlayer(player,p);
                    TabListManager.tabListAddPlayer(p,player);
                });
            }

        }
    }
    @Subscribe(priority = 3)
    public void onProxyConnect(LoginEvent evt){
        Player player = evt.getPlayer();
        UUID uuid = player.getUniqueId();
        boolean isFirstJoin = ! PlayerManager.storeOrUpdatePlayer(player);
        // add player data to memory
        if(isFirstJoin){
            playerAttrs.put(uuid, new PlayerAttribution(new HashSet<>(), new HashSet<>()));
        }else{
            playerAttrs.put(uuid,PlayerManager.getPlayerAttribution(uuid));
        }
        // broadcast proxy join message(multiple player)
        MessageManager.broadcastProxyMessage(player,true);
        //welcome message broadcast(single player)
        if(ChannelManager.isWelcomeEnabled()){
            if(ChannelManager.isWelcomeJoinFirst() && isFirstJoin) {
                MessageManager.broadcastMessage(player, ChannelManager.getWelcomeMessage());
                return;
            }
            MessageManager.broadcastMessage(player, ChannelManager.getWelcomeMessage());
        }
    }

    @Subscribe(priority = 2)
    public void onDisConnect(DisconnectEvent evt){
        Player player = evt.getPlayer();
        UUID uuid = player.getUniqueId();
        // store and clean memory player data
        PlayerManager.removePlayer(player);
        PlayerManager.updatePlayerLeftTime(uuid,System.currentTimeMillis());
        PlayerManager.updatePlayerAttrs(uuid,playerAttrs.get(uuid));
        playerAttrs.remove(uuid);
        // update player tab list
        if(TabListManager.isTabListEnable()) {
            proxyServer.getAllPlayers().forEach(p -> p.getTabList().removeEntry(uuid));
        }
        // proxy leave message broadcast
        MessageManager.broadcastProxyMessage(player,false);
    }


}

