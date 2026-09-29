package runi.myddns.challenges.games.MobArmyBattle.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
                    Component.text(
                            "Dieser Befehl kann nur von Spielern verwendet werden.",
                            NamedTextColor.RED
                    )
            );
            return true;
        }

        if (!player.isOp()) {
            player.sendMessage(
                    Component.text(
                            "Du hast keine Berechtigung dafür.",
                            NamedTextColor.RED
                    )
            );
            return true;
        }

        if (!game.isLoaded()) {
            player.sendMessage(
                    Component.text(
                            "MobArmyBattle muss zuerst geladen werden.",
                            NamedTextColor.RED
                    )
            );
            return true;
        }

        /*
         * /mobarmy reset
         */
        if (args.length == 0) {

            game.getPlugin()
                    .getResetVoteManager()
                    .startVote(
                            game.getDisplayName(),
                            "Spielstand zurücksetzen",
                            () -> {

                                showTitleToAll(
                                        "MobArmyBattle",
                                        "Spielstand wird zurückgesetzt...",
                                        NamedTextColor.GOLD
                                );

                                game.getEventManager()
                                        .resetGame(player);

                                Bukkit.broadcast(
                                        Component.text(
                                                "✔ MobArmyBattle wurde zurückgesetzt.",
                                                NamedTextColor.GREEN
                                        )
                                );
                            }
                    );

            return true;
        }

        /*
         * /mobarmy reset world
         */
        if (args[0].equalsIgnoreCase("world")) {

            game.getPlugin()
                    .getResetVoteManager()
                    .startVote(
                            game.getDisplayName(),
                            "Alle Welten zurücksetzen",
                            () -> game.getPlugin()
                                    .getGameResetManager()
                                    .resetWorlds(game)
                    );

            return true;
        }

        /**
         * /mobarmy reset arena
         */
        if (args[0].equalsIgnoreCase("arena")) {

            game.getPlugin()
                    .getResetVoteManager()
                    .startVote(
                            game.getDisplayName(),
                            "Arena zurücksetzen",
                            () -> {

                                if (game.getWorldManager()
                                        .isWorldResetBlocked()) {

                                    Bukkit.broadcast(
                                            Component.text(
                                                    "❌ Es läuft bereits ein Welt-Reset.",
                                                    NamedTextColor.RED
                                            )
                                    );

                                    return;
                                }

                                showTitleToAll(
                                        "MobArmyBattle",
                                        "Arena wird zurückgesetzt...",
                                        NamedTextColor.GOLD
                                );

                                game.getWorldManager()
                                        .resetArenaWorld();
                            }
                    );

            return true;
        }

/**
 * /mobarmy reset teamwelt
 */
        if (args[0].equalsIgnoreCase("teamwelt")
                || args[0].equalsIgnoreCase("teamworld")) {

            game.getPlugin()
                    .getResetVoteManager()
                    .startVote(
                            game.getDisplayName(),
                            "Teamwelten neu generieren",
                            () -> {

                                if (game.getWorldManager()
                                        .isWorldResetBlocked()) {

                                    Bukkit.broadcast(
                                            Component.text(
                                                    "❌ Es läuft bereits ein Welt-Reset.",
                                                    NamedTextColor.RED
                                            )
                                    );

                                    return;
                                }

                                showTitleToAll(
                                        "MobArmyBattle",
                                        "Teamwelten werden neu erstellt...",
                                        NamedTextColor.GOLD
                                );

                                game.getWorldManager()
                                        .resetTeamWorlds();
                            }
                    );

            return true;
        }

        player.sendMessage(
                Component.text(
                        "Nutzung: /mobarmy reset [world|arena|teamwelt]",
                        NamedTextColor.RED
                )
        );

        return true;
    }

    private void showTitleToAll(
            String title,
            String subtitle,
            NamedTextColor color
    ) {

        Title screenTitle =
                Title.title(
                        Component.text(
                                title,
                                color
                        ),
                        Component.text(
                                subtitle,
                                color
                        ),
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
    public List<String> onTabComplete(
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