
package runi.myddns.challenges.games.chunkrandomizer.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import runi.myddns.challenges.core.world.GameWorldDefinition;
import runi.myddns.challenges.games.chunkrandomizer.ChunkRandomizerGame;

import java.time.Duration;

public class ChunkRandomizerPlayerListener implements Listener {

    private final ChunkRandomizerGame game;

    public ChunkRandomizerPlayerListener(ChunkRandomizerGame game) {
        this.game = game;
    }

    public void showWelcomeSequence(Player player) {

        if (!player.isOnline() || !isGamePlayer(player)) return;

        player.sendMessage("");

        player.sendMessage(
                Component.text("ChunkRandomizer")
                        .color(TextColor.color(0xFF8800))
        );

        player.sendMessage(
                Component.text("Willkommen bei ChunkRandomizer!")
        );

        player.sendMessage("");

        Bukkit.getScheduler().runTaskLater(
                game.getPlugin(),
                () -> {

                    if (!player.isOnline() || !isGamePlayer(player)) {
                        return;
                    }

                    player.playSound(
                            player.getLocation(),
                            Sound.BLOCK_AMETHYST_BLOCK_CHIME,
                            0.35f,
                            1.2f
                    );

                    Component title = Component.text("ChunkRandomizer")
                            .color(TextColor.color(0xFF8800));

                    player.showTitle(
                            Title.title(
                                    title,
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

        if (!isGamePlayer(player)) return;

        // Vanilla-Respawn bevorzugen:
        // Bett oder gültiger Nether-Respawnanker.
        Location vanillaRespawn = event.getRespawnLocation();

        if (vanillaRespawn.getWorld() != null
                && isGameWorld(vanillaRespawn.getWorld())
                && (event.isBedSpawn() || event.isAnchorSpawn())) {
            return;
        }

        // Fallback: ChunkRandomizer-Overworld.
        World world = Bukkit.getWorld(
                GameWorldDefinition.CHUNK_RANDOMIZER_OVERWORLD.worldName()
        );

        if (world == null) return;

        event.setRespawnLocation(world.getSpawnLocation());
    }

    private boolean isGamePlayer(Player player) {
        return player != null
                && isGameWorld(player.getWorld());
    }

    private boolean isGameWorld(World world) {
        return world != null
                && game.getPlugin()
                .getGameWorldManager()
                .isGameWorld(world, game.getId());
    }
}

