package runi.myddns.challenges.core.player;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import runi.myddns.challenges.ChallengeMain;
import runi.myddns.challenges.core.world.GameWorldDefinition;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PlayerGameDataManager {

    private final ChallengeMain plugin;
    private final File playerDataFolder;

    public PlayerGameDataManager(ChallengeMain plugin) {
        this.plugin = plugin;

        this.playerDataFolder =
                new File(plugin.getDataFolder(), "playerdata");

        if (!playerDataFolder.exists()) {
            playerDataFolder.mkdirs();
        }
    }

    public void savePlayerData(Player player, String gameId) {

        File file = getPlayerFile(
                player.getUniqueId(),
                gameId
        );

        YamlConfiguration config =
                new YamlConfiguration();

        config.set(
                "inventory.contents",
                Arrays.asList(
                        player.getInventory().getStorageContents()
                )
        );

        config.set(
                "inventory.armor",
                Arrays.asList(
                        player.getInventory().getArmorContents()
                )
        );

        config.set(
                "inventory.offhand",
                player.getInventory().getItemInOffHand()
        );

        config.set(
                "enderchest",
                Arrays.asList(
                        player.getEnderChest().getContents()
                )
        );

        config.set(
                "experience.level",
                player.getLevel()
        );

        config.set(
                "experience.exp",
                player.getExp()
        );

        config.set(
                "experience.total",
                player.getTotalExperience()
        );

        // Spielerzustand
        config.set(
                "player.health",
                player.getHealth()
        );

        config.set(
                "player.food",
                player.getFoodLevel()
        );

        config.set(
                "player.saturation",
                player.getSaturation()
        );

        // Position
        saveLocation(
                config,
                player.getLocation()
        );

        try {

            config.save(file);

        } catch (IOException exception) {

            plugin.getLogger().severe(
                    "PlayerGameData konnte nicht gespeichert werden: "
                            + player.getName()
                            + " / "
                            + gameId
            );

            exception.printStackTrace();
        }
    }

    public void saveOnlinePlayers() {

        for (Player player : Bukkit.getOnlinePlayers()) {

            String gameId = null;

            for (GameWorldDefinition definition : GameWorldDefinition.ALL) {

                if (definition.worldName().equalsIgnoreCase(
                        player.getWorld().getName()
                )) {
                    gameId = definition.gameId();
                    break;
                }
            }

            if (gameId == null) continue;

            savePlayerData(
                    player,
                    gameId
            );
        }
    }

    public boolean loadPlayerData(Player player, String gameId) {
        return loadPlayerData(player, gameId, true);
    }

    public boolean loadPlayerData(Player player, String gameId, boolean loadLocation) {

        File file = getPlayerFile(player.getUniqueId(), gameId);

        if (!file.exists()) return false;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        clearPlayerState(player);

        // Inventar
        List<?> inventoryList = config.getList("inventory.contents");

        if (inventoryList != null) {
            player.getInventory().setStorageContents(
                    inventoryList.toArray(new ItemStack[0])
            );
        }

        // Rüstung
        List<?> armorList = config.getList("inventory.armor");

        if (armorList != null) {
            player.getInventory().setArmorContents(
                    armorList.toArray(new ItemStack[0])
            );
        }

        // Offhand
        ItemStack offhand = config.getItemStack("inventory.offhand");

        if (offhand != null) {
            player.getInventory().setItemInOffHand(offhand);
        }

        // Enderchest
        List<?> enderChestList = config.getList("enderchest");

        if (enderChestList != null) {
            player.getEnderChest().setContents(
                    enderChestList.toArray(new ItemStack[0])
            );
        }

        // XP
        player.setLevel(config.getInt("experience.level", 0));
        player.setExp((float) config.getDouble("experience.exp", 0.0));
        player.setTotalExperience(config.getInt("experience.total", 0));

        // Hunger
        player.setFoodLevel(config.getInt("player.food", 20));
        player.setSaturation((float) config.getDouble("player.saturation", 5.0));

        // Gesundheit
        double health = config.getDouble("player.health", player.getMaxHealth());
        player.setHealth(Math.min(health, player.getMaxHealth()));

        // Position nur wenn gewünscht
        if (loadLocation) {
            Location location = loadLocation(config);

            if (location != null) {
                player.teleport(location);
            }
        }

        return true;
    }

    public void clearPlayerState(Player player) {

        player.getInventory().clear();

        player.getInventory()
                .setArmorContents(
                        new ItemStack[4]
                );

        player.getInventory()
                .setItemInOffHand(null);

        player.getEnderChest().clear();

        player.setLevel(0);
        player.setExp(0);
        player.setTotalExperience(0);

        player.setFoodLevel(20);
        player.setSaturation(5.0f);

        player.setHealth(
                player.getMaxHealth()
        );
    }

    public boolean hasPlayerData(
            UUID uuid,
            String gameId
    ) {

        return getPlayerFile(
                uuid,
                gameId
        ).exists();
    }

    public void deletePlayerData(
            UUID uuid,
            String gameId
    ) {

        File file =
                getPlayerFile(
                        uuid,
                        gameId
                );

        if (file.exists()) {
            file.delete();
        }
    }

    private File getPlayerFile(
            UUID uuid,
            String gameId
    ) {

        File gameFolder =
                new File(
                        playerDataFolder,
                        gameId.toLowerCase()
                );

        if (!gameFolder.exists()) {
            gameFolder.mkdirs();
        }

        return new File(
                gameFolder,
                uuid + ".yml"
        );
    }

    private void saveLocation(
            YamlConfiguration config,
            Location location
    ) {

        config.set(
                "location.world",
                location.getWorld().getName()
        );

        config.set(
                "location.x",
                location.getX()
        );

        config.set(
                "location.y",
                location.getY()
        );

        config.set(
                "location.z",
                location.getZ()
        );

        config.set(
                "location.yaw",
                location.getYaw()
        );

        config.set(
                "location.pitch",
                location.getPitch()
        );
    }

    public Location getSavedLocation(
            UUID uuid,
            String gameId
    ) {

        File file =
                getPlayerFile(
                        uuid,
                        gameId
                );

        if (!file.exists()) return null;

        YamlConfiguration config =
                YamlConfiguration.loadConfiguration(file);

        return loadLocation(config);
    }

    private Location loadLocation(
            YamlConfiguration config
    ) {

        String worldName =
                config.getString(
                        "location.world"
                );

        if (worldName == null) {
            return null;
        }

        World world =
                Bukkit.getWorld(worldName);

        if (world == null) {
            return null;
        }

        return new Location(
                world,
                config.getDouble("location.x"),
                config.getDouble("location.y"),
                config.getDouble("location.z"),
                (float) config.getDouble("location.yaw"),
                (float) config.getDouble("location.pitch")
        );
    }
}
