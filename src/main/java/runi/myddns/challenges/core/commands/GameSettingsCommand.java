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
            String @NotNull [] args
    ) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(
                    plugin.getLanguageManager()
                            .getComponent("commands.gamesettings.player-only")
            );
            return true;
        }

        ChallengeGame game =
                plugin.getGameManager()
                        .getSelectedGame();

        if (game == null) {
            player.sendMessage(
                    plugin.getLanguageManager()
                            .getComponent("commands.gamesettings.no-game-selected")
            );
            return true;
        }

        if (!game.isLoaded()) {
            player.sendMessage(
                    plugin.getLanguageManager()
                            .getComponent("commands.gamesettings.load-first")
            );
            return true;
        }

        game.openSettings(player);

        return true;
    }
}
