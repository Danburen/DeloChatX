package org.nitrowater.velochatx.mapper;

import com.google.gson.JsonParser;
import org.nitrowater.velochatx.utils.PlayerAttribution;
import org.nitrowater.waterapi.domain.infra.io.database.DatabaseHelper;
import org.nitrowater.waterapi.utils.JsonStringParser;
import org.waterwood.io.database.DatabaseHelper;
import org.waterwood.utils.JsonStringParser;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

/**
 * An operation class used to process database data
 * @since 2.0.0
 * @author Danburen
 */
public class PlayerMapper extends MapperBase {
    public PlayerMapper() {
        super();
    }
    @Override
    protected void createTable() {
        String createTableSQL =
                // player(uuid,name)
                "CREATE TABLE IF NOT EXISTS player (" +
                        "uuid TEXT PRIMARY KEY, "  +
                        "name TEXT NOT NULL, " +
                        "is_deleted INTEGER DEFAULT 0, " +
                        "player_ind INTEGER NOT NULL AUTO_INCREMENT); "  +
                // player_status(uuid,is_chat_offline,first_join_time,left_time)
                "CREATE TABLE IF NOT EXISTS player_status (" +
                        "uuid TEXT PRIMARY KEY," +
                        "is_chat_offline INTEGER DEFAULT 0," +
                        "first_join_time TIMESTAMP NOT NULL," +
                        "left_time TIMESTAMP DEFAULT NULL," +
                        "FOREIGN KEY (uuid) REFERENCES player(uuid) ON DELETE CASCADE ON UPDATE CASCADE ); "+
                // player_ban_list
                "CREATE TABLE IF NOT EXISTS player_ban_list (" +
                        "uuid TEXT PRIMARY KEY, " +
                        "ignore_list TEXT DEFAULT NULL, " +
                        "reject_list TEXT DEFAULT NULL, " +
                        "FOREIGN KEY (uuid) REFERENCES player(uuid) ON DELETE CASCADE ON UPDATE CASCADE ); ";
        try{
            executeCreateSQL(connection, createTableSQL);
        }catch (SQLException e){
            System.out.println("Error when create database" + e.getMessage());
        }
    }

    public void insertPlayerRecord(UUID uuid, String name) {
        String insertPlayerSQL = "INSERT INTO player (uuid, name) VALUES (?, ?)";
        String insertPlayerStatusSQL = "INSERT  INTO player_status (uuid,first_join_time) VALUES (?, ?)";
        String insertPlayerBanListSQL = "INSERT INTO player_ban_list (uuid) VALUES (?)";

        String uuidStr = uuid.toString();

        DatabaseHelper.executeSQL(connection,insertPlayerSQL, stmt -> {
            stmt.setString(1, uuidStr);
            stmt.setString(2, name);
        },"Error when insert player");
        DatabaseHelper.executeSQL(connection,insertPlayerStatusSQL,stmt -> {
            stmt.setString(1, uuidStr);
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
        },"Error when insert player_status");
        DatabaseHelper.executeSQL(connection,insertPlayerBanListSQL,stmt -> {
            stmt.setString(1, uuidStr);
        });
    }

    public void updatePlayerName(UUID uuid, String newName) {
        String updateSQL = "UPDATE player SET name = ? WHERE uuid = ?";
        DatabaseHelper.executeSQL(connection,updateSQL,stmt -> {
            stmt.setString(1, newName);
            stmt.setString(2, uuid.toString());
        });
    }

    /**
     * Persistent player ban list data
     * @param uuid uuid of player
     * @param ignore ignore set
     * @param reject reject set
     */
    public void updatePlayerBanList(UUID uuid, Set<UUID> ignore,Set<UUID> reject) {
        String updateSQL = "UPDATE player_ban_list SET ignore_list = ?,reject_list = ? WHERE uuid = ?";
        String ignoreJson = JsonStringParser.hashSetToJsonArray(ignore);
        String rejectJson = JsonStringParser.hashSetToJsonArray(reject);
        DatabaseHelper.executeSQL(connection,updateSQL,stmt -> {
            stmt.setString(1, ignoreJson);
            stmt.setString(2, rejectJson);
            stmt.setString(3, uuid.toString());
        },"Error when update player ban_list");
    }

    public void updatePlayerChatOffline(UUID uuid,boolean offline) {
        String updateSQL = "UPDATE player_status SET is_chat_offline = ? WHERE uuid = ?";
        DatabaseHelper.executeSQL(connection,updateSQL,stmt -> {
            stmt.setInt(1,offline ? 1 : 0);
            stmt.setString(2, uuid.toString());
        });
    }

    public void updatePlayerFirstJoinTime(UUID uuid, Timestamp firstJoinTime) {
        String updateSQL = "UPDATE player_status SET first_join_time = ? WHERE uuid = ?";
        DatabaseHelper.executeSQL(connection,updateSQL,stmt -> {
            stmt.setTimestamp(1, firstJoinTime);
            stmt.setString(2, uuid.toString());
        });
    }

    public void updatePlayerLeftTime(UUID uuid, Timestamp leftTime) {
        String updateSQL = "UPDATE player_status SET left_time = ? WHERE uuid = ?";
        DatabaseHelper.executeSQL(connection,updateSQL,stmt -> {
            stmt.setTimestamp(1, leftTime);
            stmt.setString(2, uuid.toString());
        });
    }

    /**
     * Get the player name from player table
     * if not exist return null
     * @param uuid uuid of player
     * @return player's name
     */
    public @Nullable String getPlayerName(UUID uuid) {
        String selectSQL = "SELECT name FROM player WHERE uuid = ?";
        final String[] playerName = new String[1];
        DatabaseHelper.executeSQL(connection,selectSQL,stmt -> {
            stmt.setString(1, uuid.toString());
        },"Error when selecting player's name",rs -> {
            if(rs.next()) {
                playerName[0] = rs.getString("name");
            }
        });
        return playerName[0];
    }

    /**
     * Return the map of player's info which made up of UUID and Name
     * @return player's info map
     */
    public Map<String,String> getPlayerMap() {
        String selectSQL = "SELECT * FROM player";
        final Map<String,String> playerInfoMap = new LinkedHashMap<>();
        DatabaseHelper.executeSQL(connection,selectSQL,stmt -> {}, rs -> {
            while(rs.next()) {
                playerInfoMap.put(rs.getString("name"),rs.getString("uuid"));
            }
        });
        return playerInfoMap;
    }

    public PlayerAttribution getPlayerAttribution(UUID uuid) {
        String selectSQL = "SELECT ignore_list,reject_list FROM player_ban_list WHERE uuid = ?";
        final PlayerAttribution attribution = new PlayerAttribution();
        DatabaseHelper.executeSQL(connection,selectSQL,stmt -> {
            stmt.setString(1,uuid.toString());
        },rs -> {
            if(rs.next()) {
                String ignore = rs.getString("ignore_list");
                if(ignore != null) {
                    JsonParser.parseString(rs.getString("ignore_list")).getAsJsonArray()
                            .forEach(uid-> attribution.addIgnorePlayers(UUID.fromString(uid.getAsString())));
                }
                String reject = rs.getString("reject_list");
                if(reject != null) {
                    JsonParser.parseString(rs.getString("reject_list")).getAsJsonArray()
                            .forEach(uid-> attribution.addRejectPlayers(UUID.fromString(uid.getAsString())));
                }
                attribution.setChatOffLine(getPlayerChatOffline(uuid));
            }else{
                System.out.println("No data found for UUID: " + uuid.toString());
            }

        });
       return attribution;
    }

    public boolean getPlayerChatOffline(UUID uuid) {
        String selectSQL = "SELECT is_chat_offline FROM player_status WHERE uuid = ?";
        final Integer[] offline = new Integer[1];
        DatabaseHelper.executeSQL(connection,selectSQL,stmt -> {
            stmt.setString(1,uuid.toString());
        }, rs->{
            if(rs.next()){
                int chatOffLine = rs.getInt("is_chat_offline");
                offline[0] = chatOffLine;
            }
        });
        return offline[0] == 1;
    }

    public void showPlayerStatus(UUID uuid) {
        String selectSQL = "SELECT * FROM player_status WHERE uuid = ?";
        DatabaseHelper.executeSQL(connection, selectSQL, stmt -> {
            stmt.setString(1,uuid.toString());
        }, rs -> {
            if(rs.next()){
                System.out.println("UUID: " + rs.getString("uuid"));
                System.out.println("First Join Time: " + rs.getString("first_join_time"));
                System.out.println("Left Time: " + rs.getString("left_time"));
                System.out.println("Is Chat Offline: " + rs.getString("is_chat_offline"));
            }
        });
        String selectSQL2 = "SELECT * FROM player_ban_list WHERE uuid = ?";
        DatabaseHelper.executeSQL(connection,selectSQL2,stmt -> {
            stmt.setString(1,uuid.toString());
        },rs->{
            if(rs.next()){
                System.out.println("UUID: " + rs.getString("uuid"));
                String ignoreList = rs.getString("ignore_list");
                if(ignoreList != null){
                    System.out.println("Ignore List: " + JsonParser.parseString(rs.getString("ignore_list")).getAsJsonArray().toString());
                }
                String rejectList = rs.getString("reject_list");
                if(rejectList != null){
                    System.out.println("Reject List: " + JsonParser.parseString(rs.getString("reject_list")).getAsJsonArray().toString());
                }
            }
        });
    }
}
