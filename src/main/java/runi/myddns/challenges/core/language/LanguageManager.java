package runi.myddns.challenges.core.language;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {

    private static final String CORE = "core";

    private static final String[] LANGUAGE_GROUPS = {
            "core",
            "mobarmybattle",
            "levelborder",
            "levelblock"
    };

    private final JavaPlugin plugin;
    private final File languageFolder;
    private final MiniMessage miniMessage;

    private final Map<String, YamlConfiguration> languageConfigs;

    private final LanguageManager root;
    private final String defaultGroup;

    // Haupt-LanguageManager -> CORE
    public LanguageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.languageFolder = new File(
                plugin.getDataFolder(),
                "languages"
        );
        this.miniMessage = MiniMessage.miniMessage();

        this.languageConfigs = new HashMap<>();

        this.root = this;
        this.defaultGroup = CORE;

        reload();
    }

    // Sprachbereich für ein Game
    private LanguageManager(
            LanguageManager root,
            String defaultGroup
    ) {
        this.plugin = root.plugin;
        this.languageFolder = root.languageFolder;
        this.miniMessage = root.miniMessage;

        this.languageConfigs = root.languageConfigs;

        this.root = root;
        this.defaultGroup = defaultGroup.toLowerCase();
    }

    public LanguageManager forGroup(String group) {

        if (!languageConfigs.containsKey(group.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Unknown language group: " + group
            );
        }

        return new LanguageManager(
                root,
                group
        );
    }

    public void reload() {

        // Nur der Hauptmanager lädt die Dateien
        if (this != root) {
            root.reload();
            return;
        }

        languageConfigs.clear();

        String language =
                plugin.getConfig().getString("language");

        if (language == null
                || language.isBlank()
                || (!language.equalsIgnoreCase("de")
                && !language.equalsIgnoreCase("en"))) {

            language = "en";
        }

        language = language.toLowerCase();

        for (String group : LANGUAGE_GROUPS) {

            File file = new File(
                    languageFolder,
                    group
                            + File.separator
                            + language
                            + ".yml"
            );

            YamlConfiguration config =
                    YamlConfiguration.loadConfiguration(file);

            languageConfigs.put(
                    group,
                    config
            );
        }
    }

    // ---------------------------------------------------------
    // STRING
    // ---------------------------------------------------------

    public String get(String path) {
        return get(
                defaultGroup,
                path
        );
    }

    public String get(
            String group,
            String path
    ) {

        YamlConfiguration config =
                getConfig(group);

        String text =
                config.getString(path);

        if (text == null) {
            return "Missing language entry: "
                    + group
                    + "."
                    + path;
        }

        return text;
    }

    public String get(
            String path,
            String placeholder,
            Object value
    ) {
        return get(defaultGroup, path)
                .replace(
                        "%" + placeholder + "%",
                        String.valueOf(value)
                );
    }

    public String get(
            String group,
            String path,
            String placeholder,
            Object value
    ) {
        return get(group, path)
                .replace(
                        "%" + placeholder + "%",
                        String.valueOf(value)
                );
    }

    // ---------------------------------------------------------
    // COMPONENT
    // ---------------------------------------------------------

    public Component getComponent(String path) {
        return getComponent(
                defaultGroup,
                path
        );
    }

    public Component getComponent(
            String group,
            String path
    ) {
        return miniMessage.deserialize(
                get(group, path)
        );
    }

    public Component getComponent(
            String path,
            String placeholder,
            Component value
    ) {
        return miniMessage.deserialize(
                get(defaultGroup, path),
                Placeholder.component(
                        placeholder,
                        value
                )
        );
    }

    public Component getComponent(
            String group,
            String path,
            String placeholder,
            Component value
    ) {
        return miniMessage.deserialize(
                get(group, path),
                Placeholder.component(
                        placeholder,
                        value
                )
        );
    }

    public Component getComponent(
            String path,
            Map<String, Component> placeholders
    ) {
        return getComponent(
                defaultGroup,
                path,
                placeholders
        );
    }

    public Component getComponent(
            String group,
            String path,
            Map<String, Component> placeholders
    ) {

        TagResolver.Builder resolver =
                TagResolver.builder();

        for (Map.Entry<String, Component> entry
                : placeholders.entrySet()) {

            resolver.resolver(
                    Placeholder.component(
                            entry.getKey(),
                            entry.getValue()
                    )
            );
        }

        return miniMessage.deserialize(
                get(group, path),
                resolver.build()
        );
    }

    public Component getComponent(
            String path,
            String placeholder,
            Object value
    ) {

        String text =
                get(defaultGroup, path);

        text = text.replace(
                "%" + placeholder + "%",
                String.valueOf(value)
        );

        return miniMessage.deserialize(text);
    }

    public Component getComponent(
            String group,
            String path,
            String placeholder,
            Object value
    ) {

        String text =
                get(group, path);

        text = text.replace(
                "%" + placeholder + "%",
                String.valueOf(value)
        );

        return miniMessage.deserialize(text);
    }

    // ---------------------------------------------------------
    // LANGUAGE
    // ---------------------------------------------------------

    public void setLanguage(String language) {

        if (language == null
                || (!language.equalsIgnoreCase("de")
                && !language.equalsIgnoreCase("en"))) {
            return;
        }

        plugin.getConfig().set(
                "language",
                language.toLowerCase()
        );

        plugin.saveConfig();

        root.reload();
    }

    public String getCurrentLanguage() {

        String language =
                plugin.getConfig().getString("language");

        if (language == null
                || language.isBlank()) {
            return "en";
        }

        return language.toLowerCase();
    }

    public boolean hasLanguage() {

        String language =
                plugin.getConfig().getString("language");

        return language != null
                && !language.isBlank();
    }

    // ---------------------------------------------------------
    // INTERN
    // ---------------------------------------------------------

    private YamlConfiguration getConfig(String group) {

        YamlConfiguration config =
                languageConfigs.get(
                        group.toLowerCase()
                );

        if (config == null) {
            throw new IllegalArgumentException(
                    "Unknown language group: "
                            + group
            );
        }

        return config;
    }
}