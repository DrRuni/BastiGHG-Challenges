package runi.myddns.challenges.core.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import runi.myddns.challenges.core.language.LanguageManager;

public final class ResetMessageUtil {

    private ResetMessageUtil() {}

    public static void start(LanguageManager language, String titleKey, String descriptionKey) {
        Bukkit.broadcast(Component.empty());
        Bukkit.broadcast(language.getComponent(titleKey));
        Bukkit.broadcast(language.getComponent(descriptionKey));
        Bukkit.broadcast(Component.empty());
    }

    public static void phase(LanguageManager language, String key) {
        Bukkit.broadcast(language.getComponent(key));
    }

    public static void finish(LanguageManager language, String finishedKey, String detailsKey) {
        Bukkit.broadcast(Component.empty());
        Bukkit.broadcast(language.getComponent(finishedKey));
        Bukkit.broadcast(language.getComponent(detailsKey));
        Bukkit.broadcast(Component.empty());
    }

    public static void failed(LanguageManager language, String key) {
        Bukkit.broadcast(Component.empty());
        Bukkit.broadcast(language.getComponent(key));
        Bukkit.broadcast(Component.empty());
    }
}
