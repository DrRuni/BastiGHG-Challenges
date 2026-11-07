
package runi.myddns.challenges.games.chunkrandomizer.managers;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import runi.myddns.challenges.ChallengeMain;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class ChunkRandomizerManager implements Listener {

    private static final Set<Material> EXCLUDED_BLOCKS = Set.of(
            // Unzerstörbare und technische Blöcke
            Material.BEDROCK,
            Material.BARRIER,
            Material.LIGHT,
            Material.STRUCTURE_VOID,
            Material.COMMAND_BLOCK,
            Material.CHAIN_COMMAND_BLOCK,
            Material.REPEATING_COMMAND_BLOCK,
            Material.STRUCTURE_BLOCK,
            Material.JIGSAW,
            Material.REINFORCED_DEEPSLATE,

            // Portale
            Material.END_PORTAL,
            Material.END_PORTAL_FRAME,
            Material.END_GATEWAY,
            Material.NETHER_PORTAL,

            // Besondere Funktionsblöcke
            Material.SPAWNER,
            Material.TRIAL_SPAWNER,
            Material.VAULT,
            Material.MOVING_PISTON,
            Material.PISTON_HEAD,

            // Gefährliche Blöcke
            Material.TNT,
            Material.RESPAWN_ANCHOR,
            Material.MAGMA_BLOCK,
            Material.CAMPFIRE,
            Material.SOUL_CAMPFIRE,
            Material.CACTUS,
            Material.POWDER_SNOW,

            // Flüssigkeiten
            Material.WATER,
            Material.LAVA
    );

    // Erlaubte Sonderblöcke
    private static final Set<Material> SPECIAL_BLOCKS = Set.of(
            // Eis
            Material.ICE,
            Material.PACKED_ICE,
            Material.BLUE_ICE,

            // Bewegungsblöcke
            Material.SLIME_BLOCK,
            Material.HONEY_BLOCK,

            // Eigenständig platzierbare Redstone-Blöcke
            Material.REDSTONE_BLOCK,
            Material.PISTON,
            Material.STICKY_PISTON,
            Material.DISPENSER,
            Material.DROPPER,
            Material.OBSERVER,
            Material.CRAFTER,
            Material.REDSTONE_LAMP,
            Material.NOTE_BLOCK,
            Material.TARGET
    );

    // Automatischer Blockpool
    private static final Material[] BLOCK_TYPES =
            Arrays.stream(Material.values())
                    .filter(Material::isBlock)
                    .filter(material -> !material.isLegacy())
                    .filter(Material::isSolid)
                    .filter(material -> !material.hasGravity())
                    .filter(material ->
                            material.isOccluding()
                                    || SPECIAL_BLOCKS.contains(material))
                    .filter(material -> !EXCLUDED_BLOCKS.contains(material))
                    .toArray(Material[]::new);

    private final ChallengeMain plugin;
    private final ChunkRandomizerBorderManager borderManager;
    private final ChunkRandomizerStartManager startManager;
    private final ChunkRandomizerDataManager dataManager;
    private final Consumer<Location> onNextChunk;
    private final Set<String> processedChunks = new HashSet<>();

    public ChunkRandomizerManager(
            ChallengeMain plugin,
            ChunkRandomizerBorderManager borderManager,
            ChunkRandomizerStartManager startManager,
            ChunkRandomizerDataManager dataManager,
            Consumer<Location> onNextChunk
    ) {
        this.plugin = plugin;
        this.borderManager = borderManager;
        this.startManager = startManager;
        this.dataManager = dataManager;
        this.onNextChunk = onNextChunk;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {

        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null || to.getWorld() == null) return;

        if (from.getWorld() == to.getWorld()
                && (from.getBlockX() >> 4) == (to.getBlockX() >> 4)
                && (from.getBlockZ() >> 4) == (to.getBlockZ() >> 4)) {
            return;
        }

        if (!plugin.getGameWorldManager()
                .isGameWorld(to.getWorld(), "chunkrandomizer")) {
            return;
        }

        if (!startManager.isStarted()) return;
        if (!dataManager.isStarted()) return;

        // Solange die Border aktiv ist, keine neue Runde beginnen
        if (borderManager.isActive()) return;

        // Nicht erneut im gerade freigegebenen Chunk starten
        var data = dataManager.getData();

        String previousWorld = data.getString("border.world", "");

        int previousX = Math.floorDiv(
                (int) Math.floor(data.getDouble("border.center.x")), 16
        );

        int previousZ = Math.floorDiv(
                (int) Math.floor(data.getDouble("border.center.z")), 16
        );

        if (to.getWorld().getName().equals(previousWorld)
                && (to.getBlockX() >> 4) == previousX
                && (to.getBlockZ() >> 4) == previousZ) {
            return;
        }

        onNextChunk.accept(to);
    }

    public void processChunk(Location location) {
        if (!startManager.isStarted()) return;

        World world = location.getWorld();
        if (world == null) return;

        if (!plugin.getGameWorldManager()
                .isGameWorld(world, "chunkrandomizer")) {
            return;
        }

        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;

        String key = world.getUID() + ":" + chunkX + ":" + chunkZ;

        if (!processedChunks.add(key)) return;

        Chunk chunk = world.getChunkAt(chunkX, chunkZ);

        Material selected = BLOCK_TYPES[
                ThreadLocalRandom.current().nextInt(BLOCK_TYPES.length)
                ];

        long start = System.nanoTime();
        int changed = 0;

        try {
            for (int y = world.getMinHeight(); y < world.getMaxHeight(); y++) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {

                        Block block = chunk.getBlock(x, y, z);
                        Material current = block.getType();

                        // Luft, Höhlen und Bedrock erhalten
                        if (current.isAir() || current == Material.BEDROCK) {
                            continue;
                        }

                        // Feste Blöcke sowie Flüssigkeiten ersetzen
                        if (!current.isSolid()
                                && current != Material.WATER
                                && current != Material.LAVA) {
                            continue;
                        }

                        if (current == selected) continue;

                        block.setType(selected, false);
                        changed++;
                    }
                }
            }
        } catch (Exception ex) {
            processedChunks.remove(key);
            plugin.getLogger().severe(
                    "ChunkRandomizer: Fehler bei Chunk "
                            + chunkX + ", " + chunkZ + ": " + ex.getMessage()
            );
            return;
        }

        double millis = (System.nanoTime() - start) / 1_000_000.0;

        plugin.getLogger().info(String.format(
                "ChunkRandomizer | Welt: %s | Chunk: %d,%d | Block: %s | "
                        + "Geändert: %d | Zeit: %.2f ms",
                world.getName(),
                chunkX,
                chunkZ,
                selected.name(),
                changed,
                millis
        ));
    }

    public void reset() {
        processedChunks.clear();
    }
}
