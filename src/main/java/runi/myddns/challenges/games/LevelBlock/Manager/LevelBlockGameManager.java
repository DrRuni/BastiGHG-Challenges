package runi.myddns.challenges.games.LevelBlock.Manager;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import runi.myddns.challenges.games.LevelBlock.LevelBlockGame;

public class LevelBlockGameManager {

    private final LevelBlockGame game;
    private final BorderManager borderManager;
    private final LevelBlockDataManager dataManager;

    public LevelBlockGameManager(
            LevelBlockGame game,
            BorderManager borderManager
    ) {
        this.game = game;
        this.borderManager = borderManager;
        this.dataManager = borderManager.getDataManager();
    }

    public boolean startGame(Player starter) {
        if (starter == null) return false;
        if (!game.isLevelBlockPlayer(starter)) return false;

        World world = starter.getWorld();

        if (isStarted(world)) return false;

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

        borderManager.getLineManager().reset();

        for (LevelBlockPos pos : dataManager.getUnlockedBlocks(world)) {
            borderManager.getLineManager().rebuildColumn(
                    world,
                    pos.x(),
                    pos.z()
            );
        }

        for (Player player : world.getPlayers()) {
            if (!game.isLevelBlockPlayer(player)) continue;

            player.teleport(start);
        }

        borderManager.getDisplayManager().refreshAll();
        borderManager.getLineManager().refreshAll();
        dataManager.setEventStarted(true);
        game.getTimerManager().resume();
        return true;
    }

    public void resetGame(World world) {
        if (world == null) return;

        dataManager.resetWorld(world);
        dataManager.setEventStarted(false);

        game.getTimerManager().reset();
        game.getTimerDisplayManager().clearAll();

        borderManager.getLineManager().reset();
        borderManager.getDisplayManager().refreshAll();
        borderManager.getLineManager().refreshAll();
    }

    public boolean isStarted(World world) {
        return dataManager.isEventStarted();
    }
}