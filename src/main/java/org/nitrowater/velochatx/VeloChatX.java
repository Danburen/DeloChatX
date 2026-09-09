package org.nitrowater.velochatx;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.nitrowater.velochatx.events.PlayerEvents;
import org.nitrowater.velochatx.manager.ChannelManager;
import org.waterwood.enums.COLOR;
import org.waterwood.plugin.velocity.VelocityPlugin;
import org.nitrowater.velochatx.commands.ControlCommands;
import org.nitrowater.velochatx.commands.MentionCommand;
import org.nitrowater.velochatx.commands.MsgCommand;
import org.nitrowater.velochatx.manager.BasicMethods;
import org.waterwood.utils.DebugUtil;


@Plugin(
        id = "velochatx",
        name = "VeloChatX",
        authors = "Waterwood",
        version = "2.0.8")
public class VeloChatX extends VelocityPlugin {
    private final ProxyServer server;
    private static VeloChatX Instance;
    private final Metrics.Factory metricsFactory;
    @Inject
    public VeloChatX(ProxyServer server ,Metrics.Factory metricsFactory){
        super(server);
        this.server = server;
        this.metricsFactory = metricsFactory;
    }


    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        showPluginTitle("VeloChatX");
        long start = System.currentTimeMillis();
        Instance = this;
        this.loadConfig();
        DebugUtil.init(Boolean.TRUE.equals(getConfigs().get("debug")), loggerService, this);
        logMsg(getPluginMessage("init-process-message"));
        if(! getConfigs().get("enable",true)) {
            loggerService.logMessage(getPluginMessage("plugin-disable-message"),COLOR.RED);
            return;
        }
        init();
        logMsg(String.format(getPluginMessage("successfully-enable-message"),System.currentTimeMillis() - start));
        Metrics metrics = metricsFactory.make(this, 23273);
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {

    }
    
    public void init(){
        BasicMethods.load();
        server.getEventManager().register(this, new PlayerEvents());
        registerCommands();
        checkUpdate("Danburen","VeloChatX","2.0.1",
                getConfigs(),
                ChannelManager.getBroadcastConfigs());
    }

    public void registerCommands(){
        new MentionCommand().register(this);
        new MsgCommand().register(this);
        new ControlCommands().register(this);
    }
}
