package runi.myddns.challenges.core.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import runi.myddns.challenges.ChallengeMain;
import runi.myddns.challenges.core.game.ChallengeGame;

import java.util.ArrayList;
import java.util.List;

public class GameInfoGUI implements Listener {

    private final ChallengeMain plugin;

    private final NamespacedKey actionKey;
    private final NamespacedKey gameKey;

    private static final int DESCRIPTION_LINES_PER_BUTTON = 12;
    private static final int FEATURES_LINES_PER_BUTTON = 12;
    private static final int COMMAND_LINES_PER_BUTTON = 16;

    private static final int DESCRIPTION_LINE_LENGTH = 38;
    private static final int FEATURE_LINE_LENGTH = 42;

    public GameInfoGUI(ChallengeMain plugin) {
        this.plugin = plugin;

        this.actionKey =
                new NamespacedKey(plugin, "info_action");

        this.gameKey =
                new NamespacedKey(plugin, "info_game");
    }

    public void open(
            Player player,
            ChallengeGame game
    ) {

        Inventory inventory =
                Bukkit.createInventory(
                        null,
                        27,
                        Component.text(
                                game.getDisplayName() + " • INFO",
                                TextColor.color(0xC38CFF)
                        )
                );

        addSectionButtons(
                inventory,
                game,
                "Spielinfo",
                Material.BOOK,
                0xFF5ACD,
                wrapLines(
                        plugin.getGameInfoManager()
                                .getDescription(game.getId()),
                        DESCRIPTION_LINE_LENGTH
                ),
                DESCRIPTION_LINES_PER_BUTTON,
                new int[]{10}
        );

        addSectionButtons(
                inventory,
                game,
                "Features",
                Material.NETHER_STAR,
                0x8B5CFF,
                wrapLines(
                        plugin.getGameInfoManager()
                                .getFeatures(game.getId()),
                        FEATURE_LINE_LENGTH
                ),
                FEATURES_LINES_PER_BUTTON,
                new int[]{13, 14}
        );

        addSectionButtons(
                inventory,
                game,
                "Befehle",
                Material.PAPER,
                0x20D5FF,
                plugin.getGameInfoManager()
                        .getCommands(game.getId()),
                COMMAND_LINES_PER_BUTTON,
                new int[]{16}
        );

        inventory.setItem(
                22,
                createButton(
                        Material.BARRIER,
                        "Schließen",
                        0xFF5555,
                        "close",
                        game.getId(),
                        List.of()
                )
        );

        player.openInventory(inventory);
    }

    private void addSectionButtons(
            Inventory inventory,
            ChallengeGame game,
            String name,
            Material material,
            int color,
            List<String> lines,
            int linesPerButton,
            int[] slots
    ) {

        List<List<String>> pages =
                splitLines(
                        lines,
                        linesPerButton
                );

        if (pages.isEmpty()) {
            pages.add(
                    List.of(
                            "Keine Informationen verfügbar."
                    )
            );
        }

        int maxButtons =
                Math.min(
                        pages.size(),
                        slots.length
                );

        for (int i = 0; i < maxButtons; i++) {

            String displayName =
                    pages.size() > 1
                            ? name + " " + (i + 1)
                            : name;

            inventory.setItem(
                    slots[i],
                    createButton(
                            material,
                            displayName,
                            color,
                            "noop",
                            game.getId(),
                            pages.get(i)
                    )
            );
        }
    }

    private List<List<String>> splitLines(
            List<String> lines,
            int linesPerButton
    ) {

        List<List<String>> pages =
                new ArrayList<>();

        for (
                int i = 0;
                i < lines.size();
                i += linesPerButton
        ) {

            pages.add(
                    new ArrayList<>(
                            lines.subList(
                                    i,
                                    Math.min(
                                            i + linesPerButton,
                                            lines.size()
                                    )
                            )
                    )
            );
        }

        return pages;
    }

    private List<String> wrapLines(
            List<String> lines,
            int maxLength
    ) {

        List<String> result =
                new ArrayList<>();

        for (String line : lines) {

            if (line.length() <= maxLength) {
                result.add(line);
                continue;
            }

            String[] words =
                    line.split(" ");

            StringBuilder current =
                    new StringBuilder();

            for (String word : words) {

                if (!current.isEmpty()
                        && current.length()
                        + word.length()
                        + 1 > maxLength) {

                    result.add(
                            current.toString()
                    );

                    current =
                            new StringBuilder();
                }

                if (!current.isEmpty()) {
                    current.append(" ");
                }

                current.append(word);
            }

            if (!current.isEmpty()) {
                result.add(
                        current.toString()
                );
            }
        }

        return result;
    }

    private ItemStack createButton(
            Material material,
            String name,
            int color,
            String action,
            String gameId,
            List<String> lore
    ) {

        ItemStack item =
                new ItemStack(material);

        ItemMeta meta =
                item.getItemMeta();

        meta.displayName(
                Component.text(
                        name,
                        TextColor.color(color)
                )
        );

        if (!lore.isEmpty()) {

            meta.lore(
                    lore.stream()
                            .map(
                                    line ->
                                            Component.text(
                                                    line,
                                                    TextColor.color(
                                                            0xCCCCCC
                                                    )
                                            )
                            )
                            .toList()
            );
        }

        meta.getPersistentDataContainer().set(
                actionKey,
                PersistentDataType.STRING,
                action
        );

        meta.getPersistentDataContainer().set(
                gameKey,
                PersistentDataType.STRING,
                gameId
        );

        item.setItemMeta(meta);

        return item;
    }

    @EventHandler
    public void onInventoryClick(
            InventoryClickEvent event
    ) {

        String title =
                PlainTextComponentSerializer
                        .plainText()
                        .serialize(
                                event.getView().title()
                        );

        if (!title.endsWith(" • INFO")) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked()
                instanceof Player player)) {
            return;
        }

        ItemStack item =
                event.getCurrentItem();

        if (item == null
                || !item.hasItemMeta()) {
            return;
        }

        ItemMeta meta =
                item.getItemMeta();

        String action =
                meta.getPersistentDataContainer().get(
                        actionKey,
                        PersistentDataType.STRING
                );

        if (action == null) {
            return;
        }

        if (action.equals("close")) {
            player.closeInventory();
        }
    }
}