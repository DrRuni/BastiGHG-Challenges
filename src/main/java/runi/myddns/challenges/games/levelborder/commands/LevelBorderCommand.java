package runi.myddns.challenges.games.levelborder.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import runi.myddns.challenges.games.levelborder.LevelBorderGame;
import runi.myddns.challenges.games.levelborder.manager.BorderDataManager;
import runi.myddns.challenges.games.levelborder.manager.LevelBorderManager;
import runi.myddns.challenges.games.levelborder.manager.PortalManager;
import runi.myddns.challenges.games.levelborder.manager.ScoreboardManager;
import runi.myddns.challenges.games.levelborder.manager.TimerManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LevelBorderCommand implements CommandExecutor, TabCompleter {

    private final LevelBorderGame game;
    private final LevelBorderManager borderManager;
    private final ScoreboardManager scoreboardManager;
    private final TimerManager timerManager;
    private final PortalManager portalManager;

    public LevelBorderCommand(
            LevelBorderGame game,
            LevelBorderManager borderManager,
            ScoreboardManager scoreboardManager,
            TimerManager timerManager,
            PortalManager portalManager
    ) {
        this.game = game;
        this.borderManager = borderManager;
        this.scoreboardManager = scoreboardManager;
        this.timerManager = timerManager;
        this.portalManager = portalManager;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command cmd,
            @NotNull String label,
            String @NotNull [] args
    ) {

        if (!(sender instanceof Player player)) {

            sender.sendMessage(
                    game.getLanguageManager()
                            .getComponent(
                                    "commands.levelborder.player-only"
                            )
            );

            return true;
        }

        int playerLevelRank = getPlayerStufe(player);
        boolean isAdmin = playerLevelRank >= 4;

        if (args.length > 0
                && args[0].equalsIgnoreCase("score")) {

            ScoreboardCommand scoreCmd = new ScoreboardCommand(game, scoreboardManager);

            String[] shifted =
                    Arrays.copyOfRange(
                            args,
                            1,
                            args.length
                    );

            return scoreCmd.onCommand(
                    sender,
                    cmd,
                    label,
                    shifted
            );
        }

        BorderDataManager data =
                borderManager.getData();

        if (args.length == 0) {
            sendCommandHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "info" ->
                    sendGameInfo(player);

            case "status" ->
                    sendDetailedInfo(player, data);

            case "gamesettings" -> {

                if (!game.isLoaded()) {

                    player.sendMessage(
                            game.getLanguageManager()
                                    .getComponent(
                                            "commands.levelborder.load-first-settings"
                                    )
                    );

                    return true;
                }

                game.openSettings(player);
            }

            case "center" -> {

                if (!isAdmin) {

                    player.sendMessage(
                            game.getLanguageManager()
                                    .getComponent(
                                            "commands.levelborder.center.admin-only"
                                    )
                    );

                    return true;
                }

                Location loc =
                        player.getLocation();

                double x =
                        Math.floor(loc.getX()) + 0.5;

                double z =
                        Math.floor(loc.getZ()) + 0.5;

                Location centered =
                        new Location(
                                loc.getWorld(),
                                x,
                                loc.getY(),
                                z
                        );

                player.playSound(
                        player.getLocation(),
                        Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                        0.6f,
                        1.2f
                );

                borderManager.setCenter(centered);

                player.sendMessage(
                        game.getLanguageManager()
                                .getComponent(
                                        "commands.levelborder.center.success"
                                )
                );
            }

            case "start" -> {

                if (!isAdmin) {

                    player.sendMessage(
                            game.getLanguageManager()
                                    .getComponent(
                                            "commands.levelborder.start.admin-only"
                                    )
                    );

                    return true;
                }

                if (borderManager
                        .getData()
                        .getCenter() == null) {

                    borderManager.setCenter(
                            player.getLocation()
                    );
                }

                borderManager.setActive(true);
                scoreboardManager.show();

                for (Player p :
                        Bukkit.getOnlinePlayers()) {

                    if (!game.isLevelBorderPlayer(p)) {
                        continue;
                    }

                    Location loc =
                            p.getLocation();

                    p.playSound(
                            loc,
                            Sound.BLOCK_BEACON_ACTIVATE,
                            0.9f,
                            0.8f
                    );

                    Bukkit.getScheduler()
                            .runTaskLater(
                                    game.getPlugin(),
                                    () -> p.playSound(
                                            loc,
                                            Sound.ENTITY_WARDEN_HEARTBEAT,
                                            1.2f,
                                            0.7f
                                    ),
                                    10L
                            );

                    Bukkit.getScheduler()
                            .runTaskLater(
                                    game.getPlugin(),
                                    () -> p.playSound(
                                            loc,
                                            Sound.BLOCK_SCULK_SHRIEKER_SHRIEK,
                                            0.4f,
                                            0.6f
                                    ),
                                    18L
                            );
                }

                player.sendMessage(
                        game.getLanguageManager()
                                .getComponent(
                                        "commands.levelborder.start.success"
                                )
                );
            }

            case "stop" -> {

                if (!isAdmin) {

                    player.sendMessage(
                            game.getLanguageManager()
                                    .getComponent(
                                            "commands.levelborder.stop.admin-only"
                                    )
                    );

                    return true;
                }

                borderManager.setActive(false);

                player.playSound(
                        player.getLocation(),
                        Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                        0.6f,
                        1.2f
                );

                player.sendMessage(
                        game.getLanguageManager()
                                .getComponent(
                                        "commands.levelborder.stop.success"
                                )
                );
            }

            case "set" -> {

                if (!isAdmin) {

                    player.sendMessage(
                            game.getLanguageManager()
                                    .getComponent(
                                            "commands.levelborder.set.admin-only"
                                    )
                    );

                    return true;
                }

                if (args.length < 2) {

                    player.sendMessage(
                            game.getLanguageManager()
                                    .getComponent(
                                            "commands.levelborder.set.usage"
                                    )
                    );

                    return true;
                }

                try {

                    double size =
                            Double.parseDouble(args[1]);

                    borderManager.setSize(size);

                    player.sendMessage(
                            game.getLanguageManager()
                                    .getComponent(
                                            "commands.levelborder.set.success",
                                            "size",
                                            size
                                    )
                    );

                } catch (NumberFormatException e) {

                    player.sendMessage(
                            game.getLanguageManager()
                                    .getComponent(
                                            "commands.levelborder.set.invalid-number"
                                    )
                    );
                }

                player.playSound(
                        player.getLocation(),
                        Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                        0.6f,
                        1.2f
                );
            }

            case "reset" -> {

                if (!game.isLoaded()) {

                    player.sendMessage(
                            game.getLanguageManager()
                                    .getComponent(
                                            "commands.levelborder.reset.load-first"
                                    )
                    );

                    return true;
                }

                if (!isAdmin) {

                    player.sendMessage(
                            game.getLanguageManager()
                                    .getComponent(
                                            "commands.levelborder.reset.admin-only"
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
                                                    "commands.levelborder.reset.vote-worlds"
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
                                                "commands.levelborder.reset.vote-progress"
                                        ),
                                () -> {

                                    scoreboardManager.reset();
                                    borderManager.resetBorder(player);
                                    timerManager.reset();
                                    portalManager.clearPortalWorldData();

                                    Bukkit.broadcast(
                                            game.getLanguageManager()
                                                    .getComponent(
                                                            "commands.levelborder.reset.success"
                                                    )
                                    );
                                }
                        );
            }

            default ->
                    player.sendMessage(
                            game.getLanguageManager()
                                    .getComponent(
                                            "commands.levelborder.unknown"
                                    )
                    );
        }

        return true;
    }

    private int getPlayerStufe(Player player) {
        return player.isOp() ? 4 : 2;
    }

    private void sendCommandHelp(Player player) {

        player.sendMessage("");

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelborder.help.title"
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelborder.help.info"
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelborder.help.status"
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelborder.help.start"
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelborder.help.stop"
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelborder.help.center"
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelborder.help.set"
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelborder.help.score"
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelborder.help.settings"
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelborder.help.reset"
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelborder.help.reset-world"
                        )
        );

        player.sendMessage("");
    }

    private void sendGameInfo(Player player) {

        player.sendMessage("");

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelborder.info.title"
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelborder.info.goal"
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelborder.info.description-1"
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelborder.info.description-2"
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelborder.info.description-3"
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelborder.info.description-4"
                        )
        );

        player.sendMessage("");

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelborder.info.commands-title"
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelborder.info.status"
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelborder.info.score"
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelborder.info.settings"
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelborder.info.help"
                        )
        );

        player.sendMessage("");
    }

    private void sendDetailedInfo(
            Player player,
            BorderDataManager data
    ) {

        int total =
                Bukkit.getOnlinePlayers()
                        .stream()
                        .filter(
                                game::isLevelBorderPlayer
                        )
                        .mapToInt(
                                Player::getLevel
                        )
                        .sum();

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelborder.status.title"
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelborder.status.total-level",
                                "value",
                                total
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelborder.status.record",
                                "value",
                                data.getMaxTotalLevel()
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelborder.status.border-size",
                                "value",
                                data.getSize()
                        )
        );

        Component activeValue =
                data.isActive()
                        ? game.getLanguageManager()
                        .getComponent(
                                "commands.levelborder.status.yes"
                        )
                        : game.getLanguageManager()
                        .getComponent(
                                "commands.levelborder.status.no"
                        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelborder.status.active",
                                "value",
                                activeValue
                        )
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent(
                                "commands.levelborder.status.footer"
                        )
        );
    }

    @Override
    public @NotNull List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command cmd,
            @NotNull String alias,
            String @NotNull [] args
    ) {

        List<String> list =
                new ArrayList<>();

        if (args.length == 1) {

            list.addAll(
                    Arrays.asList(
                            "info",
                            "status",
                            "score",
                            "gamesettings",
                            "center",
                            "start",
                            "stop",
                            "set",
                            "reset"
                    )
            );

        } else if (args.length == 2
                && args[0].equalsIgnoreCase("set")) {

            list.addAll(
                    Arrays.asList(
                            "10",
                            "25",
                            "50",
                            "100"
                    )
            );

        } else if (args.length == 2
                && args[0].equalsIgnoreCase("score")) {

            list.addAll(
                    Arrays.asList(
                            "hide",
                            "reload",
                            "reset"
                    )
            );

        } else if (args.length == 2
                && args[0].equalsIgnoreCase("reset")) {

            list.add("world");
        }

        return list;
    }
}