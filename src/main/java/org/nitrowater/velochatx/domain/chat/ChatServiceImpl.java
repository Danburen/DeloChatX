package org.nitrowater.velochatx.domain.chat;

import org.nitrowater.velochatx.domain.shared.VeloChatXMessage;
import org.nitrowater.waterapi.domain.logger.MessageLoggerService;
import org.nitrowater.waterapi.domain.shared.config.Config;
import org.nitrowater.waterapi.domain.shared.service.EventListener;
import org.nitrowater.waterapi.domain.kernel.application.KernelConfigUpdateEvent;
import org.nitrowater.waterapi.domain.model.UPlayer;
import org.nitrowater.waterapi.domain.model.UServer;
import org.nitrowater.waterapi.domain.placeholder.PlaceholderContext;
import org.nitrowater.waterapi.domain.placeholder.PlaceholderService;
import org.nitrowater.waterapi.utils.StringUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of {@link ChatService}.
 * <p>
 * This service handles chat formatting and ban words filtering.
 * </p>
 *
 * @since 2.1.0
 * @author Danburen
 */
public class ChatServiceImpl implements ChatService, EventListener<KernelConfigUpdateEvent> {

    private volatile Config config;
    private final PlaceholderService placeholderService;
    private final MessageLoggerService messageLoggerService;

    private String chatFormat = "{server}{player}: {message}";

    /**
     * Create a new ChatServiceImpl.
     *
     * @param config             the config to read from
     * @param placeholderService the placeholder service for template resolution
     */
    public ChatServiceImpl(Config config, PlaceholderService placeholderService, MessageLoggerService messageLoggerService) {
        this.config = config;
        this.placeholderService = placeholderService;
        this.messageLoggerService = messageLoggerService;
        this.chatFormat = getConfig().get(ChatConfigKey.CHAT_FORMAT, "{server} {player}: {message}");
        if(!(chatFormat.contains("{server}") && chatFormat.contains("{player}") && chatFormat.contains("{message}"))) {
            chatFormat = "{server} {player}: {message}";
            messageLoggerService.warn(VeloChatXMessage.FAIL_PARSING_CHAT_FORMAT);
        }
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public void onEvent(KernelConfigUpdateEvent event) {
        this.config = event.getConfig();
    }

    @Override
    public String formatMessage(String message, UPlayer player) {
        return formatMessage(chatFormat, message, player);
    }

    @Override
    public String formatMessage(String template, String message, UPlayer player) {
        String serverName = player.getCurrentServer()
                .map(UServer::getName)
                .orElse("unknown");

        PlaceholderContext context = PlaceholderContext.builder()
                .player(player)
                .serverName(serverName)
                .build();

        Map<String, String> args = new HashMap<>();
        args.put("message", message);

        return placeholderService.resolve(template, context, args);
    }

    @Override
    public boolean hasBannedWords(String message) {
        if (!getConfig().getBoolean(ChatConfigKey.BAN_WORDS_ENABLED)) {
            return false;
        }

        String banWords = getConfig().get(ChatConfigKey.BAN_WORDS_LIST);
        if (StringUtil.isBlank(banWords)) {
            return false;
        }

        Set<String> bannedWords = new HashSet<>(Arrays.asList(banWords.split(",")));
        for (String bannedWord : bannedWords) {
            if (message.contains(bannedWord)) {
                return true;
            }
        }
        return false;
    }
}
