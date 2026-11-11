package runi.myddns.challenges.games.mobarmybattle.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import runi.myddns.challenges.games.mobarmybattle.MobArmyBattleGame;

public class ScoreboardSwitchListener implements Listener {

    private final MobArmyBattleGame game;

    public ScoreboardSwitchListener(MobArmyBattleGame game) {
        this.game = game;
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        game.getPlugin().getServer().getScheduler().runTaskLater(
                game.getPlugin(),
                () -> {
                    if (!player.isOnline()) return;

                    game.getScoreboardSwitcher()
                            .refreshScoreboard(player);
                },
                1L
        );
    }
}