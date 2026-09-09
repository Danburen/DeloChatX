package org.nitrowater.velochatx.domain.player;

import lombok.Builder;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Player attribution data (ignore/reject lists, chat offline status).
 *
 * @since 2.1.0
 * @author Danburen
 */
@Getter
@Builder
public class PlayerAttribution {

    @Builder.Default
    private final Set<UUID> ignorePlayers = new HashSet<>();

    @Builder.Default
    private final Set<UUID> rejectPlayers = new HashSet<>();

    @Builder.Default
    private boolean chatOffLine = false;

    public void addIgnorePlayer(UUID uuid) {
        ignorePlayers.add(uuid);
    }

    public void addRejectPlayer(UUID uuid) {
        rejectPlayers.add(uuid);
        ignorePlayers.add(uuid);
    }

    public void remove(UUID uuid) {
        ignorePlayers.remove(uuid);
        rejectPlayers.remove(uuid);
    }

    public boolean isEmpty() {
        return ignorePlayers.isEmpty() && rejectPlayers.isEmpty();
    }
}
