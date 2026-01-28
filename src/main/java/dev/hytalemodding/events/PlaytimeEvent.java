package dev.hytalemodding.events;

import com.hydb.api.Database;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.time.Instant;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class PlaytimeEvent {
    private static final Map<String, Instant> joinTimes = new HashMap<>();
    private static Database database;

    public static void init(Database db) {
        database = db;
    }

    public static void onPlayerReady(PlayerReadyEvent event) {
        if (database == null) {
            System.err.println("[PlaytimeTracker] ERROR: database not initialized.");
            return;
        }

        Player player = event.getPlayer();
        String playerKey = player.getPlayerRef().getUuid().toString();
        joinTimes.put(playerKey, Instant.now());
       // player.sendMessage(Message.raw("Welcome " + player.getDisplayName()));

        database.queryFirstAsync("SELECT * FROM users WHERE uuid = ?", playerKey)
        .thenAccept(result -> {
            if (result == null) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("uuid", playerKey);
                userData.put("username", player.getPlayerRef().getUsername());
                userData.put("playtime", "0");

                int rowsAffected = database.insert("users", userData);
            }
        });


    }

    public static void onPlayerDisconnect(PlayerDisconnectEvent event) {
        if (database == null) {
            return;
        }

        PlayerRef playerRef = event.getPlayerRef();
        String playerKey = playerRef.getUuid().toString();
        Instant joinTime = joinTimes.remove(playerKey);
        if (joinTime == null) {
            return;
        }

        Duration playtime = Duration.between(joinTime, Instant.now());

        Map<String, Object> userData = new HashMap<>();
        userData.put("playtime", playtime.toString());

        database.updateAsync("users", userData, "uuid = ?", playerKey)
        .thenAccept(rowsAffected -> {
            System.out.println("Updated " + rowsAffected + " row(s)");
        });
    }

}
