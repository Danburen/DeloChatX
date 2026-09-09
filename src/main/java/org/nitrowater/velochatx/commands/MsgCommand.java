package org.nitrowater.velochatx.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import org.nitrowater.velochatx.events.PlayerEvents;
import org.nitrowater.velochatx.manager.BasicMethods;
import org.nitrowater.velochatx.manager.PlayerManager;
import org.waterwood.utils.Colors;
import org.waterwood.plugin.velocity.VelocityPlugin;
import org.nitrowater.velochatx.manager.MessageManager;

import java.util.List;

public class MsgCommand extends VelocitySimpleCommand implements SimpleCommand {
    private final  static  String PRIMARY_ALIAS = "vcmsg";
    private final static String[] ALIASES = {"vctell"};
    @Override
    public void register(VelocityPlugin plugin){
            this.register(plugin,this,PRIMARY_ALIAS,ALIASES);

    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        if(args.length <2 ){
            illegalArgsMsg(source);
            return;
        }
        List<String> players = VelocityPlugin.getAllPlayerName();
        if(! players.contains(args[0])){
            failFindPlayerMsg(source,args[0]);
        }else{
            Player targetPlayer = VelocityPlugin.getProxyServer().getPlayer(args[0]).get();
            checkNoSelf(source,targetPlayer,() ->{
                StringBuilder message = new StringBuilder(args[1]);
                for(int i = 2 ; i < args.length ; i ++){
                    message.append(" ").append(args[i]);
                }
                if(source instanceof Player sourcePlayer){
                    if(PlayerEvents.getPlayerAttrs().get(targetPlayer.getUniqueId()).getRejectPlayers().contains(sourcePlayer.getUniqueId())){
                        sendRawMessage(source, MessageManager.convertMessage("msg-reject-message", targetPlayer, source));
                        return;
                    }
                    sendRawMessage(source, BasicMethods.placeValue(getMessage("msg-to-message").replace("{Message}", message.toString()),targetPlayer));
                    if(PlayerEvents.getPlayerAttrs().get(targetPlayer.getUniqueId()).getIgnorePlayers().contains(sourcePlayer.getUniqueId())){
                        return;
                    }
                    sendRawMessage(targetPlayer, BasicMethods.placeValue(getMessage("msg-receive-message").replace("{Message}", message.toString()),sourcePlayer));
                }else{
                    sendRawMessage(source, Colors.parseColor(
                            BasicMethods.placeValue(getMessage("msg-to-message").replace("{Message}", message.toString()),targetPlayer)));
                    sendRawMessage(targetPlayer, MessageManager.convertServer(getMessage("msg-receive-message").replace("{Message}", message.toString()), source,targetPlayer));
                }

            });
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if(invocation.arguments().length ==0) {
            return PlayerManager.getAllPlayer(invocation.source());
        }else{
            return List.of();
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("velochatx.player");
    }
}
