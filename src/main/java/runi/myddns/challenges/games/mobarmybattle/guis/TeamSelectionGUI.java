package runi.myddns.challenges.games.mobarmybattle.guis;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import runi.myddns.challenges.games.mobarmybattle.managers.event.TeamManager;
import runi.myddns.challenges.games.mobarmybattle.MobArmyBattleGame;

public class TeamSelectionGUI implements Listener {

    private final MobArmyBattleGame game;
    private final TeamManager teamManager;

    public TeamSelectionGUI(
            MobArmyBattleGame game,
            TeamManager teamManager
    ) {
        this.game = game;
        this.teamManager = teamManager;
    }

    public void openGUI(Player player) {

        Inventory gui = Bukkit.createInventory(
                null,
                9,
                lang("team-selection-gui.title")
        );

        gui.setItem(
                2,
                createMenuItem(
                        Material.BLUE_WOOL,
                        lang("team-selection-gui.blue")
                )
        );

        gui.setItem(
                4,
                createMenuItem(
                        Material.WHITE_WOOL,
                        lang("team-selection-gui.none")
                )
        );

        gui.setItem(
                6,
                createMenuItem(
                        Material.RED_WOOL,
                        lang("team-selection-gui.red")
                )
        );

        player.openInventory(gui);
    }

    private ItemStack createMenuItem(
            Material material,
            Component name
    ) {

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.displayName(name);
        item.setItemMeta(meta);

        return item;
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onInventoryClick(
            InventoryClickEvent event
    ) {

        if (!(event.getWhoClicked()
                instanceof Player player)) {
            return;
        }

        if (!event.getView().title().equals(
                lang("team-selection-gui.title")
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

        switch (slot) {

            case 2 ->
                    requestTeamJoin(
                            player,
                            "Blau"
                    );

            case 4 ->
                    requestNoTeam(player);

            case 6 ->
                    requestTeamJoin(
                            player,
                            "Rot"
                    );
        }
    }

    private void requestTeamJoin(
            Player player,
            String team
    ) {

        /*
         * Event noch nicht gestartet:
         * normale Teamwahl.
         */
        if (!game.getEventResume().isEventStarted()) {

            assignTeam(
                    player,
                    team
            );

            return;
        }

        /*
         * Spieler gehört bereits zum laufenden Match.
         * Team darf jetzt nicht mehr gewechselt werden.
         */
        if (teamManager.isRegisteredParticipant(player)) {

            player.closeInventory();

            player.sendMessage(
                    Component.text(
                            "Du gehörst bereits zum laufenden Event. "
                                    + "Dein Team kann jetzt nicht mehr geändert werden.",
                            NamedTextColor.RED
                    )
            );

            return;
        }

        var voters =
                Bukkit.getOnlinePlayers()
                        .stream()
                        .filter(game::isMobArmyPlayer)
                        .filter(teamManager::isRegisteredParticipant)
                        .filter(p -> !p.equals(player))
                        .toList();

        player.closeInventory();

        game.getPlugin()
                .getVoteManager()
                .startVote(
                        game.getDisplayName(),
                        "Spielerbeitritt",
                        player.getName()
                                + " möchte Team "
                                + team
                                + " beitreten.",
                        voters,

                        () -> {

                            if (!player.isOnline()) {
                                return;
                            }

                            assignTeam(
                                    player,
                                    team
                            );

                            player.sendMessage(
                                    Component.text(
                                            "✔ Dein Beitritt wurde angenommen.",
                                            NamedTextColor.GREEN
                                    )
                            );
                        },

                        () -> {

                            if (!player.isOnline()) {
                                return;
                            }

                            player.sendMessage(
                                    Component.text(
                                            "❌ Dein Beitritt wurde abgelehnt.",
                                            NamedTextColor.RED
                                    )
                            );
                        }
                );
    }

    private void requestNoTeam(
            Player player
    ) {

        if (game.getEventResume().isEventStarted()
                && teamManager.isRegisteredParticipant(player)) {

            player.closeInventory();

            player.sendMessage(
                    Component.text(
                            "Du kannst dein Team während des laufenden Events nicht verlassen.",
                            NamedTextColor.RED
                    )
            );

            return;
        }

        teamManager.removePlayerFromTeam(player);

        player.sendMessage(
                Component.empty()
        );

        player.sendMessage(
                lang(
                        "team-selection-gui.messages.none"
                )
        );

        player.closeInventory();
    }

    private void assignTeam(
            Player player,
            String team
    ) {

        teamManager.assignTeam(
                player,
                team
        );

        String path =
                team.equalsIgnoreCase("Blau")
                        ? "team-selection-gui.messages.blue"
                        : "team-selection-gui.messages.red";

        Component message =
                langPlayer(
                        path,
                        player.getName()
                );

        for (Player onlinePlayer :
                Bukkit.getOnlinePlayers()) {

            if (!game.isMobArmyPlayer(onlinePlayer)) {
                continue;
            }

            onlinePlayer.sendMessage(message);
        }

        player.closeInventory();
    }

    private Component lang(String path) {

        return game.getLanguageManager()
                .getComponent(path);
    }

    private Component langPlayer(
            String path,
            Object player
    ) {

        return game.getLanguageManager()
                .getComponent(
                        path,
                        "player",
                        player
                );
    }
}