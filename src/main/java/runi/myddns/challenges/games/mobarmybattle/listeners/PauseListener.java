package runi.myddns.challenges.games.mobarmybattle.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import runi.myddns.challenges.games.mobarmybattle.MobArmyBattleGame;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class PauseListener implements Listener {

    private final MobArmyBattleGame game;

    private final Map<Player, Location> freezeLocations = new HashMap<>();

    public PauseListener(MobArmyBattleGame game) {
        this.game = game;
    }

    private boolean isPaused() {
        return game.getEventResume().isEventStarted()
                && game.getEventResume().isEventPaused();
    }

    private boolean isTeamWorld(Player player) {
        String worldName = player.getWorld().getName().toLowerCase();
        return worldName.contains("rot")
                || worldName.contains("blau");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent event) {

        if (isPaused()
                && event.getEntity() instanceof Player player) {

            player.setInvulnerable(true);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {

        if (isPaused()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {

        if (isPaused()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {

        if (!isPaused()) {
            return;
        }

        if (event.getClickedBlock() != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        if (!isPaused()) {
            return;
        }

        Player player = event.getPlayer();

        freezeLocations.put(
                player,
                player.getLocation()
        );
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();
        freezeLocations.remove(player);

        if (!game.isMobArmyPlayer(player)) return;

        checkLastPlayer(player);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {

        if (!game.isMobArmyPlayer(event.getPlayer())
                && isMobArmyWorld(event.getFrom())) {

            checkLastPlayer(null);
        }
    }

    private boolean isMobArmyWorld(World world) {
        return game.getPlugin()
                .getGameWorldManager()
                .isGameWorld(world, game.getId());
    }

    private void checkLastPlayer(Player leavingPlayer) {

        Bukkit.getScheduler().runTask(
                game.getPlugin(),
                () -> {

                    if (!game.isLoaded()
                            || game.getTimerManager() == null
                            || !game.getTimerManager().isRunning()
                            || !game.getEventResume().isEventStarted()) {
                        return;
                    }

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (!player.equals(leavingPlayer)
                                && game.isMobArmyPlayer(player)) {
                            return;
                        }
                    }

                    game.getTimerManager().pauseTimer();

                    game.getEventResume().saveTimerState(
                            game.getTimerManager().getTimeInSeconds(),
                            game.getTimerManager().isForward()
                    );
                }
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent event) {

        if (!isPaused()) {
            return;
        }

        Player player = event.getPlayer();

        if (!isTeamWorld(player)) {
            return;
        }

        freezeLocations.putIfAbsent(
                player,
                event.getFrom()
        );

        Location base = freezeLocations.get(player);
        Location to = event.getTo();

        if (base == null) {
            return;
        }

        double dx = to.getX() - base.getX();
        double dz = to.getZ() - base.getZ();

        double max = 2.5;

        if (Math.abs(dx) > max
                || Math.abs(dz) > max) {

            Location corrected = to.clone();

            corrected.setX(
                    base.getX()
                            + Math.clamp(dx, -max, max)
            );

            corrected.setZ(
                    base.getZ()
                            + Math.clamp(dz, -max, max)
            );

            event.setTo(corrected);
        }
    }
}