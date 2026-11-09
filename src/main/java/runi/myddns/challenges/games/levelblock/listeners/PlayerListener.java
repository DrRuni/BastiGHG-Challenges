package runi.myddns.challenges.games.levelblock.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import runi.myddns.challenges.core.utils.ColorUtil;
import runi.myddns.challenges.core.world.GameWorldDefinition;
import runi.myddns.challenges.games.levelblock.LevelBlockGame;

import java.time.Duration;

public class PlayerListener implements Listener {

    private final LevelBlockGame game;

    public PlayerListener(LevelBlockGame game) {
        this.game = game;
    }


    public void showWelcomeSequence(Player player) {

        if (!player.isOnline()) return;
        if (!game.isLevelBlockPlayer(player)) return;

        player.sendMessage("");

        player.sendMessage(
                ColorUtil.borderColor(
                        game.getLanguageManager()
                                .get("player-listener.welcome.name")
                )
        );

        player.sendMessage("");

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent("player-listener.welcome.settings")
        );

        player.sendMessage(
                game.getLanguageManager()
                        .getComponent("player-listener.welcome.info")
        );

        player.sendMessage("");

        // Logo nach 40 Ticks anzeigen
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

        Location vanillaRespawn = event.getRespawnLocation();

        // Gültiges Bett in der LevelBlock-Overworld
        // oder Respawnanker im LevelBlock-Nether
        if (vanillaRespawn.getWorld() != null) {

            String worldName = vanillaRespawn.getWorld().getName();

            if (event.isBedSpawn()
                    && worldName.equalsIgnoreCase(
                    GameWorldDefinition.LEVEL_BLOCK_OVERWORLD.worldName())) {
                return;
            }

            if (event.isAnchorSpawn()
                    && worldName.equalsIgnoreCase(
                    GameWorldDefinition.LEVEL_BLOCK_NETHER.worldName())) {
                return;
            }
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

        event.setRespawnLocation(world.getSpawnLocation());
    }

}
