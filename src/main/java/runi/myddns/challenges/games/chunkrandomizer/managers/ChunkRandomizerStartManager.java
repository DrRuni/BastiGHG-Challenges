
package runi.myddns.challenges.games.chunkrandomizer.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import runi.myddns.challenges.ChallengeMain;

public class ChunkRandomizerStartManager {

    private final ChallengeMain plugin;
    private final ChunkRandomizerBorderManager borderManager;

    private boolean started = false;

    public ChunkRandomizerStartManager(
            ChallengeMain plugin,
            ChunkRandomizerBorderManager borderManager
    ) {
        this.plugin = plugin;
        this.borderManager = borderManager;
    }

    /**
     * Startet ChunkRandomizer in dem Chunk,
     * in dem sich der ausführende Spieler befindet.
     */
    public boolean start(Player initiator) {

        if (initiator == null || !initiator.isOnline()) {
            return false;
        }

        if (started) {
            initiator.sendMessage(
                    "§cChunkRandomizer wurde bereits gestartet."
            );
            return false;
        }

        // Prüfen, ob der Spieler in einer ChunkRandomizer-Welt ist
        if (!plugin.getGameWorldManager()
                .isGameWorld(initiator.getWorld(), "chunkrandomizer")) {

            initiator.sendMessage(
                    "§cDu musst dich in einer ChunkRandomizer-Welt befinden."
            );
            return false;
        }

        // Position des ausführenden Spielers merken
        Location startLocation = initiator.getLocation().clone();

        // WorldBorder um den aktuellen Chunk erstellen
        borderManager.createBorder(initiator);

        started = true;

        int teleported = 0;

        // Alle anderen ChunkRandomizer-Spieler zusammenführen
        for (Player player : Bukkit.getOnlinePlayers()) {

            if (player.equals(initiator)) {
                continue;
            }

            if (!plugin.getGameWorldManager()
                    .isGamePlayer(player, "chunkrandomizer")) {
                continue;
            }

            if (player.teleport(startLocation.clone())) {
                teleported++;
            }
        }

        plugin.getGameStateManager().setStarted(true);

        initiator.sendMessage(
                "§aChunkRandomizer gestartet! §7Spieler: "
                        + (teleported + 1)
        );

        plugin.getLogger().info(
                "ChunkRandomizer gestartet | Spieler: "
                        + (teleported + 1)
                        + " | Welt: "
                        + startLocation.getWorld().getName()
                        + " | Chunk: "
                        + (startLocation.getBlockX() >> 4)
                        + ","
                        + (startLocation.getBlockZ() >> 4)
        );

        return true;
    }

    public boolean restore(ChunkRandomizerDataManager dataManager) {

        if (started || dataManager == null || !dataManager.isStarted()) {
            return false;
        }

        FileConfiguration data = dataManager.getData();
        if (data == null) return false;

        boolean borderActive = data.getBoolean("border.active", false);

        if (borderActive) {

            String worldName = data.getString("border.world", "");
            World world = Bukkit.getWorld(worldName);

            if (world == null || !plugin.getGameWorldManager()
                    .isGameWorld(world, "chunkrandomizer")) {

                plugin.getLogger().warning(
                        "ChunkRandomizer: Gespeicherte Spielwelt nicht gefunden: "
                                + worldName
                );
                return false;
            }

            double centerX = data.getDouble("border.center.x");
            double centerZ = data.getDouble("border.center.z");
            double size = data.getDouble("border.size", 16.0);

            if (!borderManager.restoreBorder(
                    world,
                    centerX,
                    centerZ,
                    size
            )) {
                plugin.getLogger().warning(
                        "ChunkRandomizer: WorldBorder konnte nicht wiederhergestellt werden."
                );
                return false;
            }
        }

        // Event bleibt gestartet, auch wenn die Border frei ist
        started = true;
        plugin.getGameStateManager().setStarted(true);

        plugin.getLogger().info(
                borderActive
                        ? "ChunkRandomizer: Laufende Mob-Runde wiederhergestellt."
                        : "ChunkRandomizer: Event fortgesetzt - Border bereits freigegeben."
        );

        return true;
    }

    public void stop() {
        borderManager.removeBorder();
        started = false;

        plugin.getGameStateManager().setStarted(false);
    }

    public boolean isStarted() {
        return started;
    }
}