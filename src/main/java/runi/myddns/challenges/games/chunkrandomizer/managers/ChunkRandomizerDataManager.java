
package runi.myddns.challenges.games.chunkrandomizer.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import runi.myddns.challenges.ChallengeMain;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

public class ChunkRandomizerDataManager {

    private static final String RESOURCE_PATH =
            "games/chunkrandomizer/gamedata.yml";

    private final ChallengeMain plugin;
    private final File dataFile;

    private FileConfiguration data;

    public ChunkRandomizerDataManager(ChallengeMain plugin) {
        this.plugin = plugin;

        this.dataFile = new File(
                plugin.getDataFolder(),
                RESOURCE_PATH
        );
    }

    public void load() {

        if (!dataFile.exists()) {
            throw new IllegalStateException(
                    "ChunkRandomizer gamedata.yml fehlt: "
                            + dataFile.getPath()
            );
        }

        data = YamlConfiguration.loadConfiguration(dataFile);

        plugin.getLogger().info(
                "ChunkRandomizer: GameData geladen."
        );
    }

    public void save() {

        if (data == null) return;

        try {
            data.save(dataFile);

        } catch (IOException ex) {
            plugin.getLogger().log(
                    Level.SEVERE,
                    "ChunkRandomizer: GameData konnte nicht gespeichert werden.",
                    ex
            );
        }
    }

    public void saveGameStart(Location location) {

        if (data == null || location == null) return;

        World world = location.getWorld();
        if (world == null) return;

        data.set("game.event-started", true);
        data.set("game.world", world.getName());

        data.set(
                "game.start-chunk.x",
                location.getBlockX() >> 4
        );

        data.set(
                "game.start-chunk.z",
                location.getBlockZ() >> 4
        );

        save();
    }

    public boolean isStarted() {
        return data != null
                && data.getBoolean("game.event-started", false);
    }

    public void saveBorder(World world) {

        if (data == null || world == null) return;

        WorldBorder border = world.getWorldBorder();
        Location center = border.getCenter();

        data.set("border.active", true);
        data.set("border.world", world.getName());

        data.set("border.center.x", center.getX());
        data.set("border.center.z", center.getZ());

        data.set("border.size", border.getSize());

        save();
    }

    public void reset() {

        try (InputStream input =
                     plugin.getResource(RESOURCE_PATH)) {

            if (input == null) {
                plugin.getLogger().warning(
                        "ChunkRandomizer: GameData-Resource nicht gefunden."
                );
                return;
            }

            Files.copy(
                    input,
                    dataFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );

            load();

            plugin.getLogger().info(
                    "ChunkRandomizer: GameData zurückgesetzt."
            );

        } catch (IOException ex) {
            plugin.getLogger().log(
                    Level.SEVERE,
                    "ChunkRandomizer: GameData-Reset fehlgeschlagen.",
                    ex
            );
        }
    }

    public FileConfiguration getData() {
        return data;
    }

    public void saveOriginalBorder(
            ChunkRandomizerBorderManager borderManager
    ) {
        if (data == null) return;

        ChunkRandomizerBorderManager.BorderState state =
                borderManager.getOriginalBorderState();

        if (state == null) return;

        data.set("border.original.center.x", state.centerX());
        data.set("border.original.center.z", state.centerZ());
        data.set("border.original.size", state.size());
        data.set("border.original.warning-distance", state.warningDistance());
        data.set("border.original.damage-buffer", state.damageBuffer());
        data.set("border.original.damage-amount", state.damageAmount());

        save();
    }

    public void restoreOriginalBorder(
            ChunkRandomizerBorderManager borderManager
    ) {
        if (data == null || !data.contains("border.original.size")) {
            return;
        }

        borderManager.setOriginalBorderState(
                new ChunkRandomizerBorderManager.BorderState(
                        data.getDouble("border.original.center.x"),
                        data.getDouble("border.original.center.z"),
                        data.getDouble("border.original.size"),
                        data.getInt("border.original.warning-distance"),
                        data.getDouble("border.original.damage-buffer"),
                        data.getDouble("border.original.damage-amount")
                )
        );
    }

    public void restoreInactiveBorder() {

        if (data == null || !isStarted()) return;

        // Nur wenn die Challenge-Border nicht mehr aktiv sein soll
        if (data.getBoolean("border.active", false)) return;

        if (!data.contains("border.original.size")) return;

        String worldName = data.getString("border.world", "");
        World world = Bukkit.getWorld(worldName);

        if (world == null) return;

        double centerX = data.getDouble("border.original.center.x");
        double centerZ = data.getDouble("border.original.center.z");
        double size = data.getDouble("border.original.size");

        if (!Double.isFinite(centerX)
                || !Double.isFinite(centerZ)
                || !Double.isFinite(size)
                || size <= 0) {
            return;
        }

        WorldBorder border = world.getWorldBorder();

        border.setCenter(centerX, centerZ);
        border.setSize(size);

        border.setWarningDistance(
                data.getInt("border.original.warning-distance")
        );

        border.setDamageBuffer(
                data.getDouble("border.original.damage-buffer")
        );

        border.setDamageAmount(
                data.getDouble("border.original.damage-amount")
        );

        plugin.getLogger().info(
                "ChunkRandomizer: Ursprüngliche WorldBorder wiederhergestellt."
        );
    }
}

