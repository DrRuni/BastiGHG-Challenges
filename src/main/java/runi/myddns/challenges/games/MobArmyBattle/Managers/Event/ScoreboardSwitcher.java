package runi.myddns.challenges.games.MobArmyBattle.Managers.Event;

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
        if (getActiveBoard(player) == BoardType.TEAM) return;

        teamBoardManager.setBoard(player);
        activeBoards.put(player, BoardType.TEAM);
    }

    public void switchToArena(Player player) {
        if (getActiveBoard(player) == BoardType.ARENA) return;

        arenaBoardManager.setBoard(player);
        activeBoards.put(player, BoardType.ARENA);
    }

    public BoardType getActiveBoard(Player player) {
        return activeBoards.getOrDefault(player, BoardType.NONE);
    }

    public void removePlayer(Player player) {
        activeBoards.remove(player);
        arenaBoardManager.removeBoard(player);

        if (plugin.getServer().getScoreboardManager() != null) {
            player.setScoreboard(
                    plugin.getServer()
                            .getScoreboardManager()
                            .getMainScoreboard()
            );
        }
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
}
