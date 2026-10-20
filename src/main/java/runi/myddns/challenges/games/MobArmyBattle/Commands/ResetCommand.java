package runi.myddns.challenges.games.MobArmyBattle.Commands;

import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import runi.myddns.challenges.games.MobArmyBattle.MobArmyBattleGame;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class ResetCommand implements CommandExecutor, TabCompleter {

    private final MobArmyBattleGame game;

    public ResetCommand(MobArmyBattleGame game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command cmd,
            @NotNull String label,
            @NotNull String @NotNull [] args
    ) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(
                    game.getLanguageManager().getComponent("commands.mobarmy-reset.player-only")
            );
            return true;
        }

        if (!player.isOp()) {
            player.sendMessage(
                    game.getLanguageManager().getComponent("commands.mobarmy-reset.no-permission")
            );
            return true;
        }

        if (!game.isLoaded()) {
            player.sendMessage(
                    game.getLanguageManager().getComponent("commands.mobarmy-reset.game-not-loaded")
            );
            return true;
        }

        /*
         * /mobarmy reset
         */
        if (args.length == 0) {

            game.getPlugin()
                    .getVoteManager()
                    .startVote(
                            game.getDisplayName(),
                            game.getLanguageManager().get("commands.mobarmy-reset.vote.game"),
                            () -> {

                                showTitleToAll("commands.mobarmy-reset.progress.game");

                                game.getEventManager().resetGame();

                                Bukkit.broadcast(
                                        game.getLanguageManager().getComponent("commands.mobarmy-reset.game-complete")
                                );
                            }
                    );

            return true;
        }

        if (args[0].equalsIgnoreCase("world")) {

            game.getPlugin()
                    .getVoteManager()
                    .startVote(
                            game.getDisplayName(),
                            game.getLanguageManager().get("commands.mobarmy-reset.vote.worlds"),
                            () -> game.getPlugin()
                                    .getGameResetManager()
                                    .resetWorlds(game)
                    );

            return true;
        }

        if (args[0].equalsIgnoreCase("arena")) {

            game.getPlugin()
                    .getVoteManager()
                    .startVote(
                            game.getDisplayName(),
                            game.getLanguageManager().get("commands.mobarmy-reset.vote.arena"),
                            () -> {

                                if (game.getWorldManager()
                                        .isWorldResetBlocked()) {

                                    Bukkit.broadcast(
                                            game.getLanguageManager().getComponent("commands.mobarmy-reset.world-reset-running")
                                    );

                                    return;
                                }

                                showTitleToAll("commands.mobarmy-reset.progress.arena");

                                game.getWorldManager()
                                        .resetArenaWorld();
                            }
                    );

            return true;
        }

        if (args[0].equalsIgnoreCase("teamwelt")
                || args[0].equalsIgnoreCase("teamworld")) {

            game.getPlugin()
                    .getVoteManager()
                    .startVote(
                            game.getDisplayName(),
                            game.getLanguageManager().get("commands.mobarmy-reset.vote.teamworlds"),
                            () -> {

                                if (game.getPlugin().getGameResetManager().isResetRunning()) {
                                    Bukkit.broadcast(
                                            game.getLanguageManager().getComponent("commands.mobarmy-reset.world-reset-running")
                                    );
                                    return;
                                }

                                showTitleToAll("commands.mobarmy-reset.progress.teamworlds");

                                game.getWorldManager()
                                        .resetTeamWorlds();
                            }
                    );

            return true;
        }

        player.sendMessage(
                game.getLanguageManager().getComponent("commands.mobarmy-reset.usage")
        );

        return true;
    }

    private void showTitleToAll(String subtitleKey) {

        Title screenTitle = Title.title(
                game.getLanguageManager().getComponent("commands.mobarmy-reset.title"),
                game.getLanguageManager().getComponent(subtitleKey),
                Title.Times.times(
                        Duration.ofMillis(500),
                        Duration.ofSeconds(3),
                        Duration.ofMillis(800)
                )
        );

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showTitle(screenTitle);

            player.playSound(
                    player.getLocation(),
                    Sound.BLOCK_NOTE_BLOCK_PLING,
                    0.7f,
                    1.1f
            );
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command cmd,
            @NotNull String alias,
            @NotNull String @NotNull [] args
    ) {

        if (!(sender instanceof Player player)) {
            return Collections.emptyList();
        }

        if (!player.isOp()) {
            return Collections.emptyList();
        }

        if (args.length == 1) {

            return Stream.of(
                            "world",
                            "arena",
                            "teamwelt"
                    )
                    .filter(
                            entry -> entry.startsWith(
                                    args[0].toLowerCase()
                            )
                    )
                    .toList();
        }

        return Collections.emptyList();
    }
}