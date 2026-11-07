
package runi.myddns.challenges.games.chunkrandomizer.managers;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataType;
import runi.myddns.challenges.ChallengeMain;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class ChunkRandomizerMobManager implements Listener {

    private static final EntityType[] EASY_MOBS = {
            EntityType.SHEEP,
            EntityType.COW,
            EntityType.PIG,
            EntityType.CHICKEN
    };

    private final ChallengeMain plugin;
    private final ChunkRandomizerStartManager startManager;
    private final ChunkRandomizerDataManager dataManager;
    private final Runnable onMobDefeated;
    private final NamespacedKey mobKey;

    private UUID activeMobId;

    public ChunkRandomizerMobManager(
            ChallengeMain plugin,
            ChunkRandomizerStartManager startManager,
            ChunkRandomizerDataManager dataManager,
            Runnable onMobDefeated
    ) {
        this.plugin = plugin;
        this.startManager = startManager;
        this.dataManager = dataManager;
        this.onMobDefeated = onMobDefeated;
        this.mobKey = new NamespacedKey(plugin, "chunkrandomizer_mob");
    }

    public Location prepareSpawnLocation(Location location) {

        if (location == null || location.getWorld() == null) {
            return null;
        }

        return findSpawnLocation(
                location.getWorld(),
                location.getBlockX() >> 4,
                location.getBlockZ() >> 4
        );
    }

    public boolean spawnMob(Location chunkLocation) {
        return spawnMob(chunkLocation, null);
    }

    public boolean spawnMob(
            Location chunkLocation,
            Location preparedLocation
    ) {

        if (!startManager.isStarted()) return false;
        if (chunkLocation == null || chunkLocation.getWorld() == null) {
            return false;
        }

        if (dataManager.getData() == null) return false;
        if (dataManager.getData().getBoolean("mob.active", false)) {
            return false;
        }

        EntityType type = EASY_MOBS[
                ThreadLocalRandom.current().nextInt(EASY_MOBS.length)
                ];

        return spawnInChunk(chunkLocation, type, preparedLocation);
    }

    // ============================================================
    // SPAWN IM CHUNK
    // ============================================================

    private boolean spawnInChunk(
            Location chunkLocation,
            EntityType type
    ) {
        return spawnInChunk(chunkLocation, type, null);
    }

    private boolean spawnInChunk(
            Location chunkLocation,
            EntityType type,
            Location preparedLocation
    ) {

        World world = chunkLocation.getWorld();
        if (world == null) return false;

        int chunkX = chunkLocation.getBlockX() >> 4;
        int chunkZ = chunkLocation.getBlockZ() >> 4;

        Location spawnLocation = preparedLocation != null
                ? preparedLocation.clone()
                : findSpawnLocation(world, chunkX, chunkZ);

        if (spawnLocation == null) {
            plugin.getLogger().warning(
                    "ChunkRandomizer: Kein geeigneter Mob-Spawn gefunden."
            );
            return false;
        }

        if (preparedLocation != null) {

            if (!world.equals(preparedLocation.getWorld())
                    || (preparedLocation.getBlockX() >> 4) != chunkX
                    || (preparedLocation.getBlockZ() >> 4) != chunkZ) {
                return false;
            }

            Block feet = spawnLocation.getBlock();
            Block ground = feet.getRelative(0, -1, 0);

            // Sicheren Untergrund gewährleisten
            if (!ground.getType().isSolid() || ground.isPassable()) {
                ground.setType(Material.STONE, false);
            }

            // Platz für das Mob freihalten
            for (int i = 0; i < 3; i++) {
                feet.getRelative(0, i, 0)
                        .setType(Material.AIR, false);
            }
        }

        Entity entity = world.spawnEntity(spawnLocation, type);

        if (!(entity instanceof LivingEntity mob)) {
            entity.remove();
            return false;
        }

        // Dauerhaft als Challenge-Mob kennzeichnen
        mob.getPersistentDataContainer().set(
                mobKey,
                PersistentDataType.BYTE,
                (byte) 1
        );

        mob.setPersistent(true);
        mob.setRemoveWhenFarAway(false);

        activeMobId = mob.getUniqueId();

        FileConfiguration data = dataManager.getData();

        data.set("mob.active", true);
        data.set("mob.uuid", activeMobId.toString());
        data.set("mob.type", type.name());

        dataManager.save();

        Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> {
                    if (Bukkit.getWorld(world.getUID()) != null) {
                        world.save(true);
                    }
                },
                40L
        );

        plugin.getLogger().info(
                "ChunkRandomizer: Challenge-Mob gespawnt: " + type
        );

        return true;
    }

    // ============================================================
    // SICHERE OBERFLÄCHE FINDEN
    // ============================================================


    private Location findSpawnLocation(
            World world,
            int chunkX,
            int chunkZ
    ) {

        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int attempt = 0; attempt < 128; attempt++) {

            int x = chunkX * 16 + random.nextInt(1, 15);
            int z = chunkZ * 16 + random.nextInt(1, 15);

            int highestY = world.getHighestBlockYAt(
                    x, z, HeightMap.WORLD_SURFACE
            );

            for (int y = highestY; y >= highestY - 1; y--) {

                if (y < world.getMinHeight()
                        || y + 3 >= world.getMaxHeight()) {
                    continue;
                }

                Block ground = world.getBlockAt(x, y, z);
                Material type = ground.getType();

                if (!type.isSolid() || ground.isPassable()) {
                    continue;
                }

                // Kein Spawn auf Bäumen
                if (org.bukkit.Tag.LOGS.isTagged(type)
                        || org.bukkit.Tag.LEAVES.isTagged(type)) {
                    continue;
                }

                if (type == Material.MAGMA_BLOCK
                        || type == Material.CACTUS
                        || type == Material.CAMPFIRE
                        || type == Material.SOUL_CAMPFIRE
                        || type == Material.POWDER_SNOW) {
                    continue;
                }

                Block feet = ground.getRelative(0, 1, 0);
                Block head = ground.getRelative(0, 2, 0);
                Block above = ground.getRelative(0, 3, 0);

                if (!feet.getType().isAir()
                        || !head.getType().isAir()
                        || !above.getType().isAir()) {
                    continue;
                }

                return new Location(
                        world,
                        x + 0.5,
                        y + 1.0,
                        z + 0.5
                );
            }
        }

        return null;
    }


    // ============================================================
    // MOB-TOD
    // ============================================================

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {

        if (!startManager.isStarted()) return;
        if (activeMobId == null) return;

        LivingEntity mob = event.getEntity();

        if (!mob.getUniqueId().equals(activeMobId)) {
            return;
        }

        if (!isChallengeMob(mob)) return;

        activeMobId = null;

        FileConfiguration data = dataManager.getData();

        data.set("mob.active", false);
        data.set("mob.uuid", "");
        data.set("mob.type", "");

        dataManager.save();

        plugin.getLogger().info(
                "ChunkRandomizer: Challenge-Mob besiegt."
        );

        // Nur jetzt darf die Spielsteuerung
        // die WorldBorder freigeben.
        onMobDefeated.run();
    }

    // ============================================================
    // WIEDERHERSTELLUNG
    // ============================================================

    public boolean restoreMob() {

        if (!startManager.isStarted()) return false;

        FileConfiguration data = dataManager.getData();

        if (data == null
                || !data.getBoolean("mob.active", false)) {
            return false;
        }

        String worldName = data.getString("border.world", "");
        World world = Bukkit.getWorld(worldName);

        if (world == null) return false;

        String uuidText = data.getString("mob.uuid", "");

        UUID savedId = null;

        try {
            if (!uuidText.isBlank()) {
                savedId = UUID.fromString(uuidText);
            }
        } catch (IllegalArgumentException ignored) {
            // Ungültige UUID: Mob im gespeicherten Chunk suchen.
        }

        double centerX = data.getDouble("border.center.x");
        double centerZ = data.getDouble("border.center.z");

        int chunkX = Math.floorDiv((int) Math.floor(centerX), 16);
        int chunkZ = Math.floorDiv((int) Math.floor(centerZ), 16);

        Chunk chunk = world.getChunkAt(chunkX, chunkZ);

        // Vorhandenes Mob im Chunk suchen
        for (Entity entity : chunk.getEntities()) {

            if (!(entity instanceof LivingEntity mob)) continue;
            if (!isChallengeMob(mob)) continue;

            if (savedId != null
                    && !mob.getUniqueId().equals(savedId)) {
                continue;
            }

            activeMobId = mob.getUniqueId();

            data.set("mob.uuid", activeMobId.toString());
            dataManager.save();

            return true;
        }

        // Gespeichertes Mob fehlt: Ersatz erzeugen
        String typeName = data.getString("mob.type", "");

        EntityType type;

        try {
            type = EntityType.valueOf(typeName);
        } catch (IllegalArgumentException ex) {
            plugin.getLogger().warning(
                    "ChunkRandomizer: Ungültiger Mob-Typ: " + typeName
            );
            return false;
        }

        // Nur zulässige Tierarten wiederherstellen
        boolean allowed = false;

        for (EntityType allowedType : EASY_MOBS) {
            if (allowedType == type) {
                allowed = true;
                break;
            }
        }

        if (!allowed) return false;

        // Fehlendes Mob durch dieselbe Tierart ersetzen
        data.set("mob.active", false);

        return spawnInChunk(
                new Location(world, centerX, 0, centerZ),
                type
        );
    }

    // ============================================================
    // HELFER
    // ============================================================

    private boolean isChallengeMob(LivingEntity mob) {

        Byte marker = mob.getPersistentDataContainer().get(
                mobKey,
                PersistentDataType.BYTE
        );

        return marker != null && marker == (byte) 1;
    }

    public boolean hasActiveMob() {

        if (activeMobId == null) return false;

        Entity entity = Bukkit.getEntity(activeMobId);

        return entity instanceof LivingEntity mob
                && mob.isValid()
                && !mob.isDead()
                && isChallengeMob(mob);
    }

    // ============================================================
    // ENTLADEN
    // ============================================================

    public void shutdown() {

        // Mob NICHT löschen!
        // Es bleibt in der Welt gespeichert.
        activeMobId = null;
    }

    /**
     * Entfernt das aktive Challenge-Mob beim vollständigen Game-Reset.
     */
    public void reset() {

        if (activeMobId != null) {

            Entity entity = Bukkit.getEntity(activeMobId);

            if (entity != null && isChallengeMobEntity(entity)) {
                entity.remove();
            }
        }

        activeMobId = null;
    }

    private boolean isChallengeMobEntity(Entity entity) {

        if (!(entity instanceof LivingEntity mob)) {
            return false;
        }

        return isChallengeMob(mob);
    }
}
