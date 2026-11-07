
package runi.myddns.challenges.games.chunkrandomizer.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import runi.myddns.challenges.ChallengeMain;
import runi.myddns.challenges.games.chunkrandomizer.ChunkRandomizerGame;

import java.util.List;

public class ChunkRandomizerCommand implements CommandExecutor, TabCompleter {

    private final ChallengeMain plugin;
    private final ChunkRandomizerGame game;

    public ChunkRandomizerCommand(
            ChallengeMain plugin,
            ChunkRandomizerGame game
    ) {
        this.plugin = plugin;
        this.game = game;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Dieser Befehl ist nur für Spieler verfügbar.");
            return true;
        }

        if (!player.hasPermission("chunkrandomizer.admin")) {
            player.sendMessage("§cKeine Berechtigung!");
            return true;
        }

        if (!game.isLoaded()) {
            player.sendMessage("§cChunkRandomizer ist nicht geladen!");
            return true;
        }

        if (args.length == 0
                || !args[0].equalsIgnoreCase("reset")
                || args.length > 2) {

            player.sendMessage("§6ChunkRandomizer Befehle:");
            player.sendMessage("§e/chunkrandomizer reset");
            player.sendMessage("§e/chunkrandomizer reset world");
            return true;
        }

        // Vollständiger Weltenreset
        if (args.length == 2) {

            if (!args[1].equalsIgnoreCase("world")) {
                player.sendMessage("§cUnbekannter Reset-Befehl.");
                return true;
            }

            plugin.getVoteManager().startVote(
                    game.getDisplayName(),
                    plugin.getLanguageManager()
                            .get("reset-management.all.name"),
                    () -> plugin.getGameResetManager()
                            .resetWorlds(game)
            );

            return true;
        }

        // Nur Eventdaten zurücksetzen
        plugin.getVoteManager().startVote(
                game.getDisplayName(),
                plugin.getLanguageManager()
                        .get("reset-management.data.name"),
                game::resetGameData
        );

        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String[] args
    ) {

        if (!sender.hasPermission("chunkrandomizer.admin")
                || !game.isLoaded()) {
            return List.of();
        }

        if (args.length == 1) {
            return List.of("reset").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }

        if (args.length == 2
                && args[0].equalsIgnoreCase("reset")) {
            return List.of("world").stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .toList();
        }

        return List.of();
    }
}

