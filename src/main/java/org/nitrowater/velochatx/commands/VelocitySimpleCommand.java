package org.nitrowater.velochatx.commands;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.waterwood.plugin.velocity.util.MethodBase;
import org.waterwood.utils.Colors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.waterwood.plugin.velocity.VelocityPlugin;

import java.util.Arrays;
import java.util.List;

public abstract class VelocitySimpleCommand extends MethodBase {
    public void register(VelocityPlugin plugin, SimpleCommand command, String PRIMARY_ALIAS, String[] ALIASES){
        ProxyServer proxy= VelocityPlugin.getProxyServer();
        CommandManager commandManager = proxy.getCommandManager();
        CommandMeta meta = commandManager.metaBuilder(PRIMARY_ALIAS).aliases(ALIASES).plugin(plugin).build();
        commandManager.register(meta,command);
    }



    public void illegalArgsMsg(CommandSource source){
        if(source instanceof Player sourcePlayer) {
            source.sendMessage(Component.text(getPluginMessage("incorrect-command-arguments-message"), NamedTextColor.RED));
        }else{
            source.sendMessage(Component.text(Colors.parseColor(getPluginMessage("incorrect-command-arguments-message")), NamedTextColor.RED));
        }
    }

    public void failFindPlayerMsg(CommandSource source,String playerName){
        if(source instanceof Player sourcePlayer){
            source.sendMessage(Component.text(getMessage("fail-find-player-message",
                    sourcePlayer.getEffectiveLocale().getLanguage()).formatted(playerName)));
        }else{
            source.sendMessage(Component.text(Colors.parseColor(getPluginMessage("fail-find-player-message").formatted(playerName))));
        }
    }

    public void checkNoSelf(CommandSource source,Player targetPlayer,commandHandler handler){
        if (source.equals(targetPlayer)) {
            source.sendMessage(Component.text(getMessage("no-self-action-message",targetPlayer.getEffectiveLocale().getLanguage())));
                return;
        }
        handler.execute();
    }
    public void sendRawMessage(CommandSource source, String msg){
        source.sendMessage(Component.text(msg));
    }

    public void sendWarnMessage(CommandSource source, String msg){
        source.sendMessage(Component.text(msg,NamedTextColor.RED));
    }

    public void isUnknownCommand(CommandSource source,List<String> commands,String command,commandHandler handler){
        if(!(commands.contains(command))){
            if(source instanceof Player sourcePlayer){
                source.sendMessage(Component.text(getMessage("unknown-command-message",
                        sourcePlayer.getEffectiveLocale().getLanguage()),NamedTextColor.RED));
            }else{
                sendWarnMessage(source,getMessage("unknown-command-message"));
            }
        }
        handler.execute();
    }

    public void UnKnowMessage(CommandSource source){
        if(source instanceof Player sourcePlayer){
            source.sendMessage(Component.text(getMessage("unknown-command-message",
                    sourcePlayer.getEffectiveLocale().getLanguage()),NamedTextColor.RED));
        }else{
            sendWarnMessage(source,"unknown-command-message");
        }
    }
    public abstract void register(VelocityPlugin plugin);

    public interface commandHandler{
        void execute();
    }
}

class ForwardingCommand implements SimpleCommand{
    private final SimpleCommand fcmd;
    ForwardingCommand(SimpleCommand fcmd){
        this.fcmd = fcmd;
    }

    @Override
    public void execute(Invocation invocation) {
        this.fcmd.execute(invocation);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return this.fcmd.suggest(invocation);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return this.fcmd.hasPermission(invocation);
    }
}

