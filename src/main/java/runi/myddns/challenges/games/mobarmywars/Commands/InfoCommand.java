package runi.myddns.challenges.games.mobarmywars.Commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import runi.myddns.challenges.games.mobarmywars.MobArmyWarsGame;

public class InfoCommand implements CommandExecutor {

    private final MobArmyWarsGame game;

    public InfoCommand(MobArmyWarsGame game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {

        sender.sendMessage(Component.empty());
        sender.sendMessage(game.getLanguageManager().getComponent("info-command.title"));
        sender.sendMessage(game.getLanguageManager().getComponent("info-command.goal"));
        sender.sendMessage(game.getLanguageManager().getComponent("info-command.teams"));
        sender.sendMessage(game.getLanguageManager().getComponent("info-command.preparation"));
        sender.sendMessage(game.getLanguageManager().getComponent("info-command.arena"));
        sender.sendMessage(game.getLanguageManager().getComponent("info-command.team-join"));
        sender.sendMessage(game.getLanguageManager().getComponent("info-command.team-leave"));
        sender.sendMessage(game.getLanguageManager().getComponent("info-command.lobby"));
        sender.sendMessage(Component.empty());

        return true;
    }
}