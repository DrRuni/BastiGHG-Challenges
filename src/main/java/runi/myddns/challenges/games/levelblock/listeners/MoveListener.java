package runi.myddns.challenges.games.levelblock.listeners;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import runi.myddns.challenges.games.levelblock.LevelBlockGame;
import runi.myddns.challenges.games.levelblock.manager.BorderManager;

public class MoveListener implements Listener {

    private final LevelBlockGame game;

    public MoveListener(LevelBlockGame game) {
        this.game = game;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {

        Player player = event.getPlayer();

        if (!game.isLevelBlockPlayer(player)) return;

        Location from = event.getFrom();
        Location to = event.getTo();

        BorderManager border =
                game.getBorderManager();

        if (!border.isInitialized(
                player.getWorld()
        )) {
            return;
        }

        boolean changedBlock =
                from.getBlockX() != to.getBlockX()
                        || from.getBlockY() != to.getBlockY()
                        || from.getBlockZ() != to.getBlockZ();

        if (changedBlock) {

            border.getDisplayManager().updatePlayerView(
                    player,
                    to
            );

            if (from.getBlockY() != to.getBlockY()) {

                border.getLineManager()
                        .refreshAll();
            }
        }

        if (from.getBlockX() == to.getBlockX()
                && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        if (border.isInside(to)) {
            return;
        }

        int targetX = to.getBlockX();
        int targetZ = to.getBlockZ();

        if (!border.isAdjacentToUnlocked(
                to.getWorld(),
                targetX,
                targetZ
        )) {

            Location blocked = to.clone();
            blocked.setX(from.getX());
            blocked.setZ(from.getZ());
            event.setTo(blocked);
            return;
        }

        if (!border.canExpand(player)) {

            Location blocked = to.clone();

            blocked.setX(from.getX());
            blocked.setZ(from.getZ());

            event.setTo(blocked);

            player.sendActionBar(
                    game.getLanguageManager()
                            .getComponent(
                                    "move-listener.need-level"
                            )
            );

            return;
        }

        boolean unlocked =
                border.unlockBlock(
                        to.getWorld(),
                        targetX,
                        targetZ
                );

        if (!unlocked) {
            Location blocked = to.clone();
            blocked.setX(from.getX());
            blocked.setZ(from.getZ());
            event.setTo(blocked);
            return;
        }

        border.consumeLevel(player);

        player.playSound(
                player.getLocation(),
                Sound.BLOCK_AMETHYST_BLOCK_CHIME,
                0.65f,
                1.35f
        );

        player.sendActionBar(
                game.getLanguageManager()
                        .getComponent(
                                "move-listener.block-unlocked"
                        )
        );
    }
}