package runi.myddns.challenges.core.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import runi.myddns.challenges.ChallengeMain;
import runi.myddns.challenges.core.game.ChallengeGame;

import java.util.ArrayList;
import java.util.List;

public class InfoCommand implements CommandExecutor, TabCompleter {

    private final ChallengeMain plugin;

    public InfoCommand(ChallengeMain plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(
                    "Dieser Befehl kann nur von Spielern verwendet werden."
            );
            return true;
        }

        // =========================================================
        // /info
        // Info des aktuell geladenen Games
        // =========================================================

        if (args.length == 0) {

            ChallengeGame game =
                    plugin.getGameManager().getSelectedGame();

            if (game == null || !game.isLoaded()) {
                player.sendMessage(
                        "§cAktuell ist kein Game geladen."
                );
                return true;
            }

            openInfo(player, game);
            return true;
        }

        // =========================================================
        // /info <game>
        // Funktioniert auch wenn das Game nicht geladen ist
        // =========================================================

        if (args.length == 1) {

            ChallengeGame game =
                    plugin.getGameManager().getGame(args[0]);

            if (game == null) {
                player.sendMessage(
                        "§cDieses Game wurde nicht gefunden."
                );
                return true;
            }

            openInfo(player, game);
            return true;
        }

        player.sendMessage(
                "§7Verwendung: §b/info [game]"
        );

        return true;
    }

    private void openInfo(
            Player player,
            ChallengeGame game
    ) {

        plugin.getGameInfoGUI().open(
                player,
                game
        );
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String[] args
    ) {

        if (args.length != 1) {
            return List.of();
        }

        List<String> games =
                new ArrayList<>(
                        List.of(
                                "mobarmybattle",
                                "levelborder",
                                "levelblock"
                        )
                );

        String input =
                args[0].toLowerCase();

        games.removeIf(
                game ->
                        !game.startsWith(input)
        );

        return games;
    }
}