package org.nitrowater.velochatx.domain.shared;

import org.nitrowater.waterapi.domain.model.meta.MetaInfoType;

import java.util.List;

/**
 * VeloChatX meta info types.
 * <p>
 * Defines extension fields for {@link org.nitrowater.waterapi.domain.model.UServer}.
 * </p>
 */
public enum VeloChatXMetaInfoType implements MetaInfoType {

    /** Channel names this server belongs to */
    CHANNELS("channels", List.class),

    /** Whether welcome message is enabled for this server */
    WELCOME_ENABLED("welcome_enabled", Boolean.class),

    /** Welcome message template for this server */
    WELCOME_MESSAGE("welcome_message", String.class);

    private final String key;
    private final Class<?> valueType;

    VeloChatXMetaInfoType(String key, Class<?> valueType) {
        this.key = key;
        this.valueType = valueType;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Class<?> getValueType() {
        return valueType;
    }
}
