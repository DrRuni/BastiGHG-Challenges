package runi.myddns.challenges.games.LevelBlock.Commands;

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

        if (args.length == 0) {
            player.sendMessage("§e/levelblock start");
            player.sendMessage("§e/levelblock reset");
            player.sendMessage("§e/levelblock gamesettings");
            return true;
        }

        if (args[0].equalsIgnoreCase("gamesettings")) {

            if (!game.isLoaded()) {
                player.sendMessage("§6Bitte zuerst LevelBlock laden.");
                return true;
            }

            game.openSettings(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("start")) {

            boolean started = game
                    .getLevelBlockGameManager()
                    .startGame(player);

            if (!started) {
                player.sendMessage("§cLevelBlock wurde bereits gestartet.");
                return true;
            }

            player.sendMessage("§aLevelBlock wurde gestartet.");
            return true;
        }

        if (args[0].equalsIgnoreCase("reset")) {

            game.getLevelBlockGameManager()
                    .resetGame(player.getWorld());

            player.sendMessage("§aLevelBlock wurde zurückgesetzt.");
            return true;
        }

        player.sendMessage("§cUnbekannter Unterbefehl.");
        return true;
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
                    "start",
                    "reset",
                    "gamesettings"
            );
        }

        return List.of();
    }
}