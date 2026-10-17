package runi.myddns.challenges.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import runi.myddns.challenges.ChallengeMain;
import runi.myddns.challenges.core.game.ChallengeGame;

public class LobbyCommand implements CommandExecutor {

    private final ChallengeMain plugin;

    public LobbyCommand(ChallengeMain plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {

        if (args.length > 0 && args[0].equalsIgnoreCase("all")) {

            if (!(sender instanceof Player player) || !player.isOp()) {
                sender.sendMessage(
                        plugin.getLanguageManager()
                                .getComponent("commands.lobby.op-only")
                );
                return true;
            }

            ChallengeGame game = plugin.getGameManager().getSelectedGame();

            for (Player online : Bukkit.getOnlinePlayers()) {

                if (game != null && game.isLoaded()) {
                    game.leavePlayer(online);
                }

                online.teleport(plugin.getLobbyWorldManager().getSpawn());
            }

            sender.sendMessage(
                    plugin.getLanguageManager()
                            .getComponent("commands.lobby.all-teleported")
            );
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(
                    plugin.getLanguageManager()
                            .getComponent("commands.lobby.player-only")
            );
            return true;
        }

        ChallengeGame game = plugin.getGameManager().getSelectedGame();

        if (game != null && game.isLoaded()) {
            game.leavePlayer(player);
        }

        player.teleport(plugin.getLobbyWorldManager().getSpawn());
        return true;
    }
}
