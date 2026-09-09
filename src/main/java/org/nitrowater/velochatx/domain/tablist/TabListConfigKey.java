package org.nitrowater.velochatx.domain.tablist;

import org.nitrowater.waterapi.domain.shared.config.ConfigKey;

/**
 * Configuration keys for tab list settings.
 * <p>
 * These keys correspond to entries in {@code config.yml} under the
 * {@code tab-list} section.
 * </p>
 *
 * @since 2.1.0
 * @author Danburen
 */
public enum TabListConfigKey implements ConfigKey {
    /** Whether tab list customization is enabled */
    ENABLED("tab-list.enable", false),
    /** Dynamic refresh ratio (0 = event-driven, (0,1] = mixed mode) */
    REFRESH_RATIO("tab-list.refresh-ratio", 0.5),
    /** Base refresh interval in milliseconds (0 = no refresh) */
    INTERVAL("tab-list.interval", 1000L),
    /** Format for each player's tab list entry */
    FORMAT("tab-list.format", "{server}{prefix}{player}{Suffix}"),
    /** Tab list header format */
    HEADER("tab-list.header", ""),
    /** Tab list footer format */
    FOOTER("tab-list.footer", "");

    private final String key;
    private final Object defaultValue;

    TabListConfigKey(String key, Object defaultValue) {
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
