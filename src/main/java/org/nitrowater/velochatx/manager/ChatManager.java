package org.nitrowater.velochatx.manager;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.Getter;
import org.nitrowater.velochatx.entity.Channel;
import org.nitrowater.velochatx.utils.SubServer;
import org.waterwood.enums.ChatFormatModel;
import org.waterwood.hock.LuckPermsHock;
import org.waterwood.utils.Colors;

import java.util.*;

import static org.waterwood.hock.LuckPermsHock.hasLuckPerm;

/**
 * A class extends methods used to Control Player's chat
 * @since 1.4.0
 * @author Danburen(Waterwood)
 */
@Deprecated
public class ChatManager extends BasicMethods {

    private static boolean BanWordEnable;
    private static boolean BanWordLOG;

    private static String CHAT_FORMAT;
    private static boolean IS_CROSSING_CHAT_ENABLED;
    @Getter
    private static ChatFormatModel CHATFORMAT_MODEL;

    public static void initialize() {
        loadChatFormat();
        BanWordEnable = getConfigs().getBoolean("ban-words.enable",false);
        BanWordLOG = getConfigs().getBoolean("ban-words.log-to-console",false);
        IS_CROSSING_CHAT_ENABLED = getConfigs().getBoolean("crossing-chat-enable",true);

        CHATFORMAT_MODEL = switch (getConfigs().getString("chat-format-model","none").toLowerCase()) {
            case "legacy" -> ChatFormatModel.LEGACY;
            case "mini-message" -> ChatFormatModel.MINI_MESSAGE;
            default -> ChatFormatModel.NONE;
        };
    }
    /**
     * Judge whether two server can communicate
     * <p>
     * get two sub server's channel and get their intersections
     * if the intersections getBoolean empty , this indicates they can communicate
     * <b>otherwise</b> they cannot
     * @param source source of server
     * @param target target of server
     * @return whether they can communicate
     */
    public static boolean canCommunicate(SubServer source, SubServer target) {
        if(source == null || target == null) {
            return true;
        }
        Set<Channel> commonChannel= new HashSet<>(source.getChannels());
        commonChannel.retainAll(target.getChannels()); // intersections
        return !commonChannel.isEmpty(); // intersection getBoolean not null
    }

    /**
     * @see ChatManager#canCommunicate(SubServer, SubServer)
     * @param source source of server
     * @param target target of server
     * @return whether they can communicate
     */
    public static boolean canCommunicate(String source,String target){
        return canCommunicate(
                serverInfoMap.getOrDefault(source,null),
                serverInfoMap.getOrDefault(target,null)
        );
    }

    public static boolean canCommunicate(RegisteredServer source, RegisteredServer target){
        return canCommunicate(
                source.getServerInfo().getName(), target.getServerInfo().getName()
        );
    }

    public static boolean isBanWorldEnable() {
        return BanWordEnable;
    }
    public static boolean isBanWordLog() { return BanWordLOG; }

    public static boolean hasBanWords(String message){
        Set<String> banWords = new HashSet<>(Arrays.asList(
                getConfigs().getString("ban-words.words", "").split(",")));
        for(String banWord : banWords){
            if(message.contains(banWord)){
                return true;
            }
        }
        return false;
    }

    public static void loadChatFormat(){
        LuckPermsHock.checkApi();
        CHAT_FORMAT = getConfigs().getString("chat-format","[{Server}]{PlayerName}: {Message}");
        String chatFormat = CHAT_FORMAT.toLowerCase();
        if (chatFormat.contains("{message}") && chatFormat.contains("{player}") && chatFormat.contains("{server}")){
            if (hasLuckPerm()){
                return;
            }else{
                getLogger().info(Colors.parseColor(getPluginMessage("no-api-support-message")
                        .formatted("LuckPerms")));
            }
            return;
        }
        getLogger().warning(getPluginMessage("fail-parsing-chat-format-message")
                + "\n" + getPluginMessage("use-default-chat-format-message"));
        CHAT_FORMAT = "{Server}{Player} §7:§r {Message}";
    }

    public static String placeChatValue(String message, Player player){
        return placeChatValue(CHAT_FORMAT,message,player);
    }

    public static String placeChatValue(String origin, String message, Player player){
        return placeValue(origin,player).replace("{message}",message);
    }

    public static boolean isCrossingChatEnabled() {
        return IS_CROSSING_CHAT_ENABLED;
    }
}
