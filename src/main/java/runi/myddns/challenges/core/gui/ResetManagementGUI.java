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
import runi.myddns.challenges.ChallengeMain;
import runi.myddns.challenges.core.game.ChallengeGame;

import java.util.Locale;

import static runi.myddns.challenges.core.utils.ColorUtil.gradientText;
import static runi.myddns.challenges.core.utils.DisplayColor.*;

public class ResetManagementGUI implements Listener {

    private static class ResetManagementHolder
            implements InventoryHolder {

        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private final ChallengeMain plugin;

    private Component getGameTitle(
            ChallengeGame game
    ) {

        String gameName =
                game.getDisplayName();

        return switch (
                game.getId()
                        .toLowerCase(Locale.ROOT)
                ) {

            case "levelborder" ->
                    gradientText(
                            gameName,
                            LIGHT_BLUE,
                            DEEP_BLUE
                    );

            case "levelblock" ->
                    gradientText(
                            gameName,
                            LIME,
                            DARK_GREEN
                    );

            default ->
                    gradientText(
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
                    Component.text(
                            "Bitte zuerst ein Game laden.",
                            NamedTextColor.RED
                    )
            );

            return;
        }

        if (!game.getId()
                .equalsIgnoreCase("levelborder")
                && !game.getId()
                .equalsIgnoreCase("levelblock")) {

            player.sendMessage(
                    Component.text(
                            "Für dieses Spiel gibt es hier keine Reset-Verwaltung.",
                            NamedTextColor.RED
                    )
            );

            return;
        }

        Inventory inventory =
                Bukkit.createInventory(
                        new ResetManagementHolder(),
                        27,
                        Component.text(
                                "Reset » ",
                                NamedTextColor.BLACK
                        ).append(
                                getGameTitle(game)
                        )
                );

        inventory.setItem(
                11,
                createItem(
                        Material.REDSTONE,
                        "Spielstand zurücksetzen",
                        NamedTextColor.GOLD,
                        "Setzt den Fortschritt des Spiels zurück.",
                        "Die Welten bleiben erhalten."
                )
        );

        inventory.setItem(
                15,
                createItem(
                        Material.TNT,
                        "Welten zurücksetzen",
                        NamedTextColor.RED,
                        "Löscht und erstellt die Spielwelten neu.",
                        "Achtung: Alle Änderungen in den Welten gehen verloren!"
                )
        );

        inventory.setItem(
                22,
                createItem(
                        Material.ARROW,
                        "Zurück",
                        NamedTextColor.YELLOW,
                        "Zurück zu den WorldSettings."
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

        switch (slot) {

            case 11 -> {

                player.closeInventory();

                if (game.getId()
                        .equalsIgnoreCase("levelborder")) {

                    Bukkit.dispatchCommand(
                            player,
                            "levelborder reset"
                    );

                    return;
                }

                if (game.getId()
                        .equalsIgnoreCase("levelblock")) {

                    Bukkit.dispatchCommand(
                            player,
                            "levelblock reset"
                    );

                    return;
                }
            }

            case 15 -> {

                player.closeInventory();

                if (game.getId()
                        .equalsIgnoreCase("levelborder")) {

                    Bukkit.dispatchCommand(
                            player,
                            "levelborder reset world"
                    );

                    return;
                }

                if (game.getId()
                        .equalsIgnoreCase("levelblock")) {

                    Bukkit.dispatchCommand(
                            player,
                            "levelblock reset world"
                    );

                    return;
                }
            }

            case 22 -> {

                plugin.getWorldSettingsGUI()
                        .open(player);
            }
        }
    }

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
