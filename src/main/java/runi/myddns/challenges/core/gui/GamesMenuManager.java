package runi.myddns.challenges.core.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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

public class GamesMenuManager implements Listener {

    private static final int CLOSE_SLOT = 22;

    public void open(Player player) {

        GamesMenuHolder holder = new GamesMenuHolder();
        Inventory inventory = holder.getInventory();

        ItemStack close = new ItemStack(Material.BARRIER);

        ItemMeta meta = close.getItemMeta();

        meta.displayName(
                Component.text("Close")
                        .color(NamedTextColor.RED)
                        .decorate(TextDecoration.BOLD)
                        .decoration(TextDecoration.ITALIC, false)
        );

        close.setItemMeta(meta);

        inventory.setItem(
                CLOSE_SLOT,
                close
        );

        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!(event.getInventory().getHolder()
                instanceof GamesMenuHolder)) {
            return;
        }

        event.setCancelled(true);

        if (event.getRawSlot() != CLOSE_SLOT) {
            return;
        }

        if (event.getWhoClicked() instanceof Player player) {
            player.closeInventory();
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {

        if (!(event.getInventory().getHolder()
                instanceof GamesMenuHolder)) {
            return;
        }

        event.setCancelled(true);
    }

    private static class GamesMenuHolder
            implements InventoryHolder {

        private final Inventory inventory;

        private GamesMenuHolder() {

            inventory = Bukkit.createInventory(
                    this,
                    27,
                    Component.text("Games")
            );
        }

        @Override
        public @NotNull Inventory getInventory() {
            return inventory;
        }
    }
}
