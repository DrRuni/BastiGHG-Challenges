package runi.myddns.challenges.games.levelblock.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import runi.myddns.challenges.games.levelblock.LevelBlockGame;

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
