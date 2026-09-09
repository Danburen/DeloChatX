package org.nitrowater.velochatx.domain.broadcast;

import org.nitrowater.waterapi.domain.shared.config.ConfigKey;

/**
 * Configuration keys for broadcast settings.
 * <p>
 * These keys correspond to entries in {@code broadcast.yml}.
 * </p>
 *
 * @since 2.1.0
 * @author Danburen
 */
public enum BroadcastConfigKey implements ConfigKey {
    // General
    /** Whether to randomize broadcast messages */
    RANDOM("random", true),
    /** Broadcast interval in seconds */
    INTERVAL("interval", 120L),

    // Global broadcast
    /** Whether global broadcast is enabled */
    GLOBAL_ENABLED("global.enable", true),
    /** Global broadcast prefix */
    GLOBAL_PREFIX("global.prefix", "§7[§b服务器娘§7]§r §8:§r"),

    // Locale (local) broadcast
    /** Whether locale broadcast is enabled */
    LOCALE_ENABLED("locale.enable", false),

    // Welcome broadcast
    /** Whether welcome broadcast is enabled */
    WELCOME_ENABLED("welcome-broadcast.enable", true),
    /** Whether to show welcome message only on first join */
    WELCOME_ONLY_FIRST_JOIN("welcome-broadcast.only-first-join", false),
    /** Welcome message template */
    WELCOME_MESSAGE("welcome-broadcast.message", "欢迎加入§5群组§r!"),

    // Entry broadcast (player join/leave)
    /** Whether to log entry broadcast messages to console */
    ENTRY_LOG_TO_CONSOLE("entry-broadcast.log-to-console", true),
    /** Whether to use hierarchical overlay mode (true) or hierarchical broadcast mode (false) */
    ENTRY_HIERARCHICAL_OVERLAY("entry-broadcast.hierarchical-overlay", true),

    // Backend entry broadcast
    /** Whether backend entry broadcast is enabled */
    BACKEND_ENABLED("entry-broadcast.backend.enable", true),
    /** Backend broadcast scope: server / channel / proxy */
    BACKEND_SCOPE("entry-broadcast.backend.scope", "server"),
    /** Backend player join message template */
    BACKEND_JOIN_MESSAGE("entry-broadcast.backend.join-message",
            "§a(+)§r{Group}{Prefix}{Player}{Suffix} 加入了 {channel}{Server}"),
    /** Backend player leave message template */
    BACKEND_LEAVE_MESSAGE("entry-broadcast.backend.leave-message",
            "§c(-)§r{Group}{Prefix}{Player}{Suffix} 离开了 {channel}{Server}"),

    // Channel entry broadcast
    /** Whether channel entry broadcast is enabled */
    CHANNEL_ENABLED("entry-broadcast.channel.enable", true),
    /** Channel broadcast scope: server / channel / proxy */
    CHANNEL_SCOPE("entry-broadcast.channel.scope", "channel"),
    /** Channel player join message template */
    CHANNEL_JOIN_MESSAGE("entry-broadcast.channel.join-message",
            "§7(+)§r{Group}{Prefix}{Player}{Suffix} 加入了 {channel}"),
    /** Channel player leave message template */
    CHANNEL_LEAVE_MESSAGE("entry-broadcast.channel.leave-message",
            "§7(-)§r{Group}{Prefix}{Player}{Suffix} 离开了 {channel}"),

    // Proxy entry broadcast
    /** Whether proxy entry broadcast is enabled */
    PROXY_ENABLED("entry-broadcast.proxy.enable", true),
    /** Proxy broadcast scope: server / channel / proxy */
    PROXY_SCOPE("entry-broadcast.proxy.scope", "proxy"),
    /** Proxy player join message template */
    PROXY_JOIN_MESSAGE("entry-broadcast.proxy.join-message",
            "§7[+]§r{Group}{Prefix}{Player}{Suffix} 加入了 {Proxy}"),
    /** Proxy player leave message template */
    PROXY_LEAVE_MESSAGE("entry-broadcast.proxy.leave-message",
            "§7[-]§r{Group}{Prefix}{Player}{Suffix} 离开了 {Proxy}");

    private final String key;
    private final Object defaultValue;

    BroadcastConfigKey(String key, Object defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }
}
