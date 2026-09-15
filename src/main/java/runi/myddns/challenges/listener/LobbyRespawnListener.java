package runi.myddns.challenges.listener;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import runi.myddns.challenges.core.world.lobby.LobbyWorldManager;

public class LobbyRespawnListener
        implements Listener {

    private final LobbyWorldManager lobbyWorldManager;

    public LobbyRespawnListener(
            LobbyWorldManager lobbyWorldManager
    ) {

        this.lobbyWorldManager =
                lobbyWorldManager;
    }

    @EventHandler
    public void onRespawn(
            PlayerRespawnEvent event
    ) {

        World lobby =
                lobbyWorldManager.getLobbyWorld();

        if (lobby == null) {
            return;
        }

        event.setRespawnLocation(
                new Location(
                        lobby,
                        0.5,
                        67.0,
                        4.5
                )
        );
    }
}