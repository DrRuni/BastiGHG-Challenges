package runi.myddns.challenges.core.world;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import runi.myddns.challenges.ChallengeMain;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class GameWorldManager {

    private final ChallengeMain plugin;
    private final WorldSourceManager worldSourceManager;

    public GameWorldManager(ChallengeMain plugin, WorldSourceManager worldSourceManager) {
        this.plugin = plugin;
        this.worldSourceManager = worldSourceManager;
    }

    // ============================================================
    // LOAD
    // ============================================================

    public World loadWorld(GameWorldDefinition definition) {
        World existing = Bukkit.getWorld(definition.worldName());
        if (existing != null) return existing;

        worldSourceManager.prepareWorld(definition);

        WorldCreator creator = new WorldCreator(definition.worldName());
        creator.environment(definition.environment());

        if (definition.generator() != null) {
            creator.generator(definition.generator().get());
        }

        World world = Bukkit.createWorld(creator);

        if (world == null) {
            plugin.getLogger().warning("Welt konnte nicht geladen werden: " + definition.worldName());
            return null;
        }

        plugin.getLogger().info("Welt geladen: " + definition.worldName());
        return world;
    }

    public void loadGameWorlds(String gameId) {
        for (GameWorldDefinition definition : getDefinitions(gameId)) {
            loadWorld(definition);
        }
    }

    public void preloadSpawnChunks(World world, int radius) {
        if (world == null) return;

        Location spawn = world.getSpawnLocation();
        int baseX = spawn.getBlockX() >> 4;
        int baseZ = spawn.getBlockZ() >> 4;

        Queue<int[]> queue = new ArrayDeque<>();

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                queue.add(new int[]{baseX + x, baseZ + z});
            }
        }

        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            for (int i = 0; i < 2; i++) {
                int[] chunk = queue.poll();

                if (chunk == null) {
                    task.cancel();
                    return;
                }

                if (!world.isChunkLoaded(chunk[0], chunk[1])) {
                    world.loadChunk(chunk[0], chunk[1], true);
                }
            }
        }, 1L, 1L);
    }

    // ============================================================
    // UNLOAD
    // ============================================================

    public boolean unloadWorld(GameWorldDefinition definition, boolean save) {
        World world = Bukkit.getWorld(definition.worldName());
        if (world == null) return true;

        if (!world.getPlayers().isEmpty()) return false;

        boolean result = Bukkit.unloadWorld(world, save);

        if (result) {
            plugin.getLogger().info("Welt entladen: " + definition.worldName());
        }

        return result;
    }

    public void unloadGameWorlds(String gameId, boolean save) {
        for (GameWorldDefinition definition : getDefinitions(gameId)) {
            unloadWorld(definition, save);
        }
    }

    // ============================================================
    // GET
    // ============================================================

    public World getWorld(String gameId, String worldId) {
        GameWorldDefinition definition = getDefinition(gameId, worldId);
        if (definition == null) return null;

        return Bukkit.getWorld(definition.worldName());
    }

    public GameWorldDefinition getDefinition(String gameId, String worldId) {
        for (GameWorldDefinition definition : GameWorldDefinition.ALL) {
            if (!definition.gameId().equalsIgnoreCase(gameId)) continue;
            if (!definition.worldId().equalsIgnoreCase(worldId)) continue;

            return definition;
        }

        return null;
    }

    public List<GameWorldDefinition> getDefinitions(String gameId) {
        return GameWorldDefinition.ALL.stream()
                .filter(definition -> definition.gameId().equalsIgnoreCase(gameId))
                .toList();
    }

    // ============================================================
    // CHECK
    // ============================================================

    public boolean isGameWorld(World world, String gameId) {
        if (world == null) return false;

        return getDefinitions(gameId).stream()
                .anyMatch(definition -> definition.worldName().equalsIgnoreCase(world.getName()));
    }

    public boolean isGamePlayer(Player player, String gameId) {
        return player != null && isGameWorld(player.getWorld(), gameId);
    }
}
