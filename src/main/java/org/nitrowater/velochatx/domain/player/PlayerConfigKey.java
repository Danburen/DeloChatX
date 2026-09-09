package org.nitrowater.velochatx.domain.player;

import org.nitrowater.waterapi.domain.infra.config.ConfigKey;

/**
 * Configuration keys for player settings.
 * <p>
 * These keys correspond to entries in {@code config.yml} under the
 * player-related sections.
 * </p>
 *
 * @since 2.1.0
 * @author Danburen
 */
public enum PlayerConfigKey implements ConfigKey {
    /** Whether to store player data in database */
    STORAGE_ENABLED("player-storage.enable", true),
    /** Player data storage type (database/file) */
    STORAGE_TYPE("player-storage.type", "database");

    private final String key;
    private final Object defaultValue;

    PlayerConfigKey(String key, Object defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getKey() { return key; }

    @Override
    public Object getDefaultValue() { return defaultValue; }
}
