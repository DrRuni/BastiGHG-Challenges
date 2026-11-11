package runi.myddns.challenges.games.levelborder.manager;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import runi.myddns.challenges.games.levelborder.LevelBorderGame;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PortalManager {

    private final BorderDataManager data;
    private final File portalDir;
    private final Logger logger;
    private final Map<World, List<Location>> portalCache = new HashMap<>();

    public PortalManager(LevelBorderGame game, BorderDataManager data) {
        this.data = data;
        this.logger = game.getPlugin().getLogger();
        this.portalDir = new File(game.getPlugin().getDataFolder(), "games/levelborder/portals");
        if (!portalDir.exists() && !portalDir.mkdirs()) {
            logger.severe("LevelBorder: Portalordner konnte nicht erstellt werden: "
                    + portalDir.getAbsolutePath());
        }
    }

    public void loadAllPortals() {
        for (World world : Bukkit.getWorlds()) {
            loadWorldPortals(world);
        }
    }

    private void loadWorldPortals(World world) {
        File file = new File(portalDir, world.getName() + ".yml");
        List<Location> list = new ArrayList<>();
        if (file.exists()) {
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            for (String key : cfg.getKeys(false)) {
                list.add(new Location(world,
                        cfg.getDouble(key + ".x"),
                        cfg.getDouble(key + ".y"),
                        cfg.getDouble(key + ".z")));
            }
        }
        portalCache.put(world, list);
    }

    public Location getNearestPortal(Location target) {
        World world = target.getWorld();
        if (world == null) return null;

        List<Location> portals = portalCache.get(world);
        if (portals == null || portals.isEmpty()) return null;

        Location best = null;
        double bestDist = Double.MAX_VALUE;
        boolean changed = false;

        Iterator<Location> iterator = portals.iterator();
        while (iterator.hasNext()) {
            Location stored = iterator.next();
            if (!isActivePortal(stored, true)) {
                iterator.remove();
                changed = true;
                continue;
            }

            double distance = stored.distanceSquared(target);
            if (distance < bestDist) {
                bestDist = distance;
                best = stored;
            }
        }

        if (changed) saveWorldPortals(world);
        return best != null ? best.clone() : null;
    }

    public void registerPortal(Location center) {
        if (center == null || center.getWorld() == null) return;
        if (!isActivePortal(center, true)) return;

        center = new Location(center.getWorld(),
                center.getBlockX() + 0.5,
                center.getBlockY(),
                center.getBlockZ() + 0.5);

        World world = center.getWorld();
        List<Location> list = portalCache.computeIfAbsent(world, _ -> new ArrayList<>());

        for (Location stored : list) {
            if (stored.getBlockX() == center.getBlockX()
                    && stored.getBlockY() == center.getBlockY()
                    && stored.getBlockZ() == center.getBlockZ()) return;
        }

        list.add(center.clone());
        saveWorldPortals(world);
    }

    public void removeInvalidPortalsNear(Location changedBlock, double radius) {
        World world = changedBlock.getWorld();
        if (world == null) return;

        List<Location> portals = portalCache.get(world);
        if (portals == null || portals.isEmpty()) return;

        boolean changed = false;
        double squaredRadius = radius * radius;
        Iterator<Location> iterator = portals.iterator();

        while (iterator.hasNext()) {
            Location stored = iterator.next();
            if (stored.distanceSquared(changedBlock) > squaredRadius) continue;
            if (!isActivePortal(stored, false)) {
                iterator.remove();
                changed = true;
            }
        }
        if (changed) saveWorldPortals(world);
    }

    private boolean isActivePortal(Location center, boolean loadChunk) {
        World world = center.getWorld();
        if (world == null) return false;

        int x = center.getBlockX();
        int y = center.getBlockY();
        int z = center.getBlockZ();

        if (!world.isChunkLoaded(x >> 4, z >> 4) && !loadChunk) return true;
        if (loadChunk) world.getChunkAt(x >> 4, z >> 4);

        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                int py = y + dy;
                if (py < world.getMinHeight() || py >= world.getMaxHeight()) continue;
                for (int dz = -2; dz <= 2; dz++) {
                    int px = x + dx;
                    int pz = z + dz;
                    if (!world.isChunkLoaded(px >> 4, pz >> 4)) {
                        if (!loadChunk) continue;
                        world.getChunkAt(px >> 4, pz >> 4);
                    }
                    if (world.getBlockAt(px, py, pz).getType() == Material.NETHER_PORTAL) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Location findPortalCenter(Location start) {
        World world = start.getWorld();
        if (world == null) return null;

        int bx = start.getBlockX();
        int bz = start.getBlockZ();

        for (int y = start.getBlockY() - 2; y <= start.getBlockY() + 2; y++) {
            if (y < world.getMinHeight() || y >= world.getMaxHeight()) continue;
            int minX = bx, maxX = bx, minZ = bz, maxZ = bz;
            boolean found = false;

            for (int dx = -4; dx <= 4; dx++) {
                for (int dz = -4; dz <= 4; dz++) {
                    int x = bx + dx;
                    int z = bz + dz;
                    if (!world.isChunkLoaded(x >> 4, z >> 4)) continue;
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() == Material.NETHER_PORTAL) {
                        found = true;
                        minX = Math.min(minX, x);
                        maxX = Math.max(maxX, x);
                        minZ = Math.min(minZ, z);
                        maxZ = Math.max(maxZ, z);
                    }
                }
            }
            if (found) {
                return new Location(world,
                        (minX + maxX) / 2.0 + 0.5, y,
                        (minZ + maxZ) / 2.0 + 0.5);
            }
        }
        return null;
    }

    public Location createOverworldPortal(World overworld, Location fromNether) {
        int x = clampToBorder(fromNether.getBlockX() * 8, true);
        int z = clampToBorder(fromNether.getBlockZ() * 8, false);
        Chunk chunk = overworld.getChunkAt(x >> 4, z >> 4);
        if (!chunk.isLoaded()) chunk.load();
        int y = findSafeOverworldY(overworld, x, z);
        clearArea(overworld, x, y, z);
        buildPortal(overworld, x, y, z);
        Location center = new Location(overworld, x + 0.5, y + 1, z + 0.5);
        registerPortal(center);
        return center;
    }

    public Location createNetherPortal(World nether, Location fromOverworld) {
        int x = fromOverworld.getBlockX();
        int z = fromOverworld.getBlockZ();
        Chunk chunk = nether.getChunkAt(x >> 4, z >> 4);
        if (!chunk.isLoaded()) chunk.load();
        int y = findSafeNetherY(nether, x, z);
        clearArea(nether, x, y, z);
        buildPortal(nether, x, y, z);
        Location center = new Location(nether, x + 0.5, y + 1, z + 0.5);
        registerPortal(center);
        return center;
    }

    private int clampToBorder(int value, boolean xAxis) {
        Location center = data.getCenter();
        if (center == null) return value;
        double radius = Math.max(0, data.getSize() / 2.0 - 5);
        double coordinate = xAxis ? center.getX() : center.getZ();
        return (int) Math.clamp(value, coordinate - radius, coordinate + radius);
    }

    private void buildPortal(World world, int x, int y, int z) {
        for (int dx = -1; dx <= 2; dx++) {
            for (int dy = 0; dy <= 4; dy++) {
                Block block = world.getBlockAt(x + dx, y + dy, z);
                if (dx == -1 || dx == 2 || dy == 0 || dy == 4) {
                    block.setType(Material.OBSIDIAN, false);
                } else {
                    block.setType(Material.AIR, false);
                }
            }
        }
        for (int dx = 0; dx <= 1; dx++) {
            for (int dy = 1; dy <= 3; dy++) {
                world.getBlockAt(x + dx, y + dy, z).setType(Material.NETHER_PORTAL, false);
            }
        }
    }

    private void clearArea(World world, int x, int y, int z) {
        for (int dx = -1; dx <= 2; dx++) {
            for (int dy = 0; dy <= 4; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    world.getBlockAt(x + dx, y + dy, z + dz).setType(Material.AIR);
                }
            }
        }
    }

    private int findSafeNetherY(World nether, int x, int z) {
        for (int y = 32; y <= 96; y++) {
            if (!nether.isChunkLoaded(x >> 4, z >> 4)) continue;
            Block feet = nether.getBlockAt(x, y, z);
            Block head = nether.getBlockAt(x, y + 1, z);
            Block ground = nether.getBlockAt(x, y - 1, z);
            if (!feet.getType().isAir() || !head.getType().isAir()) continue;
            Material groundType = ground.getType();
            if (groundType == Material.LAVA || groundType == Material.BEDROCK) continue;
            return y;
        }
        return 64;
    }

    private int findSafeOverworldY(World world, int x, int z) {
        int top = world.getHighestBlockYAt(x, z);
        int minY = world.getMinHeight() + 5;
        for (int y = top; y > minY; y--) {
            if (portalFits(world, x, y, z, true)) return y;
            if (portalFits(world, x, y, z, false)) return y;
        }
        return world.getSeaLevel();
    }

    private boolean portalFits(World world, int x, int y, int z, boolean xAxis) {
        for (int dx = 0; dx < 2; dx++) {
            for (int dy = 1; dy <= 3; dy++) {
                int px = x + (xAxis ? dx : 0);
                int pz = z + (xAxis ? 0 : dx);
                if (!world.getBlockAt(px, y + dy, pz).getType().isAir()) return false;
            }
        }
        for (int dx = 0; dx < 2; dx++) {
            int px = x + (xAxis ? dx : 0);
            int pz = z + (xAxis ? 0 : dx);
            Block ground = world.getBlockAt(px, y, pz);
            Material type = ground.getType();
            if (type == Material.BEDROCK || ground.isLiquid() || !type.isSolid()) return false;
        }
        return true;
    }

    private void saveWorldPortals(World world) {
        File file = new File(portalDir, world.getName() + ".yml");
        YamlConfiguration cfg = new YamlConfiguration();
        List<Location> list = portalCache.get(world);
        if (list != null) {
            for (Location stored : list) {
                String key = stored.getBlockX() + "_" + stored.getBlockY() + "_" + stored.getBlockZ();
                cfg.set(key + ".x", stored.getX());
                cfg.set(key + ".y", stored.getY());
                cfg.set(key + ".z", stored.getZ());
            }
        }
        try {
            cfg.save(file);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "LevelBorder: Fehler beim Speichern von " + file.getAbsolutePath(), e);
        }
    }

    public void clearPortalWorldData() {
        portalCache.clear();
        if (portalDir.exists()) {
            File[] files = portalDir.listFiles((_, name) -> name.endsWith(".yml"));
            if (files != null) {
                for (File file : files) {
                    if (!file.delete()) {
                        logger.warning("LevelBorder: Portaldatei konnte nicht gelöscht werden: "
                                + file.getAbsolutePath());
                    }
                }
            }
        }
    }
}
