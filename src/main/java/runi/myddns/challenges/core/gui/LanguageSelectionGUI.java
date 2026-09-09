package runi.myddns.challenges.core.gui;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import runi.myddns.challenges.ChallengeMain;

import java.util.ArrayList;
import java.util.List;

public class LanguageSelectionGUI implements Listener {

    private final ChallengeMain plugin;
    private final NamespacedKey actionKey;

    public LanguageSelectionGUI(ChallengeMain plugin) {
        this.plugin = plugin;
        this.actionKey = new NamespacedKey(plugin, "language_action");
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45, lang("language-selection.title"));

        inv.setItem(20, createLanguageItem(
                Material.PAPER,
                lang("language-selection.german.name"),
                "de",
                1001.0f,
                lang("language-selection.german.description")
        ));

        inv.setItem(24, createLanguageItem(
                Material.PAPER,
                lang("language-selection.english.name"),
                "en",
                1002.0f,
                lang("language-selection.english.description")
        ));

        inv.setItem(
                40,
                createBackButton()
        );

        player.openInventory(inv);
    }

    private ItemStack createLanguageItem(Material material, Component name, String languageId, float customModelData, Component... loreLines) {
        ItemStack item = createItem(material, name, loreLines);

        item.setData(
                DataComponentTypes.CUSTOM_MODEL_DATA,
                CustomModelData.customModelData()
                        .addFloat(customModelData)
                        .build()
        );

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, languageId);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createItem(Material material, Component name, Component... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return item;

        meta.displayName(name);

        List<Component> lore = new ArrayList<>();

        if (loreLines != null) {
            for (Component line : loreLines) {
                if (line != null) lore.add(line);
            }
        }

        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createBackButton() {

        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.displayName(
                lang("language-selection.back")
        );

        meta.getPersistentDataContainer().set(
                actionKey,
                PersistentDataType.STRING,
                "back"
        );

        meta.lore(List.of());

        item.setItemMeta(meta);

        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().title().equals(lang("language-selection.title"))) return;

        event.setCancelled(true);

        if (event.getClickedInventory() == null) return;
        if (event.getRawSlot() >= event.getView().getTopInventory().getSize()) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR || !clicked.hasItemMeta()) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) return;

        String action = meta.getPersistentDataContainer().get(actionKey, PersistentDataType.STRING);
        if (action == null) return;

        switch (action) {
            case "de" -> setLanguage(player, "de", "language-selection.changed-de");
            case "en" -> setLanguage(player, "en", "language-selection.changed-en");
        }
    }

    private void setLanguage(Player player, String language, String messagePath) {
        plugin.getLanguageManager().setLanguage(language);

        player.sendMessage(lang(messagePath));
        player.closeInventory();
    }

    private Component lang(String path) {
        return plugin.getLanguageManager().getComponent(path);
    }
}