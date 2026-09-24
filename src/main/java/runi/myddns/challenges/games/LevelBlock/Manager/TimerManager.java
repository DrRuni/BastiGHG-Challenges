package runi.myddns.challenges.games.LevelBlock.Manager;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import runi.myddns.challenges.core.timer.GameTimerManager;
import runi.myddns.challenges.core.utils.ColorUtil;
import runi.myddns.challenges.games.LevelBlock.LevelBlockGame;

public class TimerManager {

    private final LevelBlockGame game;
    private final GameTimerManager timerManager;

    private BukkitTask task;
    private int gradientOffset;
    private float gradientTick = 0f;

    public TimerManager(
            LevelBlockGame game,
            GameTimerManager timerManager
    ) {
        this.game = game;
        this.timerManager = timerManager;
    }

    public void start() {
        if (task != null) return;

        task = Bukkit.getScheduler().runTaskTimer(
                game.getPlugin(),
                () -> {

                    String animated = ColorUtil.animatedGreenGradient(
                            timerManager.getFormattedTime(),
                            gradientTick
                    );

                    gradientTick += 0.35f;

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (!game.isLevelBlockPlayer(player)) continue;

                        if (!game.getLevelBlockGameManager().isStarted(player.getWorld())) {
                            clear(player);
                            continue;
                        }

                        player.sendActionBar(animated);
                    }
                },
                0L,
                1L
        );
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            clear(player);
        }
    }

    public void clear(Player player) {
        if (player == null) return;

        player.sendActionBar(
                Component.empty()
        );
    }

    public void clearAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!game.isLevelBlockPlayer(player)) continue;

            clear(player);
        }
    }
}