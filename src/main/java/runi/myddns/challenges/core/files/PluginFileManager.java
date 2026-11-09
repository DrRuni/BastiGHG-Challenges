package runi.myddns.challenges.core.files;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import runi.myddns.challenges.core.utils.ConsoleColor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;

public final class PluginFileManager {

    private static final String WORLD_SETTINGS_RESOURCE = "core/gamesettings.yml";
    private static final String MANIFEST_NAME = ".filemanagement.properties";
    private static final String BASELINE_DIR = ".filemanagement/templates";
    private static final DateTimeFormatter BACKUP_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS");

    private record GameFiles(String name, String folder, List<String> templates,
                             List<String> persistentFiles, boolean sharedWorldSettings) {}

    private static final List<GameFiles> GAMES = List.of(
            new GameFiles("MobArmyBattle", "games/mobarmybattle",
                    List.of("arena-koordinaten.yml", "gamedata.yml", "spawns.yml",
                            "team-equipment.yml", "waves.yml", "gamesettings.yml"),
                    List.of("mobData.yml", "scoreboard.yml", "teams.yml"), false),
            new GameFiles("LevelBorder", "games/levelborder",
                    List.of("gamedata.yml", "config.yml"), List.of(), true),
            new GameFiles("LevelBlock", "games/levelblock",
                    List.of("gamedata.yml"), List.of(), true),
            new GameFiles("ChunkRandomizer", "games/chunkrandomizer",
                    List.of("gamedata.yml"), List.of(), true)
    );

    private static final List<String> LANGUAGES = List.of(
            "core", "mobarmybattle", "levelborder", "levelblock", "chunkrandomizer"
    );

    private final JavaPlugin plugin;
    private final Properties manifest = new Properties();
    private final List<String> changes = new ArrayList<>();
    private int created;
    private int updated;
    private int backups;
    private int errors;

    public PluginFileManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void checkFilesOnStartup() {
        changes.clear();
        created = updated = backups = errors = 0;
        manifest.clear();

        Path dataFolder = plugin.getDataFolder().toPath();
        if (!ensureDirectory(dataFolder)) return;
        loadManifest(dataFolder.resolve(MANIFEST_NAME));

        log("");
        log(ConsoleColor.PURPLE + "                     ◆ BastiGHG Challenges" + ConsoleColor.RESET);
        log(ConsoleColor.COPPER + "                        FileManagement 2.0" + ConsoleColor.RESET);
        log("        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        int core = 0;
        core += processTemplate("config.yml", "config.yml", false);
        core += createPersistentFile("game-state.yml");
        printGroup("Core", core);

        for (GameFiles game : GAMES) {
            int inspected = 0;
            for (String name : game.templates()) {
                String relative = game.folder() + "/" + name;
                // gamedata.yml contains live progress: install once, never merge or overwrite.
                inspected += processTemplate(relative, relative, name.equals("gamedata.yml"));
            }
            if (game.sharedWorldSettings()) {
                inspected += processTemplate(WORLD_SETTINGS_RESOURCE,
                        game.folder() + "/gamesettings.yml", false);
            }
            for (String name : game.persistentFiles()) {
                inspected += createPersistentFile(game.folder() + "/" + name);
            }
            printGroup(game.name(), inspected);
        }

        int languageCount = 0;
        for (String group : LANGUAGES) {
            for (String lang : List.of("de.yml", "en.yml")) {
                String path = "languages/" + group + "/" + lang;
                languageCount += processTemplate(path, path, false);
            }
        }
        printGroup("Languages (DE / EN)", languageCount);

        // The manifest is written only after all processed resources have been recorded.
        saveManifest(dataFolder.resolve(MANIFEST_NAME));

        if (!changes.isEmpty()) {
            log("        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log(ConsoleColor.COPPER + "        ↻ " + updated + " aktualisiert, " + created
                    + " erstellt" + ConsoleColor.RESET);
            for (String change : changes) {
                log(ConsoleColor.COPPER + "          ↳ " + change + ConsoleColor.RESET);
            }
            if (backups > 0) {
                log(ConsoleColor.DARK_GOLDEN_LIME + "        ✔ " + backups
                        + " Backup(s) erstellt" + ConsoleColor.RESET);
            }
        }
        log("        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        String status = errors == 0 ? "✔ FileManagement bereit — 0 Fehler"
                : "⚠ FileManagement beendet — " + errors + " Fehler";
        if (errors == 0) {
            log(ConsoleColor.DARK_GOLDEN_LIME + "        " + status + ConsoleColor.RESET);
        } else {
            log("        " + ConsoleColor.COPPER + "⚠" + ConsoleColor.RESET
                    + " FileManagement beendet — " + errors + " Fehler");
        }
        log("");
    }

    /** Return 1 for a registered/inspected file, even if it needed no changes. */
    private int processTemplate(String resourcePath, String relativeTarget, boolean persistent) {
        Path target = plugin.getDataFolder().toPath().resolve(relativeTarget);
        byte[] resourceBytes;
        try (InputStream in = plugin.getResource(resourcePath)) {
            if (in == null) {
                missingResource(resourcePath);
                return 0;
            }
            resourceBytes = in.readAllBytes();
        } catch (IOException e) {
            error("JAR-Resource nicht lesbar: " + resourcePath, e);
            return 0;
        }

        if (Files.notExists(target)) {
            try {
                if (!ensureDirectory(target.getParent())) return 1;
                safeWrite(target, resourceBytes);
                created++;
                changes.add("erstellt: " + relativeTarget);
                if (ensureDirectory(plugin.getDataFolder().toPath().resolve(BASELINE_DIR)
                        .resolve(relativeTarget).getParent())) {
                    safeWrite(plugin.getDataFolder().toPath().resolve(BASELINE_DIR)
                            .resolve(relativeTarget), resourceBytes);
                }
                manifest.setProperty(relativeTarget, sha256(resourceBytes));
            } catch (IOException e) {
                error("Konnte Datei nicht erstellen: " + relativeTarget, e);
            }
            return 1;
        }

        if (!Files.isRegularFile(target)) {
            error("Kein regulärer Dateipfad: " + relativeTarget, null);
            return 1;
        }

        if (persistent) {
            manifest.setProperty(relativeTarget, sha256(resourceBytes));
            return 1;
        }

        String bundledHash = sha256(resourceBytes);
        Path baseline = plugin.getDataFolder().toPath().resolve(BASELINE_DIR).resolve(relativeTarget);

        try {
            YamlConfiguration latest = parseYaml(new String(resourceBytes, StandardCharsets.UTF_8));
            YamlConfiguration existing = parseYaml(Files.readString(target, StandardCharsets.UTF_8));

            YamlConfiguration previous = null;
            boolean isLanguage = relativeTarget.startsWith("languages/");
            if (isLanguage && Files.isRegularFile(baseline)) {
                previous = parseYaml(Files.readString(baseline, StandardCharsets.UTF_8));
            }

            int changedTranslations = 0;
            if (isLanguage && previous != null) {
                for (String path : latest.getKeys(true)) {
                    if (isLegacyVersionField(path) || latest.isConfigurationSection(path)) continue;
                    if (!previous.contains(path, true) || !existing.contains(path, true)) continue;
                    if (existing.isConfigurationSection(path)) continue;
                    Object oldDefault = previous.get(path);
                    Object newDefault = latest.get(path);
                    Object userValue = existing.get(path);
                    // Preserve custom translations; update only unchanged bundled defaults.
                    if (Objects.equals(userValue, oldDefault) && !Objects.equals(oldDefault, newDefault)) {
                        existing.set(path, newDefault);
                        changedTranslations++;
                    }
                }
            }

            int removedLegacyFields = 0;
            for (String versionKey : List.of("file-version", "config-version")) {
                if (!latest.contains(versionKey, true) && existing.contains(versionKey, true)) {
                    existing.set(versionKey, null);
                    removedLegacyFields++;
                }
            }

            int added = 0;
            for (String path : latest.getKeys(true)) {
                if (isLegacyVersionField(path)) continue;
                if (existing.contains(path, true)) continue;
                if (hasScalarParent(existing, path)) {
                    plugin.getLogger().warning("YAML-Strukturkonflikt, vorhandener Wert bleibt: "
                            + relativeTarget + " / " + path);
                    continue;
                }

                Object value = latest.get(path);
                if (value instanceof org.bukkit.configuration.ConfigurationSection section) {
                    if (section.getKeys(false).isEmpty()) {
                        existing.createSection(path);
                        added++;
                    }
                } else if (value != null) {
                    existing.set(path, value);
                    added++;
                }
            }

            if (added > 0 || changedTranslations > 0 || removedLegacyFields > 0) {
                if (!backup(target)) return 1;
                safeWrite(target, existing.saveToString().getBytes(StandardCharsets.UTF_8));
                updated++;
                changes.add("aktualisiert: " + relativeTarget + " (" + added
                        + " ergänzt, " + changedTranslations + " Übersetzungen erneuert, "
                        + removedLegacyFields + " alte Versionsfelder entfernt)");
            }

            if (!ensureDirectory(baseline.getParent())) return 1;
            safeWrite(baseline, resourceBytes);
            manifest.setProperty(relativeTarget, bundledHash);
        } catch (IOException | InvalidConfigurationException | IllegalArgumentException e) {
            error("YAML konnte nicht sicher aktualisiert werden: " + relativeTarget, e);
        }
        return 1;
    }

    private boolean hasScalarParent(YamlConfiguration config, String path) {
        int dot = path.indexOf('.');
        while (dot >= 0) {
            String parent = path.substring(0, dot);
            if (config.contains(parent, true) && !config.isConfigurationSection(parent)) {
                return true;
            }
            dot = path.indexOf('.', dot + 1);
        }
        return false;
    }

    private boolean isLegacyVersionField(String path) {
        return path.equals("file-version") || path.equals("config-version");
    }

    private YamlConfiguration parseYaml(String content) throws InvalidConfigurationException {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.options().parseComments(true);
        configuration.loadFromString(content);
        return configuration;
    }

    private int createPersistentFile(String relativePath) {
        Path file = plugin.getDataFolder().toPath().resolve(relativePath);

        if (Files.exists(file)) {
            if (!Files.isRegularFile(file)) {
                error("Kein regulärer Dateipfad: " + relativePath, null);
                return 0;
            }
            return 1;
        }

        try {
            if (!ensureDirectory(file.getParent())) return 0;

            Files.createFile(file);
            created++;
            changes.add("erstellt: " + relativePath);
            return 1;

        } catch (IOException e) {
            error("Konnte Datendatei nicht erstellen: " + relativePath, e);
            return 0;
        }
    }

    private boolean backup(Path original) {
        Path backupDir = original.getParent().resolve("backups");
        if (!ensureDirectory(backupDir)) return false;
        String filename = original.getFileName().toString();
        Path backup = backupDir.resolve(filename + ".backup-" + BACKUP_DATE.format(LocalDateTime.now()));
        try {
            Files.copy(original, backup); // never overwrite an existing backup
            backups++;
            return true;
        } catch (IOException e) {
            error("Backup fehlgeschlagen; Original bleibt unverändert: " + original, e);
            return false;
        }
    }

    private void safeWrite(Path destination, byte[] data) throws IOException {
        Path temp = Files.createTempFile(destination.getParent(), ".filemanagement-", ".tmp");
        try {
            Files.write(temp, data);
            try {
                Files.move(temp, destination, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    private boolean ensureDirectory(Path folder) {
        try {
            Files.createDirectories(folder);
            return true;
        } catch (IOException e) {
            error("Ordner konnte nicht erstellt werden: " + folder, e);
            return false;
        }
    }

    private String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 wird von dieser Java-Laufzeit nicht unterstützt", e);
        }
    }

    private void loadManifest(Path file) {
        if (Files.notExists(file)) return;
        try (InputStream input = Files.newInputStream(file)) {
            manifest.load(input);
        } catch (IOException e) {
            error("Datei-Manifest konnte nicht gelesen werden: " + file, e);
        }
    }

    private void saveManifest(Path file) {
        try {
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            manifest.store(out, "BastiGHG Challenges FileManagement 2.0 - JAR resource SHA-256 hashes");
            safeWrite(file, out.toByteArray());
        } catch (IOException e) {
            error("Datei-Manifest konnte nicht gespeichert werden: " + file, e);
        }
    }

    private void printGroup(String group, int count) {
        log(ConsoleColor.DARK_GOLDEN_LIME + "        ✔ "
                + String.format("%-23s", group) + count + " Dateien geprüft"
                + ConsoleColor.RESET);
    }

    private void missingResource(String path) {
        errors++;
        log("        " + ConsoleColor.BLOOD_ORANGE + "⚠ JAR fehlt" + ConsoleColor.RESET
                + "  " + path);
    }

    private void error(String message, Exception e) {
        errors++;
        if (e == null) plugin.getLogger().severe(message);
        else plugin.getLogger().log(Level.SEVERE, message, e);
    }

    private void log(String message) {
        Bukkit.getConsoleSender().sendMessage(message);
    }
}
