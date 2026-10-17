package runi.myddns.challenges.games.MobArmyBattle.Listeners;

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

public class PlayerListener implements Listener {

    private final MobArmyBattleGame game;

    public PlayerListener(MobArmyBattleGame game) {
        this.game = game;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();

        if (game.isMobArmyPlayer(player)) {
            game.getEventResume()
                    .savePlayerLastLocation(player);
        }

        game.getScoreboardSwitcher()
                .removePlayer(player);

        TimerManager timer =
                game.getTimerManager();

        if (timer != null) {
            timer.removeBossBarFor(player);
        }
    }

    public void showWelcomeSequence(Player player) {

        Bukkit.getScheduler().runTaskLater(
                game.getPlugin(),
                () -> {

                    if (!player.isOnline()) return;

                    player.sendMessage("");
                    player.sendMessage(
                            GradientText.gradient(
                                    game.getLanguageManager()
                                            .get("player-join-listener.welcome.name"),
                                    220, 50, 50,
                                    50, 110, 255
                            )
                    );
                    player.sendMessage("");

                    player.sendMessage(
                            game.getLanguageManager()
                                    .getComponent(
                                            "player-join-listener.welcome.settings"
                                    )
                    );

                    player.sendMessage(
                            game.getLanguageManager()
                                    .getComponent(
                                            "player-join-listener.welcome.info"
                                    )
                    );

                    player.sendMessage("");

                    player.playSound(
                            player.getLocation(),
                            Sound.BLOCK_AMETHYST_BLOCK_CHIME,
                            0.35f,
                            1.2f
                    );

                    Component titleImage =
                            Component.text("\uE031");

                    player.showTitle(
                            net.kyori.adventure.title.Title.title(
                                    titleImage,
                                    Component.empty(),
                                    net.kyori.adventure.title.Title.Times.times(
                                            java.time.Duration.ofMillis(300),
                                            java.time.Duration.ofSeconds(2),
                                            java.time.Duration.ofMillis(500)
                                    )
                            )
                    );
                },
                20L
        );
    }
}