package org.nitrowater.velochatx.utils;

import java.util.HashSet;
import java.util.UUID;

/**
 * A class as player addition attribution container
 * @since 1.2.0
 * @author Danburen
 * @version 1.1.0
 */
public class PlayerAttribution {
    private HashSet<UUID> ignorePlayers;
    private HashSet<UUID> rejectPlayers;
    private boolean chatOffLine = false;

    public HashSet<UUID> getRejectPlayers() {
        return rejectPlayers;
    }

    public void HashSetRejectPlayers(HashSet<UUID> rejectPlayers) {
        this.rejectPlayers = rejectPlayers;
    }

    public HashSet<UUID> getIgnorePlayers() {
        return ignorePlayers;
    }

    public void HashSetIgnorePlayers(HashSet<UUID> ignorePlayers) {
        this.ignorePlayers = ignorePlayers;
    }

    public boolean isBanListEmpty() {
        return ignorePlayers.isEmpty();
    }

    public boolean isChatOffLine() { return chatOffLine; }

    public void addIgnorePlayers(UUID uuid){
        ignorePlayers.add(uuid);
    }
    public void addRejectPlayers(UUID uuid){
        ignorePlayers.add(uuid);
        rejectPlayers.add(uuid);
    }

    public void removeIJ(UUID uuid){
        ignorePlayers.remove(uuid);
        rejectPlayers.remove(uuid);
    }
    public PlayerAttribution(HashSet<UUID> ignorePlayers,HashSet<UUID> rejectPlayers){
        this.ignorePlayers = ignorePlayers;
        this.rejectPlayers = rejectPlayers;
    }

    public PlayerAttribution(){
        this.ignorePlayers = new HashSet<>();
        this.rejectPlayers = new HashSet<>();
        this.chatOffLine = true;
    }

    public void setChatOffLine(boolean offLine) {
        this.chatOffLine = offLine;
    }
}
