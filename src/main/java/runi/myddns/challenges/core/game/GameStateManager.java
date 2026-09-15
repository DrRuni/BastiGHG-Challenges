package runi.myddns.challenges.core.game;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class GameStateManager {

    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;

    public GameStateManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "game-state.yml");
        this.config = YamlConfiguration.loadConfiguration(file);

        if (!config.contains("selected-game")) config.set("selected-game", "none");
        if (!config.contains("loaded")) config.set("loaded", false);
        if (!config.contains("started")) config.set("started", false);

        save();
    }

    public void reset() {
        config.set("selected-game", "none");
        config.set("loaded", false);
        config.set("started", false);
        save();
    }

    public void resetRuntimeState() {
        config.set("loaded", false);
        config.set("started", false);
        save();
    }

    public void setSelectedGame(String gameId) {
        config.set("selected-game", gameId == null ? "none" : gameId);
        config.set("loaded", false);
        config.set("started", false);
        save();
    }

    public String getSelectedGame() {
        return config.getString("selected-game", "none");
    }

    public void setLoaded(boolean loaded) {
        config.set("loaded", loaded);

        if (!loaded) config.set("started", false);

        save();
    }

    public boolean isLoaded() {
        return config.getBoolean("loaded", false);
    }

    public void setStarted(boolean started) {
        config.set("started", started);
        save();
    }

    public boolean isStarted() {
        return config.getBoolean("started", false);
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("game-state.yml konnte nicht gespeichert werden: " + e.getMessage());
        }
    }
}
