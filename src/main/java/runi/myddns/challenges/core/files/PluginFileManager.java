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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Properties;
import java.util.logging.Level;

public final class PluginFileManager {

    private static final String WORLD_SETTINGS_RESOURCE = "core/gamesettings.yml";
    private static final String MANIFEST_NAME = ".filemanagement.properties";
    private static final String BASELINE_DIR = ".filemanagement/templates";
    private static final String LOCK_KEY = "# filemanagement.locked:";
    private static final String VALUES_KEY = "# filemanagement.protect-values:";
    private static final Pattern CONTROL_LINE = Pattern.compile(
            "(?m)^[ \\t]*#[ \\t]*filemanagement\\.(locked|protect-values)[ \\t]*:[ \\t]*(true|false)[ \\t]*(?:\\r?\\n|$)",
            Pattern.CASE_INSENSITIVE);

    private record Protection(boolean locked, boolean protectValues,
                              boolean hasHeader, String header) {}

    private static final DateTimeFormatter BACKUP_DATE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS");

    private record GameFiles(String name, String folder, List<String> templates,
                             List<String> persistentFiles, boolean sharedWorldSettings) {}

    private record Change(String path, boolean created) {}

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
    private final List<Change> changes = new ArrayList<>();
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
        log(ConsoleColor.PURPLE + "                   ◆ BastiGHG Challenges" + ConsoleColor.RESET);
        log(ConsoleColor.ORANGE + "                     FileManagement 2.0" + ConsoleColor.RESET);
        log("        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        int firstChange = changes.size();
        int core = processTemplate("config.yml", "config.yml");
        core += createPersistentFile("game-state.yml");
        printGroup("Core", core, firstChange);

        for (GameFiles game : GAMES) {
            firstChange = changes.size();
            int inspected = 0;

            for (String name : game.templates()) {
                String relative = game.folder() + "/" + name;
                inspected += processTemplate(relative, relative);
            }

            if (game.sharedWorldSettings()) {
                inspected += processTemplate(WORLD_SETTINGS_RESOURCE,
                        game.folder() + "/gamesettings.yml");
            }

            for (String name : game.persistentFiles()) {
                inspected += createPersistentFile(game.folder() + "/" + name);
            }

            printGroup(game.name(), inspected, firstChange);
        }

        firstChange = changes.size();
        int languageCount = 0;
        for (String group : LANGUAGES) {
            for (String lang : List.of("de.yml", "en.yml")) {
                String relative = "languages/" + group + "/" + lang;
                languageCount += processTemplate(relative, relative);
            }
        }
        printGroup("Languages (DE / EN)", languageCount, firstChange);

        saveManifest(dataFolder.resolve(MANIFEST_NAME));

        log("        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        if (updated > 0 || created > 0) {
            StringBuilder summary = new StringBuilder("        ↻ ");

            if (updated > 0) {
                summary.append(ConsoleColor.ORANGE)
                        .append(updated).append(" aktualisiert")
                        .append(ConsoleColor.RESET);
            }

            if (created > 0) {
                if (updated > 0) summary.append("  ·  ");

                summary.append(ConsoleColor.BLOOD_ORANGE)
                        .append(created).append(" erstellt")
                        .append(ConsoleColor.RESET);
            }

            log(summary.toString());
        }

        if (backups > 0) {
            log(ConsoleColor.DARK_GOLDEN_LIME + "        ✔ " + backups
                    + " Backup(s) erstellt" + ConsoleColor.RESET);
        }

        if (errors == 0) {
            log(ConsoleColor.DARK_GOLDEN_LIME
                    + "        ✔ FileManagement bereit — 0 Fehler" + ConsoleColor.RESET);
        } else {
            log(ConsoleColor.BLOOD_ORANGE
                    + "        ⚠ FileManagement beendet — " + errors + " Fehler"
                    + ConsoleColor.RESET);
        }
        log("");
    }

    private int processTemplate(String resourcePath, String relativeTarget) {
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

        String bundledContent = stripControlLines(new String(resourceBytes, StandardCharsets.UTF_8));
        byte[] canonicalBytes = bundledContent.getBytes(StandardCharsets.UTF_8);
        String bundledHash = sha256(canonicalBytes);
        Path baseline = plugin.getDataFolder().toPath()
                .resolve(BASELINE_DIR).resolve(relativeTarget);
        boolean jarChanged = !bundledHash.equals(manifest.getProperty(relativeTarget));

        try {
            if (Files.notExists(target)) {
                if (!ensureDirectory(target.getParent())) return 1;
                Protection defaults = defaultProtection(relativeTarget);
                safeWrite(target, withHeader(defaults.header(), bundledContent).getBytes(StandardCharsets.UTF_8));
                created++;
                changes.add(new Change(relativeTarget, true));
            } else {
                if (!Files.isRegularFile(target)) {
                    error("Kein regulärer Dateipfad: " + relativeTarget, null);
                    return 1;
                }

                String serverContent = Files.readString(target, StandardCharsets.UTF_8);
                Protection protection = readProtection(serverContent, relativeTarget);
                String serverBody = stripControlLines(serverContent);

                if (protection.locked()) {
                    if (jarChanged && !serverBody.equals(bundledContent)) {
                        blocked(relativeTarget, "Schreibschutz aktiviert");
                    }
                } else if (protection.protectValues()) {
                    MergeResult result = mergeYaml(serverBody, bundledContent);
                    if (result.changed()) {
                        if (!backup(target)) return 1;
                        String merged = withHeader(protection.header(), result.content());
                        safeWrite(target, merged.getBytes(StandardCharsets.UTF_8));
                        updated++;
                        changes.add(new Change(relativeTarget, false));
                    }
                    if (jarChanged && result.conflictingValues()) {
                        blocked(relativeTarget, "Werteschutz aktiv; bestehende Werte erhalten");
                    }
                } else {
                    // Ohne Schutz zählt der tatsächliche Dateivergleich,
                    // nicht ausschließlich der zuletzt gespeicherte JAR-Hash.
                    if (!serverBody.equals(bundledContent)) {
                        if (!backup(target)) return 1;
                        String replacement = withHeader(protection.header(), bundledContent);
                        safeWrite(target, replacement.getBytes(StandardCharsets.UTF_8));
                        updated++;
                        changes.add(new Change(relativeTarget, false));
                    }
                }
            }

            if (!ensureDirectory(baseline.getParent())) return 1;
            safeWrite(baseline, canonicalBytes);
            manifest.setProperty(relativeTarget, bundledHash);
        } catch (IOException | InvalidConfigurationException | IllegalArgumentException e) {
            error("Datei konnte nicht aktualisiert werden: " + relativeTarget, e);
        }
        return 1;
    }

    private record MergeResult(String content, boolean changed, boolean conflictingValues) {}

    private MergeResult mergeYaml(String serverContent, String jarContent)
            throws InvalidConfigurationException {
        YamlConfiguration latest = parseYaml(jarContent);
        YamlConfiguration existing = parseYaml(serverContent);
        boolean changed = false;
        boolean conflicting = false;

        for (String path : latest.getKeys(true)) {
            if (latest.isConfigurationSection(path)) {
                if (!existing.contains(path, true)
                        && latest.getConfigurationSection(path).getKeys(false).isEmpty()
                        && !hasScalarParent(existing, path)) {
                    existing.createSection(path);
                    changed = true;
                }
                continue;
            }

            if (hasScalarParent(existing, path)) {
                conflicting = true;
                continue;
            }
            if (!existing.contains(path, true)) {
                existing.set(path, latest.get(path));
                changed = true;
            } else if (existing.isConfigurationSection(path)
                    || !Objects.equals(existing.get(path), latest.get(path))) {
                conflicting = true;
            }
        }
        return new MergeResult(changed ? existing.saveToString() : serverContent,
                changed, conflicting);
    }

    private boolean isProtectedFile(String relative) {
        if (relative.equals("config.yml") || relative.startsWith("languages/")) return true;
        if (!relative.startsWith("games/")) return false;

        String fileName = relative.substring(relative.lastIndexOf('/') + 1);
        if (fileName.equals("gamedata.yml") || fileName.equals("gamesettings.yml")) return true;
        if (relative.equals("games/mobarmybattle/team-equipment.yml")
                || relative.equals("games/mobarmybattle/waves.yml")) return true;
        return relative.equals("games/levelborder/config.yml");
    }

    private Protection defaultProtection(String relative) {
        boolean protectValues = isProtectedFile(relative);
        return new Protection(false, protectValues, false,
                LOCK_KEY + " false\n" + VALUES_KEY + " " + protectValues + "\n");
    }

    private Protection readProtection(String text, String relative) {
        Protection fallback = defaultProtection(relative);
        boolean locked = fallback.locked();
        boolean values = fallback.protectValues();
        boolean hasHeader = false;

        Matcher m = CONTROL_LINE.matcher(text);
        while (m.find()) {
            hasHeader = true;
            boolean v = Boolean.parseBoolean(m.group(2).toLowerCase(java.util.Locale.ROOT));
            if (m.group(1).equalsIgnoreCase("locked")) locked = v;
            else values = v;
        }
        // Fehlende Kopfzeilen nicht ungefragt in bestehende Serverdateien einfügen.
        // Sind Steuerzeilen vorhanden, werden beide beim Schreiben erhalten/normalisiert.
        String header = hasHeader
                ? LOCK_KEY + " " + locked + "\n" + VALUES_KEY + " " + values + "\n"
                : "";
        return new Protection(locked, values, hasHeader, header);
    }

    private String stripControlLines(String text) {
        // Entfernt ausschließlich unsere beiden Steuerkommentare, keine normalen YAML-Kommentare.
        return CONTROL_LINE.matcher(text).replaceAll("");
    }

    private String withHeader(String header, String body) {
        if (header.isEmpty()) return body;
        // Kontrollzeilen werden beim Überschreiben immer beibehalten.
        return header + stripControlLines(body).stripLeading();
    }

    private void blocked(String relative, String why) {
        log(ConsoleColor.ORANGE + "          ⚠ Update zurückgestellt: " + relative
                + " (" + why + ")" + ConsoleColor.RESET);
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
            changes.add(new Change(relativePath, true));
        } catch (IOException e) {
            error("Konnte Datendatei nicht erstellen: " + relativePath, e);
        }
        return 1;
    }

    private boolean backup(Path original) {
        Path backupDir = original.getParent().resolve("backups");
        if (!ensureDirectory(backupDir)) return false;
        Path backupFile = backupDir.resolve(original.getFileName()
                + ".backup-" + BACKUP_DATE.format(LocalDateTime.now()));
        try {
            Files.copy(original, backupFile);
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
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 wird nicht unterstützt", e);
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
            manifest.store(out, "BastiGHG Challenges FileManagement 2.1 - JAR resource SHA-256 hashes");
            safeWrite(file, out.toByteArray());
        } catch (IOException e) {
            error("Datei-Manifest konnte nicht gespeichert werden: " + file, e);
        }
    }

    private void printGroup(String group, int count, int firstChange) {
        log(ConsoleColor.DARK_GOLDEN_LIME + "        ✔ "
                + String.format("%-23s", group) + ConsoleColor.RESET
                + count + " Dateien geprüft");

        for (int i = firstChange; i < changes.size(); i++) {
            Change change = changes.get(i);
            String color = change.created() ? ConsoleColor.BLOOD_ORANGE : ConsoleColor.ORANGE;
            String action = change.created() ? "erstellt" : "aktualisiert";
            log("          " + color + "↳ " + action + ": "
                    + change.path() + ConsoleColor.RESET);
        }
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
