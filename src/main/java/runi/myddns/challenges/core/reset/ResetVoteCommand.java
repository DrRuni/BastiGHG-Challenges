package runi.myddns.challenges.core.reset;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import runi.myddns.challenges.ChallengeMain;

public class ResetVoteCommand implements CommandExecutor {

    private final ChallengeMain plugin;

    public ResetVoteCommand(ChallengeMain plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {

        if (!(sender instanceof Player player)) return true;
        if (args.length != 1) return true;

        if (args[0].equalsIgnoreCase("yes")) {
            plugin.getResetVoteManager().vote(player, true);
            return true;
        }

        if (args[0].equalsIgnoreCase("no")) {
            plugin.getResetVoteManager().vote(player, false);
            return true;
        }

        return true;
    }
}