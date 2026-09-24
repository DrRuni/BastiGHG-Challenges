package runi.myddns.challenges.games.LevelBlock.Listeners;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import runi.myddns.challenges.games.LevelBlock.LevelBlockGame;

public class BorderBlockListener implements Listener {

    private final LevelBlockGame game;

    public BorderBlockListener(LevelBlockGame game) {
        this.game = game;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        if (!game.isLevelBlockPlayer(event.getPlayer())) {
            return;
        }

        Location location =
                event.getBlock().getLocation();

        game.getPlugin()
                .getServer()
                .getScheduler()
                .runTask(
                        game.getPlugin(),
                        () -> game.getBorderManager()
                                .getDisplayManager()
                                .updatePlayerView(
                                        event.getPlayer(),
                                        event.getPlayer().getLocation()
                                )
                );
    }
}
