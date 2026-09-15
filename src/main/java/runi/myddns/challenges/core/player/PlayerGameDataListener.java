package runi.myddns.challenges.core.player;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import runi.myddns.challenges.ChallengeMain;
import runi.myddns.challenges.core.world.GameWorldDefinition;

public class PlayerGameDataListener implements Listener {

    private final ChallengeMain plugin;
    private final PlayerGameDataManager playerGameDataManager;

    public PlayerGameDataListener(ChallengeMain plugin) {
        this.plugin = plugin;
        this.playerGameDataManager = plugin.getPlayerGameDataManager();
    }

    // ============================================================
    // VOR WELTWECHSEL -> ALTES GAME SPEICHERN
    // ============================================================

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getTo() == null) return;
        if (event.getFrom().getWorld().equals(event.getTo().getWorld())) return;

        Player player = event.getPlayer();

        String oldGameId = getGameId(event.getFrom().getWorld());
        String newGameId = getGameId(event.getTo().getWorld());

        if (oldGameId == null) return;
        if (oldGameId.equalsIgnoreCase(newGameId)) return;

        playerGameDataManager.savePlayerData(player, oldGameId);
    }

    // ============================================================
    // NACH WELTWECHSEL -> NEUES GAME LADEN
    // ============================================================

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        String oldGameId = getGameId(event.getFrom());
        String newGameId = getGameId(player.getWorld());

        if (oldGameId == null && newGameId == null) return;

        // Wechsel innerhalb desselben Games
        if (oldGameId != null && oldGameId.equalsIgnoreCase(newGameId)) return;

        if (newGameId == null) return;

        if (playerGameDataManager.hasPlayerData(player.getUniqueId(), newGameId)) {
            playerGameDataManager.loadPlayerData(player, newGameId, false);
        } else {
            playerGameDataManager.clearPlayerState(player);
        }
    }

    // ============================================================
    // SERVER VERLASSEN
    // ============================================================

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        String gameId = getGameId(player.getWorld());
        if (gameId == null) return;

        playerGameDataManager.savePlayerData(player, gameId);
    }

    // ============================================================
    // SERVER JOIN
    // ============================================================

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;

            String gameId = getGameId(player.getWorld());
            if (gameId == null) return;

            if (playerGameDataManager.hasPlayerData(player.getUniqueId(), gameId)) {
                playerGameDataManager.loadPlayerData(player, gameId, false);
            } else {
                playerGameDataManager.clearPlayerState(player);
            }

        }, 2L);
    }

    // ============================================================
    // GAME ZUR WELT FINDEN
    // ============================================================

    private String getGameId(World world) {
        if (world == null) return null;

        for (GameWorldDefinition definition : GameWorldDefinition.ALL) {
            if (definition.worldName().equalsIgnoreCase(world.getName())) {
                return definition.gameId();
            }
        }

        return null;
    }
}