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
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import runi.myddns.challenges.games.LevelBlock.LevelBlockGame;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BorderDisplayManager {

    private final LevelBlockGame game;
    private final BorderManager borderManager;

    private final Map<UUID, Map<String, ItemDisplay>> playerDisplays =
            new HashMap<>();

    private static final int DISPLAY_RADIUS = 10;
    private static final float BORDER_HEIGHT_NEAR = 1.00f;
    private static final float BORDER_HEIGHT_FAR = 0.08f;
    private static final float BORDER_LENGTH = 1.00f;
    private static final float BORDER_THICKNESS = 0.01f;

    public BorderDisplayManager(
            LevelBlockGame game,
            BorderManager borderManager
    ) {
        this.game = game;
        this.borderManager = borderManager;
    }

    public void start() {
        refreshAll();
    }

    public void stop() {
        removeAll();
    }

    public void updateAfterUnlock(
            World world,
            int x,
            int z
    ) {

        if (world == null) {
            return;
        }

        for (Player player : world.getPlayers()) {

            if (!game.isLevelBlockPlayer(player)) {
                continue;
            }

            if (player.getLocation().distanceSquared(
                    new Location(
                            world,
                            x + 0.5,
                            player.getY(),
                            z + 0.5
                    )
            ) > 144.0) {
                continue;
            }

            updateEdge(
                    player,
                    world,
                    x,
                    z,
                    BorderSide.NORTH
            );

            updateEdge(
                    player,
                    world,
                    x,
                    z,
                    BorderSide.SOUTH
            );

            updateEdge(
                    player,
                    world,
                    x,
                    z,
                    BorderSide.WEST
            );

            updateEdge(
                    player,
                    world,
                    x,
                    z,
                    BorderSide.EAST
            );

            updateEdge(
                    player,
                    world,
                    x,
                    z - 1,
                    BorderSide.SOUTH
            );

            updateEdge(
                    player,
                    world,
                    x,
                    z + 1,
                    BorderSide.NORTH
            );

            updateEdge(
                    player,
                    world,
                    x - 1,
                    z,
                    BorderSide.EAST
            );

            updateEdge(
                    player,
                    world,
                    x + 1,
                    z,
                    BorderSide.WEST
            );
        }

        flashGreen(
                world,
                x,
                z
        );
    }

    private void updateEdge(
            Player player,
            World world,
            int blockX,
            int blockZ,
            BorderSide side
    ) {

        String key =
                world.getName()
                        + ":"
                        + blockX
                        + ":"
                        + blockZ
                        + ":"
                        + side.name();

        Map<String, ItemDisplay> displays =
                playerDisplays.computeIfAbsent(
                        player.getUniqueId(),
                        ignored -> new HashMap<>()
                );

        ItemDisplay existing =
                displays.get(key);

        /*
         * Block selbst ist nicht freigeschaltet.
         * Eventuell vorhandene Border entfernen.
         */
        if (!borderManager.isUnlocked(
                world,
                blockX,
                blockZ
        )) {

            if (existing != null) {

                if (existing.isValid()) {
                    existing.remove();
                }

                displays.remove(key);
            }

            return;
        }

        int neighbourX = blockX;
        int neighbourZ = blockZ;

        switch (side) {

            case NORTH ->
                    neighbourZ--;

            case SOUTH ->
                    neighbourZ++;

            case WEST ->
                    neighbourX--;

            case EAST ->
                    neighbourX++;
        }

        if (borderManager.isUnlocked(
                world,
                neighbourX,
                neighbourZ
        )) {

            if (existing != null) {

                if (existing.isValid()) {
                    existing.remove();
                }

                displays.remove(key);
            }

            return;
        }

        Integer y =
                findBorderY(
                        world,
                        blockX,
                        blockZ,
                        player.getLocation().getBlockY()
                );

        /*
         * Keine passende Oberfläche
         * in der Nähe der Spielerhöhe.
         */
        if (y == null) {

            if (existing != null) {

                if (existing.isValid()) {
                    existing.remove();
                }

                displays.remove(key);
            }

            return;
        }

        float height =
                calculateBorderHeight(
                        player,
                        blockX,
                        blockZ
                );

        Location wantedLocation =
                switch (side) {

                    case NORTH ->
                            new Location(
                                    world,
                                    blockX + 0.5,
                                    y + (height / 2.0),
                                    blockZ + 0.001
                            );

                    case SOUTH ->
                            new Location(
                                    world,
                                    blockX + 0.5,
                                    y + (height / 2.0),
                                    blockZ + 0.999
                            );

                    case WEST ->
                            new Location(
                                    world,
                                    blockX + 0.001,
                                    y + (height / 2.0),
                                    blockZ + 0.5
                            );

                    case EAST ->
                            new Location(
                                    world,
                                    blockX + 0.999,
                                    y + (height / 2.0),
                                    blockZ + 0.5
                            );
                };

        /*
         * Display existiert bereits.
         * Nur verschieben, falls sich die passende
         * Höhe geändert hat.
         */
        if (existing != null
                && existing.isValid()) {

            existing.teleport(
                    wantedLocation
            );

            existing.setTransformation(
                    getTransformation(
                            side,
                            height
                    )
            );

            return;
        }

        spawnEdge(
                player,
                world,
                blockX,
                blockZ,
                side
        );
    }

    public void refreshAll() {

        removeAll();

        for (
                Player player
                : game.getPlugin()
                .getServer()
                .getOnlinePlayers()
        ) {

            if (!game.isLevelBlockPlayer(player)) {
                continue;
            }

            World world =
                    player.getWorld();

            if (!borderManager.isInitialized(world)) {
                continue;
            }

            updatePlayerView(
                    player,
                    player.getLocation()
            );
        }
    }

    public void updatePlayerView(
            Player player,
            Location location
    ) {

        World world =
                player.getWorld();

        if (!borderManager.isInitialized(world)) {
            removePlayerDisplays(player);
            return;
        }

        int centerX =
                location.getBlockX();

        int centerZ =
                location.getBlockZ();

        Map<String, ItemDisplay> displays =
                playerDisplays.computeIfAbsent(
                        player.getUniqueId(),
                        ignored -> new HashMap<>()
                );

        Set<String> wantedKeys =
                new HashSet<>();

        for (
                int x = centerX - DISPLAY_RADIUS;
                x <= centerX + DISPLAY_RADIUS;
                x++
        ) {

            for (
                    int z = centerZ - DISPLAY_RADIUS;
                    z <= centerZ + DISPLAY_RADIUS;
                    z++
            ) {

                if (!borderManager.isUnlocked(
                        world,
                        x,
                        z
                )) {
                    continue;
                }

                /*
                 * NORTH
                 */
                if (!borderManager.isUnlocked(
                        world,
                        x,
                        z - 1
                )) {

                    String key =
                            world.getName()
                                    + ":"
                                    + x
                                    + ":"
                                    + z
                                    + ":"
                                    + BorderSide.NORTH.name();

                    wantedKeys.add(key);

                    updateEdge(
                            player,
                            world,
                            x,
                            z,
                            BorderSide.NORTH
                    );
                }

                /*
                 * SOUTH
                 */
                if (!borderManager.isUnlocked(
                        world,
                        x,
                        z + 1
                )) {

                    String key =
                            world.getName()
                                    + ":"
                                    + x
                                    + ":"
                                    + z
                                    + ":"
                                    + BorderSide.SOUTH.name();

                    wantedKeys.add(key);

                    updateEdge(
                            player,
                            world,
                            x,
                            z,
                            BorderSide.SOUTH
                    );
                }

                /*
                 * WEST
                 */
                if (!borderManager.isUnlocked(
                        world,
                        x - 1,
                        z
                )) {

                    String key =
                            world.getName()
                                    + ":"
                                    + x
                                    + ":"
                                    + z
                                    + ":"
                                    + BorderSide.WEST.name();

                    wantedKeys.add(key);

                    updateEdge(
                            player,
                            world,
                            x,
                            z,
                            BorderSide.WEST
                    );
                }

                if (!borderManager.isUnlocked(
                        world,
                        x + 1,
                        z
                )) {

                    String key =
                            world.getName()
                                    + ":"
                                    + x
                                    + ":"
                                    + z
                                    + ":"
                                    + BorderSide.EAST.name();

                    wantedKeys.add(key);

                    updateEdge(
                            player,
                            world,
                            x,
                            z,
                            BorderSide.EAST
                    );
                }
            }
        }

        /*
         * Displays entfernen, die nicht mehr
         * zur aktuellen Ansicht gehören.
         */
        Iterator<Map.Entry<String, ItemDisplay>> iterator =
                displays.entrySet().iterator();

        while (iterator.hasNext()) {

            Map.Entry<String, ItemDisplay> entry =
                    iterator.next();

            if (wantedKeys.contains(
                    entry.getKey()
            )) {
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

    /*
     * Sucht bevorzugt eine Oberfläche
     * in unmittelbarer Nähe der Spielerhöhe.
     */
    private Integer findBorderY(
            World world,
            int x,
            int z,
            int playerY
    ) {

        for (
                int offset = 0;
                offset <= 1;
                offset++
        ) {

            int[] candidates = {

                    playerY - offset,

                    playerY - offset - 1,

                    playerY + offset
            };

            for (int y : candidates) {

                if (y <= world.getMinHeight()
                        || y >= world.getMaxHeight() - 1) {

                    continue;
                }

                Block below =
                        world.getBlockAt(
                                x,
                                y - 1,
                                z
                        );

                Block feet =
                        world.getBlockAt(
                                x,
                                y,
                                z
                        );

                if (below
                        .getType()
                        .isSolid()
                        && feet.isPassable()) {

                    return y;
                }
            }
        }

        return null;
    }

    private void spawnEdge(
            Player player,
            World world,
            int blockX,
            int blockZ,
            BorderSide side
    ) {

        String key =
                world.getName()
                        + ":"
                        + blockX
                        + ":"
                        + blockZ
                        + ":"
                        + side.name();

        Integer y =
                findBorderY(
                        world,
                        blockX,
                        blockZ,
                        player.getLocation().getBlockY()
                );

        if (y == null) {
            return;
        }

        float height =
                calculateBorderHeight(
                        player,
                        blockX,
                        blockZ
                );

        Location location =
                switch (side) {

                    case NORTH ->
                            new Location(
                                    world,
                                    blockX + 0.5,
                                    y + (height / 2.0),
                                    blockZ + 0.001
                            );

                    case SOUTH ->
                            new Location(
                                    world,
                                    blockX + 0.5,
                                    y + (height / 2.0),
                                    blockZ + 0.999
                            );

                    case WEST ->
                            new Location(
                                    world,
                                    blockX + 0.001,
                                    y + (height / 2.0),
                                    blockZ + 0.5
                            );

                    case EAST ->
                            new Location(
                                    world,
                                    blockX + 0.999,
                                    y + (height / 2.0),
                                    blockZ + 0.5
                            );
                };

        ItemDisplay display =
                world.spawn(
                        location,
                        ItemDisplay.class,
                        entity -> {

                            entity.setItemStack(
                                    createBorderItem(
                                            false
                                    )
                            );

                            entity.setBillboard(
                                    Display.Billboard.FIXED
                            );

                            entity.setBrightness(
                                    new Display.Brightness(
                                            15,
                                            15
                                    )
                            );

                            entity.setViewRange(
                                    0.7f
                            );

                            entity.setShadowRadius(
                                    0.0f
                            );

                            entity.setShadowStrength(
                                    0.0f
                            );

                            entity.setPersistent(
                                    false
                            );

                            entity.setInvulnerable(
                                    true
                            );

                            entity.setItemDisplayTransform(
                                    ItemDisplay
                                            .ItemDisplayTransform
                                            .NONE
                            );

                            entity.setTransformation(
                                    getTransformation(
                                            side,
                                            height
                                    )
                            );
                        }
                );

        playerDisplays
                .computeIfAbsent(
                        player.getUniqueId(),
                        ignored -> new HashMap<>()
                )
                .put(
                        key,
                        display
                );
    }

    private float calculateBorderHeight(
            Player player,
            int blockX,
            int blockZ
    ) {

        double dx =
                (blockX + 0.5)
                        - player.getLocation().getX();

        double dz =
                (blockZ + 0.5)
                        - player.getLocation().getZ();

        double distance =
                Math.sqrt(
                        dx * dx + dz * dz
                );


        double progress =
                Math.min(
                        distance / DISPLAY_RADIUS,
                        1.0
                );


        return (float) (
                BORDER_HEIGHT_NEAR
                        - (
                        BORDER_HEIGHT_NEAR
                                - BORDER_HEIGHT_FAR
                ) * progress
        );
    }

    private Transformation getTransformation(
            BorderSide side,
            float height
    ) {

        float yaw = switch (side) {

            case NORTH ->
                    0f;

            case SOUTH ->
                    (float) Math.toRadians(
                            180
                    );

            case WEST ->
                    (float) Math.toRadians(
                            90
                    );

            case EAST ->
                    (float) Math.toRadians(
                            -90
                    );
        };


        return new Transformation(

                new Vector3f(
                        0f,
                        0f,
                        0f
                ),

                new AxisAngle4f(
                        yaw,
                        0f,
                        1f,
                        0f
                ),

                new Vector3f(
                        BORDER_LENGTH,
                        height,
                        BORDER_THICKNESS
                ),

                new AxisAngle4f()
        );
    }

    private ItemStack createBorderItem(
            boolean green
    ) {

        ItemStack item =
                ItemStack.of(
                        Material.PAPER
                );

        item.setData(
                DataComponentTypes.ITEM_MODEL,
                Key.key(
                        "bastighg_challenges",

                        green
                                ? "levelblock/border_green"
                                : "levelblock/border_red"
                )
        );

        return item;
    }

    private void setEdgeGreen(
            World world,
            int x,
            int z,
            BorderSide side
    ) {

        String key =
                world.getName()
                        + ":"
                        + x
                        + ":"
                        + z
                        + ":"
                        + side.name();

        for (
                Map<String, ItemDisplay> displays
                : playerDisplays.values()
        ) {

            ItemDisplay display =
                    displays.get(key);

            if (display == null
                    || !display.isValid()) {

                continue;
            }

            display.setItemStack(
                    createBorderItem(
                            true
                    )
            );
        }
    }

    private void setEdgeRed(
            World world,
            int x,
            int z,
            BorderSide side
    ) {

        String key =
                world.getName()
                        + ":"
                        + x
                        + ":"
                        + z
                        + ":"
                        + side.name();

        for (
                Map<String, ItemDisplay> displays
                : playerDisplays.values()
        ) {

            ItemDisplay display =
                    displays.get(key);

            if (display == null
                    || !display.isValid()) {

                continue;
            }

            display.setItemStack(
                    createBorderItem(
                            false
                    )
            );
        }
    }

    private void flashGreen(
            World world,
            int centerX,
            int centerZ
    ) {

        int[][] blocks = {

                {
                        centerX,
                        centerZ
                },

                {
                        centerX + 1,
                        centerZ
                },

                {
                        centerX - 1,
                        centerZ
                },

                {
                        centerX,
                        centerZ + 1
                },

                {
                        centerX,
                        centerZ - 1
                }
        };

        for (int[] block : blocks) {

            int x =
                    block[0];

            int z =
                    block[1];

            for (
                    BorderSide side
                    : BorderSide.values()
            ) {

                setEdgeGreen(
                        world,
                        x,
                        z,
                        side
                );
            }
        }

        game.getPlugin()
                .getServer()
                .getScheduler()
                .runTaskLater(

                        game.getPlugin(),

                        () -> {

                            for (
                                    int[] block
                                    : blocks
                            ) {

                                int x =
                                        block[0];

                                int z =
                                        block[1];

                                for (
                                        BorderSide side
                                        : BorderSide.values()
                                ) {

                                    setEdgeRed(
                                            world,
                                            x,
                                            z,
                                            side
                                    );
                                }
                            }
                        },

                        40L
                );
    }

    public void removePlayerDisplays(
            Player player
    ) {

        Map<String, ItemDisplay> displays =
                playerDisplays.remove(
                        player.getUniqueId()
                );

        if (displays == null) {
            return;
        }

        for (ItemDisplay display : displays.values()) {

            if (display != null
                    && display.isValid()) {

                display.remove();
            }
        }
    }

    private void removeAll() {

        for (
                Map<String, ItemDisplay> displays
                : playerDisplays.values()
        ) {

            for (
                    Entity entity
                    : displays.values()
            ) {

                if (entity != null
                        && entity.isValid()) {

                    entity.remove();
                }
            }

            displays.clear();
        }

        playerDisplays.clear();
    }

    private enum BorderSide {

        NORTH,
        SOUTH,
        WEST,
        EAST
    }
}