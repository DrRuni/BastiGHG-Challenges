package runi.myddns.challenges.games.MobArmyBattle.Managers.Event;

import org.bukkit.*;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import runi.myddns.challenges.games.MobArmyBattle.MobArmyBattleGame;

import java.util.*;

public class ArenaBuildProtectionManager implements Listener {

    private final MobArmyBattleGame game;
    private final Map<String, Set<BlockPos>> placedBlocks = new HashMap<>();

    private record BlockPos(String world, int x, int y, int z) { }

    private BlockPos toPos(Location loc) {
        return new BlockPos(
                loc.getWorld().getName().toLowerCase(),
                loc.getBlockX(),
                loc.getBlockY(),
                loc.getBlockZ()
        );
    }

    public ArenaBuildProtectionManager(MobArmyBattleGame game) {
        this.game = game;
    }

    private boolean isAllowedNaturalBlock(Material type) {
        if (type == null) return false;

        if (Tag.FLOWERS.isTagged(type)) return true;
        if (Tag.SAPLINGS.isTagged(type)) return true;
        if (Tag.CROPS.isTagged(type)) return true;

        return switch (type) {
            // Gras / Bodenpflanzen
            case SHORT_GRASS,
                 SHORT_DRY_GRASS,
                 TALL_GRASS,
                 TALL_DRY_GRASS,
                 FERN,
                 LARGE_FERN,
                 DEAD_BUSH,
                 AZALEA,
                 FLOWERING_AZALEA,
                 BROWN_MUSHROOM,
                 RED_MUSHROOM,
                 SWEET_BERRY_BUSH,
                 VINE,
                 CAVE_VINES,
                 CAVE_VINES_PLANT,
                 TWISTING_VINES,
                 TWISTING_VINES_PLANT,
                 WEEPING_VINES,
                 WEEPING_VINES_PLANT,
                 LILY_PAD,
                 SEAGRASS,
                 TALL_SEAGRASS,
                 KELP,
                 KELP_PLANT,
                 SMALL_DRIPLEAF,
                 BIG_DRIPLEAF,
                 BIG_DRIPLEAF_STEM,
                 SUGAR_CANE,
                 BAMBOO,
                 BAMBOO_SAPLING,
                 COCOA,
                 MELON_STEM,
                 ATTACHED_MELON_STEM,
                 PUMPKIN_STEM,
                 ATTACHED_PUMPKIN_STEM,
                 TORCHFLOWER_CROP,
                 PITCHER_CROP,
                 TRIPWIRE,
                 NETHER_WART,
                 CRIMSON_ROOTS,
                 WARPED_ROOTS,
                 NETHER_SPROUTS,
                 CRIMSON_FUNGUS,
                 WARPED_FUNGUS,
                 HANGING_ROOTS,
                 MANGROVE_PROPAGULE,
                 CHORUS_PLANT,
                 CHORUS_FLOWER -> true;

            default -> false;
        };
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
        if (game.getEventResume().isEventStarted()
                && game.getEventResume().isEventPaused()) {
            e.setCancelled(true);
            return;
        }

        Player p = e.getPlayer();
        Location loc = e.getBlock().getLocation();
        Material type = e.getBlock().getType();

        if (loc.getWorld() == null) {
            e.setCancelled(true);
            return;
        }

        String worldName = loc.getWorld().getName().toLowerCase();

        ArenaConfig.ArenaData arena =
                game.getArenaConfig().getActiveArena();

        if (arena == null) {
            return;
        }

        // Nur aktive Arena-Welt behandeln
        if (!worldName.equalsIgnoreCase(arena.world())) {
            return;
        }

        String team = getTeam(p);

        // Naturblöcke in Arena-Zonen dürfen entfernt werden, aber ohne Drops/XP
        if (isAllowedNaturalBlock(type)) {
            if (!isInsideAnyTeamArea(loc, arena)) {
                e.setCancelled(true);
                return;
            }

            e.setDropItems(false);
            e.setExpToDrop(0);
            return;
        }

        if (team == null) {
            e.setCancelled(true);
            return;
        }

        if (!isInsideAnyTeamArea(loc, arena)) {
            e.setCancelled(true);
            return;
        }

        if (!isInsideTeamArea(loc, team, arena)) {
            e.setCancelled(true);
            return;
        }

        BlockPos pos = toPos(loc);
        Set<BlockPos> placed = placedBlocks.get(team.toLowerCase());

        // Nur selbst gesetzte Blöcke dürfen wieder abgebaut werden
        if (placed == null || !placed.remove(pos)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (game.getEventResume().isEventStarted()
                && game.getEventResume().isEventPaused()) {
            e.setCancelled(true);
            return;
        }

        Player p = e.getPlayer();
        Location loc = e.getBlock().getLocation();

        if (loc.getWorld() == null) {
            e.setCancelled(true);
            return;
        }

        String worldName = loc.getWorld().getName().toLowerCase();

        ArenaConfig.ArenaData arena =
                game.getArenaConfig().getActiveArena();

        if (arena == null) {
            return;
        }

        // Nur aktive Arena-Welt behandeln
        if (!worldName.equalsIgnoreCase(arena.world())) {
            return;
        }

        String team = getTeam(p);

        if (team == null) {
            e.setCancelled(true);
            return;
        }

        if (!isInsideAnyTeamArea(loc, arena)) {
            e.setCancelled(true);
            return;
        }

        if (!isInsideTeamArea(loc, team, arena)) {
            e.setCancelled(true);
            return;
        }

        placedBlocks.computeIfAbsent(
                team.toLowerCase(Locale.ROOT),
                _ -> new HashSet<>()
        ).add(toPos(loc));
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent e) {
        World w = e.getLocation().getWorld();

        if (isProtectedMobArmyWorld(w)) {
            e.blockList().clear();
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent e) {
        World w = e.getBlock().getWorld();

        if (isProtectedMobArmyWorld(w)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onFireSpread(BlockSpreadEvent e) {
        World w = e.getBlock().getWorld();

        if (isProtectedMobArmyWorld(w) && e.getSource().getType() == Material.FIRE) {
            e.setCancelled(true);
        }
    }

    private boolean isProtectedMobArmyWorld(World world) {
        if (world == null) return false;

        ArenaConfig.ArenaData arena =
                game.getArenaConfig().getActiveArena();

        return arena != null
                && world.getName().equalsIgnoreCase(
                arena.world()
        );
    }

    private boolean isInsideTeamArea(Location loc, String team, ArenaConfig.ArenaData arena) {
        if (!loc.getWorld().getName().equalsIgnoreCase(arena.world())) return false;
        Location c1, c2;

        if (team.equalsIgnoreCase("rot")) {
            c1 = arena.rotCorner1();
            c2 = arena.rotCorner2();
        } else if (team.equalsIgnoreCase("blau")) {
            c1 = arena.blauCorner1();
            c2 = arena.blauCorner2();
        } else return false;

        if (c1 == null || c2 == null) return false;

        double x = loc.getX(), y = loc.getY(), z = loc.getZ();
        return x >= Math.min(c1.getX(), c2.getX()) && x <= Math.max(c1.getX(), c2.getX()) &&
                y >= Math.min(c1.getY(), c2.getY()) && y <= Math.max(c1.getY(), c2.getY()) &&
                z >= Math.min(c1.getZ(), c2.getZ()) && z <= Math.max(c1.getZ(), c2.getZ());
    }

    private boolean isInsideAnyTeamArea(Location loc, ArenaConfig.ArenaData arena) {
        return isInside(loc, arena.rotCorner1(), arena.rotCorner2())
                || isInside(loc, arena.blauCorner1(), arena.blauCorner2());
    }

    private boolean isInside(Location loc, Location c1, Location c2) {
        if (c1 == null || c2 == null) return false;
        double x = loc.getX(), y = loc.getY(), z = loc.getZ();
        return x >= Math.min(c1.getX(), c2.getX()) && x <= Math.max(c1.getX(), c2.getX()) &&
                y >= Math.min(c1.getY(), c2.getY()) && y <= Math.max(c1.getY(), c2.getY()) &&
                z >= Math.min(c1.getZ(), c2.getZ()) && z <= Math.max(c1.getZ(), c2.getZ());
    }

    private String getTeam(Player player) {

        String team =
                game.getTeamManager()
                        .getPlayerTeam(player);

        if (team == null
                || team.equalsIgnoreCase("Kein Team")) {
            return null;
        }

        return team.toLowerCase(Locale.ROOT);
    }

    public void clearTeamData(String team) {
        placedBlocks.remove(team.toLowerCase());
    }
}