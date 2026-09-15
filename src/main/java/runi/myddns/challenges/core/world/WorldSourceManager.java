package runi.myddns.challenges.core.world;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import runi.myddns.challenges.ChallengeMain;
import runi.myddns.challenges.core.utils.ConsoleColor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class WorldSourceManager {

    private final ChallengeMain plugin;

    public WorldSourceManager(ChallengeMain plugin) {
        this.plugin = plugin;
    }

    // ============================================================
    // PREPARE
    // ============================================================

    public void prepareAllWorlds() {
        for (GameWorldDefinition definition : GameWorldDefinition.ALL) {
            if (isMobArmyTeamWorld(definition)) continue;

            prepareWorld(definition);
        }

        prepareMobArmyTeamWorlds();
    }

    private boolean isMobArmyTeamWorld(GameWorldDefinition definition) {
        return definition == GameWorldDefinition.MOB_ARMY_ROT
                || definition == GameWorldDefinition.MOB_ARMY_ROT_NETHER
                || definition == GameWorldDefinition.MOB_ARMY_BLAU
                || definition == GameWorldDefinition.MOB_ARMY_BLAU_NETHER;
    }

    private void prepareMobArmyTeamWorlds() {
        boolean rot = worldExists(GameWorldDefinition.MOB_ARMY_ROT);
        boolean rotNether = worldExists(GameWorldDefinition.MOB_ARMY_ROT_NETHER);
        boolean blau = worldExists(GameWorldDefinition.MOB_ARMY_BLAU);
        boolean blauNether = worldExists(GameWorldDefinition.MOB_ARMY_BLAU_NETHER);

        if (rot && rotNether && blau && blauNether) return;

        if (rot || rotNether || blau || blauNether) {
            plugin.getLogger().warning("MobArmyBattle-Teamwelten sind nur teilweise vorhanden. Automatische Neuerstellung wird übersprungen.");
            return;
        }

        long seed = new Random().nextLong();

        generateWorld(GameWorldDefinition.MOB_ARMY_ROT, seed);
        generateWorld(GameWorldDefinition.MOB_ARMY_ROT_NETHER, seed);
        generateWorld(GameWorldDefinition.MOB_ARMY_BLAU, seed);
        generateWorld(GameWorldDefinition.MOB_ARMY_BLAU_NETHER, seed);
    }

    public void prepareWorld(GameWorldDefinition definition) {
        if (worldExists(definition)) return;

        plugin.getLogger().info("Bereite Welt vor: " + definition.worldName());

        if (definition.isZip()) {
            extractWorld(definition);
            return;
        }

        if (definition.isGenerated()) {
            generateWorld(definition);
        }
    }

    // ============================================================
    // WORLD EXISTS
    // ============================================================

    public boolean worldExists(GameWorldDefinition definition) {
        if (Bukkit.getWorld(definition.worldName()) != null) return true;

        File dimensionFolder = getDimensionWorldFolder(definition);

        if (dimensionFolder.exists() && (
                new File(dimensionFolder, "region").exists()
                        || new File(dimensionFolder, "entities").exists()
                        || new File(dimensionFolder, "poi").exists()
                        || new File(dimensionFolder, "paper-world.yml").exists()
        )) {
            return true;
        }

        File legacyFolder = getLegacyWorldFolder(definition);
        return new File(legacyFolder, "level.dat").exists();
    }

    public File getLegacyWorldFolder(GameWorldDefinition definition) {
        return new File(Bukkit.getWorldContainer(), definition.worldName());
    }

    public File getDimensionWorldFolder(GameWorldDefinition definition) {
        return new File(
                Bukkit.getWorldContainer(),
                "world/dimensions/minecraft/" + definition.worldName()
        );
    }

    // ============================================================
    // GENERATED WORLD
    // ============================================================

    private void generateWorld(GameWorldDefinition definition) {
        generateWorld(definition, new Random().nextLong());
    }

    // ============================================================
    // ZIP WORLD
    // ============================================================

    private void extractWorld(GameWorldDefinition definition) {
        String resourcePath = definition.zipFile();

        if (resourcePath == null || resourcePath.isBlank()) {
            plugin.getLogger().warning("Keine ZIP-Resource für Welt definiert: " + definition.worldName());
            return;
        }

        try (InputStream resourceStream = plugin.getResource(resourcePath)) {
            if (resourceStream == null) {
                plugin.getLogger().warning("ZIP-Resource nicht gefunden: " + resourcePath);
                return;
            }

            extractZip(
                    resourceStream,
                    getWorldFolder(definition).toPath(),
                    definition.worldName()
            );

            Bukkit.getConsoleSender().sendMessage(
                    ConsoleColor.LIME +
                            "        Welt - '" + definition.worldName() + "' wurde aus ZIP erstellt!" +
                            ConsoleColor.RESET
            );

        } catch (IOException ex) {
            plugin.getLogger().severe("Fehler beim Entpacken der Welt " + definition.worldName() + ": " + ex.getMessage());
        }
    }

    private void extractZip(InputStream inputStream, Path targetDirectory, String worldName) throws IOException {
        Path normalizedTarget = targetDirectory.toAbsolutePath().normalize();
        Files.createDirectories(normalizedTarget);

        String prefix = worldName + "/";

        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            ZipEntry entry;

            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (!entry.getName().startsWith(prefix)) {
                    zipInputStream.closeEntry();
                    continue;
                }

                String relativePath = entry.getName().substring(prefix.length());

                if (relativePath.isEmpty()) {
                    zipInputStream.closeEntry();
                    continue;
                }

                Path target = normalizedTarget.resolve(relativePath).normalize();

                if (!target.startsWith(normalizedTarget)) {
                    throw new IOException("Ungültiger ZIP-Eintrag: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(target);
                } else {
                    Path parent = target.getParent();
                    if (parent != null) Files.createDirectories(parent);

                    Files.copy(zipInputStream, target, StandardCopyOption.REPLACE_EXISTING);
                }

                zipInputStream.closeEntry();
            }
        }
    }

    private boolean deleteWorldFolders(GameWorldDefinition definition) {
        File legacyFolder = getLegacyWorldFolder(definition);
        File dimensionFolder = getDimensionWorldFolder(definition);

        if (legacyFolder.exists() && !deleteFolder(legacyFolder)) {
            plugin.getLogger().warning("Legacy-Weltordner konnte nicht gelöscht werden: " + legacyFolder.getPath());
            return false;
        }

        if (dimensionFolder.exists() && !deleteFolder(dimensionFolder)) {
            plugin.getLogger().warning("Dimensions-Weltordner konnte nicht gelöscht werden: " + dimensionFolder.getPath());
            return false;
        }

        return true;
    }

    public boolean resetGeneratedWorld(GameWorldDefinition definition, long seed) {
        World loadedWorld = Bukkit.getWorld(definition.worldName());

        if (loadedWorld != null && !Bukkit.unloadWorld(loadedWorld, false)) {
            plugin.getLogger().warning("Welt konnte nicht entladen werden: " + definition.worldName());
            return false;
        }

        if (!deleteWorldFolders(definition)) return false;

        generateWorld(definition, seed);
        return worldExists(definition);
    }

    public void generateWorld(GameWorldDefinition definition, long seed) {
        if (!definition.isGenerated()) {
            plugin.getLogger().warning("Welt ist keine GENERATED-Welt: " + definition.worldName());
            return;
        }

        WorldCreator creator = new WorldCreator(definition.worldName());
        creator.environment(definition.environment());
        creator.seed(seed);

        if (definition.generator() != null) {
            creator.generator(definition.generator().get());
        }

        World world = creator.createWorld();

        if (world == null) {
            plugin.getLogger().warning("Welt konnte nicht erstellt werden: " + definition.worldName());
            return;
        }

        world.save();

        Bukkit.getConsoleSender().sendMessage(
                ConsoleColor.LIME +
                        "        Welt - '" + definition.worldName() + "' wurde erstellt!" +
                        ConsoleColor.RESET
        );

        if (!Bukkit.unloadWorld(world, true)) {
            plugin.getLogger().warning("Welt konnte nach der Erstellung nicht entladen werden: " + definition.worldName());
        }
    }

    // ============================================================
    // RESET
    // ============================================================

    public boolean resetWorld(GameWorldDefinition definition) {
        World loadedWorld = Bukkit.getWorld(definition.worldName());

        if (loadedWorld != null && !Bukkit.unloadWorld(loadedWorld, false)) {
            plugin.getLogger().warning("Welt konnte nicht entladen werden: " + definition.worldName());
            return false;
        }

        if (!deleteWorldFolders(definition)) return false;

        prepareWorld(definition);
        return worldExists(definition);
    }

    private boolean deleteFolder(File file) {
        File[] children = file.listFiles();

        if (children != null) {
            for (File child : children) {
                if (!deleteFolder(child)) return false;
            }
        }

        return file.delete();
    }

    // ============================================================
    // FILE
    // ============================================================

    public File getWorldFolder(GameWorldDefinition definition) {
        return definition.isZip()
                ? getDimensionWorldFolder(definition)
                : getLegacyWorldFolder(definition);
    }
}