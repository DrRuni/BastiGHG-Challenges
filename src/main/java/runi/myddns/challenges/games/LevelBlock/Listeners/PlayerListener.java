package runi.myddns.challenges.games.LevelBlock.Listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import runi.myddns.challenges.core.utils.ColorUtil;
import runi.myddns.challenges.core.world.GameWorldDefinition;
import runi.myddns.challenges.games.LevelBlock.LevelBlockGame;

import java.time.Duration;

public class PlayerListener implements Listener {

    private final LevelBlockGame game;

    public PlayerListener(LevelBlockGame game) {
        this.game = game;
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        if (!game.isLevelBlockPlayer(player)) return;

        player.sendMessage("");
        player.sendMessage(ColorUtil.borderColor("Run´s LevelBlock"));
        player.sendMessage("");
        player.sendMessage(ChatColor.GRAY + "Einstellungen: " + ChatColor.AQUA + "/gamesettings");
        player.sendMessage(ChatColor.GRAY + "Spielinfo: " + ChatColor.AQUA + "/levelblock info");
        player.sendMessage("");

        Bukkit.getScheduler().runTaskLater(
                game.getPlugin(),
                () -> {

                    if (!player.isOnline()) return;
                    if (!game.isLevelBlockPlayer(player)) return;

                    Component titleImage =
                            Component.text("\uE033");

                    player.playSound(
                            player.getLocation(),
                            Sound.BLOCK_AMETHYST_BLOCK_CHIME,
                            0.35f,
                            1.2f
                    );

                    player.showTitle(
                            Title.title(
                                    titleImage,
                                    Component.empty(),
                                    Title.Times.times(
                                            Duration.ofMillis(1000),
                                            Duration.ofSeconds(2),
                                            Duration.ofMillis(800)
                                    )
                            )
                    );

                },
                40L
        );
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (!game.isLevelBlockPlayer(player)) return;

        Location bedSpawn = player.getRespawnLocation();

        if (bedSpawn != null
                && bedSpawn.getWorld() != null
                && bedSpawn.getWorld().getName().equalsIgnoreCase(
                GameWorldDefinition.LEVEL_BLOCK_OVERWORLD.worldName()
        )) {

            event.setRespawnLocation(bedSpawn);
            return;
        }

        World world = Bukkit.getWorld(
                GameWorldDefinition.LEVEL_BLOCK_OVERWORLD.worldName()
        );

        if (world == null) return;

        Location startLocation =
                game.getBorderManager()
                        .getDataManager()
                        .getStartLocation(world);

        if (startLocation != null) {
            event.setRespawnLocation(startLocation);
            return;
        }

        event.setRespawnLocation(
                world.getSpawnLocation()
        );
    }
}
