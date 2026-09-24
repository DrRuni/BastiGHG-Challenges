package runi.myddns.challenges.core.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import runi.myddns.challenges.ChallengeMain;
import runi.myddns.challenges.core.game.ChallengeGame;
import runi.myddns.challenges.core.world.GameWorldSettingsManager;

import static runi.myddns.challenges.core.utils.ColorUtil.gradientText;
import static runi.myddns.challenges.core.utils.DisplayColor.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WorldSettingsGUI implements Listener {

    private static class WorldSettingsHolder implements InventoryHolder {

        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private final ChallengeMain plugin;

    private static final List<String> DIFFICULTIES = List.of(
            "peaceful",
            "easy",
            "normal",
            "hard",
            "ultra-hardcore",
            "ultra-ultra-hardcore"
    );

    private Component getGameTitle(ChallengeGame game) {

        String gameName = game.getDisplayName();

        return switch (game.getId().toLowerCase(Locale.ROOT)) {
            case "mobarmybattle" -> gradientText(gameName,
                            BRIGHT_RED,
                            BLUE);

            case "levelborder" -> gradientText(gameName,
                            LIGHT_BLUE,
                            DEEP_BLUE);

            case "levelblock" -> gradientText(gameName,
                            LIME,
                            DARK_GREEN);

            default ->
                    gradientText(
                            gameName,
                            WHITE,
                            LIGHT_GREY
                    );
        };
    }

    public WorldSettingsGUI(ChallengeMain plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {

        ChallengeGame game = plugin.getGameManager().getSelectedGame();

        if (game == null || !game.isLoaded()) {
            plugin.getLobbyDisplayManager().setLoadStatus(
                    "Bitte zuerst ein Game laden.",
                    0xFFAA00
            );
            return;
        }

        GameWorldSettingsManager settings =
                game.getWorldSettingsManager();

        Inventory inv = Bukkit.createInventory(
                new WorldSettingsHolder(),
                45,
                Component.text(
                        "Settings » ",
                        TextColor.color(NEON_RED)
                ).append(
                        getGameTitle(game)
                )
        );

        inv.setItem(
                20,
                createDifficultyItem(
                        settings.getDifficulty()
                )
        );

        inv.setItem(
                22,
                createToggleItem(
                        settings.isMobSpawningEnabled()
                                ? Material.ZOMBIE_HEAD
                                : Material.PLAYER_HEAD,
                        "Mob Spawning",
                        settings.isMobSpawningEnabled()
                )
        );

        inv.setItem(
                24,
                createToggleItem(
                        settings.isKeepInventoryEnabled()
                                ? Material.LIME_WOOL
                                : Material.RED_WOOL,
                        "Keep Inventory",
                        settings.isKeepInventoryEnabled()
                )
        );

        inv.setItem(
                29,
                createToggleItem(
                        settings.isNightVisionEnabled()
                                ? Material.LIGHT
                                : Material.GRAY_CANDLE,
                        "Night Vision",
                        settings.isNightVisionEnabled()
                )
        );

        inv.setItem(
                31,
                createTimeItem(
                        settings.getWorldTime()
                )
        );

        inv.setItem(
                33,
                createToggleItem(
                        settings.isDaylightCycleEnabled()
                                ? Material.CLOCK
                                : Material.DAYLIGHT_DETECTOR,
                        "Daylight Cycle",
                        settings.isDaylightCycleEnabled()
                )
        );

        inv.setItem(
                40,
                createBackButton()
        );

        player.openInventory(inv);
    }

    private ItemStack createItem(
            Material material,
            Component name,
            Component... loreLines
    ) {

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.displayName(name);

        List<Component> lore = new ArrayList<>();

        if (loreLines != null) {
            for (Component line : loreLines) {
                if (line != null) {
                    lore.add(line);
                }
            }
        }

        meta.lore(lore);

        meta.addItemFlags(
                ItemFlag.HIDE_ATTRIBUTES,
                ItemFlag.HIDE_ENCHANTS,
                ItemFlag.HIDE_UNBREAKABLE
        );

        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createToggleItem(
            Material material,
            String name,
            boolean state
    ) {

        Component displayName =
                Component.text(
                        (state ? "AN" : "AUS") +
                                " - " +
                                name
                );

        return createItem(
                material,
                displayName,
                Component.empty(),
                Component.text(
                        "Status: " +
                                (state ? "AN" : "AUS")
                )
        );
    }

    private ItemStack createDifficultyItem(
            String difficulty
    ) {

        Material material =
                getDifficultyMaterial(difficulty);

        return createItem(
                material,
                Component.text(
                        "Difficulty: " +
                                getDifficultyName(difficulty)
                ),
                Component.empty(),
                Component.text(
                        "Klicken zum Ändern"
                )
        );
    }

    private ItemStack createTimeItem(
            long currentTime
    ) {

        String phase;
        Material material;

        if (currentTime < 6000) {
            phase = "Morgen";
            material = Material.ORANGE_WOOL;

        } else if (currentTime < 12000) {
            phase = "Mittag";
            material = Material.YELLOW_WOOL;

        } else if (currentTime < 18000) {
            phase = "Abend";
            material = Material.RED_WOOL;

        } else {
            phase = "Nacht";
            material = Material.BLUE_WOOL;
        }

        return createItem(
                material,
                Component.text(
                        "World Time: " +
                                phase
                ),
                Component.empty(),
                Component.text(
                        "Klicken zum Ändern"
                )
        );
    }

    private ItemStack createBackButton() {

        return createItem(
                Material.ARROW,
                Component.text("Zurück"),
                Component.empty(),
                Component.text(
                        "Zurück zur Lobby"
                )
        );
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!(event.getView()
                .getTopInventory()
                .getHolder()
                instanceof WorldSettingsHolder)) {
            return;
        }

        event.setCancelled(true);

        int slot = event.getRawSlot();

        if (slot < 0
                || slot >= event.getView()
                .getTopInventory()
                .getSize()) {
            return;
        }

        ChallengeGame game =
                plugin.getGameManager()
                        .getSelectedGame();

        if (game == null || !game.isLoaded()) {
            player.closeInventory();
            return;
        }

        GameWorldSettingsManager settings =
                game.getWorldSettingsManager();

        boolean reopen = true;

        switch (slot) {

            case 20 -> nextDifficulty(settings);

            case 22 -> settings.setMobSpawning(
                    !settings.isMobSpawningEnabled()
            );

            case 24 -> settings.setKeepInventory(
                    !settings.isKeepInventoryEnabled()
            );

            case 29 -> settings.setNightVisionEnabled(
                    !settings.isNightVisionEnabled()
            );

            case 31 -> nextWorldTime(settings);

            case 33 -> settings.setDaylightCycle(
                    !settings.isDaylightCycleEnabled()
            );

            case 40 -> {
                player.closeInventory();
                reopen = false;
            }

            default -> {
                return;
            }
        }

        if (slot != 40) {
            settings.save();
            settings.applyAll();
        }

        if (reopen) {
            Bukkit.getScheduler().runTaskLater(
                    plugin,
                    () -> open(player),
                    2L
            );
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {

        if (!(event.getView()
                .getTopInventory()
                .getHolder()
                instanceof WorldSettingsHolder)) {
            return;
        }

        event.setCancelled(true);
    }

    private void nextDifficulty(
            GameWorldSettingsManager settings
    ) {

        String current =
                settings.getDifficulty()
                        .toLowerCase(Locale.ROOT);

        int index =
                DIFFICULTIES.indexOf(current);

        if (index == -1) {
            index =
                    DIFFICULTIES.indexOf("normal");
        }

        int nextIndex =
                (index + 1)
                        % DIFFICULTIES.size();

        settings.setDifficulty(
                DIFFICULTIES.get(nextIndex)
        );
    }

    private Material getDifficultyMaterial(
            String difficulty
    ) {

        return switch (
                difficulty.toLowerCase(Locale.ROOT)
                ) {
            case "peaceful" ->
                    Material.WHITE_WOOL;

            case "easy" ->
                    Material.LIME_WOOL;

            case "normal" ->
                    Material.YELLOW_WOOL;

            case "hard" ->
                    Material.RED_WOOL;

            case "ultra-hardcore" ->
                    Material.PURPLE_WOOL;

            case "ultra-ultra-hardcore" ->
                    Material.BLACK_WOOL;

            default ->
                    Material.YELLOW_WOOL;
        };
    }

    private String getDifficultyName(
            String difficulty
    ) {

        return switch (
                difficulty.toLowerCase(Locale.ROOT)
                ) {
            case "peaceful" ->
                    "Peaceful";

            case "easy" ->
                    "Easy";

            case "normal" ->
                    "Normal";

            case "hard" ->
                    "Hard";

            case "ultra-hardcore" ->
                    "Ultra Hardcore";

            case "ultra-ultra-hardcore" ->
                    "Ultra Ultra Hardcore";

            default ->
                    "Normal";
        };
    }

    private void nextWorldTime(
            GameWorldSettingsManager settings
    ) {

        long current =
                settings.getWorldTime();

        long next;

        if (current < 6000) {
            next = 6000;

        } else if (current < 12000) {
            next = 12000;

        } else if (current < 18000) {
            next = 18000;

        } else {
            next = 0;
        }

        settings.setWorldTime(next);
    }
}