package org.nitrowater.velochatx.domain.server;

import org.nitrowater.waterapi.domain.shared.config.ConfigKey;

import java.util.Map;

/**
 * Configuration keys for server display name settings.
 * <p>
 * These keys correspond to entries in {@code config.yml} under the
 * {@code server-display} section.
 * </p>
 *
 * @since 2.1.0
 * @author Danburen
 */
public enum ServerConfigKey implements ConfigKey {
    /** Whether server display name feature getBoolean enabled */
    DISPLAY_ENABLED("server-display.enable", false),
    /** Proxy server display name */
    PROXY_DISPLAY("server-display.proxy", "[§5Proxy Server§r]"),
    DISPLAY_SERVER_DISPLAY("server-display.display", Map.<String, String>of());

    private final String key;
    private final Object defaultValue;

    ServerConfigKey(String key, Object defaultValue) {
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
