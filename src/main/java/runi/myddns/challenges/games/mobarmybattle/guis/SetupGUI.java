package runi.myddns.challenges.games.mobarmybattle.guis;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import runi.myddns.challenges.games.mobarmybattle.MobArmyBattleGame;
import runi.myddns.challenges.games.mobarmybattle.utils.Sounds;

import java.util.ArrayList;
import java.util.List;

public class SetupGUI implements Listener {

    private final MobArmyBattleGame game;

    public SetupGUI(MobArmyBattleGame game) {
        this.game = game;
    }

    public void open(Player player) {

        Inventory inv = Bukkit.createInventory(
                null,
                45,
                lang("setup-gui.title")
        );

        inv.setItem(4, createItem(
                Material.ENDER_PEARL,
                lang("setup-gui.teleports.name"),
                Component.empty(),
                lang("setup-gui.teleports.description")
        ));

        inv.setItem(10, createItem(
                Material.GRASS_BLOCK,
                lang("setup-gui.world-settings.name"),
                Component.empty(),
                lang("setup-gui.world-settings.description")
        ));

        inv.setItem(12, createItem(
                Material.PLAYER_HEAD,
                lang("setup-gui.players.name"),
                Component.empty(),
                lang("setup-gui.players.description")
        ));

        inv.setItem(14, createItem(
                Material.SHIELD,
                lang("setup-gui.teams.name"),
                Component.empty(),
                lang("setup-gui.teams.description")
        ));

        inv.setItem(16, createItem(
                Material.IRON_SWORD,
                lang("setup-gui.arena.name"),
                Component.empty(),
                lang("setup-gui.arena.description")
        ));

        inv.setItem(31, createItem(
                Material.TNT,
                lang("setup-gui.reset-management.name"),
                Component.empty(),
                lang("setup-gui.reset-management.description")
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

    @EventHandler
    public void onClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!event.getView().title().equals(
                lang("setup-gui.title")
        )) {
            return;
        }

        event.setCancelled(true);

        if (event.getClickedInventory() == null) {
            return;
        }

        if (event.getRawSlot()
                >= event.getView().getTopInventory().getSize()) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();

        if (clicked == null
                || clicked.getType() == Material.AIR) {
            return;
        }

        switch (event.getRawSlot()) {

            case 4 -> {
                Sounds.playClick(player);
                game.getMobArmySettingsGUI().open(player);
            }

            case 10 -> {
                Sounds.playClick(player);

                game.getPlugin()
                        .getWorldSettingsGUI()
                        .open(player);
            }

            case 12 -> {
                Sounds.playClick(player);
                game.getPlayerGUI().open(player);
            }

            case 14 -> {
                Sounds.playClick(player);
                game.getTeamSettingsGUI().open(player);
            }

            case 16 -> {
                Sounds.playClick(player);
                game.getArenaSettingsGUI().open(player);
            }

            case 31 -> {
                Sounds.playClick(player);
                game.getPlugin()
                        .getWorldSettingsGUI()
                        .openResetManagement(player);
            }

            case 40 -> {
                Sounds.playBack(player);
                game.getOptionenGUI().open(player);
            }
        }
    }

    private ItemStack createBackButton() {

        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.displayName(
                lang("setup-gui.back")
        );

        meta.lore(List.of());

        item.setItemMeta(meta);

        return item;
    }

    private Component lang(String path) {

        return game.getLanguageManager()
                .getComponent(path);
    }
}