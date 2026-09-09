package org.nitrowater.velochatx.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import org.nitrowater.velochatx.events.PlayerEvents;
import org.nitrowater.velochatx.manager.PlayerManager;
import org.nitrowater.velochatx.utils.TitleShow;
import org.waterwood.io.FileConfiguration;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.waterwood.plugin.velocity.VelocityPlugin;
import org.nitrowater.velochatx.manager.MessageManager;

import java.util.List;

public class  MentionCommand extends VelocitySimpleCommand implements SimpleCommand {
    private final  static  String PRIMARY_ALIAS = "mention";
    private final static String[] ALIASES = {"men","at"};
    public void register(VelocityPlugin plugin){
        this.register(plugin,this,PRIMARY_ALIAS,ALIASES);
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        List<String> players = VelocityPlugin.getAllPlayerName();
        if(args.length != 1){
            illegalArgsMsg(source);
            return;
        }
        if(! players.contains(args[0])){
            failFindPlayerMsg(source,args[0]);
        }else{
            Player targetPlayer = VelocityPlugin.getProxyServer().getPlayer(args[0]).get();
            if(source instanceof Player sourcePlayer){
                if(PlayerEvents.getPlayerAttrs().get(targetPlayer.getUniqueId()).getRejectPlayers().contains(sourcePlayer.getUniqueId())){
                    sendRawMessage(source, MessageManager.convertMessage("msg-reject-message", targetPlayer, source));
                    return;
                }
                if(PlayerEvents.getPlayerAttrs().get(targetPlayer.getUniqueId()).getIgnorePlayers().contains(sourcePlayer.getUniqueId())){
                    return;
                }
            }
            checkNoSelf(source,targetPlayer,() -> {
                FileConfiguration config = getConfigs();
                sendRawMessage(source, MessageManager.convertMessage("mention-to-message", targetPlayer, source));
                sendRawMessage(targetPlayer, MessageManager.convertMessage("mention-receive-message", source, targetPlayer));
                if(config.getBoolean("mention-show-title.enable")) {
                    String mainTitle = MessageManager.convertMessage("mention-title-show-main",source,targetPlayer);
                    String subTitle = MessageManager.convertMessage("mention-title-show-sub",source,targetPlayer);
                    int[] time = { config.getInteger("mention-show-title.time.fade-in"),
                            config.getInteger("mention-show-title.time.stay"),
                            config.getInteger("mention-show-title.time.fade-out") };
                    targetPlayer.showTitle(new TitleShow(mainTitle,subTitle,time));
                }
                targetPlayer.playSound(Sound.sound(Key.key("minecraft:block.note_block.pling"), Sound.Source.BLOCK,1f,1f),Sound.Emitter.self());

            });
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return PlayerManager.getAllPlayer(invocation.source());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("velochatx.player");
    }

}
