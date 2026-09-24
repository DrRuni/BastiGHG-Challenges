package runi.myddns.challenges.core.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import runi.myddns.challenges.ChallengeMain;
import runi.myddns.challenges.core.game.ChallengeGame;

public class GameSettingsCommand implements CommandExecutor {

    private final ChallengeMain plugin;

    public GameSettingsCommand(ChallengeMain plugin) {
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
            sender.sendMessage("Dieser Befehl kann nur von Spielern verwendet werden.");
            return true;
        }

        ChallengeGame game =
                plugin.getGameManager()
                        .getSelectedGame();

        if (game == null) {
            player.sendMessage("§cKein Game ausgewählt.");
            return true;
        }

        if (!game.isLoaded()) {
            player.sendMessage("§6Bitte zuerst das Game laden.");
            return true;
        }

        game.openSettings(player);

        return true;
    }
}
