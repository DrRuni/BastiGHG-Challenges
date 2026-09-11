package runi.myddns.challenges.games.mobarmywars.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import runi.myddns.challenges.games.mobarmywars.Managers.Event.ScoreboardSwitcher;
import runi.myddns.challenges.games.mobarmywars.MobArmyWarsGame;

public class ScoreboardSwitchListener implements Listener {

    private final MobArmyWarsGame game;

    public ScoreboardSwitchListener(MobArmyWarsGame game) {
        this.game = game;
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        game.getPlugin().getServer().getScheduler().runTaskLater(game.getPlugin(), () -> {
            if (!player.isOnline()) return;
            switchScoreboard(player);
        }, 1L);
    }

    private void switchScoreboard(Player player) {
        String worldName = player.getWorld().getName().toLowerCase();

        if (isArenaBoardWorld(worldName)) {
            game.getScoreboardSwitcher().switchToArena(player);
            return;
        }

        if (worldName.equals("world_rot") || worldName.equals("world_blau")) {
            game.getScoreboardSwitcher().switchToTeam(player);
            return;
        }

        if (worldName.equals("world_mobarmy_lobby")) {

            if (game.getScoreboardSwitcher().getActiveBoard(player)
                    != ScoreboardSwitcher.BoardType.NONE) {

                game.getScoreboardSwitcher().switchToTeam(player);
            }

            return;
        }

        game.getScoreboardSwitcher().removePlayer(player);
    }

    private boolean isArenaBoardWorld(String worldName) {
        return worldName.equals("world_mobarmy_arena");
    }

    private boolean isTeamBoardWorld(String worldName) {
        return worldName.equals("world_mobarmy_lobby")
                || worldName.equals("world_rot")
                || worldName.equals("world_blau");
    }

}