package dev.hytalemodding.commands;

import com.hydb.api.Database;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class PlaytimeCommand extends AbstractCommand {

    private final Database database;

    public PlaytimeCommand(String name, String description, Database database) {
        super(name, description);
        this.database = database;
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

    @Nullable
    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        Player player = (Player) context.sender();

        database.queryFirstAsync("SELECT * FROM users WHERE uuid = ?", player.getPlayerRef().getUuid().toString())
        .thenAccept(result -> {
            if (result != null) {
                String rawPlaytime = String.valueOf(result.get("playtime"));
                double minutes = 0.0;

                try {
                    if (rawPlaytime.startsWith("PT") || rawPlaytime.startsWith("-PT")) {
                        Duration duration = Duration.parse(rawPlaytime);
                        minutes = duration.toMillis() / 60000.0;
                    } else if (!rawPlaytime.isEmpty()) {
                        minutes = Double.parseDouble(rawPlaytime);
                    }
                } catch (Exception ignored) {
                    minutes = 0.0;
                }

                DecimalFormat format = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.US));
                String minutesText = format.format(minutes);
                String unit = "1".equals(minutesText) ? "minute" : "minutes";
                context.sendMessage(Message.raw("Player has " + minutesText + " " + unit + " on server!"));
            }
        });

        return CompletableFuture.completedFuture(null);
    }

}
