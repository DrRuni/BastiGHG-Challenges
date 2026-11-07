
package runi.myddns.challenges.games.chunkrandomizer.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import runi.myddns.challenges.ChallengeMain;
import runi.myddns.challenges.core.timer.GameTimerManager;

public class TimerManager {

    private final ChallengeMain plugin;
    private final GameTimerManager timerManager;
    private final ChunkRandomizerStartManager startManager;

    private BukkitTask task;
    private float gradientTick = 0f;

    public TimerManager(
            ChallengeMain plugin,
            GameTimerManager timerManager,
            ChunkRandomizerStartManager startManager
    ) {
        this.plugin = plugin;
        this.timerManager = timerManager;
        this.startManager = startManager;
    }

    // ============================================================
    // START
    // ============================================================

    public void start() {
        if (task != null) return;

        task = Bukkit.getScheduler().runTaskTimer(
                plugin,
                () -> {

                    Component display = animatedGradient(
                            timerManager.getFormattedTime()
                    );

                    gradientTick += 0.08f;

                    for (Player player : Bukkit.getOnlinePlayers()) {

                        if (!plugin.getGameWorldManager()
                                .isGamePlayer(player, "chunkrandomizer")) {
                            continue;
                        }

                        if (!startManager.isStarted()) {
                            clear(player);
                            continue;
                        }

                        player.sendActionBar(display);
                    }
                },
                0L,
                2L
        );
    }

    // ============================================================
    // FARBVERLAUF
    // ============================================================

    private Component animatedGradient(String text) {

        TextColor[] colors = {
                TextColor.color(0xFFD700), // Gold
                TextColor.color(0xFFAA00), // Orange
                TextColor.color(0xFF4500)  // Rot-Orange
        };

        TextComponent.Builder builder = Component.text();

        for (int i = 0; i < text.length(); i++) {

            float position = (i * 0.12f + gradientTick) % 3f;

            int first = (int) position;
            int second = (first + 1) % colors.length;

            float factor = position - first;

            int rgb1 = colors[first].value();
            int rgb2 = colors[second].value();

            int red = interpolate(
                    (rgb1 >> 16) & 0xFF,
                    (rgb2 >> 16) & 0xFF,
                    factor
            );

            int green = interpolate(
                    (rgb1 >> 8) & 0xFF,
                    (rgb2 >> 8) & 0xFF,
                    factor
            );

            int blue = interpolate(
                    rgb1 & 0xFF,
                    rgb2 & 0xFF,
                    factor
            );

            int rgb = (red << 16) | (green << 8) | blue;

            builder.append(
                    Component.text(
                            String.valueOf(text.charAt(i)),
                            TextColor.color(rgb)
                    )
            );
        }

        return builder.build();
    }

    private int interpolate(int first, int second, float factor) {
        return Math.round(first + (second - first) * factor);
    }

    public void stop() {

        if (task != null) {
            task.cancel();
            task = null;
        }

        clearAll();
    }

    public void clear(Player player) {
        if (player == null) return;

        player.sendActionBar(Component.empty());
    }

    public void clearAll() {

        for (Player player : Bukkit.getOnlinePlayers()) {

            if (!plugin.getGameWorldManager()
                    .isGamePlayer(player, "chunkrandomizer")) {
                continue;
            }

            clear(player);
        }
    }
}

