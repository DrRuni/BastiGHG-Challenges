package runi.myddns.challenges.games.mobarmywars.GUIs;

import net.kyori.adventure.text.Component;
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
import runi.myddns.challenges.games.mobarmywars.Managers.Event.BlockRandomizerManager;
import runi.myddns.challenges.games.mobarmywars.MobArmyWarsGame;
import runi.myddns.challenges.games.mobarmywars.Utils.Sounds;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WorldSettingsGUI implements Listener {

    private final MobArmyWarsGame game;
    private final BlockRandomizerManager blockRandomizerManager;

    public WorldSettingsGUI(
            MobArmyWarsGame game,
            BlockRandomizerManager blockRandomizerManager
    ) {
        this.game = game;
        this.blockRandomizerManager = blockRandomizerManager;
    }

    public void open(Player player) {

        Inventory inv = Bukkit.createInventory(
                null,
                45,
                lang("world-settings-gui.title")
        );

        boolean randomizerOn =
                blockRandomizerManager.isGlobalRandomizerEnabled();

        boolean keepInvOn =
                game.getWorldSettings().isKeepInventoryEnabled();

        boolean mobSpawningOn =
                game.getWorldSettings().isMobSpawningEnabled();

        boolean daylightCycleOn =
                game.getWorldSettings().isDaylightCycleEnabled();

        boolean nightVisionOn =
                game.getWorldSettings().isNightVisionEnabled();

        boolean chestRandomizerOn =
                game.getWorldSettings().isChestRandomizerEnabled();

        String difficulty =
                game.getWorldSettings().getDifficulty();

        long currentTime =
                game.getWorldSettings().getCurrentWorldTime();

        inv.setItem(10, createToggleItem(
                randomizerOn ? Material.LIME_WOOL : Material.RED_WOOL,
                "world-settings-gui.block-randomizer.name",
                randomizerOn,
                lang("world-settings-gui.block-randomizer.description-1"),
                lang("world-settings-gui.block-randomizer.description-2")
        ));

        inv.setItem(12, createToggleItem(
                chestRandomizerOn ? Material.CHEST : Material.BARRIER,
                "world-settings-gui.chest-randomizer.name",
                chestRandomizerOn
        ));

        inv.setItem(14, createItem(
                Material.SPAWNER,
                lang("world-settings-gui.block-randomizer.exclusions.name"),
                Component.empty(),
                lang("world-settings-gui.block-randomizer.exclusions.description-1"),
                lang("world-settings-gui.block-randomizer.exclusions.description-2")
        ));

        inv.setItem(16, createItem(
                Material.TNT,
                lang("world-settings-gui.block-randomizer.reset.name"),
                Component.empty(),
                lang("world-settings-gui.block-randomizer.reset.description-1"),
                lang("world-settings-gui.block-randomizer.reset.description-2")
        ));

        inv.setItem(
                20,
                createDifficultyItem(difficulty)
        );

        inv.setItem(22, createToggleItem(
                mobSpawningOn ? Material.ZOMBIE_HEAD : Material.PLAYER_HEAD,
                "world-settings-gui.mob-spawning.name",
                mobSpawningOn
        ));

        inv.setItem(24, createToggleItem(
                keepInvOn ? Material.LIME_WOOL : Material.RED_WOOL,
                "world-settings-gui.keep-inventory.name",
                keepInvOn,
                lang("world-settings-gui.keep-inventory.description-1"),
                lang("world-settings-gui.keep-inventory.description-2")
        ));

        inv.setItem(29, createToggleItem(
                nightVisionOn ? Material.LIGHT : Material.GRAY_CANDLE,
                "world-settings-gui.night-vision.name",
                nightVisionOn,
                nightVisionOn
                        ? lang("world-settings-gui.night-vision.enabled-description")
                        : lang("world-settings-gui.night-vision.disabled-description")
        ));

        inv.setItem(
                31,
                createTimeItem(currentTime)
        );

        inv.setItem(33, createToggleItem(
                daylightCycleOn ? Material.CLOCK : Material.DAYLIGHT_DETECTOR,
                "world-settings-gui.daylight-cycle.name",
                daylightCycleOn,
                daylightCycleOn
                        ? lang("world-settings-gui.daylight-cycle.enabled-description")
                        : lang("world-settings-gui.daylight-cycle.disabled-description")
        ));

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
            String namePath,
            boolean state,
            Component... description
    ) {

        Component name = Component.empty()
                .append(state
                        ? lang("world-settings-gui.status.on")
                        : lang("world-settings-gui.status.off"))
                .append(Component.text(" - "))
                .append(lang(namePath));

        List<Component> lore = new ArrayList<>();

        lore.add(Component.empty());
        lore.add(
                langStatus(
                        state
                                ? langRaw("world-settings-gui.status.on")
                                : langRaw("world-settings-gui.status.off")
                )
        );

        if (description != null
                && description.length > 0) {

            lore.add(Component.empty());

            for (Component line : description) {
                if (line != null) {
                    lore.add(line);
                }
            }
        }

        return createItem(
                material,
                name,
                lore.toArray(Component[]::new)
        );
    }

    private ItemStack createDifficultyItem(
            String difficulty
    ) {

        Material material = switch (
                difficulty.toLowerCase(Locale.ROOT)
                ) {
            case "peaceful" -> Material.WHITE_WOOL;
            case "easy" -> Material.LIME_WOOL;
            case "normal" -> Material.ORANGE_WOOL;
            case "hard" -> Material.RED_WOOL;
            case "ultra-ultra-hardcore" -> Material.PURPLE_WOOL;
            default -> Material.GRAY_WOOL;
        };

        Component difficultyName =
                getDifficultyName(difficulty);

        return createItem(
                material,
                game.getLanguageManager().getComponent(
                        "world-settings-gui.difficulty.name",
                        "difficulty",
                        difficultyName
                ),
                Component.empty(),
                lang("world-settings-gui.difficulty.click")
        );
    }

    private ItemStack createTimeItem(
            long currentTime
    ) {

        String phaseKey;
        Material material;

        if (currentTime < 6000) {
            phaseKey = "morning";
            material = Material.ORANGE_WOOL;

        } else if (currentTime < 12000) {
            phaseKey = "noon";
            material = Material.YELLOW_WOOL;

        } else if (currentTime < 18000) {
            phaseKey = "evening";
            material = Material.RED_WOOL;

        } else {
            phaseKey = "night";
            material = Material.BLUE_WOOL;
        }

        Component phase =
                lang("world-settings-gui.time." + phaseKey);

        return createItem(
                material,
                game.getLanguageManager().getComponent(
                        "world-settings-gui.time.name",
                        "phase",
                        phase
                ),
                Component.empty(),
                lang("world-settings-gui.time.click"),
                lang("world-settings-gui.time.cycle")
        );
    }

    private ItemStack createBackButton() {

        return createItem(
                Material.ARROW,
                lang("world-settings-gui.back.name"),
                Component.empty(),
                lang("world-settings-gui.back.description")
        );
    }

    @EventHandler
    public void onClick(
            InventoryClickEvent event
    ) {

        if (!(event.getWhoClicked()
                instanceof Player player)) {
            return;
        }

        if (!event.getView().title().equals(
                lang("world-settings-gui.title")
        )) {
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

        boolean reopen = true;

        switch (slot) {

            case 10 -> {

                Sounds.playClick(player);

                game.getWorldSettings()
                        .toggleRandomizer();

                boolean newState =
                        game.getWorldSettings()
                                .isRandomizerEnabled();

                blockRandomizerManager
                        .setGlobalRandomizerEnabled(newState);

                broadcast(
                        newState
                                ? lang("world-settings-gui.block-randomizer.enabled")
                                : lang("world-settings-gui.block-randomizer.disabled")
                );
            }

            case 12 -> {

                Sounds.playClick(player);

                game.getWorldSettings()
                        .toggleChestRandomizer();

                boolean newState =
                        game.getWorldSettings()
                                .isChestRandomizerEnabled();

                broadcast(
                        newState
                                ? lang("world-settings-gui.chest-randomizer.enabled")
                                : lang("world-settings-gui.chest-randomizer.disabled")
                );
            }

            case 14 -> {

                Sounds.playClick(player);

                game.getSpawnEggGUI()
                        .openGUI(player);

                reopen = false;
            }

            case 16 -> {

                Sounds.playReset(player);

                blockRandomizerManager
                        .resetRandomizer();

                broadcast(
                        lang("world-settings-gui.block-randomizer.reset.message")
                );
            }

            case 20 -> {

                Sounds.playClick(player);

                handleDifficultyClick();
            }

            case 22 -> {

                Sounds.playClick(player);

                game.getWorldSettings()
                        .toggleMobSpawning();

                boolean newState =
                        game.getWorldSettings()
                                .isMobSpawningEnabled();

                broadcast(
                        newState
                                ? lang("world-settings-gui.mob-spawning.enabled")
                                : lang("world-settings-gui.mob-spawning.disabled")
                );
            }

            case 24 -> {

                Sounds.playClick(player);

                game.getWorldSettings()
                        .toggleKeepInventory();

                boolean newState =
                        game.getWorldSettings()
                                .isKeepInventoryEnabled();

                broadcast(
                        newState
                                ? lang("world-settings-gui.keep-inventory.enabled")
                                : lang("world-settings-gui.keep-inventory.disabled")
                );
            }

            case 29 -> {

                Sounds.playClick(player);

                game.getWorldSettings()
                        .toggleNightVision();

                game.getPlayerEffectManager()
                        .applyNightVisionToAll();

                boolean newState =
                        game.getWorldSettings()
                                .isNightVisionEnabled();

                broadcast(
                        newState
                                ? lang("world-settings-gui.night-vision.enabled")
                                : lang("world-settings-gui.night-vision.disabled")
                );
            }

            case 31 -> {

                Sounds.playClick(player);

                handleTimeClick();
            }

            case 33 -> {

                Sounds.playClick(player);

                game.getWorldSettings()
                        .toggleDaylightCycle();

                boolean newState =
                        game.getWorldSettings()
                                .isDaylightCycleEnabled();

                broadcast(
                        newState
                                ? lang("world-settings-gui.daylight-cycle.enabled")
                                : lang("world-settings-gui.daylight-cycle.disabled")
                );
            }

            case 40 -> {

                Sounds.playBack(player);

                game.getEventSettingsGUI()
                        .open(player);

                reopen = false;
            }
        }

        if (reopen) {

            Bukkit.getScheduler().runTaskLater(
                    game.getPlugin(),
                    () -> open(player),
                    2L
            );
        }
    }

    @EventHandler
    public void onDrag(
            InventoryDragEvent event
    ) {

        if (event.getView().title().equals(
                lang("world-settings-gui.title")
        )) {
            event.setCancelled(true);
        }
    }

    private void handleDifficultyClick() {

        String next =
                game.getWorldSettings()
                        .cycleDifficulty();

        Component difficulty =
                getDifficultyName(next);

        broadcast(
                game.getLanguageManager().getComponent(
                        "world-settings-gui.difficulty.changed",
                        "difficulty",
                        difficulty
                )
        );
    }

    private void handleTimeClick() {

        long next =
                game.getWorldSettings()
                        .cycleTime();

        String phaseKey;

        if (next < 6000) {
            phaseKey = "morning";

        } else if (next < 12000) {
            phaseKey = "noon";

        } else if (next < 18000) {
            phaseKey = "evening";

        } else {
            phaseKey = "night";
        }

        Component phase =
                lang("world-settings-gui.time." + phaseKey);

        broadcast(
                game.getLanguageManager().getComponent(
                        "world-settings-gui.time.changed",
                        "phase",
                        phase
                )
        );
    }

    private Component getDifficultyName(
            String difficulty
    ) {

        String key = switch (
                difficulty.toLowerCase(Locale.ROOT)
                ) {
            case "peaceful" -> "peaceful";
            case "easy" -> "easy";
            case "normal" -> "normal";
            case "hard" -> "hard";
            case "ultra-ultra-hardcore" ->
                    "ultra-ultra-hardcore";
            default -> "unknown";
        };

        return lang(
                "world-settings-gui.difficulty." + key
        );
    }

    private void broadcast(
            Component message
    ) {

        for (Player player :
                Bukkit.getOnlinePlayers()) {

            player.sendMessage(message);
        }
    }

    private Component lang(String path) {

        return game.getLanguageManager()
                .getComponent(path);
    }

    private String langRaw(String path) {

        return game.getLanguageManager()
                .get(path);
    }

    private Component langStatus(
            String status
    ) {

        return game.getLanguageManager()
                .getComponent(
                        "world-settings-gui.toggle.status",
                        "status",
                        status
                );
    }
}