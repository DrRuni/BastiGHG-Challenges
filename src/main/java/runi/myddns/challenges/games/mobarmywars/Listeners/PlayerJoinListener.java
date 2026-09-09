package runi.myddns.challenges.games.mobarmywars.Listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import runi.myddns.challenges.games.mobarmywars.Managers.Event.TimerManager;
import runi.myddns.challenges.games.mobarmywars.MobArmyWarsGame;
import runi.myddns.challenges.games.mobarmywars.Utils.GradientText;
import runi.myddns.challenges.games.mobarmywars.Managers.World.TeleportManager;

public class PlayerJoinListener implements Listener {

    private final MobArmyWarsGame game;

    public PlayerJoinListener(MobArmyWarsGame game) {
        this.game = game;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskLater(game.getPlugin(), () -> {
            if (!player.isOnline()) return;

            boolean restored =
                    game.getEventResume().restorePlayerPosition(player);

            if (!restored) {
                TeleportManager.teleport(
                        game,
                        player,
                        "world_mobarmy_lobby"
                );
            }

            game.getPlayerEffectManager()
                    .applyNightVision(player);

            showWelcomeSequence(player);
            showProjectNotice(player);

            Bukkit.getScheduler().runTaskLater(game.getPlugin(), () -> {
                if (!player.isOnline()) return;

                game.getTimerManager().ensureBossBarExists();
                game.getTimerManager().addPlayerToBossBar(player);
                game.getTimerManager().updatePauseState();

                game.getTeamScoreboardManager().updateBoard();

                for (Player online : Bukkit.getOnlinePlayers()) {
                    game.getScoreboardSwitcher()
                            .switchToTeam(online);
                }
            }, 20L * 7);

        }, 80L);

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        game.getEventResume().savePlayerLastLocation(player);
        game.getScoreboardSwitcher().removePlayer(player);

        TimerManager timer = game.getTimerManager();
        if (timer != null) {
            timer.removeBossBarFor(player);
        }
    }

    private void showWelcomeSequence(Player player) {
        Bukkit.getScheduler().runTaskLater(game.getPlugin(), () -> {
            if (!player.isOnline()) return;

            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.15f);

            Component welcomeTitle = GradientText.gradient(
                    game.getLanguageManager().get(
                            "player-join-listener.welcome.title"
                    ),
                    220, 50, 50,
                    50, 110, 255
            );

            player.showTitle(net.kyori.adventure.title.Title.title(
                    welcomeTitle,
                    Component.empty(),
                    net.kyori.adventure.title.Title.Times.times(
                            java.time.Duration.ofMillis(300),
                            java.time.Duration.ofSeconds(2),
                            java.time.Duration.ofMillis(300)
                    )
            ));

            Bukkit.getScheduler().runTaskLater(game.getPlugin(), () -> {
                if (!player.isOnline()) return;

                Component zuSubtitle = Component.text(
                        game.getLanguageManager().get(
                                "player-join-listener.welcome.to"
                        )
                ).color(
                        net.kyori.adventure.text.format.TextColor.color(
                                210, 210, 210
                        )
                );

                player.showTitle(net.kyori.adventure.title.Title.title(
                        welcomeTitle,
                        zuSubtitle,
                        net.kyori.adventure.title.Title.Times.times(
                                java.time.Duration.ofMillis(200),
                                java.time.Duration.ofSeconds(1),
                                java.time.Duration.ofMillis(200)
                        )
                ));
            }, 30L);

            Bukkit.getScheduler().runTaskLater(game.getPlugin(), () -> {
                if (!player.isOnline()) return;

                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.1f);

                Component mainTitle = GradientText.gradient(
                        "MobArmyWars",
                        220, 50, 50,
                        50, 110, 255
                );

                Component subtitle = Component.empty();

                player.showTitle(net.kyori.adventure.title.Title.title(
                        mainTitle,
                        subtitle,
                        net.kyori.adventure.title.Title.Times.times(
                                java.time.Duration.ofMillis(300),
                                java.time.Duration.ofSeconds(2),
                                java.time.Duration.ofMillis(500)
                        )
                ));
            }, 60L);
        }, 20L);
    }

    public void showProjectNotice(Player player) {
        Bukkit.getScheduler().runTaskLater(game.getPlugin(), () -> {
            if (!player.isOnline()) return;

            Component clickText = game.getLanguageManager()
                    .getComponent("player-join-listener.notice.line-1")
                    .clickEvent(
                            ClickEvent.callback(audience -> {
                                if (!(audience instanceof Player clickedPlayer)) {
                                    return;
                                }

                                clickedPlayer.sendMessage(Component.empty());

                                clickedPlayer.sendMessage(
                                        game.getLanguageManager().getComponent(
                                                "player-join-listener.notice.details-1"
                                        )
                                );

                                clickedPlayer.sendMessage(
                                        game.getLanguageManager().getComponent(
                                                "player-join-listener.notice.details-2"
                                        )
                                );

                                clickedPlayer.sendMessage(
                                        game.getLanguageManager().getComponent(
                                                "player-join-listener.notice.details-3"
                                        )
                                );

                                clickedPlayer.sendMessage(
                                        game.getLanguageManager().getComponent(
                                                "player-join-listener.notice.details-4"
                                        )
                                );

                                clickedPlayer.sendMessage(Component.empty());
                            })
                    )
                    .hoverEvent(
                            HoverEvent.showText(
                                    game.getLanguageManager().getComponent(
                                            "player-join-listener.notice.hover"
                                    )
                            )
                    );

            player.sendMessage(Component.empty());
            player.sendMessage(clickText);
            player.sendMessage(Component.empty());

        }, 100L);
    }
}