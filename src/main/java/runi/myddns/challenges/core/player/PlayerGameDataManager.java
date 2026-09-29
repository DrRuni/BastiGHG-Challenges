package runi.myddns.challenges.core.player;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import runi.myddns.challenges.ChallengeMain;
import runi.myddns.challenges.core.world.GameWorldDefinition;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PlayerGameDataManager {

    private final ChallengeMain plugin;
    private final File playerDataFolder;

    private final PlayerEnderChestManager enderChestManager;


    public PlayerGameDataManager(
            ChallengeMain plugin
    ) {

        this.plugin =
                plugin;

        this.playerDataFolder =
                new File(
                        plugin.getDataFolder(),
                        "playerdata"
                );

        if (!playerDataFolder.exists()) {
            playerDataFolder.mkdirs();
        }

        this.enderChestManager =
                new PlayerEnderChestManager();
    }

    public void savePlayerData(
            Player player,
            String gameId
    ) {

        File file =
                getPlayerFile(
                        player,
                        gameId
                );

        YamlConfiguration config =
                new YamlConfiguration();

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

        config.set(
                "player.uuid",
                player.getUniqueId().toString()
        );

        config.set(
                "player.name",
                player.getName()
        );

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

        config.set(
                "effects",
                new ArrayList<>(
                        player.getActivePotionEffects()
                )
        );

        saveLocation(
                config,
                player.getLocation()
        );

        config.set(
                "inventory.contents",
                Arrays.asList(
                        player.getInventory()
                                .getStorageContents()
                )
        );

        config.set(
                "inventory.armor",
                Arrays.asList(
                        player.getInventory()
                                .getArmorContents()
                )
        );

        config.set(
                "inventory.offhand",
                player.getInventory()
                        .getItemInOffHand()
        );

        enderChestManager.save(
                player,
                config
        );


        try {

            config.save(
                    file
            );

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

        for (Player player
                : Bukkit.getOnlinePlayers()) {

            String gameId =
                    null;

            for (GameWorldDefinition definition
                    : GameWorldDefinition.ALL) {

                if (definition.worldName()
                        .equalsIgnoreCase(
                                player.getWorld()
                                        .getName()
                        )) {

                    gameId =
                            definition.gameId();

                    break;
                }
            }

            if (gameId == null) {
                continue;
            }

            savePlayerData(
                    player,
                    gameId
            );
        }
    }

    public void deleteGamePlayerData(
            String gameId
    ) {

        File gameFolder =
                new File(
                        playerDataFolder,
                        gameId.toLowerCase()
                );

        if (!gameFolder.exists()) {
            return;
        }

        File[] files =
                gameFolder.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {

            if (!file.isFile()) {
                continue;
            }

            if (!file.delete()) {

                plugin.getLogger()
                        .warning(
                                "Playerdaten konnten nicht gelöscht werden: "
                                        + file.getName()
                        );
            }
        }
    }
    public void deletePlayerData(
            UUID uuid,
            String gameId
    ) {

        File file =
                findPlayerFile(
                        uuid,
                        gameId
                );

        if (file != null) {
            file.delete();
        }
    }

    public void resetPlayerState(
            Player player,
            boolean keepNightVision
    ) {

        clearPlayerState(player);

        if (keepNightVision) {

            player.addPotionEffect(
                    new PotionEffect(
                            PotionEffectType.NIGHT_VISION,
                            Integer.MAX_VALUE,
                            0,
                            false,
                            false,
                            false
                    )
            );
        }
    }
    public boolean loadPlayerData(
            Player player,
            String gameId
    ) {

        return loadPlayerData(
                player,
                gameId,
                true
        );
    }


    public boolean loadPlayerData(
            Player player,
            String gameId,
            boolean loadLocation
    ) {

        File file =
                findPlayerFile(
                        player.getUniqueId(),
                        gameId
                );

        if (file == null) {
            return false;
        }

        if (!file.exists()) {
            return false;
        }

        YamlConfiguration config =
                YamlConfiguration.loadConfiguration(
                        file
                );


        /*
         * Alten Zustand komplett entfernen.
         */
        clearPlayerState(
                player
        );


        /*
         * Potion-Effekte
         */
        List<?> effects =
                config.getList(
                        "effects"
                );

        if (effects != null) {

            for (Object object : effects) {

                if (object
                        instanceof PotionEffect effect) {

                    player.addPotionEffect(
                            effect,
                            true
                    );
                }
            }
        }


        /*
         * Inventar
         */
        List<?> inventoryList =
                config.getList(
                        "inventory.contents"
                );

        if (inventoryList != null) {

            player.getInventory()
                    .setStorageContents(
                            inventoryList.toArray(
                                    new ItemStack[0]
                            )
                    );
        }


        /*
         * Rüstung
         */
        List<?> armorList =
                config.getList(
                        "inventory.armor"
                );

        if (armorList != null) {

            player.getInventory()
                    .setArmorContents(
                            armorList.toArray(
                                    new ItemStack[0]
                            )
                    );
        }


        /*
         * Offhand
         */
        ItemStack offhand =
                config.getItemStack(
                        "inventory.offhand"
                );

        if (offhand != null) {

            player.getInventory()
                    .setItemInOffHand(
                            offhand
                    );
        }


        /*
         * Enderchest
         */
        enderChestManager.load(
                player,
                config
        );


        /*
         * XP
         */
        player.setLevel(
                config.getInt(
                        "experience.level",
                        0
                )
        );

        player.setExp(
                (float) config.getDouble(
                        "experience.exp",
                        0.0
                )
        );

        player.setTotalExperience(
                config.getInt(
                        "experience.total",
                        0
                )
        );


        /*
         * Hunger
         */
        player.setFoodLevel(
                config.getInt(
                        "player.food",
                        20
                )
        );

        player.setSaturation(
                (float) config.getDouble(
                        "player.saturation",
                        5.0
                )
        );


        /*
         * Gesundheit
         */
        double health =
                config.getDouble(
                        "player.health",
                        player.getMaxHealth()
                );

        player.setHealth(
                Math.min(
                        health,
                        player.getMaxHealth()
                )
        );


        /*
         * Position
         */
        if (loadLocation) {

            Location location =
                    loadLocation(
                            config
                    );

            if (location != null) {

                player.teleport(
                        location
                );
            }
        }

        return true;
    }


    /*
     * =========================================================
     * SPIELERZUSTAND LEEREN
     * =========================================================
     */

    public void clearPlayerState(
            Player player
    ) {

        clearPotionEffects(
                player
        );

        player.getInventory()
                .clear();

        player.getInventory()
                .setArmorContents(
                        new ItemStack[4]
                );

        player.getInventory()
                .setItemInOffHand(
                        null
                );

        enderChestManager.clear(
                player
        );

        player.setLevel(
                0
        );

        player.setExp(
                0
        );

        player.setTotalExperience(
                0
        );

        player.setFoodLevel(
                20
        );

        player.setSaturation(
                5.0f
        );

        player.setHealth(
                player.getMaxHealth()
        );
    }

    public void clearPotionEffects(
            Player player
    ) {

        for (PotionEffect effect
                : player.getActivePotionEffects()) {

            player.removePotionEffect(
                    effect.getType()
            );
        }
    }

    public void clearSavedLocations(
            String gameId
    ) {

        File gameFolder =
                new File(
                        playerDataFolder,
                        gameId.toLowerCase()
                );

        if (!gameFolder.exists()) {
            return;
        }

        File[] files =
                gameFolder.listFiles(
                        (dir, name) ->
                                name.toLowerCase()
                                        .endsWith(".yml")
                );

        if (files == null) {
            return;
        }

        for (File file : files) {

            YamlConfiguration config =
                    YamlConfiguration
                            .loadConfiguration(
                                    file
                            );

            config.set(
                    "location",
                    null
            );

            try {

                config.save(
                        file
                );

            } catch (IOException exception) {

                plugin.getLogger()
                        .severe(
                                "Gespeicherte Spielerposition konnte nicht gelöscht werden: "
                                        + file.getName()
                        );

                exception.printStackTrace();
            }
        }
    }

    public boolean hasPlayerData(
            UUID uuid,
            String gameId
    ) {

        return findPlayerFile(
                uuid,
                gameId
        ) != null;
    }

    private File getPlayerFile(
            Player player,
            String gameId
    ) {

        File gameFolder =
                getGameFolder(
                        gameId
                );

        File existingFile =
                findPlayerFile(
                        player.getUniqueId(),
                        gameId
                );

        if (existingFile != null) {

            File wantedFile =
                    new File(
                            gameFolder,
                            player.getName() + ".yml"
                    );

            if (!existingFile.getName()
                    .equalsIgnoreCase(
                            wantedFile.getName()
                    )) {

                if (existingFile.renameTo(
                        wantedFile
                )) {

                    return wantedFile;
                }
            }

            return existingFile;
        }

        return new File(
                gameFolder,
                player.getName() + ".yml"
        );
    }

    private File findPlayerFile(
            UUID uuid,
            String gameId
    ) {

        File gameFolder =
                getGameFolder(
                        gameId
                );

        File[] files =
                gameFolder.listFiles(
                        (dir, name) ->
                                name.toLowerCase()
                                        .endsWith(".yml")
                );

        if (files == null) {
            return null;
        }

        String wantedUuid =
                uuid.toString();

        for (File file : files) {

            YamlConfiguration config =
                    YamlConfiguration
                            .loadConfiguration(
                                    file
                            );

            String savedUuid =
                    config.getString(
                            "player.uuid"
                    );

            if (wantedUuid.equalsIgnoreCase(
                    savedUuid
            )) {

                return file;
            }
        }

        File oldFile =
                new File(
                        gameFolder,
                        uuid + ".yml"
                );

        if (oldFile.exists()) {
            return oldFile;
        }

        return null;
    }

    private File getGameFolder(
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

        return gameFolder;
    }

    private void saveLocation(
            YamlConfiguration config,
            Location location
    ) {

        config.set(
                "location.world",
                location.getWorld()
                        .getName()
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
                findPlayerFile(
                        uuid,
                        gameId
                );

        if (file == null) {
            return null;
        }

        YamlConfiguration config =
                YamlConfiguration
                        .loadConfiguration(
                                file
                        );

        return loadLocation(
                config
        );
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
                Bukkit.getWorld(
                        worldName
                );

        if (world == null) {
            return null;
        }

        return new Location(
                world,
                config.getDouble(
                        "location.x"
                ),
                config.getDouble(
                        "location.y"
                ),
                config.getDouble(
                        "location.z"
                ),
                (float) config.getDouble(
                        "location.yaw"
                ),
                (float) config.getDouble(
                        "location.pitch"
                )
        );
    }
}