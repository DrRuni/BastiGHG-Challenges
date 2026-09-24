package runi.myddns.challenges.games.MobArmyBattle.Commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import runi.myddns.challenges.games.MobArmyBattle.MobArmyBattleGame;

import java.util.Arrays;
import java.util.List;

public class MobArmyCommand implements CommandExecutor, TabCompleter {

    private final MobArmyBattleGame game;

    public MobArmyCommand(MobArmyBattleGame game) {
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
                    Component.text("Dieser Befehl kann nur von Spielern verwendet werden.")
            );
            return true;
        }

        if (!player.isOp()) {
            player.sendMessage(
                    Component.text("Du hast keine Berechtigung dafür.")
            );
            return true;
        }

        if (args.length == 0) {
            sendUsage(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("info")) {

            player.sendMessage(Component.empty());
            player.sendMessage(game.getLanguageManager().getComponent("info-command.title"));
            player.sendMessage(game.getLanguageManager().getComponent("info-command.goal"));
            player.sendMessage(game.getLanguageManager().getComponent("info-command.teams"));
            player.sendMessage(game.getLanguageManager().getComponent("info-command.preparation"));
            player.sendMessage(game.getLanguageManager().getComponent("info-command.arena"));
            player.sendMessage(game.getLanguageManager().getComponent("info-command.team-join"));
            player.sendMessage(game.getLanguageManager().getComponent("info-command.team-leave"));
            player.sendMessage(game.getLanguageManager().getComponent("info-command.lobby"));
            player.sendMessage(Component.empty());

            return true;
        }

        if (args[0].equalsIgnoreCase("resume")) {
            game.getEventResume().resumeEvent();
            return true;
        }

        if (args[0].equalsIgnoreCase("gamesettings")) {

            if (!game.isLoaded()) {
                player.sendMessage(
                        Component.text("Bitte zuerst MobArmyBattle laden.")
                );
                return true;
            }

            game.openSettings(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("reset")) {

            ResetCommand resetCommand =
                    new ResetCommand(game);

            String[] shifted =
                    Arrays.copyOfRange(
                            args,
                            1,
                            args.length
                    );

            return resetCommand.onCommand(
                    sender,
                    command,
                    label,
                    shifted
            );
        }

        sendUsage(player);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String @NotNull [] args
    ) {

        if (args.length == 1) {
            return List.of(
                    "resume",
                    "gamesettings",
                    "info",
                    "reset"
            );
        }

        if (args.length >= 2
                && args[0].equalsIgnoreCase("reset")) {

            ResetCommand resetCommand =
                    new ResetCommand(game);

            String[] shifted =
                    Arrays.copyOfRange(
                            args,
                            1,
                            args.length
                    );

            return resetCommand.onTabComplete(
                    sender,
                    command,
                    alias,
                    shifted
            );
        }

        return List.of();
    }

    private void sendUsage(Player player) {

        player.sendMessage(
                Component.text(
                        "/mobarmy info\n" +
                                "/mobarmy resume\n" +
                                "/mobarmy gamesettings\n" +
                                "/mobarmy reset <arena|lobby|teamworld|playerdata>"
                )
        );
    }
}