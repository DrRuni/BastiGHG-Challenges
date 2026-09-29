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
                    Component.text(
                            "Dieser Befehl kann nur von Spielern verwendet werden."
                    )
            );

            return true;
        }

        if (!player.isOp()) {

            player.sendMessage(
                    Component.text(
                            "Du hast keine Berechtigung dafür."
                    )
            );

            return true;
        }


        /*
         * /mobarmy
         */
        if (args.length == 0) {

            sendCommandOverview(
                    player
            );

            return true;
        }


        /*
         * /mobarmy info
         * /mobarmy help
         */
        if (args[0].equalsIgnoreCase("info")
                || args[0].equalsIgnoreCase("help")) {

            sendInfo(
                    player
            );

            return true;
        }


        /*
         * /mobarmy resume
         */
        if (args[0].equalsIgnoreCase("resume")) {

            game.getEventResume()
                    .resumeEvent();

            return true;
        }


        /*
         * /mobarmy gamesettings
         */
        if (args[0].equalsIgnoreCase("gamesettings")) {

            if (!game.isLoaded()) {

                player.sendMessage(
                        game.getLanguageManager()
                                .getComponent(
                                        "mobarmy-command.game-not-loaded"
                                )
                );

                return true;
            }

            game.openSettings(
                    player
            );

            return true;
        }


        /*
         * /mobarmy reset ...
         */
        if (args[0].equalsIgnoreCase("reset")) {

            ResetCommand resetCommand =
                    new ResetCommand(
                            game
                    );

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


        sendCommandOverview(
                player
        );

        return true;
    }


    /*
     * =========================================================
     * /MOBARMY
     * =========================================================
     */

    private void sendCommandOverview(
            Player player
    ) {

        player.sendMessage(
                Component.empty()
        );

        player.sendMessage(
                lang(
                        "mobarmy-command.commands.title"
                )
        );

        player.sendMessage(
                lang(
                        "mobarmy-command.commands.info"
                )
        );

        player.sendMessage(
                lang(
                        "mobarmy-command.commands.resume"
                )
        );

        player.sendMessage(
                lang(
                        "mobarmy-command.commands.gamesettings"
                )
        );

        player.sendMessage(
                lang(
                        "mobarmy-command.commands.team"
                )
        );

        player.sendMessage(
                lang(
                        "mobarmy-command.commands.mobstatus"
                )
        );

        player.sendMessage(
                lang(
                        "mobarmy-command.commands.arenasummary"
                )
        );

        player.sendMessage(
                lang(
                        "mobarmy-command.commands.reset"
                )
        );

        player.sendMessage(
                lang(
                        "mobarmy-command.commands.setphase"
                )
        );

        player.sendMessage(
                Component.empty()
        );
    }


    /*
     * =========================================================
     * /MOBARMY INFO / HELP
     * =========================================================
     */

    private void sendInfo(
            Player player
    ) {

        player.sendMessage(
                Component.empty()
        );

        player.sendMessage(
                lang(
                        "mobarmy-command.info.title"
                )
        );

        player.sendMessage(
                lang(
                        "mobarmy-command.info.goal"
                )
        );

        player.sendMessage(
                lang(
                        "mobarmy-command.info.teams"
                )
        );

        player.sendMessage(
                lang(
                        "mobarmy-command.info.preparation"
                )
        );

        player.sendMessage(
                lang(
                        "mobarmy-command.info.arena"
                )
        );

        player.sendMessage(
                lang(
                        "mobarmy-command.info.commands-title"
                )
        );

        player.sendMessage(
                lang(
                        "mobarmy-command.info.team-command"
                )
        );

        player.sendMessage(
                lang(
                        "mobarmy-command.info.status-command"
                )
        );

        player.sendMessage(
                lang(
                        "mobarmy-command.info.settings-command"
                )
        );

        player.sendMessage(
                lang(
                        "mobarmy-command.info.help-command"
                )
        );

        player.sendMessage(
                Component.empty()
        );
    }


    private Component lang(
            String path
    ) {

        return game.getLanguageManager()
                .getComponent(
                        path
                );
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
                    "info",
                    "help",
                    "resume",
                    "gamesettings",
                    "reset"
            );
        }

        if (args.length >= 2
                && args[0].equalsIgnoreCase("reset")) {

            ResetCommand resetCommand =
                    new ResetCommand(
                            game
                    );

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
}