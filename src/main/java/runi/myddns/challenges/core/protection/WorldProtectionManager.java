package runi.myddns.challenges.core.protection;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.world.WorldLoadEvent;
import runi.myddns.challenges.ChallengeMain;

import java.util.Set;

public class WorldProtectionManager implements Listener {

    private static final String CENTRAL_LOBBY =
            "world_challenges_lobby";

    private static final String MOBARMY_LOBBY =
            "world_mobarmy_lobby";

    private static final Set<String> PROTECTED_WORLDS =
            Set.of(
                    CENTRAL_LOBBY,
                    MOBARMY_LOBBY
            );

    private final ChallengeMain plugin;

    public WorldProtectionManager(ChallengeMain plugin) {
        this.plugin = plugin;
    }

    public void applyLoadedWorlds() {

        for (World world : Bukkit.getWorlds()) {

            if (isProtectedLobby(world)) {
                applyWorldSettings(world);
            }
        }
    }

    private void applyWorldSettings(World world) {

        world.setGameRule(
                GameRule.DO_MOB_SPAWNING,
                false
        );

        world.setGameRule(
                GameRule.MOB_GRIEFING,
                false
        );

        world.setGameRule(
                GameRule.DO_FIRE_TICK,
                false
        );

        world.setGameRule(
                GameRule.DO_WEATHER_CYCLE,
                false
        );

        world.setStorm(false);
        world.setThundering(false);

        /*
         * Nur die zentrale Challenge-Lobby
         * bleibt dauerhaft Nacht.
         */
        if (world.getName().equalsIgnoreCase(CENTRAL_LOBBY)) {

            world.setGameRule(
                    GameRule.DO_DAYLIGHT_CYCLE,
                    false
            );

            world.setTime(18000);
        }
    }

    private boolean isProtectedLobby(World world) {

        if (world == null) {
            return false;
        }

        return PROTECTED_WORLDS.contains(
                world.getName().toLowerCase()
        );
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {

        if (isProtectedLobby(event.getWorld())) {
            applyWorldSettings(event.getWorld());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        if (isProtectedLobby(event.getBlock().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {

        if (isProtectedLobby(event.getBlock().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {

        if (isProtectedLobby(event.getPlayer().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {

        if (isProtectedLobby(event.getPlayer().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {

        if (isProtectedLobby(event.getLocation().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {

        if (isProtectedLobby(event.getLocation().getWorld())) {
            event.blockList().clear();
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {

        if (isProtectedLobby(event.getBlock().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFireSpread(BlockSpreadEvent event) {

        if (isProtectedLobby(event.getBlock().getWorld())) {
            event.setCancelled(true);
        }
    }
}
