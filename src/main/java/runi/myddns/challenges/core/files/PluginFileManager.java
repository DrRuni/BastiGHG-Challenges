package runi.myddns.challenges.core.files;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import runi.myddns.challenges.core.utils.ConsoleColor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

public class PluginFileManager {

    private final JavaPlugin plugin;

    private record GameFiles(
            String gameName,
            String targetFolder,
            String resourcePrefix,
            List<String> yamlFiles,
            List<String> emptyFiles,
            String templatePrefix
    ) {}
    private static final List<String> ROOT_RESOURCE_FILES = List.of(
            "config.yml"
    );

    private static final List<String> ROOT_EMPTY_FILES = List.of(
            "game-state.yml"
    );

    private static final List<String> LANGUAGE_FILES = List.of(
            "de.yml",
            "en.yml"
    );

    private static final GameFiles MOB_ARMY_BATTLE = new GameFiles(
            "MobArmyBattle",
            "games/mobarmywars/",
            "games/mobarmywars/",
            List.of(
                    "arena-koordinaten.yml",
                    "eventdaten.yml",
                    "spawns.yml",
                    "team-equipment.yml",
                    "waves.yml",
                    "worldsettings.yml"
            ),
            List.of(
                    "mobData.yml",
                    "scoreboard.yml",
                    "teams.yml"
            ),
            "world_mobarmy"
    );
    private static final List<GameFiles> GAMES = List.of(
            MOB_ARMY_BATTLE
    );

    public PluginFileManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void checkFilesOnStartup() {
        checkRootFiles();
        checkLanguageFiles();

        for (GameFiles game : GAMES) {
            checkGameFiles(game);
        }
    }

    private void checkRootFiles() {
        File targetFolder = plugin.getDataFolder();

        if (!targetFolder.exists() && !targetFolder.mkdirs()) {
            plugin.getLogger().warning("Plugin-Datenordner konnte nicht erstellt werden: " + targetFolder.getPath());
            return;
        }

        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage(
                ConsoleColor.PURPLE +
                        "        Prüfe zentrale Plugin-Dateien..." +
                        ConsoleColor.RESET
        );
        Bukkit.getConsoleSender().sendMessage("");

        for (String fileName : ROOT_RESOURCE_FILES) {
            copyResourceIfMissing(fileName, new File(targetFolder, fileName));
        }

        for (String fileName : ROOT_EMPTY_FILES) {
            createEmptyFileIfMissing(targetFolder, fileName);
        }

        Bukkit.getConsoleSender().sendMessage("");
    }

    private void checkLanguageFiles() {

        File languageFolder = new File(plugin.getDataFolder(), "languages");

        if (!languageFolder.exists() && !languageFolder.mkdirs()) {
            plugin.getLogger().warning("Sprachordner konnte nicht erstellt werden.");
            return;
        }

        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage(
                ConsoleColor.PURPLE +
                        "        Prüfe Sprachdateien..." +
                        ConsoleColor.RESET
        );
        Bukkit.getConsoleSender().sendMessage("");

        for (String fileName : LANGUAGE_FILES) {
            checkYamlFile(languageFolder, "languages/", fileName);
        }

        Bukkit.getConsoleSender().sendMessage("");
    }

    private void checkGameFiles(GameFiles game) {
        File targetFolder = new File(plugin.getDataFolder(), game.targetFolder());
        String resourcePrefix = normalizePrefix(game.resourcePrefix());

        createDataFolder(game.gameName(), targetFolder);

        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage(
                ConsoleColor.PURPLE +
                        "        Prüfe " + game.gameName() + "-Dateien..." +
                        ConsoleColor.RESET
        );
        Bukkit.getConsoleSender().sendMessage("");

        if (game.templatePrefix() != null) {
            String newestTemplate = findNewestTemplateInJar(
                    game.gameName(),
                    resourcePrefix,
                    game.templatePrefix()
            );

            if (newestTemplate != null) {
                checkTemplateByVersion(
                        game.gameName(),
                        targetFolder,
                        resourcePrefix,
                        game.templatePrefix(),
                        newestTemplate
                );
            }
        }

        for (String file : game.yamlFiles()) {
            checkYamlFile(targetFolder, resourcePrefix, file);
        }

        for (String file : game.emptyFiles()) {
            createEmptyFileIfMissing(targetFolder, file);
        }

        Bukkit.getConsoleSender().sendMessage("");
    }

    private void createDataFolder(String gameName, File targetFolder) {
        if (targetFolder.exists()) return;

        if (!targetFolder.mkdirs()) {
            plugin.getLogger().warning(gameName + "-Datenordner konnte nicht erstellt werden: " + targetFolder.getPath());
        }
    }

    private String normalizePrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) return "";
        return prefix.endsWith("/") ? prefix : prefix + "/";
    }

    private String findNewestTemplateInJar(String gameName, String resourcePrefix, String templatePrefix) {
        try {
            File jarFile = new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());

            try (JarFile jar = new JarFile(jarFile)) {
                String newestResourcePath = null;
                String newestVersion = null;

                Enumeration<JarEntry> entries = jar.entries();

                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String resourcePath = entry.getName();

                    if (!resourcePath.startsWith(resourcePrefix + templatePrefix + " V")) continue;
                    if (!resourcePath.endsWith(".zip")) continue;

                    String fileName = resourcePath.substring(resourcePrefix.length());
                    String version = extractVersionFromFileName(fileName, templatePrefix);

                    if (version == null) continue;

                    if (newestVersion == null || compareVersions(version, newestVersion) > 0) {
                        newestVersion = version;
                        newestResourcePath = resourcePath;
                    }
                }

                return newestResourcePath;
            }

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Suchen des Templates für " + gameName + ".", e);
            return null;
        }
    }

    private void checkTemplateByVersion(
            String gameName,
            File targetFolder,
            String resourcePrefix,
            String templatePrefix,
            String resourcePath
    ) {
        String fileName = resourcePath.substring(resourcePrefix.length());
        String resourceVersion = extractVersionFromFileName(fileName, templatePrefix);

        if (resourceVersion == null) return;

        File[] existingFiles = targetFolder.listFiles((_, name) ->
                name.startsWith(templatePrefix + " V") && name.endsWith(".zip"));

        File newestExistingFile = null;
        String newestExistingVersion = null;

        if (existingFiles != null) {
            for (File file : existingFiles) {
                String version = extractVersionFromFileName(file.getName(), templatePrefix);
                if (version == null) continue;

                if (newestExistingVersion == null || compareVersions(version, newestExistingVersion) > 0) {
                    newestExistingVersion = version;
                    newestExistingFile = file;
                }
            }
        }

        File targetFile = new File(targetFolder, fileName);

        if (newestExistingFile == null) {
            copyResource(resourcePath, targetFile);
            printFileStatus(fileName, "erstellt.");
            return;
        }

        if (compareVersions(resourceVersion, newestExistingVersion) > 0) {
            backupFile(targetFolder, newestExistingFile, newestExistingFile.getName());
            copyResource(resourcePath, targetFile);

            if (!newestExistingFile.equals(targetFile) && newestExistingFile.exists() && !newestExistingFile.delete()) {
                plugin.getLogger().warning("Alte Template-Datei konnte nicht gelöscht werden: " + newestExistingFile.getName());
            }

            printFileStatus(fileName, "aktualisiert.");
            return;
        }

        printFileStatus(newestExistingFile.getName(), "I.O.");
    }

    private void checkYamlFile(File targetFolder, String resourcePrefix, String fileName) {
        File targetFile = new File(targetFolder, fileName);
        String resourcePath = resourcePrefix + fileName;

        if (plugin.getResource(resourcePath) == null) {
            plugin.getLogger().warning("Resource nicht gefunden: " + resourcePath);
            return;
        }

        if (!targetFile.exists()) {
            copyResource(resourcePath, targetFile);
            printFileStatus(fileName, "erstellt.");
            return;
        }

        int currentVersion = getFileVersion(targetFile);
        int newestVersion = getResourceVersion(resourcePrefix, fileName);

        if (newestVersion <= 0) {
            plugin.getLogger().warning(fileName + " hat in der Plugin-JAR keine gültige file-version.");
            return;
        }

        if (currentVersion < newestVersion) {
            backupFile(targetFolder, targetFile, fileName);
            overwriteResource(resourcePath, targetFile);
            printFileStatus(fileName, "aktualisiert.");
            return;
        }

        printFileStatus(fileName, "I.O.");
    }

    private void createEmptyFileIfMissing(File targetFolder, String fileName) {
        try {
            File file = new File(targetFolder, fileName);
            File parent = file.getParentFile();

            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                plugin.getLogger().warning("Ordner konnte nicht erstellt werden: " + parent.getPath());
                return;
            }

            if (!file.exists()) {
                if (!file.createNewFile()) {
                    plugin.getLogger().warning("Datei konnte nicht erstellt werden: " + fileName);
                    return;
                }

                printFileStatus(fileName, "erstellt.");
                return;
            }

            printFileStatus(fileName, "I.O.");

        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Erstellen von " + fileName + ".", e);
        }
    }

    private int getFileVersion(File file) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        if (config.contains("file-version")) return config.getInt("file-version");
        if (config.contains("config-version")) return config.getInt("config-version");

        return 0;
    }

    private int getResourceVersion(String resourcePrefix, String fileName) {
        String resourcePath = resourcePrefix + fileName;

        try (InputStream inputStream = plugin.getResource(resourcePath)) {
            if (inputStream == null) return 0;

            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            FileConfiguration config = YamlConfiguration.loadConfiguration(reader);

            if (config.contains("file-version")) return config.getInt("file-version");
            if (config.contains("config-version")) return config.getInt("config-version");

            return 0;

        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Lesen der Resource-Version von " + fileName + ".", e);
            return 0;
        }
    }

    private void overwriteResource(String resourcePath, File targetFile) {
        try {
            File parent = targetFile.getParentFile();

            if (parent != null && !parent.exists() && !parent.mkdirs()) return;

            try (InputStream inputStream = plugin.getResource(resourcePath)) {
                if (inputStream == null) return;
                Files.copy(inputStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Aktualisieren von " + resourcePath + ".", e);
        }
    }

    private void copyResource(String resourcePath, File targetFile) {
        overwriteResource(resourcePath, targetFile);
    }

    private void copyResourceIfMissing(String resourcePath, File targetFile) {
        if (targetFile.exists()) {
            printFileStatus(targetFile.getName(), "I.O.");
            return;
        }

        if (plugin.getResource(resourcePath) == null) {
            plugin.getLogger().warning("Resource nicht gefunden: " + resourcePath);
            return;
        }

        copyResource(resourcePath, targetFile);
        printFileStatus(targetFile.getName(), "erstellt.");
    }

    private void backupFile(File targetFolder, File file, String fileName) {
        try {
            File backupFolder = new File(targetFolder, "backups");

            if (!backupFolder.exists() && !backupFolder.mkdirs()) return;

            long timestamp = System.currentTimeMillis();
            File backupFile = new File(backupFolder, fileName + ".backup-" + timestamp);

            Files.copy(file.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Backup für " + fileName + " konnte nicht erstellt werden.", e);
        }
    }

    private String extractVersionFromFileName(String fileName, String baseName) {
        String prefix = baseName + " V";
        String suffix = ".zip";

        if (!fileName.startsWith(prefix) || !fileName.endsWith(suffix)) return null;

        return fileName.substring(prefix.length(), fileName.length() - suffix.length());
    }

    private int compareVersions(String v1, String v2) {
        String[] parts1 = v1.replace("V", "").replace("v", "").split("\\.");
        String[] parts2 = v2.replace("V", "").replace("v", "").split("\\.");

        int length = Math.max(parts1.length, parts2.length);

        for (int i = 0; i < length; i++) {
            int num1 = i < parts1.length ? parseVersionPart(parts1[i]) : 0;
            int num2 = i < parts2.length ? parseVersionPart(parts2[i]) : 0;

            if (num1 != num2) return Integer.compare(num1, num2);
        }

        return 0;
    }

    private int parseVersionPart(String part) {
        try {
            return Integer.parseInt(part);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void printFileStatus(String fileName, String status) {
        String namePart = "Datei - '" + fileName + "'";
        int width = 48;
        int dots = Math.max(3, width - namePart.length());

        Bukkit.getConsoleSender().sendMessage(
                ConsoleColor.COPPER +
                        "        " +
                        namePart +
                        ".".repeat(dots) +
                        ConsoleColor.DARK_GOLDEN_LIME +
                        status +
                        ConsoleColor.RESET
        );
    }
}