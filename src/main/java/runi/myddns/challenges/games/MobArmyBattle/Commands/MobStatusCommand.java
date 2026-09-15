package runi.myddns.challenges.games.MobArmyBattle.Commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import runi.myddns.challenges.games.MobArmyBattle.Managers.Event.MobSaveManager;
import runi.myddns.challenges.games.MobArmyBattle.Managers.Event.TeamManager;
import runi.myddns.challenges.games.MobArmyBattle.MobArmyBattleGame;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MobStatusCommand implements CommandExecutor, TabCompleter {

    private final MobArmyBattleGame game;
    private final MobSaveManager mobSaveManager;
    private final TeamManager teamManager;

    public MobStatusCommand(
            MobArmyBattleGame game,
            MobSaveManager mobSaveManager,
            TeamManager teamManager
    ) {
        this.game = game;
        this.mobSaveManager = mobSaveManager;
        this.teamManager = teamManager;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String @NotNull [] args
    ) {

        if (!(sender instanceof Player player)) {

            sender.sendMessage(
                    lang("commands.mob-status.player-only")
            );

            return true;
        }

        if (args.length == 1
                && player.hasPermission("mobarmy.mobstatus.admin")) {

            String team = normalizeTeam(args[0]);

            if (team == null) {

                player.sendMessage(
                        lang("commands.mob-status.invalid-team")
                );

                return true;
            }

            showMobStatus(player, team);
            return true;
        }

        String team = teamManager.getPlayerTeam(player);

        /*
         * Null zusätzlich absichern.
         */
        if (team == null
                || team.equalsIgnoreCase("Kein Team")) {

            player.sendMessage(
                    lang("commands.mob-status.no-team")
            );

            return true;
        }

        showMobStatus(player, team);

        return true;
    }

    private void showMobStatus(
            Player player,
            String team
    ) {

        Map<String, Integer> mobs =
                mobSaveManager.getMobKillsForTeam(team);

        if (mobs == null || mobs.isEmpty()) {

            player.sendMessage(
                    langTeam(
                            "commands.mob-status.no-mobs",
                            team
                    )
            );

            return;
        }

        String headerPath =
                team.equalsIgnoreCase("Rot")
                        ? "commands.mob-status.header-red"
                        : "commands.mob-status.header-blue";

        player.sendMessage(
                langTeam(
                        headerPath,
                        team
                )
        );

        for (Map.Entry<String, Integer> entry : mobs.entrySet()) {

            player.sendMessage(
                    langMobEntry(
                            entry.getValue(),
                            entry.getKey()
                    )
            );
        }
    }

    private String normalizeTeam(String input) {

        return switch (input.toLowerCase()) {

            // Deutsch + Englisch akzeptieren
            case "rot", "red" -> "Rot";
            case "blau", "blue" -> "Blau";

            default -> null;
        };
    }

    @Override
    public List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String @NotNull [] args
    ) {

        if (!(sender instanceof Player player)) {
            return Collections.emptyList();
        }

        if (!player.hasPermission("mobarmy.mobstatus.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {

            String language =
                    game.getLanguageManager()
                            .getCurrentLanguage();

            if (language.equalsIgnoreCase("en")) {
                return List.of("red", "blue");
            }

            return List.of("rot", "blau");
        }

        return Collections.emptyList();
    }

    private Component lang(String path) {

        return game.getLanguageManager()
                .getComponent(path);
    }

    private Component langTeam(
            String path,
            String team
    ) {

        return game.getLanguageManager()
                .getComponent(
                        path,
                        "team",
                        team
                );
    }

    private Component langMobEntry(
            int amount,
            String mob
    ) {

        String text = game.getLanguageManager()
                .get("commands.mob-status.mob-entry");

        text = text
                .replace("%amount%", String.valueOf(amount))
                .replace("%mob%", mob);

        return net.kyori.adventure.text.minimessage.MiniMessage
                .miniMessage()
                .deserialize(text);
    }
}