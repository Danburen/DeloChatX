package org.nitrowater.velochatx.domain.channel;

import org.nitrowater.waterapi.domain.shared.config.ConfigKey;

import java.util.ArrayList;

/**
 * Configuration keys for channel settings.
 * <p>
 * These keys correspond to entries in {@code config.yml} under the
 * {@code channels} section.
 * </p>
 *
 * @since 2.1.0
 * @author Danburen
 */
public enum ChannelConfigKey implements ConfigKey {
    BROADCAST_ENABLED("broadcast-enable", false),
    GLOBAL("channels.global", true),
    CHANNEL_LIST("channels.channel-list", new ArrayList<Channel>() ),;

    private final String key;
    private final Object defaultValue;

    ChannelConfigKey(String key, Object defaultValue) {
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
