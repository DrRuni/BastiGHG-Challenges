package runi.myddns.challenges.games.mobarmybattle.managers.event;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class ScoreboardSwitcher {

    public enum BoardType {
        NONE,
        TEAM,
        ARENA
    }

    private final JavaPlugin plugin;
    private final TeamScoreboardManager teamBoardManager;
    private final ArenaScoreboardManager arenaBoardManager;

    private final Map<Player, BoardType> activeBoards = new HashMap<>();

    public ScoreboardSwitcher(JavaPlugin plugin,
                              TeamScoreboardManager teamBoardManager,
                              ArenaScoreboardManager arenaBoardManager) {
        this.plugin = plugin;
        this.teamBoardManager = teamBoardManager;
        this.arenaBoardManager = arenaBoardManager;
    }

    public void switchToTeam(Player player) {
        if (player == null) return;

        teamBoardManager.setBoard(player);
        activeBoards.put(player, BoardType.TEAM);
    }

    public void switchToArena(Player player) {
        if (player == null) return;

        arenaBoardManager.setBoard(player);
        activeBoards.put(player, BoardType.ARENA);
    }

    public BoardType getActiveBoard(Player player) {
        return activeBoards.getOrDefault(player, BoardType.NONE);
    }

    public void removePlayer(Player player) {
        activeBoards.remove(player);
        arenaBoardManager.removeBoard(player);

        player.setScoreboard(
                plugin.getServer()
                        .getScoreboardManager()
                        .getMainScoreboard()
        );
    }

    public void forceTeamForAll() {
        teamBoardManager.rebuildBoard();

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            activeBoards.remove(player);
            arenaBoardManager.removeBoard(player);
            teamBoardManager.setBoard(player);
            activeBoards.put(player, BoardType.TEAM);
        }
    }

    public void refreshScoreboard(Player player) {
        if (player == null || !player.isOnline()) return;

        String world = player.getWorld().getName();

        if (world.equalsIgnoreCase("world_mobarmy_arena")) {
            switchToArena(player);
            return;
        }

        if (world.equalsIgnoreCase("world_mobarmy_lobby")
                || world.equalsIgnoreCase("world_rot")
                || world.equalsIgnoreCase("world_blau")
                || world.equalsIgnoreCase("world_rot_nether")
                || world.equalsIgnoreCase("world_blau_nether")) {

            teamBoardManager.updateBoard();
            switchToTeam(player);
            return;
        }

        removePlayer(player);
    }
}
