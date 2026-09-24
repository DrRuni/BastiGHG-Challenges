package runi.myddns.challenges.games.LevelBlock.Manager;

import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import runi.myddns.challenges.games.LevelBlock.LevelBlockGame;

import java.util.Set;

public class BorderManager {

    private final BorderDisplayManager displayManager;
    private final BorderLineManager lineManager;
    private final LevelBlockDataManager dataManager;
    private final LevelBlockGame game;


    public BorderManager(LevelBlockGame game) {
        this.game = game;
        this.dataManager = new LevelBlockDataManager(game);
        this.displayManager = new BorderDisplayManager(game, this);
        this.lineManager = new BorderLineManager(game, this);
    }

    public boolean startGame(Player starter) {
        if (starter == null) return false;

        World world = starter.getWorld();

        if (!game.isLevelBlockPlayer(starter)) return false;

        if (isInitialized(world)) {
            return false;
        }

        Location start = starter.getLocation()
                .getBlock()
                .getLocation()
                .add(0.5, 0.0, 0.5);

        dataManager.saveStartLocation(world, start);

        int centerX = start.getBlockX();
        int centerZ = start.getBlockZ();

        for (int x = centerX - 1; x <= centerX + 1; x++) {
            for (int z = centerZ - 1; z <= centerZ + 1; z++) {
                dataManager.saveUnlockedBlock(world, x, z);
            }
        }

        lineManager.reset();

        for (LevelBlockPos pos : dataManager.getUnlockedBlocks(world)) {
            lineManager.rebuildColumn(world, pos.x(), pos.z());
        }

        for (Player player : world.getPlayers()) {
            if (!game.isLevelBlockPlayer(player)) continue;
            player.teleport(start);
        }

        displayManager.refreshAll();
        lineManager.refreshAll();

        return true;
    }

    public boolean isInside(
            Location location
    ) {

        if (location == null
                || location.getWorld() == null) {

            return false;
        }

        return dataManager.isUnlocked(
                location.getWorld(),
                location.getBlockX(),
                location.getBlockZ()
        );
    }

    public Location getStartLocation(
            World world
    ) {

        return dataManager.getStartLocation(
                world
        );
    }

    public Location prepareStartLocation(
            World world
    ) {

        Location saved =
                dataManager.getStartLocation(
                        world
                );

        if (saved != null) {
            return saved;
        }


        Location worldSpawn =
                world.getSpawnLocation();

        int startX =
                worldSpawn.getBlockX();

        int startZ =
                worldSpawn.getBlockZ();

        int searchRadius =
                128;


        for (
                int radius = 0;
                radius <= searchRadius;
                radius++
        ) {

            for (
                    int x = startX - radius;
                    x <= startX + radius;
                    x++
            ) {

                for (
                        int z = startZ - radius;
                        z <= startZ + radius;
                        z++
                ) {

                    Location safeLocation =
                            findSafeLocation(
                                    world,
                                    x,
                                    z
                            );

                    if (safeLocation != null) {

                        return setStartLocation(
                                safeLocation
                        );
                    }
                }
            }
        }

        throw new IllegalStateException(
                "Für LevelBlock wurde kein sicherer Startblock gefunden."
        );
    }

    public void removePlayerView(Player player) {

        displayManager.removePlayerDisplays(player);
        lineManager.removePlayer(player);
    }

    private Location findSafeLocation(
            World world,
            int x,
            int z
    ) {

        int y =
                world.getHighestBlockYAt(
                        x,
                        z,
                        HeightMap.MOTION_BLOCKING_NO_LEAVES
                );

        Block ground =
                world.getBlockAt(
                        x,
                        y,
                        z
                );

        Block feet =
                world.getBlockAt(
                        x,
                        y + 1,
                        z
                );

        Block head =
                world.getBlockAt(
                        x,
                        y + 2,
                        z
                );

        Material groundType =
                ground.getType();


        if (!groundType.isSolid()) {
            return null;
        }


        if (groundType == Material.MAGMA_BLOCK
                || groundType == Material.CACTUS
                || groundType == Material.POWDER_SNOW) {

            return null;
        }


        if (!feet.isPassable()) {
            return null;
        }


        if (!head.isPassable()) {
            return null;
        }


        return new Location(
                world,
                x + 0.5,
                y + 1.0,
                z + 0.5
        );
    }


    private Location setStartLocation(
            Location location
    ) {

        World world =
                location.getWorld();

        if (world == null) {
            return null;
        }


        int x =
                location.getBlockX();

        int z =
                location.getBlockZ();


        /*
         * Alte Daten dieser Welt entfernen.
         */
        dataManager.resetWorld(
                world
        );


        /*
         * Startpunkt speichern.
         */
        dataManager.saveStartLocation(
                world,
                location
        );


        /*
         * Startblock gleichzeitig freischalten.
         */
        dataManager.saveUnlockedBlock(
                world,
                x,
                z
        );


        /*
         * Liniencache dieser Welt neu aufbauen.
         *
         * Diesen Teil passen wir im nächsten
         * Schritt noch auf Multiworld an.
         */
        lineManager.reset();


        displayManager.refreshAll();
        lineManager.refreshAll();


        return location.clone();
    }


    /*
     * =========================================================
     * FREIGESCHALTETE BLÖCKE
     * =========================================================
     */

    public boolean isUnlocked(
            World world,
            int x,
            int z
    ) {

        return dataManager.isUnlocked(
                world,
                x,
                z
        );
    }


    public boolean isAdjacentToUnlocked(
            World world,
            int x,
            int z
    ) {

        return isUnlocked(
                world,
                x + 1,
                z
        )
                || isUnlocked(
                world,
                x - 1,
                z
        )
                || isUnlocked(
                world,
                x,
                z + 1
        )
                || isUnlocked(
                world,
                x,
                z - 1
        );
    }


    /*
     * =========================================================
     * BLOCK FREISCHALTEN
     * =========================================================
     */

    public boolean unlockBlock(
            World world,
            int x,
            int z
    ) {

        if (world == null) {
            return false;
        }


        if (isUnlocked(
                world,
                x,
                z
        )) {

            return false;
        }


        if (!isAdjacentToUnlocked(
                world,
                x,
                z
        )) {

            return false;
        }


        /*
         * RAM + gamedata.yml
         */
        dataManager.saveUnlockedBlock(
                world,
                x,
                z
        );

        displayManager.updateAfterUnlock(
                world,
                x,
                z
        );

        lineManager.updateAfterUnlock(
                world,
                x,
                z
        );

        return true;
    }

    public boolean canExpand(
            Player player
    ) {

        return player.getLevel() > 0;
    }


    public void consumeLevel(
            Player player
    ) {

        player.setLevel(
                player.getLevel() - 1
        );
    }

    public void startRenderer() {

        displayManager.start();
        lineManager.start();
    }


    public void stopRenderer() {

        lineManager.stop();
        displayManager.stop();
    }


    /*
     * =========================================================
     * DATEN ABFRAGEN
     * =========================================================
     */

    public Set<LevelBlockPos> getUnlockedBlocks(
            World world
    ) {

        return dataManager.getUnlockedBlocks(
                world
        );
    }


    public boolean isInitialized(
            World world
    ) {

        return dataManager.getStartLocation(
                world
        ) != null;
    }


    public LevelBlockDataManager getDataManager() {
        return dataManager;
    }


    public BorderDisplayManager getDisplayManager() {
        return displayManager;
    }


    public BorderLineManager getLineManager() {
        return lineManager;
    }
}