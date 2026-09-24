package runi.myddns.challenges.core.world;

import org.bukkit.Difficulty;
import org.bukkit.GameRules;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

public class GameWorldSettingsManager {

    private final JavaPlugin plugin;
    private final String gameId;
    private final List<String> worldNames;

    private File file;
    private FileConfiguration config;
    private boolean keepInventory;
    private boolean nightVisionEnabled;
    private boolean mobSpawning;
    private boolean daylightCycle;
    private String difficulty;
    private long worldTime;

    private final Set<String> mobSpawningForcedOffWorlds = new HashSet<>();

    public GameWorldSettingsManager(
            JavaPlugin plugin,
            String gameId,
            List<String> worldNames
    ) {
        this.plugin = plugin;
        this.gameId = gameId;
        this.worldNames = worldNames;
    }

    public void load() {
        file = new File(
                plugin.getDataFolder(),
                "games/" + gameId + "/gamesettings.yml"
        );

        config = YamlConfiguration.loadConfiguration(file);

        keepInventory = config.getBoolean("world-settings.keep-inventory", true);
        nightVisionEnabled = config.getBoolean("world-settings.night-vision-enabled", false);
        mobSpawning = config.getBoolean("world-settings.mob-spawning", true);
        daylightCycle = config.getBoolean("world-settings.daylight-cycle", true);
        difficulty = config.getString("world-settings.difficulty", "normal");
        worldTime = config.getLong("world-settings.world-time", 6000L);
    }

    public void save() {
        if (file == null) return;

        config = YamlConfiguration.loadConfiguration(file);

        config.set("world-settings.keep-inventory", keepInventory);
        config.set("world-settings.night-vision-enabled", nightVisionEnabled);
        config.set("world-settings.mob-spawning", mobSpawning);
        config.set("world-settings.daylight-cycle", daylightCycle);
        config.set("world-settings.difficulty", difficulty);
        config.set("world-settings.world-time", worldTime);

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(
                    Level.SEVERE,
                    "Fehler beim Speichern von " + file.getPath(),
                    e
            );
        }
    }

    public void applyAll() {
        for (String worldName : worldNames) {
            World world = plugin.getServer().getWorld(worldName);

            if (world != null) {
                applyToWorld(world);
            }
        }

        applyPlayerSettingsToAll();

    }

    public void applyToWorld(World world) {
        if (world == null) return;
        if (!worldNames.contains(world.getName())) return;

        world.setGameRule(
                GameRules.KEEP_INVENTORY,
                keepInventory
        );

        world.setGameRule(
                GameRules.SPAWN_MOBS,
                mobSpawningForcedOffWorlds.contains(world.getName())
                        ? false
                        : mobSpawning
        );

        world.setGameRule(
                GameRules.ADVANCE_TIME,
                daylightCycle
        );

        world.setDifficulty(
                getBukkitDifficulty()
        );

        world.setGameRule(
                GameRules.NATURAL_HEALTH_REGENERATION,
                !isUltraHardcore()
        );

        if (world.getEnvironment() == World.Environment.NORMAL) {
            world.setTime(worldTime);
        }
    }

    private Difficulty getBukkitDifficulty() {
        return switch (difficulty.toLowerCase(Locale.ROOT)) {
            case "peaceful" -> Difficulty.PEACEFUL;
            case "easy" -> Difficulty.EASY;
            case "hard", "ultra-hardcore", "ultra-ultra-hardcore" -> Difficulty.HARD;
            default -> Difficulty.NORMAL;
        };
    }

    public boolean isUltraHardcore() {
        return difficulty.equalsIgnoreCase("ultra-hardcore")
                || difficulty.equalsIgnoreCase("ultra-ultra-hardcore");
    }

    public boolean isUltraUltraHardcore() {
        return difficulty.equalsIgnoreCase("ultra-ultra-hardcore");
    }

    public void applyPlayerSettings(Player player) {
        if (player == null) return;
        if (!worldNames.contains(player.getWorld().getName())) return;

        if (nightVisionEnabled) {
            player.addPotionEffect(
                    new PotionEffect(
                            PotionEffectType.NIGHT_VISION,
                            Integer.MAX_VALUE,
                            0,
                            false,
                            false
                    )
            );
        } else {
            player.removePotionEffect(
                    PotionEffectType.NIGHT_VISION
            );
        }
    }

    public void applyPlayerSettingsToAll() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            applyPlayerSettings(player);
        }
    }

    public boolean isKeepInventoryEnabled() {
        return keepInventory;
    }

    public boolean isNightVisionEnabled() {
        return nightVisionEnabled;
    }

    public boolean isMobSpawningEnabled() {
        return mobSpawning;
    }

    public boolean isDaylightCycleEnabled() {
        return daylightCycle;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public long getWorldTime() {
        return worldTime;
    }

    public void setKeepInventory(boolean keepInventory) {
        this.keepInventory = keepInventory;
    }

    public void setNightVisionEnabled(boolean nightVisionEnabled) {
        this.nightVisionEnabled = nightVisionEnabled;
    }

    public void setMobSpawning(boolean mobSpawning) {
        this.mobSpawning = mobSpawning;
    }

    public void setMobSpawningForcedOffWorlds(Collection<String> worldNames) {mobSpawningForcedOffWorlds.clear();mobSpawningForcedOffWorlds.addAll(worldNames);}

    public void setDaylightCycle(boolean daylightCycle) {
        this.daylightCycle = daylightCycle;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public void setWorldTime(long worldTime) {
        this.worldTime = worldTime;
    }

    public String getGameId() {
        return gameId;
    }

    public List<String> getWorldNames() {
        return worldNames;
    }
}
