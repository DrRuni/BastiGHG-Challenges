package runi.myddns.challenges.games.MobArmyBattle.Managers.World;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import runi.myddns.challenges.games.MobArmyBattle.MobArmyBattleGame;

import java.io.File;
import java.io.IOException;

public class WorldSettings {

    private final MobArmyBattleGame game;

    private File file;

    private boolean randomizerEnabled;
    private boolean chestRandomizerEnabled;
    private boolean arenaCompassEnabled;

    public WorldSettings(MobArmyBattleGame game) {
        this.game = game;
        load();
    }

    public void load() {

        file = new File(
                game.getDataFolder(),
                "gamesettings.yml"
        );

        FileConfiguration config =
                YamlConfiguration.loadConfiguration(file);

        randomizerEnabled =
                config.getBoolean(
                        "game-settings.randomizer-enabled",
                        true
                );

        chestRandomizerEnabled =
                config.getBoolean(
                        "game-settings.chest-randomizer-enabled",
                        true
                );

        arenaCompassEnabled =
                config.getBoolean(
                        "game-settings.arena-compass-enabled",
                        false
                );
    }

    public void save() {

        if (file == null) return;

        FileConfiguration config =
                YamlConfiguration.loadConfiguration(file);

        config.set(
                "game-settings.randomizer-enabled",
                randomizerEnabled
        );

        config.set(
                "game-settings.chest-randomizer-enabled",
                chestRandomizerEnabled
        );

        config.set(
                "game-settings.arena-compass-enabled",
                arenaCompassEnabled
        );

        try {

            config.save(file);

        } catch (IOException e) {

            game.getPlugin()
                    .getLogger()
                    .log(
                            java.util.logging.Level.SEVERE,
                            "Failed to save gamesettings.yml.",
                            e
                    );
        }
    }

    public boolean isRandomizerEnabled() {
        return randomizerEnabled;
    }

    public boolean isChestRandomizerEnabled() {
        return chestRandomizerEnabled;
    }

    public boolean isArenaCompassEnabled() {
        return arenaCompassEnabled;
    }

    public void toggleRandomizer() {
        randomizerEnabled = !randomizerEnabled;
        save();
    }

    public void toggleChestRandomizer() {
        chestRandomizerEnabled = !chestRandomizerEnabled;
        save();
    }

    public void toggleArenaCompassEnabled() {
        arenaCompassEnabled = !arenaCompassEnabled;
        save();
    }
}