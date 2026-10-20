package runi.myddns.challenges.core.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import runi.myddns.challenges.ChallengeMain;
import runi.myddns.challenges.core.game.ChallengeGame;
import runi.myddns.challenges.core.world.GameWorldDefinition;
import runi.myddns.challenges.games.MobArmyBattle.MobArmyBattleGame;

import java.util.List;

import java.util.Locale;

import static runi.myddns.challenges.core.utils.ColorUtil.gradientText;
import static runi.myddns.challenges.core.utils.DisplayColor.*;

public class ResetManagementGUI implements Listener {

    private static class ResetManagementHolder
            implements InventoryHolder {

        private Inventory inventory;

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }

        @Override
        public @NotNull Inventory getInventory() {
            return inventory;
        }
    }

    private final ChallengeMain plugin;

    private Component getGameTitle(ChallengeGame game) {

        String gameName = game.getDisplayName();

        return switch (game.getId().toLowerCase(Locale.ROOT)) {

            case "mobarmybattle" -> gradientText(
                    gameName,
                    BRIGHT_RED,
                    BLUE
            );

            case "levelborder" -> gradientText(
                    gameName,
                    LIGHT_BLUE,
                    DEEP_BLUE
            );

            case "levelblock" -> gradientText(
                    gameName,
                    LIME,
                    DARK_GREEN
            );

            default -> gradientText(
                    gameName,
                    WHITE,
                    LIGHT_GREY
            );
        };
    }

    public ResetManagementGUI(
            ChallengeMain plugin
    ) {
        this.plugin = plugin;

        Bukkit.getPluginManager()
                .registerEvents(
                        this,
                        plugin
                );
    }

    public void open(
            Player player
    ) {

        ChallengeGame game =
                plugin.getGameManager()
                        .getSelectedGame();

        if (game == null
                || !game.isLoaded()) {

            player.closeInventory();

            player.sendMessage(
                    plugin.getLanguageManager()
                            .getComponent("reset-management.load-first")
            );

            return;
        }

        if (!game.getId()
                .equalsIgnoreCase("levelborder")
                && !game.getId()
                .equalsIgnoreCase("levelblock")
                && !game.getId()
                .equalsIgnoreCase("mobarmybattle")) {

            player.sendMessage(
                    plugin.getLanguageManager()
                            .getComponent("reset-management.unsupported-game")
            );

            return;
        }

        ResetManagementHolder holder = new ResetManagementHolder();

        Inventory inventory =
                Bukkit.createInventory(
                        holder,
                        27,
                        Component.text(
                                plugin.getLanguageManager()
                                        .get("reset-management.title-prefix"),
                                NamedTextColor.BLACK
                        ).append(
                                getGameTitle(game)
                        )
                );

        holder.setInventory(inventory);

        if (game instanceof MobArmyBattleGame) {
            inventory.setItem(10, createItem(
                    Material.EXPERIENCE_BOTTLE,
                    plugin.getLanguageManager().get("reset-management.mobarmybattle.progress.name"),
                    NamedTextColor.GOLD,
                    "",
                    plugin.getLanguageManager().get("reset-management.mobarmybattle.progress.description")
            ));

            inventory.setItem(12, createItem(
                    Material.GRASS_BLOCK,
                    plugin.getLanguageManager().get("reset-management.mobarmybattle.teamworlds.name"),
                    NamedTextColor.YELLOW,
                    "",
                    plugin.getLanguageManager().get("reset-management.mobarmybattle.teamworlds.description")
            ));

            inventory.setItem(14, createItem(
                    Material.IRON_SWORD,
                    plugin.getLanguageManager().get("reset-management.mobarmybattle.arena.name"),
                    NamedTextColor.RED,
                    plugin.getLanguageManager().get("reset-management.mobarmybattle.arena.line-1"),
                    "",
                    plugin.getLanguageManager().get("reset-management.mobarmybattle.arena.line-2"),
                    plugin.getLanguageManager().get("reset-management.mobarmybattle.arena.line-3")
            ));

            inventory.setItem(16, createItem(
                    Material.TNT,
                    plugin.getLanguageManager().get("reset-management.mobarmybattle.all.name"),
                    NamedTextColor.DARK_RED,
                    "",
                    plugin.getLanguageManager().get("reset-management.mobarmybattle.all.description")
            ));

        } else {
            inventory.setItem(
                    11,
                    createItem(
                            Material.REDSTONE,
                            plugin.getLanguageManager()
                                    .get("reset-management.progress.name"),
                            NamedTextColor.GOLD,
                            plugin.getLanguageManager()
                                    .get("reset-management.progress.line-1"),
                            plugin.getLanguageManager()
                                    .get("reset-management.progress.line-2")
                    )
            );

            inventory.setItem(
                    15,
                    createItem(
                            Material.TNT,
                            plugin.getLanguageManager()
                                    .get("reset-management.worlds.name"),
                            NamedTextColor.RED,
                            plugin.getLanguageManager()
                                    .get("reset-management.worlds.line-1"),
                            plugin.getLanguageManager()
                                    .get("reset-management.worlds.line-2")
                    )
            );

        }

        inventory.setItem(
                22,
                createItem(
                        Material.ARROW,
                        plugin.getLanguageManager()
                                .get("reset-management.back.name"),
                        NamedTextColor.YELLOW,
                        plugin.getLanguageManager()
                                .get("reset-management.back.description")
                )
        );

        player.openInventory(inventory);
    }

    @EventHandler
    public void onClick(
            InventoryClickEvent event
    ) {

        if (!(event.getWhoClicked()
                instanceof Player player)) {
            return;
        }

        if (!(event.getView()
                .getTopInventory()
                .getHolder()
                instanceof ResetManagementHolder)) {
            return;
        }

        event.setCancelled(true);

        int slot =
                event.getRawSlot();

        if (slot < 0
                || slot >= event.getView()
                .getTopInventory()
                .getSize()) {
            return;
        }

        ChallengeGame game =
                plugin.getGameManager()
                        .getSelectedGame();

        if (game == null
                || !game.isLoaded()) {

            player.closeInventory();
            return;
        }

        if (game instanceof MobArmyBattleGame mab) {
            if (slot == 22) {
                mab.getEventSettingsGUI().open(player);
                return;
            }
            if (slot != 10 && slot != 12 && slot != 14 && slot != 16) return;

            player.closeInventory();
            switch (slot) {
                case 10 -> plugin.getVoteManager().startVote(
                        game.getDisplayName(),
                        plugin.getLanguageManager().get("reset-management.mobarmybattle.progress.name"),
                        () -> mab.getEventManager().resetGame()
                );
                case 12 -> plugin.getVoteManager().startVote(
                        game.getDisplayName(),
                        plugin.getLanguageManager().get("reset-management.mobarmybattle.teamworlds.name"),
                        () -> mab.getWorldManager().resetTeamWorlds()
                );
                case 14 -> plugin.getVoteManager().startVote(
                        game.getDisplayName(),
                        plugin.getLanguageManager().get("reset-management.mobarmybattle.arena.name"),
                        () -> plugin.getGameResetManager().resetSelectedWorlds(
                                mab,
                                List.of(GameWorldDefinition.MOB_ARMY_ARENA),
                                () -> mab.getArenaManager().resetArena(),
                                () -> mab.getArenaConfig().reload()
                        )
                );
                case 16 -> plugin.getVoteManager().startVote(
                        game.getDisplayName(),
                        plugin.getLanguageManager().get("reset-management.mobarmybattle.all.name"),
                        () -> plugin.getGameResetManager().resetWorlds(mab)
                );
                default -> { }
            }
            return;
        }

        switch (slot) {

            case 11 -> {

                player.closeInventory();

                if (game.getId().equalsIgnoreCase("levelborder")) {

                    Bukkit.dispatchCommand(
                            player,
                            "levelborder reset"
                    );

                } else if (game.getId().equalsIgnoreCase("levelblock")) {

                    Bukkit.dispatchCommand(
                            player,
                            "levelblock reset"
                    );
                }
            }

            case 15 -> {

                player.closeInventory();

                if (game.getId().equalsIgnoreCase("levelborder")) {

                    Bukkit.dispatchCommand(
                            player,
                            "levelborder reset world"
                    );

                } else if (game.getId().equalsIgnoreCase("levelblock")) {

                    Bukkit.dispatchCommand(
                            player,
                            "levelblock reset world"
                    );
                }
            }

            case 22 -> plugin.getWorldSettingsGUI().open(player);
        }
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onDrag(
            InventoryDragEvent event
    ) {

        if (!(event.getView()
                .getTopInventory()
                .getHolder()
                instanceof ResetManagementHolder)) {
            return;
        }

        event.setCancelled(true);
    }

    private ItemStack createItem(
            Material material,
            String name,
            NamedTextColor color,
            String... lore
    ) {

        ItemStack item =
                new ItemStack(material);

        ItemMeta meta =
                item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.addItemFlags(
                org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES
        );

        meta.displayName(
                Component.text(
                        name,
                        color
                )
        );

        meta.lore(
                java.util.Arrays.stream(lore)
                        .map(line ->
                                Component.text(
                                        line,
                                        NamedTextColor.GRAY
                                )
                        )
                        .toList()
        );

        item.setItemMeta(meta);

        return item;
    }
}
