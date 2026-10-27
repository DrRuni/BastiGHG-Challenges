package runi.myddns.challenges.games.mobarmybattle.managers.event;

import io.papermc.paper.event.entity.SulfurCubeSwallowItemEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.scheduler.BukkitTask;
import runi.myddns.challenges.games.mobarmybattle.MobArmyBattleGame;
import java.util.*;

public final class ArenaMobTracker implements Listener {
    private record Entry(LivingEntity mob, String team, int wave) {}
    private final MobArmyBattleGame game;
    private final ArenaScoreboardManager board;
    private final Map<UUID, Entry> mobs = new HashMap<>();
    private final Map<String, Integer> waves = new HashMap<>();
    private final Map<String, Integer> totals = new HashMap<>();
    private BukkitTask task;
    private boolean running;

    public ArenaMobTracker(MobArmyBattleGame game, ArenaScoreboardManager board) {
        this.game = game;
        this.board = board;
        Bukkit.getPluginManager().registerEvents(this, game.getPlugin());
    }

    public void startBattle() {
        reset();
        running = true;
        task = Bukkit.getScheduler().runTaskTimer(game.getPlugin(), this::tick, 20L, 20L);
    }

    public void beginWave(String team) {
        waves.merge(team, 1, Integer::sum);
        totals.put(team, 0);
    }

    public void register(LivingEntity mob, String team) {
        if (!running || mob == null || !waves.containsKey(team)) return;
        if (mobs.putIfAbsent(mob.getUniqueId(), new Entry(mob, team, waves.get(team))) != null) return;
        totals.merge(team, 1, Integer::sum);
        mob.setPersistent(true);
        mob.setRemoveWhenFarAway(false);
    }

    public int total(String team) { return totals.getOrDefault(team, 0); }

    public int alive(String team) {
        int wave = waves.getOrDefault(team, -1);
        int count = 0;
        for (Entry entry : mobs.values()) {
            if (entry.team.equals(team) && entry.wave == wave
                    && entry.mob.isValid() && !entry.mob.isDead()) count++;
        }
        return count;
    }

    public boolean finished(String team) {
        return total(team) > 0 && alive(team) == 0;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSulfurSwallow(SulfurCubeSwallowItemEvent event) {
        if (!running) return;

        if (teamAt(event.getEntity().getLocation()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSulfurPickup(EntityPickupItemEvent event) {
        if (!running) return;

        if (!(event.getEntity() instanceof SulfurCube)) return;

        if (teamAt(event.getEntity().getLocation()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(EntityDeathEvent event) {
        if (!running) return;
        countOnce(event.getEntity().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!running) return;

        boolean cubeChild =
                event.getEntity() instanceof AbstractCubeMob
                        && event.getSpawnReason()
                        == CreatureSpawnEvent.SpawnReason.SLIME_SPLIT;

        boolean summonedVex =
                event.getEntity() instanceof Vex
                        && event.getSpawnReason()
                        == CreatureSpawnEvent.SpawnReason.SPELL;

        if (!cubeChild && !summonedVex) return;

        LivingEntity mob = event.getEntity();
        String team = teamAt(mob.getLocation());

        if (team == null || !waves.containsKey(team)) return;

        register(mob, team);
        game.getArenaManager().registerChildMob(mob, team);
    }

    private void countOnce(UUID id) {
        Entry entry = mobs.remove(id);
        if (entry != null) board.addKill(entry.team);
    }

    private void tick() {
        if (!running) return;
        for (UUID id : new HashSet<>(mobs.keySet())) {
            Entry entry = mobs.get(id);
            if (entry == null) continue;

            if (!entry.mob.isValid() || entry.mob.isDead()) {
                countOnce(id);
            } else if (entry.mob instanceof Vex vex) {
                keepVex(vex, entry.team);
            }
        }
    }

    private void keepVex(Vex vex, String team) {
        ArenaConfig.ArenaData arena = game.getArenaConfig().getActiveArena();
        if (arena == null) return;
        Location a = team.equals("Rot") ? arena.rotCorner1() : arena.blauCorner1();
        Location b = team.equals("Rot") ? arena.rotCorner2() : arena.blauCorner2();
        if (!inside(vex.getLocation(), a, b)) {
            var locations = team.equals("Rot") ? arena.rotMobSpawns() : arena.blauMobSpawns();
            if (!locations.isEmpty()) vex.teleport(locations.getFirst().clone());
        }
        Player target = null;
        double best = Double.MAX_VALUE;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!game.isMobArmyPlayer(player) || !player.getWorld().equals(vex.getWorld())) continue;
            if (!team.equalsIgnoreCase(game.getTeamManager().getPlayerTeam(player))) continue;
            double dist = player.getLocation().distanceSquared(vex.getLocation());
            if (dist < best) { best = dist; target = player; }
        }
        if (target != null) vex.setTarget(target);
    }

    private String teamAt(Location loc) {
        ArenaConfig.ArenaData arena = game.getArenaConfig().getActiveArena();
        if (arena == null || loc.getWorld() == null
                || !loc.getWorld().getName().equalsIgnoreCase(arena.world())) return null;
        if (inside(loc, arena.rotCorner1(), arena.rotCorner2())) return "Rot";
        if (inside(loc, arena.blauCorner1(), arena.blauCorner2())) return "Blau";
        return null;
    }

    private static boolean inside(Location loc, Location a, Location b) {
        return a != null && b != null && a.getWorld() != null && loc.getWorld() != null
                && a.getWorld().equals(loc.getWorld())
                && loc.getX() >= Math.min(a.getX(), b.getX()) && loc.getX() <= Math.max(a.getX(), b.getX())
                && loc.getY() >= Math.min(a.getY(), b.getY()) && loc.getY() <= Math.max(a.getY(), b.getY())
                && loc.getZ() >= Math.min(a.getZ(), b.getZ()) && loc.getZ() <= Math.max(a.getZ(), b.getZ());
    }

    public void reset() {
        running = false;
        if (task != null) { task.cancel(); task = null; }
        mobs.clear();
        waves.clear();
        totals.clear();
    }
}
