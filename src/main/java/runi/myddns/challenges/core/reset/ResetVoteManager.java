package runi.myddns.challenges.core.reset;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import runi.myddns.challenges.ChallengeMain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ResetVoteManager implements Listener {

    private final ChallengeMain plugin;

    private final Set<UUID> voters = new HashSet<>();
    private final Set<UUID> accepted = new HashSet<>();

    private final Map<UUID, Inventory> voteInventories =
            new HashMap<>();

    private Runnable onAccepted;

    private boolean active;

    public ResetVoteManager(ChallengeMain plugin) {
        this.plugin = plugin;

        Bukkit.getPluginManager()
                .registerEvents(
                        this,
                        plugin
                );
    }

    public void startVote(
            String gameName,
            String resetName,
            Runnable onAccepted
    ) {

        if (active) {

            Bukkit.broadcast(
                    Component.text(
                            "Es läuft bereits eine Reset-Abstimmung.",
                            NamedTextColor.RED
                    )
            );

            return;
        }

        active = true;

        this.onAccepted = onAccepted;

        voters.clear();
        accepted.clear();
        voteInventories.clear();

        for (Player player : Bukkit.getOnlinePlayers()) {
            voters.add(player.getUniqueId());
        }

        for (Player player : Bukkit.getOnlinePlayers()) {

            Inventory inventory =
                    Bukkit.createInventory(
                            null,
                            9,
                            Component.text(
                                    gameName + " - Reset",
                                    NamedTextColor.DARK_RED
                            )
                    );

            fillInventory(inventory);

            inventory.setItem(
                    3,
                    createYesItem(resetName)
            );

            inventory.setItem(
                    5,
                    createNoItem()
            );

            voteInventories.put(
                    player.getUniqueId(),
                    inventory
            );

            player.openInventory(inventory);

            player.playSound(
                    player.getLocation(),
                    Sound.BLOCK_NOTE_BLOCK_PLING,
                    0.6f,
                    1.2f
            );
        }
    }

    @EventHandler
    public void onInventoryClick(
            InventoryClickEvent event
    ) {

        if (!active) return;

        if (!(event.getWhoClicked()
                instanceof Player player)) {
            return;
        }

        Inventory voteInventory =
                voteInventories.get(
                        player.getUniqueId()
                );

        if (voteInventory == null) return;

        if (event.getView()
                .getTopInventory()
                != voteInventory) {
            return;
        }

        event.setCancelled(true);

        if (event.getRawSlot() == 3) {

            vote(
                    player,
                    true
            );

            return;
        }

        if (event.getRawSlot() == 5) {

            vote(
                    player,
                    false
            );
        }
    }

    @EventHandler
    public void onInventoryClose(
            InventoryCloseEvent event
    ) {

        if (!active) return;

        if (!(event.getPlayer()
                instanceof Player player)) {
            return;
        }

        UUID uuid =
                player.getUniqueId();

        Inventory voteInventory =
                voteInventories.get(uuid);

        if (voteInventory == null) return;

        if (event.getInventory()
                != voteInventory) {
            return;
        }

        cancelVote(
                player,
                "die Abstimmung abgebrochen"
        );
    }

    public void vote(
            Player player,
            boolean yes
    ) {

        if (!active) return;

        UUID uuid =
                player.getUniqueId();

        if (!voters.contains(uuid)) return;
        if (accepted.contains(uuid)) return;

        voteInventories.remove(uuid);

        player.closeInventory();

        if (!yes) {

            player.playSound(
                    player.getLocation(),
                    Sound.BLOCK_NOTE_BLOCK_BASS,
                    0.7f,
                    0.8f
            );

            cancelVote(
                    player,
                    "den Reset abgelehnt"
            );

            return;
        }

        accepted.add(uuid);

        player.playSound(
                player.getLocation(),
                Sound.BLOCK_NOTE_BLOCK_PLING,
                0.7f,
                1.5f
        );

        player.sendMessage(
                Component.text(
                        "✔ Du hast dem Reset zugestimmt.",
                        NamedTextColor.GREEN
                )
        );

        if (accepted.containsAll(voters)) {

            Runnable action =
                    onAccepted;

            active = false;

            closeAllVoteInventories();

            clear();

            if (action != null) {
                action.run();
            }
        }
    }

    private void cancelVote(
            Player player,
            String reason
    ) {

        active = false;

        closeAllVoteInventories();

        Bukkit.broadcast(
                Component.text(
                        player.getName()
                                + " hat "
                                + reason
                                + ".",
                        NamedTextColor.RED
                )
        );

        clear();
    }

    private void closeAllVoteInventories() {

        for (Player player
                : Bukkit.getOnlinePlayers()) {

            Inventory inventory =
                    voteInventories.get(
                            player.getUniqueId()
                    );

            if (inventory == null) {
                continue;
            }

            if (player.getOpenInventory()
                    .getTopInventory()
                    == inventory) {

                player.closeInventory();
            }
        }

        voteInventories.clear();
    }

    private void clear() {

        active = false;

        voters.clear();
        accepted.clear();
        voteInventories.clear();

        onAccepted = null;
    }

    private void fillInventory(
            Inventory inventory
    ) {

        ItemStack filler =
                new ItemStack(
                        Material.GRAY_STAINED_GLASS_PANE
                );

        ItemMeta meta =
                filler.getItemMeta();

        meta.displayName(
                Component.text(" ")
        );

        filler.setItemMeta(meta);

        for (int slot = 0;
             slot < inventory.getSize();
             slot++) {

            inventory.setItem(
                    slot,
                    filler
            );
        }
    }

    private ItemStack createYesItem(
            String resetName
    ) {

        ItemStack item =
                new ItemStack(
                        Material.LIME_CONCRETE
                );

        ItemMeta meta =
                item.getItemMeta();

        meta.displayName(
                Component.text(
                                "JA",
                                NamedTextColor.GREEN
                        )
                        .decorate(
                                TextDecoration.BOLD
                        )
        );

        meta.lore(
                java.util.List.of(
                        Component.text(
                                resetName,
                                NamedTextColor.GRAY
                        ),
                        Component.empty(),
                        Component.text(
                                "Reset zustimmen",
                                NamedTextColor.GREEN
                        )
                )
        );

        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createNoItem() {

        ItemStack item =
                new ItemStack(
                        Material.RED_CONCRETE
                );

        ItemMeta meta =
                item.getItemMeta();

        meta.displayName(
                Component.text(
                                "NEIN",
                                NamedTextColor.RED
                        )
                        .decorate(
                                TextDecoration.BOLD
                        )
        );

        meta.lore(
                java.util.List.of(
                        Component.text(
                                "Reset ablehnen",
                                NamedTextColor.RED
                        )
                )
        );

        item.setItemMeta(meta);

        return item;
    }

    public boolean isActive() {
        return active;
    }
}