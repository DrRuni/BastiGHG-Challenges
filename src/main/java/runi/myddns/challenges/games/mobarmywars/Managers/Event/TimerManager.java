package runi.myddns.challenges.games.mobarmywars.Managers.Event;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import runi.myddns.challenges.games.mobarmywars.Managers.World.ResumeManager;
import runi.myddns.challenges.games.mobarmywars.MobArmyWarsGame;

import java.util.Locale;

public class TimerManager implements Listener {
    private final MobArmyWarsGame game;
    public boolean isRunning = false;
    public boolean isForward = false;
    private boolean collectPhaseEnded = false;
    private int timeInSeconds = 3600;
    private BossBar bossBar = null;
    private BukkitTask resumeSaveTask;

    public MobArmyWarsGame game() {
        return game;
    }

    public TimerManager(MobArmyWarsGame game) {
        this.game = game;

        loadTime();
        startTimerTask();
    }

    public void setTime(int seconds) {
        this.timeInSeconds = seconds;
        game.getEventResume().saveTimerState(seconds, isForward);
        updateBossBar();

        if (seconds <= 0) {
            onTimerReachedZero();
        }
    }

    public void addPlayerToBossBar(Player player) {

        if (bossBar == null) {
            bossBar = Bukkit.createBossBar(
                    game.getLanguageManager().get(
                            "timer-manager.bossbar.timer"
                    ),
                    BarColor.BLUE,
                    BarStyle.SEGMENTED_6
            );
        }

        if (!bossBar.getPlayers().contains(player)) {
            bossBar.addPlayer(player);
        }

        updateBossBar();
    }

    public void removeBossBar() {
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }
    }

    private boolean timerTaskRunning = false;

    public void startTimerTask() {
        if (timerTaskRunning) return;
        timerTaskRunning = true;

        Bukkit.getScheduler().runTaskTimer(game.getPlugin(), this::updateTimer, 20L, 20L);
    }

    public void updateTimer() {
        if (!isRunning) return;

        if (isForward) {
            timeInSeconds++;
            updateBossBar();
            return;
        }

        if (timeInSeconds > 0) {
            timeInSeconds--;

            if (timeInSeconds == 0) {
                onTimerReachedZero();
            }
        }

        updateBossBar();
    }

    private String formatTime(int timeInSeconds) {
        int days = timeInSeconds / 86400;
        int hours = (timeInSeconds % 86400) / 3600;
        int minutes = (timeInSeconds % 3600) / 60;
        int seconds = timeInSeconds % 60;

        if (days > 0) {
            String dayText =
                    days == 1
                            ? game.getLanguageManager().get(
                            "timer-manager.day"
                    )
                            : game.getLanguageManager().get(
                            "timer-manager.days"
                    );

            return String.format(
                    "§c%d §9%s, §c%02d§9h §c%02d§9m §c%02d§9s",
                    days,
                    dayText,
                    hours,
                    minutes,
                    seconds
            );
        }

        if (hours > 0) {
            return String.format(
                    "§c%d§9h §c%02d§9m §c%02d§9s",
                    hours,
                    minutes,
                    seconds
            );
        }

        if (minutes > 0) {
            return String.format(
                    "§c%d§9m §c%02d§9s",
                    minutes,
                    seconds
            );
        }

        return String.format(
                "§c%d§9%s",
                seconds,
                game.getLanguageManager().get(
                        "timer-manager.second-short"
                )
        );
    }

    public void updatePauseState() {

        game.getEventResume().setEventPaused(!isRunning);
    }

    public void loadTime() {
        timeInSeconds = game.getEventResume().loadTimerTime();
        isForward = game.getEventResume().loadTimerDirection();

        isRunning = false;
        updatePauseState();
    }

    public void startTimer() {
        isRunning = true;
        startResumeSaveTask();

        if (!isForward) {
            collectPhaseEnded = false;
        }

        updatePauseState();

        for (Player p : Bukkit.getOnlinePlayers()) {
            String world = p.getWorld().getName().toLowerCase(Locale.ROOT);
            if (world.contains("mobarena") || world.contains("rot") || world.contains("blau")) {
                p.sendMessage(
                        game.getLanguageManager().getComponent(
                                "timer-manager.started"
                        )
                );
            }
        }

        updatePauseState();
        ensureBossBarExists();
        updateBossBar();
    }

    public void pauseTimer() {
        isRunning = false;
        stopResumeSaveTask();
        updatePauseState();

        for (Player p : Bukkit.getOnlinePlayers()) {
            String world = p.getWorld().getName().toLowerCase(Locale.ROOT);
            if (world.contains("mobarena") || world.contains("rot") || world.contains("blau")) {
                p.sendMessage(
                        game.getLanguageManager().getComponent(
                                "timer-manager.paused"
                        )
                );
            }
        }
    }

    public void stopTimer() {
        isRunning = false;
        stopResumeSaveTask();

        if (!isForward && timeInSeconds <= 0) {
            collectPhaseEnded = true;
        }

        updatePauseState();
        updateBossBar();
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isForward() {
        return isForward;
    }

    public int getTimeInSeconds() {
        return timeInSeconds;
    }

    public void addTime(int seconds) {
        timeInSeconds += seconds;
        updateBossBar();
    }

    public void removeTime(int seconds) {
        timeInSeconds = Math.max(0, timeInSeconds - seconds);
        updateBossBar();

        if (timeInSeconds == 0) {
            onTimerReachedZero();
        }
    }

    public void setForward(boolean forward) {
        this.isForward = forward;

        if (forward && timeInSeconds < 0) {
            timeInSeconds = 0;
        }

        game.getEventResume().saveTimerState(timeInSeconds, isForward);

        updateBossBar();
    }

    public void ensureBossBarExists() {
        if (bossBar == null) {
            bossBar = Bukkit.createBossBar(
                    game.getLanguageManager().get(
                            "timer-manager.bossbar.timer"
                    ),
                    BarColor.BLUE,
                    BarStyle.SEGMENTED_6
            );
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!bossBar.getPlayers().contains(player)) {
                bossBar.addPlayer(player);
            }
        }
        updateBossBar();
    }

    public void removeBossBarFor(Player player) {
        if (bossBar != null && bossBar.getPlayers().contains(player)) {
            bossBar.removePlayer(player);
        }
    }

    private void onTimerReachedZero() {

        if (collectPhaseEnded) return;
        if (isForward) return;
        if (!isRunning) return;
        if (game.getEventResume().loadPhase() != ResumeManager.PHASE_TEAMWELT) return;

        collectPhaseEnded = true;

        Bukkit.getScheduler().runTask(
                game.getPlugin(),
                () -> game.getEventManager().handleTimerEnd()
        );
    }

    private void startResumeSaveTask() {
        if (resumeSaveTask != null) return;

        resumeSaveTask = Bukkit.getScheduler().runTaskTimer(
                game.getPlugin(),
                () -> {
                    if (!isRunning) return;
                    game.getEventResume().saveTimerState(timeInSeconds, isForward);
                },
                20L * 30,
                20L * 30
        );
    }

    private void stopResumeSaveTask() {
        if (resumeSaveTask != null) {
            resumeSaveTask.cancel();
            resumeSaveTask = null;
        }
    }

    public void updateBossBar() {
        updateBossBarInternal(null);
    }

    public void updateBossBar(String customMessage) {
        updateBossBarInternal(customMessage);
    }

    private void updateBossBarInternal(String customMessage) {
        if (bossBar == null) return;

        if (timeInSeconds < 0 && !isForward) {
            bossBar.setTitle(
                    LegacyComponentSerializer.legacySection().serialize(
                            game.getLanguageManager().getComponent(
                                    "timer-manager.bossbar.expired"
                            )
                    )
            );
            return;
        }

        int displayTime = Math.max(0, timeInSeconds);

        String time =
                customMessage != null
                        ? customMessage
                        : formatTime(displayTime);

        bossBar.setTitle(time);
    }
}
