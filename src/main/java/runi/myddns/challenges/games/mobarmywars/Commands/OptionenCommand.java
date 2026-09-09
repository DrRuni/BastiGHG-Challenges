package runi.myddns.challenges.games.mobarmywars.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import runi.myddns.challenges.games.mobarmywars.MobArmyWarsGame;

import java.util.Collections;
import java.util.List;

public class OptionenCommand implements CommandExecutor, TabCompleter {

    private final MobArmyWarsGame game;

    public OptionenCommand(MobArmyWarsGame game) {
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
                            .getComponent("commands.options.player-only")
            );
            return true;
        }

        game.getOptionenGUI().open(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String @NotNull [] args
    ) {
        return Collections.emptyList();
    }
}