package runi.myddns.challenges.games.LevelBlock.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import runi.myddns.challenges.games.LevelBlock.LevelBlockGame;

import java.util.List;

public class LevelBlockCommand implements CommandExecutor, TabCompleter {

    private final LevelBlockGame game;

    public LevelBlockCommand(LevelBlockGame game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {

        if (!(sender instanceof Player player)) {
            return true;
        }

        boolean isAdmin = player.isOp();

        if (args.length == 0) {
            sendCommandHelp(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("info")) {
            sendGameInfo(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("gamesettings")) {

            if (!game.isLoaded()) {
                player.sendMessage(
                        "§6Bitte zuerst LevelBlock laden."
                );
                return true;
            }

            game.openSettings(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("start")) {

            if (!game.isLoaded()) {
                player.sendMessage(
                        "§cLevelBlock muss zuerst geladen werden."
                );
                return true;
            }

            if (!isAdmin) {
                player.sendMessage(
                        "§c❌ Nur ein Admin darf LevelBlock starten."
                );
                return true;
            }

            boolean started =
                    game.getLevelBlockGameManager()
                            .startGame(player);

            if (!started) {
                player.sendMessage(
                        "§cLevelBlock wurde bereits gestartet."
                );
                return true;
            }

            player.sendMessage(
                    "§aLevelBlock wurde gestartet."
            );

            return true;
        }

        if (args[0].equalsIgnoreCase("reset")) {

            if (!game.isLoaded()) {
                player.sendMessage(
                        "§cLevelBlock muss zuerst geladen werden."
                );
                return true;
            }

            if (!isAdmin) {
                player.sendMessage(
                        "§c❌ Nur ein Admin darf LevelBlock zurücksetzen."
                );
                return true;
            }

            if (args.length >= 2
                    && args[1].equalsIgnoreCase("world")) {

                game.getPlugin()
                        .getResetVoteManager()
                        .startVote(
                                game.getDisplayName(),
                                "Welten komplett zurücksetzen",
                                () -> game.getPlugin()
                                        .getGameResetManager()
                                        .resetWorlds(game)
                        );

                return true;
            }

            World resetWorld = player.getWorld();

            game.getPlugin()
                    .getResetVoteManager()
                    .startVote(
                            game.getDisplayName(),
                            "Spielstand zurücksetzen",
                            () -> {

                                game.getLevelBlockGameManager()
                                        .resetGame(
                                                resetWorld
                                        );

                                Bukkit.broadcastMessage(
                                        ChatColor.GREEN
                                                + "✔ LevelBlock wurde zurückgesetzt."
                                );
                            }
                    );

            return true;
        }

        player.sendMessage(
                "§cUnbekannter Unterbefehl."
        );

        return true;
    }

    private void sendCommandHelp(Player player) {

        player.sendMessage("");
        player.sendMessage("§6══════ LevelBlock Befehle ══════");
        player.sendMessage("§e/levelblock info §7- Spielinfo anzeigen");
        player.sendMessage("§e/levelblock start §7- Challenge starten");
        player.sendMessage("§e/levelblock gamesettings §7- Spieleinstellungen öffnen");
        player.sendMessage("§e/levelblock reset §7- Spielstand zurücksetzen");
        player.sendMessage("§e/levelblock reset world §7- Welten komplett zurücksetzen");
        player.sendMessage("");
    }

    private void sendGameInfo(Player player) {

        player.sendMessage("");
        player.sendMessage("§6══════ LevelBlock ══════");
        player.sendMessage("§7Ziel: Erweitert eure Welt Block für Block.");
        player.sendMessage("§7Zu Beginn steht euch nur ein kleiner Bereich zur Verfügung.");
        player.sendMessage("§7Durch euren Level Fortschritt werden weitere Blöcke freigeschaltet");
        player.sendMessage("§7und die spielbare Welt wächst immer weiter.");
        player.sendMessage("");
        player.sendMessage("§6Wichtige Befehle:");
        player.sendMessage("§e/levelblock start §7- Challenge starten");
        player.sendMessage("§e/levelblock gamesettings §7- Einstellungen öffnen");
        player.sendMessage("§e/levelblock §7- Alle Befehle anzeigen");
        player.sendMessage("");
    }

    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args
    ) {

        if (args.length == 1) {
            return List.of(
                    "info",
                    "start",
                    "reset",
                    "gamesettings"
            );
        }

        if (args.length == 2
                && args[0].equalsIgnoreCase("reset")) {

            return List.of(
                    "world"
            );
        }

        return List.of();
    }
}