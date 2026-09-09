package org.nitrowater.velochatx.domain.player;

import org.nitrowater.velochatx.mapper.PlayerMapper;
import org.nitrowater.waterapi.domain.model.UPlayer;
import org.nitrowater.waterapi.domain.placeholder.PlaceholderService;
import org.nitrowater.waterapi.domain.placeholder.PlaceholdersContributor;
import org.nitrowater.waterapi.domain.spi.PermissionService;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of {@link PlayerService} and {@link PlaceholdersContributor}.
 * <p>
 * Provides player-related operations and registers player placeholders.
 * </p>
 *
 * @since 2.1.0
 * @author Danburen
 */
public class PlayerServiceImpl implements PlayerService, PlaceholdersContributor {

    private final PermissionService permissionService;
    private final Map<UUID, PlayerAttribution> attributions = new ConcurrentHashMap<>();
    private final PlayerMapper playerMapper;

    public PlayerServiceImpl(PermissionService permissionService, PlayerMapper playerMapper) {
        this.permissionService = permissionService;
        this.playerMapper = playerMapper;
    }

    @Override
    public boolean storeOrUpdatePlayer(UPlayer player) {
        UUID uuid = player.getUuid();
        boolean exists = attributions.containsKey(uuid);
        if (!exists) {
            attributions.put(uuid, PlayerAttribution.builder().build());
        }
        // query the database for the player's name and update if necessary, use database as the reliable exists source
        String playerName = playerMapper.getPlayerName(uuid);
        if(playerName == null) {
            playerMapper.insertPlayerRecord(uuid, player.getLoginName());
            exists = false;
        } else {
            playerMapper.updatePlayerName(uuid, player.getLoginName());
            exists = true;
        }
        return exists;
    }

    @Override
    public Optional<PlayerAttribution> getPlayerAttribution(UUID uuid) {
        return Optional.ofNullable(attributions.get(uuid));
    }

    @Override
    public void updatePlayerAttribution(UUID uuid, PlayerAttribution attribution) {
        attributions.put(uuid, attribution);
    }

    @Override
    public void updateFirstJoinTime(UUID uuid, long time) {
        playerMapper.updatePlayerFirstJoinTime(uuid, new Timestamp(time));
    }

    @Override
    public void updateLeaveTime(UUID uuid, long time) {
       playerMapper.updatePlayerLeftTime(uuid, new Timestamp(time));
    }

    @Override
    public String getDisplayName(UPlayer player) {
        String prefix = permissionService.getPrefix(player).orElse("");
        String name = player.getLoginName();
        String suffix = permissionService.getSuffix(player).orElse("");
        return prefix + name + suffix;
    }

    @Override
    public Map<UUID, PlayerAttribution> getOnlinePlayerAttributions() {
        return Map.copyOf(attributions);
    }

    @Override
    public PlayerAttribution getOrCreateAttrs(UUID uuid) {
        return this.attributions.computeIfAbsent(uuid, k -> PlayerAttribution.builder().build());
    }

    @Override
    public void contributePlaceholders(PlaceholderService service) {
        service.register("player", ctx -> ctx.getPlayer()
                .map(UPlayer::getLoginName)
                .orElse(""));

        service.register("ping", ctx -> ctx.getPlayer()
                .map(p -> String.valueOf(p.getPing()))
                .orElse("0"));

        service.register("prefix", ctx -> ctx.getPlayer()
                .flatMap(permissionService::getPrefix)
                .orElse(""));

        service.register("suffix", ctx -> ctx.getPlayer()
                .flatMap(permissionService::getSuffix)
                .orElse(""));

        service.register("group", ctx -> ctx.getPlayer()
                .flatMap(permissionService::getGroup)
                .orElse(""));
    }
}
