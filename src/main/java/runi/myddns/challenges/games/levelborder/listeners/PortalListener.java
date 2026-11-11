package runi.myddns.challenges.games.levelborder.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.PortalCreateEvent;
import runi.myddns.challenges.games.levelborder.LevelBorderGame;
import runi.myddns.challenges.games.levelborder.manager.BorderDataManager;
import runi.myddns.challenges.games.levelborder.manager.PortalManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PortalListener implements Listener {

    private final LevelBorderGame game;
    private final PortalManager portalManager;
    private final BorderDataManager borderData;
    private final Map<UUID, Long> lastPortalUse = new HashMap<>();
    private static final long COOLDOWN_MS = 3000;

    public PortalListener(LevelBorderGame game, PortalManager portalManager, BorderDataManager borderData) {
        this.game = game;
        this.portalManager = portalManager;
        this.borderData = borderData;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPortal(PlayerPortalEvent e) {
        if (e.getCause() != PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) return;

        Player p = e.getPlayer();
        if (!game.isLevelBorderPlayer(p)) return;

        UUID id = p.getUniqueId();
        long now = System.currentTimeMillis();
        Long last = lastPortalUse.get(id);
        if (last != null && now - last < COOLDOWN_MS) return;

        World fromWorld = e.getFrom().getWorld();
        if (fromWorld == null) return;

        Location portalCenter = portalManager.findPortalCenter(e.getFrom());
        if (portalCenter == null) return;
        portalManager.registerPortal(portalCenter);

        Location target = null;

        if (fromWorld.getEnvironment() == World.Environment.NORMAL) {
            World nether = Bukkit.getWorld(fromWorld.getName() + "_nether");
            if (nether == null) return;

            Location search = portalCenter.clone();
            search.setWorld(nether);
            search.setX(portalCenter.getX());
            search.setZ(portalCenter.getZ());

            target = portalManager.getNearestPortal(search);
            if (target == null) target = portalManager.createNetherPortal(nether, portalCenter);
        } else if (fromWorld.getEnvironment() == World.Environment.NETHER) {
            World overworld = Bukkit.getWorld("world_levelborder");
            if (overworld == null) return;

            Location search = portalCenter.clone();
            search.setWorld(overworld);
            search.setX(clampToBorder(portalCenter.getBlockX(), true));
            search.setZ(clampToBorder(portalCenter.getBlockZ(), false));

            target = portalManager.getNearestPortal(search);
            if (target == null) target = portalManager.createOverworldPortal(overworld, portalCenter);
        }

        if (target == null) return;

        e.setCancelled(true);
        e.setCanCreatePortal(false);
        lastPortalUse.put(id, now);
        Location finalTarget = target.clone();

        Bukkit.getScheduler().runTask(game.getPlugin(), () -> {
            if (!p.isOnline()) return;
            p.teleport(finalTarget, PlayerTeleportEvent.TeleportCause.PLUGIN);
            p.setPortalCooldown(200);
        });
    }

    private int clampToBorder(int value, boolean xAxis) {
        Location center = borderData.getCenter();
        if (center == null) return value;

        double radius = Math.max(0, borderData.getSize() / 2.0 - 5);
        double coordinate = xAxis ? center.getX() : center.getZ();
        return (int) Math.clamp(value, coordinate - radius, coordinate + radius);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPortalCreateBorder(PortalCreateEvent e) {
        Location center = borderData.getCenter();
        if (center == null) return;

        double radius = borderData.getSize() / 2.0 - 1;
        for (BlockState state : e.getBlocks()) {
            Location loc = state.getLocation();
            World world = loc.getWorld();
            if (world == null || !game.isLevelBorderWorld(world)) continue;
            if (world.getEnvironment() != World.Environment.NORMAL
                    && world.getEnvironment() != World.Environment.NETHER) continue;

            double dx = Math.abs(loc.getX() - center.getX());
            double dz = Math.abs(loc.getZ() - center.getZ());
            if (dx > radius || dz > radius) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPortalCreated(PortalCreateEvent e) {
        if (e.getReason() != PortalCreateEvent.CreateReason.FIRE
                && e.getReason() != PortalCreateEvent.CreateReason.NETHER_PAIR) return;
        if (!game.isLevelBorderWorld(e.getWorld())) return;

        Location portalBlock = null;
        for (BlockState state : e.getBlocks()) {
            if (state.getType() == Material.NETHER_PORTAL) {
                portalBlock = state.getLocation();
                break;
            }
        }
        if (portalBlock == null) return;

        Location location = portalBlock.clone();
        Bukkit.getScheduler().runTaskLater(game.getPlugin(), () -> {
            Location actual = portalManager.findPortalCenter(location);
            if (actual != null) portalManager.registerPortal(actual);
        }, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPortalBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        if (!game.isLevelBorderWorld(block.getWorld())) return;
        if (block.getType() != Material.OBSIDIAN && block.getType() != Material.NETHER_PORTAL) return;
        scheduleCleanup(block.getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFluidFlow(BlockFromToEvent e) {
        Block to = e.getToBlock();
        if (!game.isLevelBorderWorld(to.getWorld())) return;
        if (to.getType() != Material.NETHER_PORTAL) return;
        scheduleCleanup(to.getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        if (!game.isLevelBorderWorld(e.getBlockClicked().getWorld())) return;
        if (e.getBucket() != Material.WATER_BUCKET && e.getBucket() != Material.LAVA_BUCKET) return;
        scheduleCleanup(e.getBlockClicked().getRelative(e.getBlockFace()).getLocation());
    }

    private void scheduleCleanup(Location changedBlock) {

        Bukkit.getScheduler().runTaskLater(game.getPlugin(),
                () -> portalManager.removeInvalidPortalsNear(changedBlock, 24.0), 2L);

        Bukkit.getScheduler().runTaskLater(game.getPlugin(),
                () -> portalManager.removeInvalidPortalsNear(changedBlock, 24.0), 10L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        lastPortalUse.remove(e.getPlayer().getUniqueId());
    }
}
