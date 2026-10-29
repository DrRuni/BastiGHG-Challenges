package runi.myddns.challenges.games.levelborder.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import runi.myddns.challenges.games.levelborder.LevelBorderGame;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class BorderDataManager {

    private final LevelBorderGame game;
    private final File dataFile;
    private final YamlConfiguration dataCfg;

    public BorderDataManager(LevelBorderGame game) {
        this.game = game;

        File gameFolder = new File(
                game.getPlugin().getDataFolder(),
                "games/levelborder"
        );

        if (!gameFolder.exists() && !gameFolder.mkdirs()) {
            game.getPlugin().getLogger().severe(
                    "LevelBorder-Ordner konnte nicht erstellt werden: "
                            + gameFolder.getAbsolutePath()
            );
        }

        dataFile = new File(
                gameFolder,
                "gamedata.yml"
        );

        dataCfg = YamlConfiguration.loadConfiguration(dataFile);
    }

    public Location getCenter() {
        String worldName = dataCfg.getString("border.world", null);
        if (worldName == null) return null;

        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;

        if (!dataCfg.isConfigurationSection("border.center")) return null;

        double x = dataCfg.getDouble("border.center.x", 0);
        double y = dataCfg.getDouble("border.center.y", 64);
        double z = dataCfg.getDouble("border.center.z", 0);

        return new Location(world, x, y, z);
    }

    public void setCenter(Location center) {

        if (center == null) {
            dataCfg.set("border.center", null);
            dataCfg.set("border.world", null);
            save();
            return;
        }

        if (center.getWorld() == null) {
            return;
        }

        dataCfg.set(
                "border.world",
                center.getWorld().getName()
        );

        dataCfg.set(
                "border.center.x",
                center.getX()
        );

        dataCfg.set(
                "border.center.y",
                center.getY()
        );

        dataCfg.set(
                "border.center.z",
                center.getZ()
        );

        save();
    }

    public double getSize() {
        return dataCfg.getDouble(
                "border.size",
                50.0
        );
    }

    public void setSize(double size) {
        dataCfg.set(
                "border.size",
                size
        );

        save();
    }

    public boolean isActive() {
        return dataCfg.getBoolean(
                "border.active",
                false
        );
    }

    public void setActive(boolean active) {
        dataCfg.set(
                "border.active",
                active
        );

        save();
    }

    public int getMaxTotalLevel() {
        return dataCfg.getInt(
                "border.max-total-level",
                0
        );
    }

    public void setMaxTotalLevel(int level) {
        dataCfg.set(
                "border.max-total-level",
                level
        );

        save();
    }

    public void savePlayerLevel(Player player) {

        String path =
                "players."
                        + player.getName();

        dataCfg.set(
                path + ".uuid",
                player.getUniqueId().toString()
        );

        dataCfg.set(
                path + ".level",
                player.getLevel()
        );

        save();
    }

    public Map<String, Integer> getAllPlayerLevels() {

        Map<String, Integer> map =
                new LinkedHashMap<>();

        ConfigurationSection players = dataCfg.getConfigurationSection("players");

        if (players != null) {
            for (String name : players.getKeys(false)) {

                map.put(
                        name,
                        dataCfg.getInt(
                                "players."
                                        + name
                                        + ".level",
                                0
                        )
                );
            }
        }

        return map;
    }

    public void registerPlayer(Player player) {
        String path = "players." + player.getName();
        if (dataCfg.isConfigurationSection(path)) return;

        dataCfg.set(path + ".uuid", player.getUniqueId().toString());
        dataCfg.set(path + ".level", player.getLevel());
        save();
    }

    public boolean isScoreboardVisible(Player player) {
        return dataCfg.getBoolean("players." + player.getName() + ".scoreboard-visible", false);
    }

    public void setScoreboardVisible(Player player, boolean visible) {
        registerPlayer(player);
        dataCfg.set("players." + player.getName() + ".scoreboard-visible", visible);
        save();
    }

    public long getTimerSeconds() {
        return dataCfg.getLong(
                "timer.seconds",
                0
        );
    }

    public void setTimer(long seconds) {
        dataCfg.set(
                "timer.seconds",
                seconds
        );

        save();
    }

    public int getTotalLevelSum() {

        int total = 0;

        ConfigurationSection players = dataCfg.getConfigurationSection("players");

        if (players != null) {
            for (String name : players.getKeys(false)) {

                total += dataCfg.getInt(
                        "players."
                                + name
                                + ".level",
                        0
                );
            }
        }

        return total;
    }

    public void resetAllPlayerLevels() {

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!game.isLevelBorderPlayer(player)) continue;

            player.setLevel(0);
            player.setExp(0.0f);
            player.setTotalExperience(0);
        }

        dataCfg.set("players", null);
        dataCfg.set("border.world", null);
        dataCfg.set("border.center", null);
        dataCfg.set("border.size", 3.0);
        dataCfg.set("border.active", false);
        dataCfg.set("border.max-total-level", 0);
        dataCfg.set("scoreboard.visible", false);
        dataCfg.set("timer.seconds", 0L);

        save();
    }

    public void resetPlayerProgress() {

        Map<String, Boolean> visiblePlayers = new LinkedHashMap<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!game.isLevelBorderPlayer(player)) continue;

            visiblePlayers.put(
                    player.getName(),
                    isScoreboardVisible(player)
            );
        }

        dataCfg.set("players", null);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!game.isLevelBorderPlayer(player)) continue;

            String path = "players." + player.getName();

            dataCfg.set(path + ".uuid",
                    player.getUniqueId().toString());

            dataCfg.set(path + ".level", 0);

            dataCfg.set(path + ".scoreboard-visible",
                    visiblePlayers.getOrDefault(player.getName(), false));
        }

        dataCfg.set("border.size", 3.0);
        dataCfg.set("border.max-total-level", 0);

        save();
    }

    private void save() {

        try {

            dataCfg.save(dataFile);

        } catch (IOException exception) {

            game.getPlugin()
                    .getLogger()
                    .warning(
                            "Failed to save gamedata.yml"
                    );
        }
    }
}