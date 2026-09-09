package org.nitrowater.velochatx.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import org.nitrowater.velochatx.manager.ChannelManager;
import org.nitrowater.velochatx.manager.PlayerManager;
import org.nitrowater.velochatx.utils.PlayerAttribution;
import org.nitrowater.velochatx.VeloChatX;
import org.nitrowater.velochatx.events.PlayerEvents;
import org.nitrowater.velochatx.manager.BasicMethods;
import org.nitrowater.velochatx.entity.Channel;
import org.nitrowater.velochatx.utils.SubServer;
import org.waterwood.utils.Colors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.waterwood.plugin.velocity.VelocityPlugin;
import org.nitrowater.velochatx.manager.MessageManager;

import java.util.*;

public class ControlCommands extends VelocitySimpleCommand implements SimpleCommand {
    private static final List<String> playerActions = List.of("reject","remove","ignore");
    private static final String PRIMARY_ALIAS = "velochatx";
    private static final String[] ALIASES = {"vcx","chatx","vc"};

    private static final List<String> consoleCommands = List.of("help", "reload","show-channel-info");
    private static final List<String> defaultPlayerCommands = List.of("help", "on", "off", "ignore", "reject", "remove","list");
    private static final List<String> adminCommands = List.of("help","show-channel-info","placeholder", "on", "off", "ignore", "reject", "remove","list");
    public static final ArrayList<String> configFiles = new ArrayList<>(Arrays.asList("config","message","broadcast"));
    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        List<String> subCmds = this.suggest(invocation);
        if(args.length > 2){
            illegalArgsMsg(source);
            return;
        }
        if(args.length == 0){
            source.sendMessage(Component.text(VeloChatX.getInstance().getPluginInfo()));
            return;
        }

        //help command
        if (args[0].equalsIgnoreCase("help")) {
            if(source instanceof  ConsoleCommandSource){
                source.sendMessage(Component.text(Colors.parseColor(VeloChatX.getInstance().getPluginInfo())));
                for (String cmd : consoleCommands) {
                    source.sendMessage(Component.text(Colors.parseColor(getPluginMessage(cmd + "-command-format-message"))));
                }
            }else{
                source.sendMessage(Component.text(VeloChatX.getInstance().getPluginInfo()));
                for (String cmd : source.hasPermission("velochatx.admin") ? adminCommands : defaultPlayerCommands) {
                    source.sendMessage(Component.text(getPluginMessage(cmd + "-command-format-message")));
                }
            }
            return;
        }

        // command that console or admin execute
        if (source.hasPermission("velochatx.admin")) {
            //reload command
            if(args[0].equalsIgnoreCase("reload")) {
                if (args.length == 1) {
                    VeloChatX.getInstance().reloadConfig();
                    BasicMethods.load();
                    source.sendMessage(Component.text(getPluginMessage("config-reload-completed-message"), NamedTextColor.GREEN));
                    return;
                }
                if (configFiles.contains(args[1])) {
                    switch (args[1]){
                        case "config": VeloChatX.getInstance().reloadConfig();break;
                        case "message": VeloChatX.getInstance().reloadPluginMessage();break;
                        case "broadcast": ChannelManager.initialize();break;
                        default:
                    }
                    source.sendMessage(Component.text(getPluginMessage("config-file-reload-message")
                            .formatted(args[1].concat(".yml")), NamedTextColor.GREEN));
                } else {
                    source.sendMessage(Component.text(String.format(getPluginMessage("incorrect-config-file-message"), args[1], configFiles), NamedTextColor.RED));
                    return;
                }
            }
            // show-channel-info-command
            if(args[0].equalsIgnoreCase("show-channel-info")){
                source.sendMessage(Component.text( getPluginMessage("show-channel-info-command-title")));
                for(Channel channel: ChannelManager.getChannels().values()){
                    String channelDisplayName = source instanceof ConsoleCommandSource ?
                            Colors .parseColor( channel.getChannelDisplayName() ) : channel.getChannelDisplayName();
                    source.sendMessage(Component.text(channel.getChannelName() + " | " + channelDisplayName  + " : ")
                            .append(Component.text( channel.getOnlinePlayerCount() + " Player", NamedTextColor.GOLD)));
                    for(SubServer server : channel.getServers()){
                        String serverDisplayName = source instanceof ConsoleCommandSource ?
                                Colors .parseColor( server.getServerDisplayName() ) : server.getServerDisplayName();
                        source.sendMessage(Component.text("  " + serverDisplayName, NamedTextColor.GRAY)
                                .append(Component.text(" : ",NamedTextColor.GRAY))
                                .append(Component.text(server.getPlayers().size() + " ONLINE",NamedTextColor.GOLD)));
                    }
                }
                return;
            }
            if(args[0].equalsIgnoreCase("placeholder")){
                if(args.length == 2){
                    if(source instanceof  Player player){
                        source.sendMessage(Component.text( BasicMethods.placeValue(args[1],player)));
                    }
                }
                return;
            }
        }

        // commands that player may execute
        if(source instanceof Player sourcePlayer) {
            String language = Optional.ofNullable( sourcePlayer.getEffectiveLocale()).map(Locale::getLanguage).orElse("en");
            PlayerAttribution attrs = PlayerEvents.getPlayerAttrs().get(sourcePlayer.getUniqueId());
            List<String> players = VelocityPlugin.getAllPlayerName();
            //velochatx on command
            if (args[0].equalsIgnoreCase("on")) {
                attrs.setChatOffLine(false);
                source.sendMessage(Component.text(getMessage("enable-vc-chat-message", language), NamedTextColor.GREEN));
            } else if (args[0].equalsIgnoreCase("off")) {
                //velochatx off command
                attrs.setChatOffLine(true);
                source.sendMessage(Component.text(getMessage("disable-vc-chat-message", language), NamedTextColor.DARK_RED));
            } else if(args[0].equalsIgnoreCase("list")) {
                //velochatx list command
                if(!attrs.getIgnorePlayers().isEmpty()){
                    source.sendMessage(Component.text(getMessage("ignore-list-show-message", language), NamedTextColor.GREEN));
                    attrs.getIgnorePlayers().forEach(
                            playersUUID ->{
                                String name = VelocityPlugin.getPlayerName(playersUUID);
                                source.sendMessage(Component.text("§6*§r " + name));
                            }
                    );
                    if(!attrs.getRejectPlayers().isEmpty()){
                        source.sendMessage(Component.text(getMessage("reject-list-show-message", language), NamedTextColor.GREEN));
                        attrs.getRejectPlayers().forEach(playersUUID ->{
                            String name = VelocityPlugin.getPlayerName(playersUUID);
                            source.sendMessage(Component.text("§6*§r " + name));
                        });
                    }
                    return;
                }
                source.sendMessage(Component.text(getMessage("empty-list-show-message", language)));
            }else {
                //velochatx ignore command
                String command = args[0].toLowerCase();
                if(! defaultPlayerCommands.contains(command)) {
                    UnKnowMessage(sourcePlayer);
                    return;
                }
                if (args.length != 2) {
                    illegalArgsMsg(source);
                    return;
                }
                if (!(players.contains(args[1]))) {
                    sendWarnMessage(source, getMessage("fail-find-player-message", language).formatted(args[1]));
                    return;
                }
                if (sourcePlayer.getUsername().equals(args[1])) {
                    sendWarnMessage(source, getMessage("no-self-action-message", language));
                    return;
                }
                Player targetPlayer = VelocityPlugin.getProxyServer().getPlayer(args[1]).get();
                UUID targetUUID = targetPlayer.getUniqueId();
                // velochatX ignore / reject / remove
                switch (command) {
                    case "ignore" -> {
                        attrs.addIgnorePlayers(targetUUID);
                        sendRawMessage(source, MessageManager.convertMessage("has-ignore-message", targetPlayer, source));
                    }
                    case "reject" -> {
                        attrs.addRejectPlayers(targetUUID);
                        sendRawMessage(source, MessageManager.convertMessage("has-reject-message", targetPlayer, source));
                        sendRawMessage(targetPlayer, MessageManager.convertMessage("msg-reject-message", source, targetPlayer));
                    }
                    case "remove" -> {
                        Set<UUID> mutedPlayers = new HashSet<>();
                        mutedPlayers.addAll(attrs.getRejectPlayers());
                        mutedPlayers.addAll(attrs.getIgnorePlayers());
                        if (mutedPlayers.contains(targetUUID)) {
                            attrs.removeIJ(targetUUID);
                            sendRawMessage(source, MessageManager.convertMessage("normal-chat-message", targetPlayer, source));
                            sendRawMessage(targetPlayer, MessageManager.convertMessage("normal-chat-message", source, targetPlayer));
                        } else {
                            sendRawMessage(source, MessageManager.convertMessage("no-in-list-message", targetPlayer, source));
                        }
                    }
                    default -> UnKnowMessage(source);
                }
            }
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        int argNum = invocation.arguments().length;
        String[] args = invocation.arguments();
        if(argNum == 0 || argNum == 1) {
            if (invocation.source() instanceof ConsoleCommandSource) {
                return consoleCommands;
            } else {
                if(invocation.source().hasPermission("velochatx.admin")){
                    return adminCommands;
                }
                return defaultPlayerCommands;
            }
        }else if(argNum == 2) {
            if(invocation.source() instanceof ConsoleCommandSource){
                if(args[0].equalsIgnoreCase("reload")){
                    return configFiles;
                }
            }else{
                if(playerActions.contains(args[0])) {
                    return PlayerManager.getAllPlayer(invocation.source());
                }
            }
        }
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("velochatx.player");
    }

    @Override
    public void register(VelocityPlugin plugin){
        this.register(plugin,this,PRIMARY_ALIAS,ALIASES);
    }
}
