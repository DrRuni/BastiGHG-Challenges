package runi.myddns.challenges.games.levelborder.manager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import runi.myddns.challenges.games.levelborder.LevelBorderGame;
import runi.myddns.challenges.core.utils.ColorUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ScoreboardManager {

    private final Map<Player, Scoreboard> boards = new HashMap<>();
    private final Map<Player, Objective> objectives = new HashMap<>();

    private final LevelBorderGame game;
    private final BorderDataManager data;

    private BukkitRunnable updaterTask;
    private boolean updaterRunning = false;
    private float titleTick = 0f;

    public ScoreboardManager(LevelBorderGame game, BorderDataManager data) {
        this.game = game;
        this.data = data;
    }

    public void startUpdater() {
        if (updaterRunning) return;
        updaterRunning = true;

        updaterTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!data.isScoreboardVisible()) return;

                titleTick += 0.25f;

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (game.isLevelBorderPlayer(player)) {
                        updateBoard(player);
                    }
                }
            }
        };

        updaterTask.runTaskTimer(game.getPlugin(), 0L, 4L);
    }

    public void stopUpdater() {
        if (updaterTask != null) {
            updaterTask.cancel();
            updaterTask = null;
        }

        updaterRunning = false;
    }

    private void updateBoard(Player player) {

        Scoreboard board = boards.get(player);
        Objective objective = objectives.get(player);

        if (board == null || objective == null) {
            addPlayer(player);
            return;
        }

        objective.displayName(
                LegacyComponentSerializer.legacySection().deserialize(
                        ColorUtil.animatedBlueGradient(
                                game.getLanguageManager().get("scoreboard.title"),
                                titleTick
                        )
                )
        );

        if (player.getScoreboard() != board) {
            player.setScoreboard(board);
        }

        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }

        AtomicInteger score = new AtomicInteger(0);

        addLine(
                player,
                score.getAndIncrement(),
                game.getLanguageManager()
                        .getComponent("scoreboard.players")
        );

        addLine(
                player,
                score.getAndIncrement(),
                Component.empty()
        );

        Map<String, Integer> allPlayers =
                new HashMap<>(
                        data.getAllPlayerLevels()
                );

        allPlayers.entrySet().stream()
                .sorted(
                        Map.Entry.comparingByKey(
                                String.CASE_INSENSITIVE_ORDER
                        )
                )
                .forEach(entry -> {

                    Component playerLine =
                            game.getLanguageManager()
                                    .getComponent(
                                            "scoreboard.player-line",
                                            Map.of(
                                                    "player",
                                                    Component.text(entry.getKey()),

                                                    "level",
                                                    Component.text(String.valueOf(entry.getValue()))
                                            )
                                    );

                    addLine(
                            player,
                            score.getAndIncrement(),
                            playerLine
                    );
                });

        addLine(
                player,
                score.getAndIncrement(),
                Component.empty()
        );

        addLine(
                player,
                score.getAndIncrement(),
                game.getLanguageManager()
                        .getComponent(
                                "scoreboard.border-size"
                        )
        );

        addLine(
                player,
                score.getAndIncrement(),
                game.getLanguageManager()
                        .getComponent(
                                "scoreboard.border-size-value",
                                "size",
                                Component.text(
                                        (int) data.getSize()
                                )
                        )
        );
    }

    private void addLine(
            Player player,
            int lineNumber,
            Component text
    ) {

        Scoreboard board = boards.get(player);
        Objective objective = objectives.get(player);

        String entry = "§" + "0123456789abcdefklmnor".charAt(lineNumber);

        Team team =
                board.getTeam(
                        "line" + lineNumber
                );

        if (team == null) {

            team = board.registerNewTeam(
                    "line" + lineNumber
            );

            team.addEntry(entry);
        }

        team.prefix(text);
        team.suffix(Component.empty());

        objective.getScore(entry)
                .setScore(0);
    }

    public void hide() {
        data.setScoreboardVisible(false);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (game.isLevelBorderPlayer(player)) {
                removePlayer(player);
            }
        }
    }

    public void show() {
        data.setScoreboardVisible(true);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (game.isLevelBorderPlayer(player)) {
                addPlayer(player);
                updateBoard(player);
            }
        }
    }

    public void reset() {
        for (Player player : boards.keySet().toArray(new Player[0])) {
            removePlayer(player);
        }

        boards.clear();
        objectives.clear();
    }

    public void addPlayer(Player player) {
        if (player == null || boards.containsKey(player)) return;
        if (!game.isLevelBorderPlayer(player)) return;

        if (!data.isScoreboardVisible()) return;

        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();

        Objective objective = board.registerNewObjective(
                "lbinfo",
                Criteria.DUMMY,
                game.getLanguageManager()
                        .getComponent("scoreboard.objective-title")
        );

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        boards.put(player, board);
        objectives.put(player, objective);

        player.setScoreboard(board);
        updateBoard(player);
    }

    public void removePlayer(Player player) {
        boards.remove(player);
        objectives.remove(player);

        player.setScoreboard(
                Bukkit.getScoreboardManager().getMainScoreboard()
        );
    }
}