package org.nitrowater.velochatx.mapper;

import org.nitrowater.velochatx.VeloChatX;
import org.nitrowater.velochatx.utils.PlayerAttribution;
import org.waterwood.io.file.FileMapperBase;
import org.waterwood.utils.JsonStringParser;

import java.io.File;
import java.sql.Timestamp;
import java.util.*;

import com.google.gson.JsonObject;

/**
 * A class to handle player data with file
 * @since 2.0.0
 * @author Danburen
 */
@Deprecated
public class FilePlayerMapper extends FileMapperBase {

    private static final String USERDATA_PATH = VeloChatX.getInstance().getDefaultFilePath("userdata" + File.pathSeparator);

    public FilePlayerMapper() {
        super(USERDATA_PATH);
    }

    // Create a new player and store in a file
    public void insertPlayerRecord(UUID uuid, String name) {
        String playerFile = USERDATA_PATH + uuid.toString() + ".json";

        JsonObject playerJson = new JsonObject();
        playerJson.addProperty("uuid", uuid.toString());
        playerJson.addProperty("name", name);

        JsonObject statusJson = new JsonObject();
        statusJson.addProperty("first_join_time", new Timestamp(System.currentTimeMillis()).toString());
        statusJson.addProperty("is_chat_offline", 0);  // Default to online
        statusJson.add("ban_list", new JsonObject()); // Empty ban list

        playerJson.add("player_status", statusJson);

        writeJsonFile(playerFile, playerJson);
    }

    // Update the player's name in the file
    public void updatePlayerName(UUID uuid, String newName) {
        String playerFile = USERDATA_PATH + uuid.toString() + ".json";
        JsonObject playerJson = readJsonFile(playerFile);

        if (playerJson != null) {
            playerJson.addProperty("name", newName);
            writeJsonFile(playerFile, playerJson);
        }
    }

    // Update the player's ban list in the file
    public void updatePlayerBanList(UUID uuid, PlayerAttribution attribution) {
        String playerFile = USERDATA_PATH + uuid.toString() + ".json";
        JsonObject playerJson = readJsonFile(playerFile);

        if (playerJson != null) {
            JsonObject statusJson = playerJson.getAsJsonObject("player_status");

            String jsonData = JsonStringParser.hashSetToJson(
                    List.of(attribution.getIgnorePlayers(), attribution.getRejectPlayers()),
                    "ignoreList", "rejectList"
            );
            statusJson.addProperty("ban_list", jsonData);
            writeJsonFile(playerFile, playerJson);
        }
    }

    // Update the player's chat offline status
    public void updatePlayerChatOffline(UUID uuid, boolean offline) {
        String playerFile = USERDATA_PATH + uuid.toString() + ".json";
        JsonObject playerJson = readJsonFile(playerFile);

        if (playerJson != null) {
            JsonObject statusJson = playerJson.getAsJsonObject("player_status");
            statusJson.addProperty("is_chat_offline", offline ? 1 : 0);
            writeJsonFile(playerFile, playerJson);
        }
    }

    // Update the player's first join time
    public void updatePlayerFirstJoinTime(UUID uuid, Timestamp firstJoinTime) {
        String playerFile = USERDATA_PATH + uuid.toString() + ".json";
        JsonObject playerJson = readJsonFile(playerFile);

        if (playerJson != null) {
            JsonObject statusJson = playerJson.getAsJsonObject("player_status");
            statusJson.addProperty("first_join_time", firstJoinTime.toString());
            writeJsonFile(playerFile, playerJson);
        }
    }

    // Update the player's left time
    public void updatePlayerLeftTime(UUID uuid, Timestamp leftTime) {
        String playerFile = USERDATA_PATH + uuid.toString() + ".json";
        JsonObject playerJson = readJsonFile(playerFile);

        if (playerJson != null) {
            JsonObject statusJson = playerJson.getAsJsonObject("player_status");
            statusJson.addProperty("left_time", leftTime.toString());
            writeJsonFile(playerFile, playerJson);
        }
    }

    // Get the player's name from the file
    public String getPlayerName(UUID uuid) {
        String playerFile = USERDATA_PATH + uuid.toString() + ".json";
        JsonObject playerJson = readJsonFile(playerFile);

        if (playerJson != null) {
            return playerJson.get("name").getAsString();
        }

        return null;
    }

    // Get a map of all players' names and UUIDs
    public Map<String, String> getPlayerNames() {
        Map<String, String> playerInfoMap = new LinkedHashMap<>();

        File userdataFolder = new File(USERDATA_PATH);
        File[] files = userdataFolder.listFiles((dir, name) -> name.endsWith(".json"));

        if (files != null) {
            for (File file : files) {
                JsonObject playerJson = readJsonFile(file.getPath());
                if (playerJson != null) {
                    String name = playerJson.get("name").getAsString();
                    String uuid = playerJson.get("uuid").getAsString();
                    playerInfoMap.put(name, uuid);
                }
            }
        }

        return playerInfoMap;
    }

    // Get player attribution (ban list, etc.)
    public PlayerAttribution getPlayerAttribution(UUID uuid) {
        String playerFile = USERDATA_PATH + uuid.toString() + ".json";
        JsonObject playerJson = readJsonFile(playerFile);

        PlayerAttribution attribution = new PlayerAttribution();

        if (playerJson != null) {
            JsonObject statusJson = playerJson.getAsJsonObject("player_status");
            String banListJson = statusJson.get("ban_list").getAsString();
            Map<String, HashSet<String>> banListMap = JsonStringParser.JsonToStringHashMap(banListJson);

            banListMap.get("ignoreList").forEach(ignoreUUID -> attribution.addIgnorePlayers(UUID.fromString(ignoreUUID)));
            banListMap.get("rejectList").forEach(rejectUUID -> attribution.addRejectPlayers(UUID.fromString(rejectUUID)));
            attribution.setChatOffLine(!isPlayerChatOffline(uuid));
        }

        return attribution;
    }

    // Check if the player getBoolean chat offline
    public boolean isPlayerChatOffline(UUID uuid) {
        String playerFile = USERDATA_PATH + uuid.toString() + ".json";
        JsonObject playerJson = readJsonFile(playerFile);

        if (playerJson != null) {
            JsonObject statusJson = playerJson.getAsJsonObject("player_status");
            return statusJson.get("is_chat_offline").getAsInt() == 1;
        }

        return false;
    }
}