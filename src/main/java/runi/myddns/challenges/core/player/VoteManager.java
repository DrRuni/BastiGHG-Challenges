package runi.myddns.challenges.core.player;

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

import java.util.*;

import static runi.myddns.challenges.core.utils.ColorUtil.gradientText;
import static runi.myddns.challenges.core.utils.DisplayColor.*;

public class VoteManager implements Listener {

    private final ChallengeMain plugin;

    private final Set<UUID> voters = new HashSet<>();
    private final Set<UUID> accepted = new HashSet<>();

    private final Map<UUID, Inventory> voteInventories =
            new HashMap<>();

    private Runnable onAccepted;
    private Runnable onRejected;

    private String voteDescription;

    private boolean active;

    public VoteManager(ChallengeMain plugin) {
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(
                this,
                plugin
        );
    }

    public boolean startVote(
            String gameName,
            String voteType,
            String voteDescription,
            Collection<? extends Player> votePlayers,
            Runnable onAccepted,
            Runnable onRejected
    ) {

        if (active) {

            for (Player player : votePlayers) {
                player.sendMessage(
                        plugin.getLanguageManager()
                                .getComponent("vote.already-running")
                );
            }

            return false;
        }

        this.voteDescription = voteDescription;
        this.onAccepted = onAccepted;
        this.onRejected = onRejected;

        voters.clear();
        accepted.clear();
        voteInventories.clear();

        for (Player player : votePlayers) {

            if (player == null || !player.isOnline()) {
                continue;
            }

            voters.add(
                    player.getUniqueId()
            );
        }

        if (voters.isEmpty()) {

            Runnable action = this.onAccepted;

            clear();

            if (action != null) {
                Bukkit.getScheduler().runTask(
                        plugin,
                        action
                );
            }

            return true;
        }

        active = true;

        for (Player player : votePlayers) {

            if (player == null
                    || !player.isOnline()
                    || !voters.contains(player.getUniqueId())) {
                continue;
            }

            Inventory inventory =
                    Bukkit.createInventory(
                            null,
                            9,
                            Component.text(
                                    voteType + " » ",
                                    NamedTextColor.BLACK
                            ).append(
                                    getGameTitle(gameName)
                            )
                    );

            fillInventory(inventory);

            inventory.setItem(
                    2,
                    createYesItem()
            );

            inventory.setItem(
                    4,
                    createVoteInfoItem()
            );

            inventory.setItem(
                    6,
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

        return true;
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean startVote(
            String gameName,
            String resetName,
            Runnable onAccepted
    ) {

        return startVote(
                gameName,
                "Reset",
                resetName,
                new ArrayList<>(Bukkit.getOnlinePlayers()),
                onAccepted,
                null
        );
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onInventoryClick(
            InventoryClickEvent event
    ) {

        if (!active) {
            return;
        }

        if (!(event.getWhoClicked()
                instanceof Player player)) {
            return;
        }

        Inventory voteInventory =
                voteInventories.get(
                        player.getUniqueId()
                );

        if (voteInventory == null) {
            return;
        }

        if (event.getView()
                .getTopInventory()
                != voteInventory) {
            return;
        }

        event.setCancelled(true);

        if (event.getRawSlot() == 2) {

            vote(
                    player,
                    true
            );

            return;
        }

        if (event.getRawSlot() == 6) {

            vote(
                    player,
                    false
            );
        }
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onInventoryClose(
            InventoryCloseEvent event
    ) {

        if (!active) {
            return;
        }

        if (!(event.getPlayer()
                instanceof Player player)) {
            return;
        }

        UUID uuid =
                player.getUniqueId();

        Inventory voteInventory =
                voteInventories.get(uuid);

        if (voteInventory == null) {
            return;
        }

        if (event.getInventory()
                != voteInventory) {
            return;
        }

        cancelVote(
                player,
                plugin.getLanguageManager()
                        .get("vote.cancelled")
        );
    }

    public void vote(
            Player player,
            boolean yes
    ) {

        if (!active) {
            return;
        }

        UUID uuid =
                player.getUniqueId();

        if (!voters.contains(uuid)) {
            return;
        }

        if (accepted.contains(uuid)) {
            return;
        }

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
                    plugin.getLanguageManager()
                            .get("vote.rejected")
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
                plugin.getLanguageManager()
                        .getComponent("vote.accepted-self")
        );

        if (accepted.containsAll(voters)) {

            Runnable action =
                    onAccepted;

            active = false;

            closeAllVoteInventories();

            clear();

            if (action != null) {

                Bukkit.getScheduler()
                        .runTaskLater(
                                plugin,
                                action,
                                20L
                        );
            }
        }
    }

    private void cancelVote(
            Player player,
            String reason
    ) {

        if (!active) {
            return;
        }

        Runnable rejectedAction =
                onRejected;

        active = false;

        closeAllVoteInventories();

        Bukkit.broadcast(
                Component.text(
                        player.getName() + " " + reason,
                        NamedTextColor.RED
                )
        );

        clear();

        if (rejectedAction != null) {

            Bukkit.getScheduler().runTask(
                    plugin,
                    rejectedAction
            );
        }
    }

    private void closeAllVoteInventories() {

        for (Player player :
                Bukkit.getOnlinePlayers()) {

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
        onRejected = null;

        voteDescription = null;
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

    private ItemStack createVoteInfoItem() {

        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return item;

        String description = voteDescription == null || voteDescription.isBlank()
                ? plugin.getLanguageManager().get("vote.yes.description")
                : voteDescription;

        meta.displayName(
                Component.text(description, NamedTextColor.GOLD)
                        .decorate(TextDecoration.BOLD)
        );

        meta.lore(List.of(
                Component.text(description, NamedTextColor.GRAY)
        ));

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createYesItem() {

        ItemStack item =
                new ItemStack(
                        Material.LIME_CONCRETE
                );

        ItemMeta meta =
                item.getItemMeta();

        meta.displayName(
                Component.text(
                        plugin.getLanguageManager()
                                .get("vote.yes.name"),
                        NamedTextColor.GREEN
                ).decorate(
                        TextDecoration.BOLD
                )
        );

        List<Component> lore =
                new ArrayList<>();

        if (voteDescription != null
                && !voteDescription.isBlank()) {

            lore.add(
                    Component.text(
                            voteDescription,
                            NamedTextColor.GRAY
                    )
            );

            lore.add(
                    Component.empty()
            );
        }

        lore.add(
                Component.text(
                        plugin.getLanguageManager()
                                .get("vote.yes.description"),
                        NamedTextColor.GREEN
                )
        );

        meta.lore(lore);

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
                        plugin.getLanguageManager()
                                .get("vote.no.name"),
                        NamedTextColor.RED
                ).decorate(
                        TextDecoration.BOLD
                )
        );

        meta.lore(
                List.of(
                        Component.text(
                                plugin.getLanguageManager()
                                        .get("vote.no.description"),
                                NamedTextColor.RED
                        )
                )
        );

        item.setItemMeta(meta);

        return item;
    }

    private Component getGameTitle(
            String gameName
    ) {

        String id =
                gameName
                        .toLowerCase(Locale.ROOT)
                        .replace(" ", "");

        return switch (id) {

            case "mobarmybattle" ->
                    gradientText(
                            gameName,
                            BRIGHT_RED,
                            BLUE
                    );

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

    public boolean isActive() {
        return active;
    }
}