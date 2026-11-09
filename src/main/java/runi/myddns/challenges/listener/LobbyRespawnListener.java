
package runi.myddns.challenges.listener;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import runi.myddns.challenges.ChallengeMain;
import runi.myddns.challenges.core.world.lobby.LobbyWorldManager;

public class LobbyRespawnListener implements Listener {

    private final ChallengeMain plugin;
    private final LobbyWorldManager lobbyWorldManager;

    public LobbyRespawnListener(
            ChallengeMain plugin,
            LobbyWorldManager lobbyWorldManager
    ) {
        this.plugin = plugin;
        this.lobbyWorldManager = lobbyWorldManager;
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {

        if (plugin.getGameWorldManager().isGameWorld(
                event.getPlayer().getWorld(),
                "mobarmybattle"
        ) || plugin.getGameWorldManager().isGameWorld(
                event.getPlayer().getWorld(),
                "levelborder"
        ) || plugin.getGameWorldManager().isGameWorld(
                event.getPlayer().getWorld(),
                "levelblock"
        ) || plugin.getGameWorldManager().isGameWorld(
                event.getPlayer().getWorld(),
                "chunkrandomizer"
        )) {
            return;
        }

        World lobby = lobbyWorldManager.getLobbyWorld();

        if (lobby == null) return;

        event.setRespawnLocation(
                lobbyWorldManager.getSpawn()
        );
    }
}
