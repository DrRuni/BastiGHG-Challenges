package runi.myddns.challenges.games.MobArmyBattle.Listeners;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import runi.myddns.challenges.games.MobArmyBattle.Managers.Event.TimerManager;
import runi.myddns.challenges.games.MobArmyBattle.MobArmyBattleGame;
import runi.myddns.challenges.games.MobArmyBattle.Utils.GradientText;

public class PlayerJoinListener implements Listener {

    private final MobArmyBattleGame game;

    public PlayerJoinListener(MobArmyBattleGame game) {
        this.game = game;
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

    public void showWelcomeSequence(Player player) {
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

                Component titleImage = Component.text("\uE031");

                player.showTitle(net.kyori.adventure.title.Title.title(
                        titleImage,
                        Component.empty(),
                        net.kyori.adventure.title.Title.Times.times(
                                java.time.Duration.ofMillis(300),
                                java.time.Duration.ofSeconds(2),
                                java.time.Duration.ofMillis(500)
                        )
                ));
            }, 60L);
        }, 20L);
    }
    public void showHelpHint(Player player) {
        Bukkit.getScheduler().runTaskLater(game.getPlugin(), () -> {
            if (!player.isOnline()) return;

            Component message = game.getLanguageManager()
                    .getComponent("player-join-listener.help-hint")
                    .clickEvent(ClickEvent.runCommand("/info"))
                    .hoverEvent(
                            HoverEvent.showText(
                                    game.getLanguageManager().getComponent(
                                            "player-join-listener.help-hover"
                                    )
                            )
                    );

            player.sendMessage(Component.empty());
            player.sendMessage(message);
            player.sendMessage(Component.empty());

        }, 100L);
    }
}