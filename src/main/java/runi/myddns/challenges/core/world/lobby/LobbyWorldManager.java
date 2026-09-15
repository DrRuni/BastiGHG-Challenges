package runi.myddns.challenges.core.world.lobby;

import org.bukkit.Difficulty;
import org.bukkit.GameRules;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import runi.myddns.challenges.ChallengeMain;
import runi.myddns.challenges.core.world.GameWorldDefinition;

public class LobbyWorldManager {

    private final ChallengeMain plugin;
    private World lobbyWorld;

    public LobbyWorldManager(ChallengeMain plugin) {
        this.plugin = plugin;
    }

    public void loadWorld() {
        lobbyWorld = plugin.getGameWorldManager().loadWorld(GameWorldDefinition.LOBBY);

        if (lobbyWorld == null) {
            plugin.getLogger().severe("Lobby-Welt konnte nicht geladen werden.");
            return;
        }

        configureWorld();
        setSpawn();
        applyWarmOceanBiome();

        plugin.getLogger().info("Lobby-Welt erfolgreich geladen: " + lobbyWorld.getKey());
    }

    private void applyWarmOceanBiome() {
        if (lobbyWorld == null) return;

        int radius = 8;

        for (int chunkX = -radius; chunkX <= radius; chunkX++) {
            for (int chunkZ = -radius; chunkZ <= radius; chunkZ++) {

                lobbyWorld.getChunkAt(chunkX, chunkZ).load();

                int startX = chunkX << 4;
                int startZ = chunkZ << 4;

                for (int x = startX; x < startX + 16; x += 4) {
                    for (int z = startZ; z < startZ + 16; z += 4) {
                        for (int y = lobbyWorld.getMinHeight(); y < lobbyWorld.getMaxHeight(); y += 4) {
                            lobbyWorld.setBiome(x, y, z, Biome.WARM_OCEAN);
                        }
                    }
                }
            }
        }
    }

    private void configureWorld() {
        if (lobbyWorld == null) return;

        lobbyWorld.setDifficulty(Difficulty.PEACEFUL);
        lobbyWorld.setGameRule(GameRules.SPAWN_MOBS, true);
        lobbyWorld.setGameRule(GameRules.SPAWN_PATROLS, false);
        lobbyWorld.setGameRule(GameRules.SPAWN_WANDERING_TRADERS, false);
        lobbyWorld.setGameRule(GameRules.SPAWN_PHANTOMS, false);
        lobbyWorld.setTime(6000);
        lobbyWorld.setStorm(false);
    }

    private void setSpawn() {
        if (lobbyWorld == null) return;
        lobbyWorld.setSpawnLocation(getSpawn());
    }

    public Location getSpawn() {
        if (lobbyWorld == null) {
            throw new IllegalStateException("Lobby-Welt wurde noch nicht geladen.");
        }

        return new Location(lobbyWorld, 0.5, 67.0, 4.5, 0.0f, 0.0f);
    }

    public World getLobbyWorld() {
        return lobbyWorld;
    }
}