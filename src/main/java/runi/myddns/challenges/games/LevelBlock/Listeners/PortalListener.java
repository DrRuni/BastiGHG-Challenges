package runi.myddns.challenges.games.LevelBlock.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import runi.myddns.challenges.core.world.GameWorldDefinition;
import runi.myddns.challenges.games.LevelBlock.LevelBlockGame;
import runi.myddns.challenges.games.LevelBlock.Manager.BorderManager;
import runi.myddns.challenges.games.LevelBlock.Manager.LevelBlockPos;

public class PortalListener implements Listener {

    private final LevelBlockGame game;

    public PortalListener(LevelBlockGame game) {
        this.game = game;
    }

    @EventHandler(
            priority = EventPriority.HIGH,
            ignoreCancelled = true
    )
    public void onPortal(PlayerPortalEvent event) {

        Player player = event.getPlayer();

        if (!game.isLevelBlockPlayer(player)) {
            return;
        }

        if (event.getCause()
                == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {

            handleNetherPortal(event);
        }

        Bukkit.getScheduler().runTaskLater(
                game.getPlugin(),
                () -> {

                    initializeDestination(player);

                    BorderManager border =
                            game.getBorderManager();

                    border.getDisplayManager()
                            .refreshAll();

                    border.getLineManager()
                            .refreshAll();

                    if (player.getWorld()
                            .getName()
                            .equalsIgnoreCase(
                                    GameWorldDefinition
                                            .LEVEL_BLOCK_OVERWORLD
                                            .worldName()
                            )) {

                        faceUnlockedExit(player);
                    }
                },
                2L
        );
    }

    private void handleNetherPortal(
            PlayerPortalEvent event
    ) {

        World fromWorld =
                event.getFrom().getWorld();

        if (fromWorld == null) {
            return;
        }

        String worldName =
                fromWorld.getName();

        /*
         * OVERWORLD -> LEVELBLOCK NETHER
         */
        if (worldName.equalsIgnoreCase(
                GameWorldDefinition.LEVEL_BLOCK_OVERWORLD.worldName()
        )) {

            World nether =
                    Bukkit.getWorld(
                            GameWorldDefinition.LEVEL_BLOCK_NETHER.worldName()
                    );

            if (nether == null) {
                return;
            }

            Location from =
                    event.getFrom();

            Location target =
                    new Location(
                            nether,
                            from.getX() / 8.0,
                            from.getY(),
                            from.getZ() / 8.0,
                            from.getYaw(),
                            from.getPitch()
                    );

            event.setTo(target);

            return;
        }

        /*
         * LEVELBLOCK NETHER -> OVERWORLD
         */
        if (worldName.equalsIgnoreCase(
                GameWorldDefinition.LEVEL_BLOCK_NETHER.worldName()
        )) {

            World overworld =
                    Bukkit.getWorld(
                            GameWorldDefinition.LEVEL_BLOCK_OVERWORLD.worldName()
                    );

            if (overworld == null) {
                return;
            }

            Location from =
                    event.getFrom();

            double targetX =
                    from.getX() * 8.0;

            double targetZ =
                    from.getZ() * 8.0;

            BorderManager border =
                    game.getBorderManager();

            LevelBlockPos nearest =
                    null;

            double nearestDistance =
                    Double.MAX_VALUE;

            for (LevelBlockPos pos :
                    border.getUnlockedBlocks(overworld)) {

                double dx =
                        pos.x() - targetX;

                double dz =
                        pos.z() - targetZ;

                double distance =
                        dx * dx + dz * dz;

                if (distance < nearestDistance) {

                    nearestDistance =
                            distance;

                    nearest =
                            pos;
                }
            }

            if (nearest == null) {
                return;
            }

            int x =
                    nearest.x();

            int z =
                    nearest.z();

            int y =
                    overworld.getHighestBlockYAt(
                            x,
                            z
                    ) + 1;

            Location target =
                    new Location(
                            overworld,
                            x + 0.5,
                            y + 1.0,
                            z + 0.5,
                            from.getYaw(),
                            from.getPitch()
                    );

            event.setTo(target);
            event.setSearchRadius(16);
            event.setCanCreatePortal(false);

            createPortalFrame(
                    target
            );
        }
    }

    private void createPortalFrame(
            Location location
    ) {

        World world =
                location.getWorld();

        if (world == null) {
            return;
        }

        int x =
                location.getBlockX();

        int y =
                location.getBlockY();

        int z =
                location.getBlockZ();

        for (int dx = -1; dx <= 2; dx++) {

            world.getBlockAt(
                    x + dx,
                    y,
                    z
            ).setType(Material.OBSIDIAN, false);

            world.getBlockAt(
                    x + dx,
                    y + 4,
                    z
            ).setType(Material.OBSIDIAN, false);
        }

        for (int dy = 1; dy <= 3; dy++) {

            world.getBlockAt(
                    x - 1,
                    y + dy,
                    z
            ).setType(Material.OBSIDIAN, false);

            world.getBlockAt(
                    x + 2,
                    y + dy,
                    z
            ).setType(Material.OBSIDIAN, false);
        }

        /*
         * Portalfläche.
         */
        for (int dx = 0; dx <= 1; dx++) {

            for (int dy = 1; dy <= 3; dy++) {

                world.getBlockAt(
                        x + dx,
                        y + dy,
                        z
                ).setType(
                        Material.NETHER_PORTAL,
                        false
                );
            }
        }
    }

    private void faceUnlockedExit(
            Player player
    ) {

        World world =
                player.getWorld();

        if (!world.getName().equalsIgnoreCase(
                GameWorldDefinition.LEVEL_BLOCK_OVERWORLD.worldName()
        )) {
            return;
        }

        BorderManager border =
                game.getBorderManager();

        int x =
                player.getLocation().getBlockX();

        int z =
                player.getLocation().getBlockZ();

        int[][] directions = {
                {1, 0},
                {-1, 0},
                {0, 1},
                {0, -1}
        };

        for (int[] direction : directions) {

            int targetX =
                    x + direction[0];

            int targetZ =
                    z + direction[1];

            if (!border.isUnlocked(
                    world,
                    targetX,
                    targetZ
            )) {
                continue;
            }

            Location location =
                    player.getLocation();

            float yaw;

            if (direction[0] == 1) {
                yaw = -90.0f;
            } else if (direction[0] == -1) {
                yaw = 90.0f;
            } else if (direction[1] == 1) {
                yaw = 0.0f;
            } else {
                yaw = 180.0f;
            }

            location.setYaw(yaw);

            player.teleport(location);

            return;
        }
    }

    private void initializeDestination(
            Player player
    ) {

        if (!player.isOnline()) {
            return;
        }

        if (!game.isLevelBlockPlayer(player)) {
            return;
        }

        World world =
                player.getWorld();

        BorderManager border =
                game.getBorderManager();

        if (border.isInitialized(world)) {
            return;
        }

        Location location =
                player.getLocation()
                        .getBlock()
                        .getLocation()
                        .add(
                                0.5,
                                0.0,
                                0.5
                        );

        border.initializePortalArea(
                location
        );
    }
}