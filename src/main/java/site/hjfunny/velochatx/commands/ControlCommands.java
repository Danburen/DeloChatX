package site.hjfunny.velochatx.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import me.waterwood.VelocityPlugin;
import me.waterwood.common.Colors;
import me.waterwood.common.PluginBase;
import me.waterwood.plugin.Plugin;
import me.waterwood.plugin.WaterPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import site.hjfunny.velochatx.PlayerAttribution;
import site.hjfunny.velochatx.events.PlayerEvents;
import site.hjfunny.velochatx.methods.Methods;
import site.hjfunny.velochatx.methods.MsgMethods;

import java.util.*;

public class ControlCommands extends VelocitySimpleCommand implements SimpleCommand {
    private static final String PRIMARY_ALIAS = "velochatx";
    private static final String[] ALIASES = {"vcx","chatx","vc"};
    public static final ArrayList<String> configFiles = new ArrayList<>(Arrays.asList("config","message"));
    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        List<String> subCmds = this.suggest(invocation);
        if(args.length < 1 || args.length > 2){
            illegalArgsMsg(source,"control");
            return;
        }
        if(args[0].equalsIgnoreCase("reload")) { //reload command
            if (source.hasPermission("velochatx.admin")) {
                if (args.length == 1) {
                    WaterPlugin.upgradeConfig();
                    PluginBase.reloadConfig();
                    Methods.load();
                    source.sendMessage(Component.text(getMessage("config-reload-completed-message"), NamedTextColor.GREEN));
                    return;
                }
                if (configFiles.contains(args[1])) {
                    WaterPlugin.getConfig().reloadConfig(args[1] + ".yml");
                    source.sendMessage(Component.text(getMessage("config-file-reload-message")
                            .formatted(args[1].concat(".yml")), NamedTextColor.GREEN));
                } else {
                    source.sendMessage(Component.text(String.format(getMessage("incorrect-config-file-message"), args[1], configFiles), NamedTextColor.RED));
                }
            } else {
                source.sendMessage(Component.text(getMessage("no-permission"), NamedTextColor.RED));
            }
            return;
        }
        if(source instanceof Player sourcePlayer) {
            String language = sourcePlayer.getEffectiveLocale().getLanguage();
                if (args[0].equalsIgnoreCase("help")) {
                    source.sendMessage(Component.text("---------------------------\n§1VeloChatX§r  §bv%s§r  §2By:%s§r \n---------------------------"
                            .formatted(WaterPlugin.getPluginInfo("version"), WaterPlugin.getPluginInfo("author"))));
                    for (String cmd : subCmds) {
                        source.sendMessage(Component.text(getMessage(cmd + "-command-format-message", language)));
                    }
                    return;
                } else {
                    PlayerAttribution attrs = PlayerEvents.getPlayerAttrs().get(sourcePlayer.getUsername());
                    List<String> players = VelocityPlugin.getAllPlayerName();
                    if (args[0].equalsIgnoreCase("on")) { //velochatx on command
                        attrs.setAccess(true);
                        source.sendMessage(Component.text(getMessage("enable-vc-chat-message", language), NamedTextColor.GREEN));
                    } else if (args[0].equalsIgnoreCase("off")) { //velochatx off command
                        attrs.setAccess(false);
                        source.sendMessage(Component.text(getMessage("disable-vc-chat-message", language), NamedTextColor.DARK_RED));
                    } else { //velochatx ignore command
                        String command = args[0].toLowerCase();
                        if (args.length != 2) {
                            illegalArgsMsg(source, "ignore");
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
                        if (command.equals("ignore")) {      // velochatX ignore command
                            attrs.addIgnorePlayers(args[1]);
                            sendRawMessage(source, MsgMethods.convertMessage("has-ignore-message",targetPlayer,source));
                        } else if(command.equals("reject")){                              // velochatX reject command
                            attrs.addRejectPlayers(args[1]);
                            sendRawMessage(source, MsgMethods.convertMessage("has-reject-message",targetPlayer,source));
                        } else if(command.equals("remove")){
                            Set<String> mutedPlayers = new HashSet<>();
                            mutedPlayers.addAll(attrs.getRejectPlayers());
                            mutedPlayers.addAll(attrs.getIgnorePlayers());
                            if(mutedPlayers.contains(args[1])){
                                attrs.removeIJ(args[1]);
                                sendRawMessage(source, MsgMethods.convertMessage("normal-chat-message",targetPlayer,source));
                                sendRawMessage(targetPlayer, MsgMethods.convertMessage("normal-chat-message",source,targetPlayer));
                            }else{
                                sendRawMessage(source, MsgMethods.convertMessage("no-in-list-message",targetPlayer,source));
                            }
                        }else{
                            UnKnowMessage(source);
                        }
                    }
                }
        }else{
            if (args[0].equalsIgnoreCase("help")) { //help command
                source.sendMessage(Component.text(Colors.parseColor("---------------------------\n§1VeloChatX§r  §bv%s§r  §2By:%s§r\n---------------------------"
                        .formatted(WaterPlugin.getPluginInfo("version"), WaterPlugin.getPluginInfo("author")))));
                for (String cmd : subCmds) {
                    source.sendMessage(Component.text(Colors.parseColor(getMessage(cmd + "-command-format-message"))));
                }
            }
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if(invocation.arguments().length == 0 || invocation.arguments().length == 1 ) {
            if (invocation.source() instanceof ConsoleCommandSource) {
                return List.of("help", "reload");
            } else {
                return List.of("help", "on", "off", "ignore", "reject", "remove");
            }
        }else{
            return List.of();
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("velochatx.player");
    }

    @Override
    public void register(VelocityPlugin plugin){
        this.register(plugin,this,PRIMARY_ALIAS,ALIASES,false);
    }
}
