package runi.myddns.challenges.core.language;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class LanguageManager {

    private final JavaPlugin plugin;
    private final File languageFolder;
    private final MiniMessage miniMessage;

    private YamlConfiguration languageConfig;

    public LanguageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.languageFolder = new File(plugin.getDataFolder(), "languages");
        this.miniMessage = MiniMessage.miniMessage();

        createLanguageFiles();
        reload();
    }

    private void createLanguageFiles() {
        if (!languageFolder.exists() && !languageFolder.mkdirs()) {
            plugin.getLogger().warning("Sprachordner konnte nicht erstellt werden.");
            return;
        }

        copyLanguageFile("de.yml");
        copyLanguageFile("en.yml");
    }

    private void copyLanguageFile(String fileName) {
        File target = new File(languageFolder, fileName);
        if (target.exists()) return;

        plugin.saveResource("languages/" + fileName, false);
    }

    public void reload() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        String language = config.getString("language");

        if (language == null || language.isBlank()
                || (!language.equalsIgnoreCase("de") && !language.equalsIgnoreCase("en"))) {
            language = "en";
        }

        File languageFile = new File(languageFolder, language.toLowerCase() + ".yml");
        languageConfig = YamlConfiguration.loadConfiguration(languageFile);
    }

    public String get(String path) {
        String text = languageConfig.getString(path);
        if (text == null) return "Missing language entry: " + path;
        return text;
    }

    public Component getComponent(String path) {
        String text = languageConfig.getString(path);

        if (text == null) return Component.text("Missing language entry: " + path);

        return miniMessage.deserialize(text);
    }

    public Component getComponent(String path, String placeholder, Component value) {
        return miniMessage.deserialize(
                get(path),
                Placeholder.component(placeholder, value)
        );
    }

    public Component getComponent(String path, Map<String, Component> placeholders) {
        TagResolver.Builder resolver = TagResolver.builder();

        for (Map.Entry<String, Component> entry : placeholders.entrySet()) {
            resolver.resolver(Placeholder.component(entry.getKey(), entry.getValue()));
        }

        return miniMessage.deserialize(get(path), resolver.build());
    }

    public Component getComponent(String path, String placeholder, Object value) {
        String text = languageConfig.getString(path);

        if (text == null) return Component.text("Missing language entry: " + path);

        text = text.replace("%" + placeholder + "%", String.valueOf(value));
        return miniMessage.deserialize(text);
    }

    public String get(String path, String placeholder, Object value) {
        return get(path).replace("%" + placeholder + "%", String.valueOf(value));
    }

    public void setLanguage(String language) {
        if (language == null || (!language.equalsIgnoreCase("de") && !language.equalsIgnoreCase("en"))) return;

        plugin.getConfig().set("language", language.toLowerCase());
        plugin.saveConfig();

        reload();
    }

    public String getCurrentLanguage() {
        String language = plugin.getConfig().getString("language");

        if (language == null || language.isBlank()) return "en";

        return language.toLowerCase();
    }

    public boolean hasLanguage() {
        String language = plugin.getConfig().getString("language");
        return language != null && !language.isBlank();
    }
}