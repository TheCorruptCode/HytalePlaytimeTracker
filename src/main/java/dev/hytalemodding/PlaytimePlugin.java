package dev.hytalemodding;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import dev.hytalemodding.commands.PlaytimeCommand;
import dev.hytalemodding.events.PlaytimeEvent;

import javax.annotation.Nonnull;

// DB stuff
import com.hydb.HyDBPlugin;
import com.hydb.api.HyDBAPI;
import com.hydb.api.Database;

public class PlaytimePlugin extends JavaPlugin {
    private HyDBAPI hydb;
    private Database database;

    public PlaytimePlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        try {
            hydb = HyDBPlugin.getAPI();
            System.out.println("[PlaytimeTracker] HyDB API acquired successfully!");
        } catch (IllegalStateException e) {
            System.err.println("[PlaytimeTracker] ERROR: HyDB is not loaded! Make sure HyDB plugin is installed.");
            return;
        }
        // Get or create a database
        database = hydb.getGlobalDatabase("playtimetracker");

        // Create a table
        if (!database.tableExists("users")) {
            // Create table
            database.createTable("users",
                    "id INTEGER PRIMARY KEY AUTOINCREMENT",
                    "uuid TEXT NOT NULL UNIQUE",
                    "username TEXT NOT NULL",
                    "playtime TEXT DEFAULT 0"
            );
        }

        PlaytimeEvent.init(database);
        this.getCommandRegistry().registerCommand(new PlaytimeCommand("playtime", "Check Playtime", database));
        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, PlaytimeEvent::onPlayerReady);
        this.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, PlaytimeEvent::onPlayerDisconnect);
    }
}
