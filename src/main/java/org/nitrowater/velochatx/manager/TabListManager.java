package org.nitrowater.velochatx.manager;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.player.TabListEntry;
import com.velocitypowered.api.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.Map;

@Deprecated
public class TabListManager extends BasicMethods {
    private static final Map<Player,ScheduledTask> playerTasks = new HashMap<>();
    private static boolean TAB_LIST_ENABLE;
    private static String TAB_LIST_FORMAT;
    private static String HEADER_FORMAT;
    private static String FOOTER_FORMAT;
    public static void initialize(){
        TAB_LIST_FORMAT =  getConfigs().getString("tab-list.format");
        HEADER_FORMAT = getConfigs().getString("tab-list.header");
        FOOTER_FORMAT = getConfigs().getString("tab-list.footer");
        TAB_LIST_ENABLE = getConfigs().getBoolean("tab-list.enable");
    }

    public static void updateHeadAndFooter(Player player){
        player.sendPlayerListHeaderAndFooter(
                Component.text(placeValue(HEADER_FORMAT,player)),Component.text(placeValue(FOOTER_FORMAT,player)));
    }

    public static Map<Player,ScheduledTask> getAllPlayerTask(){
        return playerTasks;
    }

    public static String getTabListFormat() {
        return TAB_LIST_FORMAT;
    }

    public static String getHeaderFormat() {
        return HEADER_FORMAT;
    }

    public static String getFooterFormat() {
        return FOOTER_FORMAT;
    }

    public static boolean isTabListEnable() {
        return TAB_LIST_ENABLE;
    }

    public static void updateTabListUtils(Player player){
        player.getTabList().getEntries().forEach(tabListEntry ->
                proxyServer.getPlayer(tabListEntry.getProfile().getId()).ifPresent(p ->
                        tabListEntry.setDisplayName(Component.text(BasicMethods.placeValue(TAB_LIST_FORMAT, p)))));
    }

    public static void tabListAddPlayer(Player sourcePlayer,Player targetPlayer){
        if (targetPlayer.getTabList().containsEntry(sourcePlayer.getUniqueId())) return;
        targetPlayer.getTabList().addEntry(TabListEntry.builder()
                .profile(sourcePlayer.getGameProfile())
                .displayName(Component.text(BasicMethods.placeValue(TAB_LIST_FORMAT,sourcePlayer)))
                .tabList(targetPlayer.getTabList()).build());
    }
}
