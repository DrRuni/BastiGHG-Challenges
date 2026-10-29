package runi.myddns.challenges.games.levelblock.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import runi.myddns.challenges.games.levelblock.LevelBlockGame;

import java.util.List;

public class LevelBlockCommand implements CommandExecutor, TabCompleter {

    private final LevelBlockGame game;

    public LevelBlockCommand(LevelBlockGame game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            String @NotNull [] args
    ) {

        if (!(sender instanceof Player player)) {
            return true;
        }

        boolean isAdmin = player.isOp();

        if (args.length == 0) {
            sendCommandHelp(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("info")) {
            sendGameInfo(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("gamesettings")) {

            if (!game.isLoaded()) {
                player.sendMessage(
                        game.getLanguageManager()
                                .getComponent(
                                        "commands.levelblock.load-first-settings"
                                )
                );

                return true;
            }

            game.openSettings(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("start")) {

            if (!game.isLoaded()) {
                player.sendMessage(
                        game.getLanguageManager()
                                .getComponent(
                                        "commands.levelblock.load-first"
                                )
                );

                return true;
            }

            if (!isAdmin) {
                player.sendMessage(
                        game.getLanguageManager()
                                .getComponent(
                                        "commands.levelblock.admin-start-only"
                                )
                );

                return true;
            }

            if (!game.isLevelBlockPlayer(player)) {
                player.sendMessage(
                        game.getLanguageManager()
                                .getComponent("commands.levelblock.start-game-world-only")
                );
                return true;
            }

            boolean started =
                    game.getLevelBlockGameManager()
                            .startGame(player);

            if (!started) {
                player.sendMessage(
                        game.getLanguageManager()
                                .getComponent(
                                        "commands.levelblock.already-started"
                                )
                );

                return true;
            }

            player.sendMessage(
                    game.getLanguageManager()
                            .getComponent(
                                    "commands.levelblock.started"
                            )
            );

            return true;
        }

        if (args[0].equalsIgnoreCase("reset")) {

            if (!game.isLoaded()) {
                player.sendMessage(
                        game.getLanguageManager()
                                .getComponent(
                                        "commands.levelblock.load-first"
                                )
                );

                return true;
            }

            if (!isAdmin) {
                player.sendMessage(
                        game.getLanguageManager()
                                .getComponent(
                                        "commands.levelblock.admin-reset-only"
                                )
                );

                return true;
            }

            if (args.length >= 2
                    && args[1].equalsIgnoreCase("world")) {

                game.getPlugin()
                        .getVoteManager()
                        .startVote(
                                game.getDisplayName(),
                                game.getLanguageManager()
                                        .get(
                                                "commands.levelblock.vote.reset-worlds"
                                        ),
                                () -> game.getPlugin()
                                        .getGameResetManager()
                                        .resetWorlds(game)
                        );

                return true;
            }

            game.getPlugin()
                    .getVoteManager()
                    .startVote(
                            game.getDisplayName(),
                            game.getLanguageManager()
                                    .get(
                                            "commands.levelblock.vote.reset-progress"
                                    ),
                            () -> {

                                game.resetGameData();

                                Bukkit.broadcast(
                                        game.getLanguageManager()
                                                .getComponent(
                                                        "commands.levelblock.reset-complete"
                                                )
                                );
                            }
                    );

            return true;
        }

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelblock.unknown"
                        )
        );

        return true;
    }

    private void sendCommandHelp(Player player) {

        player.sendMessage("");

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelblock.help.title"
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelblock.help.info"
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelblock.help.start"
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelblock.help.settings"
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelblock.help.reset"
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelblock.help.reset-world"
                        )
        );

        player.sendMessage("");
    }

    private void sendGameInfo(Player player) {

        player.sendMessage("");

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelblock.info.title"
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelblock.info.goal"
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelblock.info.start-area"
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelblock.info.progress-1"
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelblock.info.progress-2"
                        )
        );

        player.sendMessage("");

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelblock.info.commands-title"
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelblock.info.start-command"
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelblock.info.settings-command"
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelblock.info.help-command"
                        )
        );

        player.sendMessage("");
    }

    @Override
    public @NotNull List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            String @NotNull [] args
    ) {

        if (args.length == 1) {
            return List.of(
                    "info",
                    "start",
                    "reset",
                    "gamesettings"
            );
        }

        if (args.length == 2
                && args[0].equalsIgnoreCase("reset")) {

            return List.of(
                    "world"
            );
        }

        return List.of();
    }
}