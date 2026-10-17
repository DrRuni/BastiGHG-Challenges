package runi.myddns.challenges.core.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
import runi.myddns.challenges.games.MobArmyBattle.MobArmyBattleGame;

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

    private final ResetManagementGUI resetManagementGUI;
    private final ChallengeMain plugin;

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

        this.resetManagementGUI =
                new ResetManagementGUI(plugin);
    }

    public void open(Player player) {

        ChallengeGame game = plugin.getGameManager().getSelectedGame();

        if (game == null || !game.isLoaded()) {
            plugin.getLobbyDisplayManager().setLoadStatus(
                    plugin.getLanguageManager()
                            .get("world-settings.load-first"),
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
                        plugin.getLanguageManager()
                                .get("world-settings.title-prefix"),
                        TextColor.color(BLACK)
                ).append(
                        getGameTitle(game)
                )
        );

        inv.setItem(
                10,
                createDifficultyItem(
                        settings.getDifficulty()
                )
        );

        inv.setItem(
                13,
                createToggleItem(
                        settings.isMobSpawningEnabled()
                                ? Material.ZOMBIE_HEAD
                                : Material.PLAYER_HEAD,
                        plugin.getLanguageManager().get("world-settings.mob-spawning"),
                        settings.isMobSpawningEnabled()
                )
        );

        inv.setItem(
                16,
                createToggleItem(
                        settings.isKeepInventoryEnabled()
                                ? Material.LIME_WOOL
                                : Material.RED_WOOL,
                        plugin.getLanguageManager().get("world-settings.keep-inventory"),
                        settings.isKeepInventoryEnabled()
                )
        );

        inv.setItem(
                20,
                createToggleItem(
                        settings.isNightVisionEnabled()
                                ? Material.LIGHT
                                : Material.GRAY_CANDLE,
                        plugin.getLanguageManager().get("world-settings.night-vision"),
                        settings.isNightVisionEnabled()
                )
        );

        inv.setItem(
                22,
                createTimeItem(
                        settings.getWorldTime()
                )
        );

        inv.setItem(
                24,
                createToggleItem(
                        settings.isDaylightCycleEnabled()
                                ? Material.CLOCK
                                : Material.DAYLIGHT_DETECTOR,
                        plugin.getLanguageManager().get("world-settings.daylight-cycle"),
                        settings.isDaylightCycleEnabled()
                )
        );

        if (game.getId().equalsIgnoreCase("levelborder")
                || game.getId().equalsIgnoreCase("levelblock")) {

            inv.setItem(
                    4,
                    createItem(
                            Material.GRASS_BLOCK,
                            Component.text(
                                    plugin.getLanguageManager()
                                            .get("world-settings.reset.name"),
                                    NamedTextColor.RED
                            ),
                            Component.empty(),
                            Component.text(
                                    plugin.getLanguageManager()
                                            .get("world-settings.reset.description"),
                                    NamedTextColor.GRAY
                            )
                    )
            );
        }

        if (game instanceof MobArmyBattleGame mobArmyGame) {

            boolean randomizerOn =
                    mobArmyGame.getBlockRandomizerManager()
                            .isGlobalRandomizerEnabled();

            boolean chestRandomizerOn =
                    mobArmyGame.getWorldSettings()
                            .isChestRandomizerEnabled();

            inv.setItem(
                    28,
                    createItem(
                            randomizerOn
                                    ? Material.LIME_WOOL
                                    : Material.RED_WOOL,
                            mobArmyGame.getLanguageManager()
                                    .getComponent(
                                            "world-settings-gui.block-randomizer.name"
                                    ),
                            Component.empty(),
                            mobArmyGame.getLanguageManager()
                                    .getComponent(
                                            "world-settings-gui.block-randomizer.description-1"
                                    ),
                            mobArmyGame.getLanguageManager()
                                    .getComponent(
                                            "world-settings-gui.block-randomizer.description-2"
                                    )
                    )
            );

            inv.setItem(
                    30,
                    createItem(
                            chestRandomizerOn
                                    ? Material.CHEST
                                    : Material.BARRIER,
                            mobArmyGame.getLanguageManager()
                                    .getComponent(
                                            "world-settings-gui.chest-randomizer.name"
                                    )
                    )
            );

            inv.setItem(
                    32,
                    createItem(
                            Material.SPAWNER,
                            mobArmyGame.getLanguageManager()
                                    .getComponent(
                                            "world-settings-gui.block-randomizer.exclusions.name"
                                    ),
                            Component.empty(),
                            mobArmyGame.getLanguageManager()
                                    .getComponent(
                                            "world-settings-gui.block-randomizer.exclusions.description-1"
                                    ),
                            mobArmyGame.getLanguageManager()
                                    .getComponent(
                                            "world-settings-gui.block-randomizer.exclusions.description-2"
                                    )
                    )
            );

            inv.setItem(
                    34,
                    createItem(
                            Material.TNT,
                            mobArmyGame.getLanguageManager()
                                    .getComponent(
                                            "world-settings-gui.block-randomizer.reset.name"
                                    ),
                            Component.empty(),
                            mobArmyGame.getLanguageManager()
                                    .getComponent(
                                            "world-settings-gui.block-randomizer.reset.description-1"
                                    ),
                            mobArmyGame.getLanguageManager()
                                    .getComponent(
                                            "world-settings-gui.block-randomizer.reset.description-2"
                                    )
                    )
            );
        }

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

        String stateText =
                plugin.getLanguageManager().get(
                        state
                                ? "world-settings.state.on"
                                : "world-settings.state.off"
                );

        Component displayName =
                Component.text(
                        stateText + " - " + name
                );

        return createItem(
                material,
                displayName,
                Component.empty(),
                Component.text(
                        plugin.getLanguageManager().get(
                                "world-settings.state.status",
                                "state",
                                stateText
                        )
                )
        );
    }

    private ItemStack createDifficultyItem(
            String difficulty
    ) {

        Material material =
                getDifficultyMaterial(difficulty);

        String difficultyName =
                getDifficultyName(difficulty);

        return createItem(
                material,
                Component.text(
                        plugin.getLanguageManager().get(
                                "world-settings.difficulty.title",
                                "difficulty",
                                difficultyName
                        )
                ),
                Component.empty(),
                Component.text(
                        plugin.getLanguageManager()
                                .get("world-settings.difficulty.click")
                )
        );
    }

    private ItemStack createTimeItem(
            long currentTime
    ) {

        String phase;
        Material material;

        if (currentTime < 6000) {

            phase = plugin.getLanguageManager()
                    .get("world-settings.time.morning");

            material = Material.ORANGE_WOOL;

        } else if (currentTime < 12000) {

            phase = plugin.getLanguageManager()
                    .get("world-settings.time.noon");

            material = Material.YELLOW_WOOL;

        } else if (currentTime < 18000) {

            phase = plugin.getLanguageManager()
                    .get("world-settings.time.evening");

            material = Material.RED_WOOL;

        } else {

            phase = plugin.getLanguageManager()
                    .get("world-settings.time.night");

            material = Material.BLUE_WOOL;
        }

        return createItem(
                material,
                Component.text(
                        plugin.getLanguageManager().get(
                                "world-settings.time.title",
                                "phase",
                                phase
                        )
                ),
                Component.empty(),
                Component.text(
                        plugin.getLanguageManager()
                                .get("world-settings.time.click")
                )
        );
    }

    private ItemStack createBackButton() {

        return createItem(
                Material.ARROW,
                Component.text(
                        plugin.getLanguageManager()
                                .get("world-settings.back.name")
                ),
                Component.empty(),
                Component.text(
                        plugin.getLanguageManager()
                                .get("world-settings.back.description")
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

            case 4 -> {

                if (!game.getId().equalsIgnoreCase("levelborder")
                        && !game.getId().equalsIgnoreCase("levelblock")) {
                    return;
                }

                resetManagementGUI.open(player);

                reopen = false;
            }

            case 10 -> nextDifficulty(settings);

            case 13 -> settings.setMobSpawning(
                    !settings.isMobSpawningEnabled()
            );

            case 16 -> settings.setKeepInventory(
                    !settings.isKeepInventoryEnabled()
            );

            case 20 -> settings.setNightVisionEnabled(
                    !settings.isNightVisionEnabled()
            );

            case 22 -> nextWorldTime(settings);

            case 24 -> settings.setDaylightCycle(
                    !settings.isDaylightCycleEnabled()
            );

            case 28 -> {

                if (!(game instanceof MobArmyBattleGame mobArmyGame)) {
                    return;
                }

                mobArmyGame.getWorldSettings()
                        .toggleRandomizer();

                boolean newState =
                        mobArmyGame.getWorldSettings()
                                .isRandomizerEnabled();

                mobArmyGame.getBlockRandomizerManager()
                        .setGlobalRandomizerEnabled(newState);

                broadcast(
                        newState
                                ? mobArmyGame.getLanguageManager()
                                .getComponent(
                                        "world-settings-gui.block-randomizer.enabled"
                                )
                                : mobArmyGame.getLanguageManager()
                                .getComponent(
                                        "world-settings-gui.block-randomizer.disabled"
                                )
                );
            }

            case 30 -> {

                if (!(game instanceof MobArmyBattleGame mobArmyGame)) {
                    return;
                }

                mobArmyGame.getWorldSettings()
                        .toggleChestRandomizer();

                boolean newState =
                        mobArmyGame.getWorldSettings()
                                .isChestRandomizerEnabled();

                broadcast(
                        newState
                                ? mobArmyGame.getLanguageManager()
                                .getComponent(
                                        "world-settings-gui.chest-randomizer.enabled"
                                )
                                : mobArmyGame.getLanguageManager()
                                .getComponent(
                                        "world-settings-gui.chest-randomizer.disabled"
                                )
                );
            }

            case 32 -> {

                if (!(game instanceof MobArmyBattleGame mobArmyGame)) {
                    return;
                }

                mobArmyGame.getSpawnEggGUI()
                        .openGUI(player);

                reopen = false;
            }

            case 34 -> {

                if (!(game instanceof MobArmyBattleGame mobArmyGame)) {
                    return;
                }

                mobArmyGame.getBlockRandomizerManager()
                        .resetRandomizer();

                broadcast(
                        mobArmyGame.getLanguageManager()
                                .getComponent(
                                        "world-settings-gui.block-randomizer.reset.message"
                                )
                );
            }

            case 40 -> {

                if (game instanceof MobArmyBattleGame mobArmyGame) {

                    mobArmyGame.getEventSettingsGUI()
                            .open(player);

                } else {

                    player.closeInventory();
                }

                reopen = false;
            }

            default -> {
                return;
            }
        }

        if (slot == 10
                || slot == 13
                || slot == 16
                || slot == 20
                || slot == 22
                || slot == 24) {

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

        settings.cycleDifficulty();
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
                    plugin.getLanguageManager()
                            .get("world-settings.difficulty.peaceful");

            case "easy" ->
                    plugin.getLanguageManager()
                            .get("world-settings.difficulty.easy");

            case "normal" ->
                    plugin.getLanguageManager()
                            .get("world-settings.difficulty.normal");

            case "hard" ->
                    plugin.getLanguageManager()
                            .get("world-settings.difficulty.hard");

            case "ultra-hardcore" ->
                    plugin.getLanguageManager()
                            .get("world-settings.difficulty.ultra-hardcore");

            case "ultra-ultra-hardcore" ->
                    plugin.getLanguageManager()
                            .get("world-settings.difficulty.ultra-ultra-hardcore");

            default ->
                    plugin.getLanguageManager()
                            .get("world-settings.difficulty.normal");
        };
    }

    private void broadcast(
            Component message
    ) {

        for (Player player :
                Bukkit.getOnlinePlayers()) {

            player.sendMessage(message);
        }
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