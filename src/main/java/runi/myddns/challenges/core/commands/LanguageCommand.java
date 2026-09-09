package runi.myddns.challenges.core.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import runi.myddns.challenges.ChallengeMain;

public class LanguageCommand implements CommandExecutor {

    private final ChallengeMain plugin;

    public LanguageCommand(ChallengeMain plugin) {
        this.plugin = plugin;
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
                    plugin.getLanguageManager()
                            .getComponent("commands.language.player-only")
            );

            return true;
        }

        plugin.getLanguageSelectionGUI().open(player);

        return true;
    }
}