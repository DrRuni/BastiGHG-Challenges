package runi.myddns.challenges.games.mobarmywars.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import runi.myddns.challenges.games.mobarmywars.MobArmyWarsGame;

public class ArenaSummaryCommand implements CommandExecutor {

    private final MobArmyWarsGame game;

    public ArenaSummaryCommand(
            MobArmyWarsGame game
    ) {
        this.game = game;
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
                    game.getLanguageManager()
                            .getComponent(
                                    "commands.arena-summary.player-only"
                            )
            );

            return true;
        }

        if (game.getArenaManager() == null) {

            player.sendMessage(
                    game.getLanguageManager()
                            .getComponent(
                                    "commands.arena-summary.manager-unavailable"
                            )
            );

            return true;
        }

        game.getArenaManager()
                .showArenaSummary(player);

        return true;
    }
}