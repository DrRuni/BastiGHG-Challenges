package runi.myddns.challenges.games.LevelBlock.Manager;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.key.Key;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import runi.myddns.challenges.games.LevelBlock.LevelBlockGame;

import java.util.*;

public class BorderLineManager {

    private final LevelBlockGame game;
    private final BorderManager borderManager;

    private final Map<String, Map<Long, List<Integer>>> surfaceCache =
            new HashMap<>();

    private final Map<UUID, Map<String, ItemDisplay>> playerDisplays =
            new HashMap<>();

    private final Map<UUID, String> lastPlayerPositions =
            new HashMap<>();

    private BukkitTask updateTask;

    private static final int LINE_RADIUS = 32;
    private static final int BIG_BORDER_RADIUS = 10;
    private static final int BIG_BORDER_Y_RANGE = 2;
    private static final float LINE_LENGTH = 1.00f;
    private static final float LINE_WIDTH = 0.06f;
    private static final double LINE_Y_OFFSET = 0.01;
    private static final double LINE_SIDE_OFFSET = 0.03;
    private static final long UPDATE_INTERVAL = 5L;

    public BorderLineManager(
            LevelBlockGame game,
            BorderManager borderManager
    ) {

        this.game = game;
        this.borderManager = borderManager;
    }

    public void start() {

        rebuildAllCaches();

        refreshAll();

        startUpdateTask();
    }

    private void rebuildAllCaches() {

        surfaceCache.clear();

        for (World world
                : game.getPlugin()
                .getServer()
                .getWorlds()) {

            if (!borderManager.isInitialized(
                    world
            )) {
                continue;
            }

            for (LevelBlockPos pos
                    : borderManager.getUnlockedBlocks(
                    world
            )) {

                rebuildColumn(
                        world,
                        pos.x(),
                        pos.z()
                );
            }
        }
    }

    public void stop() {

        stopUpdateTask();

        removeAll();

        lastPlayerPositions.clear();
    }

    public void reset() {

        surfaceCache.clear();

        removeAll();

        lastPlayerPositions.clear();
    }

    public void updateAfterUnlock(
            World world,
            int x,
            int z
    ) {

        if (world == null) {
            return;
        }

        rebuildColumn(
                world,
                x,
                z
        );

        refreshAll();
    }

    public void updateColumn(
            World world,
            int x,
            int z
    ) {

        if (world == null) {
            return;
        }

        if (!borderManager.isUnlocked(
                world,
                x,
                z
        )) {
            return;
        }

        rebuildColumn(
                world,
                x,
                z
        );

        refreshAll();
    }

    public void rebuildColumn(
            World world,
            int x,
            int z
    ) {

        if (world == null) {
            return;
        }

        Map<Long, List<Integer>> worldCache =
                surfaceCache.computeIfAbsent(
                        world.getName(),
                        ignored -> new HashMap<>()
                );

        long columnKey =
                createColumnKey(
                        x,
                        z
                );

        if (!borderManager.isUnlocked(
                world,
                x,
                z
        )) {

            worldCache.remove(
                    columnKey
            );

            return;
        }

        List<Integer> surfaces =
                new ArrayList<>();

        for (
                int y = world.getMinHeight();
                y < world.getMaxHeight() - 1;
                y++
        ) {

            Block block =
                    world.getBlockAt(
                            x,
                            y,
                            z
                    );

            if (!block.getType().isSolid()) {
                continue;
            }

            Block above =
                    world.getBlockAt(
                            x,
                            y + 1,
                            z
                    );

            if (!above.getType().isAir()) {
                continue;
            }

            surfaces.add(
                    y + 1
            );
        }

        worldCache.put(
                columnKey,
                surfaces
        );
    }

    public void updatePlayerView(
            Player player
    ) {

        World world =
                player.getWorld();

        if (!borderManager.isInitialized(world)) {

            removePlayer(
                    player
            );

            return;
        }

        Map<Long, List<Integer>> worldCache =
                surfaceCache.get(
                        world.getName()
                );

        if (worldCache == null) {
            return;
        }

        int playerX =
                player.getLocation().getBlockX();

        int playerY =
                player.getLocation().getBlockY();

        int playerZ =
                player.getLocation().getBlockZ();


        Map<String, ItemDisplay> displays =
                playerDisplays.computeIfAbsent(
                        player.getUniqueId(),
                        ignored -> new HashMap<>()
                );


        Set<String> wantedKeys =
                new HashSet<>();

        for (
                int x = playerX - LINE_RADIUS;
                x <= playerX + LINE_RADIUS;
                x++
        ) {

            for (
                    int z = playerZ - LINE_RADIUS;
                    z <= playerZ + LINE_RADIUS;
                    z++
            ) {

                int dx =
                        x - playerX;

                int dz =
                        z - playerZ;

                if (
                        dx * dx + dz * dz
                                > LINE_RADIUS * LINE_RADIUS
                ) {

                    continue;
                }

                if (!borderManager.isUnlocked(
                        world,
                        x,
                        z
                )) {

                    continue;
                }

                List<Integer> surfaces =
                        worldCache.get(
                                createColumnKey(
                                        x,
                                        z
                                )
                        );

                if (surfaces == null
                        || surfaces.isEmpty()) {

                    continue;
                }

                boolean north =
                        !borderManager.isUnlocked(
                                world,
                                x,
                                z - 1
                        );

                boolean south =
                        !borderManager.isUnlocked(
                                world,
                                x,
                                z + 1
                        );

                boolean west =
                        !borderManager.isUnlocked(
                                world,
                                x - 1,
                                z
                        );

                boolean east =
                        !borderManager.isUnlocked(
                                world,
                                x + 1,
                                z
                        );


                if (!north
                        && !south
                        && !west
                        && !east) {

                    continue;
                }


                for (int surfaceY : surfaces) {

                    if (
                            shouldHideForBigBorder(
                                    playerX,
                                    playerY,
                                    playerZ,
                                    x,
                                    surfaceY,
                                    z
                            )
                    ) {

                        continue;
                    }


                    if (north) {

                        addLine(
                                player,
                                world,
                                displays,
                                wantedKeys,
                                x,
                                surfaceY,
                                z,
                                BorderSide.NORTH
                        );
                    }


                    if (south) {

                        addLine(
                                player,
                                world,
                                displays,
                                wantedKeys,
                                x,
                                surfaceY,
                                z,
                                BorderSide.SOUTH
                        );
                    }


                    if (west) {

                        addLine(
                                player,
                                world,
                                displays,
                                wantedKeys,
                                x,
                                surfaceY,
                                z,
                                BorderSide.WEST
                        );
                    }


                    if (east) {

                        addLine(
                                player,
                                world,
                                displays,
                                wantedKeys,
                                x,
                                surfaceY,
                                z,
                                BorderSide.EAST
                        );
                    }
                }
            }
        }

        Iterator<Map.Entry<String, ItemDisplay>> iterator =
                displays.entrySet().iterator();


        while (iterator.hasNext()) {

            Map.Entry<String, ItemDisplay> entry =
                    iterator.next();


            if (
                    wantedKeys.contains(
                            entry.getKey()
                    )
            ) {

                continue;
            }


            ItemDisplay display =
                    entry.getValue();


            if (display != null
                    && display.isValid()) {

                display.remove();
            }


            iterator.remove();
        }
    }

    private boolean shouldHideForBigBorder(
            int playerX,
            int playerY,
            int playerZ,
            int blockX,
            int surfaceY,
            int blockZ
    ) {

        int dx =
                blockX - playerX;

        int dz =
                blockZ - playerZ;

        if (
                dx * dx + dz * dz
                        > BIG_BORDER_RADIUS
                        * BIG_BORDER_RADIUS
        ) {

            return false;
        }

        return Math.abs(
                surfaceY - playerY
        ) <= BIG_BORDER_Y_RANGE;
    }

    private void addLine(
            Player player,
            World world,
            Map<String, ItemDisplay> displays,
            Set<String> wantedKeys,
            int blockX,
            int surfaceY,
            int blockZ,
            BorderSide side
    ) {

        String key =
                createDisplayKey(
                        world,
                        blockX,
                        surfaceY,
                        blockZ,
                        side
                );


        wantedKeys.add(
                key
        );


        ItemDisplay existing =
                displays.get(
                        key
                );


        if (existing != null
                && existing.isValid()) {

            return;
        }


        spawnLine(
                world,
                displays,
                key,
                blockX,
                surfaceY,
                blockZ,
                side
        );
    }

    private void spawnLine(
            World world,
            Map<String, ItemDisplay> displays,
            String key,
            int blockX,
            int surfaceY,
            int blockZ,
            BorderSide side
    ) {

        Location location =
                switch (side) {

                    case NORTH ->
                            new Location(
                                    world,
                                    blockX + 0.5 - LINE_SIDE_OFFSET,
                                    surfaceY + LINE_Y_OFFSET,
                                    blockZ + 0.001
                            );

                    case SOUTH ->
                            new Location(
                                    world,
                                    blockX + 0.5 - LINE_SIDE_OFFSET,
                                    surfaceY + LINE_Y_OFFSET,
                                    blockZ + 0.999
                            );

                    case WEST ->
                            new Location(
                                    world,
                                    blockX + 0.001,
                                    surfaceY + LINE_Y_OFFSET,
                                    blockZ + 0.5 - LINE_SIDE_OFFSET
                            );

                    case EAST ->
                            new Location(
                                    world,
                                    blockX + 0.999,
                                    surfaceY + LINE_Y_OFFSET,
                                    blockZ + 0.5 - LINE_SIDE_OFFSET
                            );
                };


        ItemDisplay display =
                world.spawn(
                        location,
                        ItemDisplay.class,
                        entity -> {

                            entity.setItemStack(createLineItem());
                            entity.setBillboard(Display.Billboard.FIXED);
                            entity.setBrightness(new Display.Brightness(15, 15));
                            entity.setViewRange(0.7f);
                            entity.setShadowRadius(0.0f);
                            entity.setShadowStrength(0.0f);
                            entity.setPersistent(false);
                            entity.setInvulnerable(true);
                            entity.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.NONE);
                            entity.setTransformation(getLineTransformation(side));
                        }
                );


        displays.put(
                key,
                display
        );
    }

    private Transformation getLineTransformation(
            BorderSide side
    ) {

        float yaw =
                switch (side) {

                    case NORTH, SOUTH ->
                            0f;

                    case WEST, EAST ->
                            (float) Math.toRadians(
                                    90
                            );
                };


        Quaternionf rotation =
                new Quaternionf()
                        .rotateY(
                                yaw
                        )
                        .rotateX(
                                (float) Math.toRadians(
                                        90
                                )
                        );


        return new Transformation(

                new Vector3f(
                        0f,
                        0f,
                        0f
                ),

                rotation,

                new Vector3f(
                        LINE_LENGTH,
                        LINE_WIDTH,
                        0.01f
                ),

                new Quaternionf()
        );
    }

    private ItemStack createLineItem() {
        ItemStack item = ItemStack.of(Material.PAPER);

        item.setData(
                DataComponentTypes.ITEM_MODEL,
                Key.key(
                        "bastighg_challenges",
                        "levelblock/border_line_red"
                )
        );

        return item;
    }

    private void startUpdateTask() {

        stopUpdateTask();


        updateTask =
                game.getPlugin()
                        .getServer()
                        .getScheduler()
                        .runTaskTimer(

                                game.getPlugin(),

                                this::updatePlayers,

                                0L,

                                UPDATE_INTERVAL
                        );
    }


    private void updatePlayers() {

        Set<UUID> activePlayers =
                new HashSet<>();

        for (
                Player player
                : game.getPlugin()
                .getServer()
                .getOnlinePlayers()
        ) {

            if (!game.isLevelBlockPlayer(
                    player
            )) {
                continue;
            }

            World world =
                    player.getWorld();

            if (!borderManager.isInitialized(
                    world
            )) {
                continue;
            }

            UUID uuid =
                    player.getUniqueId();

            activePlayers.add(
                    uuid
            );

            String position =
                    world.getName()
                            + ":"
                            + player.getLocation().getBlockX()
                            + ":"
                            + player.getLocation().getBlockY()
                            + ":"
                            + player.getLocation().getBlockZ();

            String previous =
                    lastPlayerPositions.get(
                            uuid
                    );

            if (position.equals(previous)) {
                continue;
            }

            lastPlayerPositions.put(
                    uuid,
                    position
            );

            updatePlayerView(
                    player
            );
        }

        Iterator<Map.Entry<UUID, Map<String, ItemDisplay>>> iterator =
                playerDisplays
                        .entrySet()
                        .iterator();

        while (iterator.hasNext()) {

            Map.Entry<UUID, Map<String, ItemDisplay>> entry =
                    iterator.next();

            UUID uuid =
                    entry.getKey();

            if (activePlayers.contains(
                    uuid
            )) {
                continue;
            }

            for (
                    Entity entity
                    : entry.getValue().values()
            ) {

                if (entity != null
                        && entity.isValid()) {

                    entity.remove();
                }
            }

            iterator.remove();

            lastPlayerPositions.remove(
                    uuid
            );
        }
    }

    private void stopUpdateTask() {

        if (updateTask != null) {

            updateTask.cancel();

            updateTask = null;
        }
    }

    public void refreshAll() {

        for (
                Player player
                : game.getPlugin()
                .getServer()
                .getOnlinePlayers()
        ) {

            if (!game.isLevelBlockPlayer(
                    player
            )) {
                continue;
            }

            World world =
                    player.getWorld();

            if (!borderManager.isInitialized(
                    world
            )) {
                continue;
            }

            updatePlayerView(
                    player
            );
        }
    }

    public void removePlayer(
            Player player
    ) {

        UUID uuid =
                player.getUniqueId();


        Map<String, ItemDisplay> displays =
                playerDisplays.remove(
                        uuid
                );


        if (displays != null) {

            for (Entity entity : displays.values()) {

                if (entity != null
                        && entity.isValid()) {

                    entity.remove();
                }
            }
        }


        lastPlayerPositions.remove(
                uuid
        );
    }

    private void removeAll() {

        for (
                Map<String, ItemDisplay> displays
                : playerDisplays.values()
        ) {

            for (Entity entity : displays.values()) {

                if (entity != null
                        && entity.isValid()) {

                    entity.remove();
                }
            }

            displays.clear();
        }

        playerDisplays.clear();
    }

    private long createColumnKey(
            int x,
            int z
    ) {

        return ((long) x << 32)
                ^ (z & 0xffffffffL);
    }

    private String createDisplayKey(
            World world,
            int x,
            int y,
            int z,
            BorderSide side
    ) {

        return world.getName()
                + ":"
                + x
                + ":"
                + y
                + ":"
                + z
                + ":"
                + side.name();
    }

    private enum BorderSide {
        NORTH,
        SOUTH,
        WEST,
        EAST
    }
}