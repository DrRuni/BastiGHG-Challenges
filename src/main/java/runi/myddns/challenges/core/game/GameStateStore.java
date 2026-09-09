package runi.myddns.challenges.core.game;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class GameStateStore {

    private final File file;
    private final YamlConfiguration config;

    public GameStateStore(
            JavaPlugin plugin
    ) {

        this.file =
                new File(
                        plugin.getDataFolder(),
                        "state.yml"
                );

        this.config =
                YamlConfiguration.loadConfiguration(
                        file
                );
    }

    public String getSelectedGame() {
        return config.getString(
                "selected-game",
                ""
        );
    }

    public void setSelectedGame(
            String gameId
    ) {

        config.set(
                "selected-game",
                gameId
        );

        save();
    }

    public String getLoadedGame() {
        return config.getString(
                "loaded-game",
                ""
        );
    }

    public void setLoadedGame(
            String gameId
    ) {

        config.set(
                "loaded-game",
                gameId
        );

        save();
    }

    private void save() {

        try {

            config.save(file);

        } catch (IOException exception) {

            exception.printStackTrace();
        }
    }
}
