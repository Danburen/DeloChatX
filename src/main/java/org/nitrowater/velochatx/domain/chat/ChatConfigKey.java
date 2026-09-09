package org.nitrowater.velochatx.domain.chat;

import org.nitrowater.waterapi.domain.shared.config.ConfigKey;

/**
 * Configuration keys for chat settings.
 * <p>
 * These keys correspond to entries in {@code config.yml} under the
 * chat and ban-words sections.
 * </p>
 *
 * @since 2.1.0
 * @author Danburen
 */
public enum ChatConfigKey implements ConfigKey {
    /** Whether crossing (cross-server) chat getBoolean enabled */
    CROSSING_CHAT_ENABLED("crossing-chat-enable", true),
    /** Chat format template */
    CHAT_FORMAT("chat-format", "{channel}{Group}{Server}{Prefix}{Player}{Suffix} §8:§r {Message}"),
    /** Chat format model: none, legacy, mini-message */
    CHAT_FORMAT_MODEL("chat-format-model", "none"),

    // Ban words
    /** Whether ban words filter getBoolean enabled */
    BAN_WORDS_ENABLED("ban-words.enable", false),
    /** Whether to log ban word detection to console */
    BAN_WORDS_LOG("ban-words.log-to-console", false),
    /** Comma-separated list of banned words */
    BAN_WORDS_LIST("ban-words.words", ""),
    LOG_CHAT_ENABLED("log-text.enable", true),
    LOG_CHAT_CONVERT("log-text.convert", true),;

    private final String key;
    private final Object defaultValue;

    ChatConfigKey(String key, Object defaultValue) {
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
