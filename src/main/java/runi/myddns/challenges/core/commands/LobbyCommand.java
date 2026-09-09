package runi.myddns.challenges.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import runi.myddns.challenges.ChallengeMain;

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
                sender.sendMessage("§cDafür musst du OP sein.");
                return true;
            }

            for (Player online : Bukkit.getOnlinePlayers()) {
                online.teleport(plugin.getLobbyWorldManager().getSpawn());
            }

            sender.sendMessage("§aAlle Spieler wurden in die Lobby teleportiert.");
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Dieser Befehl kann nur von Spielern verwendet werden.");
            return true;
        }

        player.teleport(plugin.getLobbyWorldManager().getSpawn());
        return true;
    }
}
