package runi.myddns.challenges.games.levelborder.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import runi.myddns.challenges.games.levelborder.LevelBorderGame;
import runi.myddns.challenges.games.levelborder.manager.ScoreboardManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ScoreboardCommand implements CommandExecutor, TabCompleter {

    private final LevelBorderGame game;
    private final ScoreboardManager scoreboardManager;

    public ScoreboardCommand(
            LevelBorderGame game,
            ScoreboardManager scoreboardManager
    ) {
        this.game = game;
        this.scoreboardManager = scoreboardManager;
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
                    game.getLanguageManager()
                            .getComponent(
                                    "scoreboard-command.player-only"
                            )
            );

            return true;
        }

        int playerLevelRank = getPlayerStufe(player);
        boolean isAdmin = playerLevelRank >= 4;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "hide" -> {

                scoreboardManager.hide(player);

                player.sendMessage(
                        game.getLanguageManager()
                                .getComponent(
                                        "scoreboard-command.hidden"
                                )
                );
            }

            case "reload" -> {

                scoreboardManager.show(player);
                scoreboardManager.startUpdater();

                player.sendMessage(
                        game.getLanguageManager()
                                .getComponent(
                                        "scoreboard-command.shown"
                                )
                );
            }

            case "reset" -> {

                if (!isAdmin) {

                    player.sendMessage(
                            game.getLanguageManager()
                                    .getComponent(
                                            "scoreboard-command.admin-reset-only"
                                    )
                    );

                    return true;
                }

                scoreboardManager.reset();

                player.sendMessage(
                        game.getLanguageManager()
                                .getComponent(
                                        "scoreboard-command.reset"
                                )
                );
            }

            default -> sendHelp(player);
        }

        return true;
    }

    private void sendHelp(Player player) {

        player.sendMessage("");

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "scoreboard-command.help.title"
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "scoreboard-command.help.hide"
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "scoreboard-command.help.reload"
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "scoreboard-command.help.reset"
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "scoreboard-command.help.footer"
                        )
        );
    }

    private int getPlayerStufe(Player player) {
        return player.isOp() ? 4 : 2;
    }

    @Override
    public @NotNull List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            String @NotNull [] args
    ) {

        if (args.length == 1) {
            return Arrays.asList(
                    "hide",
                    "reload",
                    "reset"
            );
        }

        return Collections.emptyList();
    }
}