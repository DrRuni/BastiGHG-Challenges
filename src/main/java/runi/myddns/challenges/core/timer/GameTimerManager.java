package runi.myddns.challenges.core.timer;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class GameTimerManager {

    private final JavaPlugin plugin;
    private final String gameId;

    private File file;
    private long seconds;
    private BukkitTask task;

    public GameTimerManager(
            JavaPlugin plugin,
            String gameId
    ) {
        this.plugin = plugin;
        this.gameId = gameId;
    }

    public void load() {
        file = new File(
                plugin.getDataFolder(),
                "games/" + gameId + "/gamedata.yml"
        );

        FileConfiguration config =
                YamlConfiguration.loadConfiguration(file);

        seconds = config.getLong(
                "timer.seconds",
                0L
        );
    }

    public void save() {
        if (file == null) return;

        FileConfiguration config =
                YamlConfiguration.loadConfiguration(file);

        config.set(
                "timer.seconds",
                seconds
        );

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(
                    Level.SEVERE,
                    "Timer konnte nicht gespeichert werden: " + gameId,
                    e
            );
        }
    }

    public void resume() {
        if (task != null) return;

        task = plugin.getServer()
                .getScheduler()
                .runTaskTimer(
                        plugin,
                        () -> seconds++,
                        20L,
                        20L
                );
    }

    public void pause() {
        if (task != null) {
            task.cancel();
            task = null;
        }

        save();
    }

    public void reset() {
        pause();
        seconds = 0L;
        save();
    }

    public boolean isRunning() {
        return task != null;
    }

    public long getSeconds() {
        return seconds;
    }

    public String getFormattedTime() {
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (days > 0) {
            return String.format(
                    "%dd %02dh %02dm %02ds",
                    days,
                    hours,
                    minutes,
                    secs
            );
        }

        if (hours > 0) {
            return String.format(
                    "%dh %02dm %02ds",
                    hours,
                    minutes,
                    secs
            );
        }

        if (minutes > 0) {
            return String.format(
                    "%dm %02ds",
                    minutes,
                    secs
            );
        }

        return String.format(
                "%ds",
                secs
        );
    }
}
