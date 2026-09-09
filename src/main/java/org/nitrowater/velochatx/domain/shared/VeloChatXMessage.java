package org.nitrowater.velochatx.domain.shared;

import org.nitrowater.waterapi.domain.shared.common.MessageTemplate;

/**
 * VeloChatX plugin messages, loaded from locale resource files.
 * <p>
 * Keys correspond to entries in {@code locale/{locale}.properties}.
 * </p>
 *
 * @since 2.1.0
 * @author Danburen
 */
public enum VeloChatXMessage implements MessageTemplate {
    // Command messages
    SHOW_CHANNEL_INFO_COMMAND_TITLE("show-channel-info-command-title"),
    MENTION_COMMAND_FORMAT("mention-command-format-message"),
    MSG_COMMAND_FORMAT("msg-command-format-message"),
    RELOAD_COMMAND_FORMAT("reload-command-format-message"),
    HELP_COMMAND_FORMAT("help-command-format-message"),
    ON_COMMAND_FORMAT("on-command-format-message"),
    OFF_COMMAND_FORMAT("off-command-format-message"),
    IGNORE_COMMAND_FORMAT("ignore-command-format-message"),
    REJECT_COMMAND_FORMAT("reject-command-format-message"),
    REMOVE_COMMAND_FORMAT("remove-command-format-message"),
    CONTROL_COMMAND_FORMAT("control-command-format-message"),
    SHOW_CHANNEL_INFO_COMMAND_FORMAT("show-channel-info-command-format-message"),
    LIST_COMMAND_FORMAT("list-command-format-message"),
    PLACEHOLDER_COMMAND_FORMAT("placeholder-command-format-message"),
    // Chat messages
    BAN_WORDS_LOG("ban-words-log-message"),
    USE_DEFAULT_CHAT_FORMAT("use-default-chat-format-message"),
    FAIL_PARSING_CHAT_FORMAT("fail-parsing-chat-format-message"),
    CROSSING_CHAT_DISABLE("crossing-chat-disable-message"),
    BROADCAST_DISABLE("broadcast-disable-message"),
    // system messages
    FAIL_FIND_PLAYER("fail-find-player-message"),
    NO_SELF_ACTION("no-self-action-message"),
    UNKNOWN_COMMAND("unknown-command-message"),
    INCORRECT_COMMAND_ARGUMENTS("incorrect-command-arguments-message"),
    NO_PERMISSION("no-permission-message"),
    // player action messages
    BAN_WORDS("ban-words-message"),
    HAS_IGNORE("has-ignore-message"),
    HAS_REJECT("has-reject-message"),
    MSG_REJECT("msg-reject-message"),
    MSG_TO("msg-to-message"),
    MSG_RECEIVE("msg-receive-message"),
    MENTION_TO("mention-to-message"),
    MENTION_RECEIVE("mention-receive-message"),
    MENTION_TITLE_MAIN("mention-title-show-main"),
    MENTION_TITLE_SUB("mention-title-show-sub"),
    NORMAL_CHAT("normal-chat-message"),
    NO_IN_LIST("no-in-list-message"),
    IGNORE_LIST_SHOW("ignore-list-show-message"),
    REJECT_LIST_SHOW("reject-list-show-message"),
    EMPTY_LIST_SHOW("empty-list-show-message"),
    ENABLE_VC_CHAT("enable-vc-chat-message"),
    DISABLE_VC_CHAT("disable-vc-chat-message"),
    // config messages
    CONFIG_RELOAD_COMPLETED("config-reload-completed-message"),
    CONFIG_FILE_RELOAD("config-file-reload-message"),
    INCORRECT_CONFIG_FILE("incorrect-config-file-message"),
    MESSAGE_LOCALE_CANT_LOAD("cant-load-message");

    private final String key;

    VeloChatXMessage(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }
}
