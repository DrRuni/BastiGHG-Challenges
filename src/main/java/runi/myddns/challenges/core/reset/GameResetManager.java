package runi.myddns.challenges.core.reset;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import runi.myddns.challenges.ChallengeMain;
import runi.myddns.challenges.core.game.ChallengeGame;
import runi.myddns.challenges.core.world.GameWorldDefinition;

import java.time.Duration;

public class GameResetManager {

    private final ChallengeMain plugin;

    private boolean resetRunning;

    public GameResetManager(ChallengeMain plugin) {
        this.plugin = plugin;
    }

    public void resetGame(ChallengeGame game) {

        if (game == null) return;
        if (!game.isLoaded()) return;

        if (resetRunning) {
            Bukkit.broadcastMessage(
                    ChatColor.RED
                            + "❌ Es läuft bereits ein Reset."
            );
            return;
        }

        resetRunning = true;

        showTitleToAll(
                game.getDisplayName(),
                "Spielstand wird zurückgesetzt...",
                NamedTextColor.GOLD
        );

        plugin.getLogger().info(
                "Game-Reset gestartet: " + game.getId()
        );

        resetPlayers(
                game
        );

        plugin.getPlayerGameDataManager()
                .deleteGamePlayerData(
                        game.getId()
                );

        showTitleToAll(
                game.getDisplayName(),
                "Reset abgeschlossen!",
                NamedTextColor.GREEN
        );

        Bukkit.broadcastMessage(
                ChatColor.GREEN
                        + "✔ "
                        + game.getDisplayName()
                        + " wurde zurückgesetzt."
        );

        resetRunning = false;
    }

    public void resetWorlds(ChallengeGame game) {

        if (game == null) return;
        if (!game.isLoaded()) return;

        if (resetRunning) {
            Bukkit.broadcastMessage(
                    ChatColor.RED
                            + "❌ Es läuft bereits ein Reset."
            );
            return;
        }

        resetRunning = true;

        showTitleToAll(
                game.getDisplayName(),
                "Welten werden zurückgesetzt...",
                NamedTextColor.GOLD
        );

        resetPlayers(
                game
        );

        for (Player player : Bukkit.getOnlinePlayers()) {

            if (!plugin.getGameWorldManager()
                    .isGamePlayer(
                            player,
                            game.getId()
                    )) {
                continue;
            }

            player.teleport(
                    plugin.getLobbyWorldManager()
                            .getSpawn()
            );
        }

        Bukkit.broadcastMessage(
                ChatColor.GOLD
                        + "⏳ "
                        + game.getDisplayName()
                        + "-Welten werden zurückgesetzt..."
        );

        Bukkit.getScheduler()
                .runTaskLater(
                        plugin,
                        () -> {

                            plugin.getGameWorldManager()
                                    .unloadGameWorlds(
                                            game.getId(),
                                            true
                                    );

                            showTitleToAll(
                                    game.getDisplayName(),
                                    "Welten werden neu erstellt...",
                                    NamedTextColor.YELLOW
                            );

                            boolean success = true;

                            for (GameWorldDefinition definition
                                    : GameWorldDefinition.ALL) {

                                if (!definition.gameId()
                                        .equalsIgnoreCase(
                                                game.getId()
                                        )) {
                                    continue;
                                }

                                boolean result =
                                        plugin.getWorldSourceManager()
                                                .resetWorld(definition);

                                if (!result) {
                                    success = false;
                                }
                            }

                            if (!success) {

                                showTitleToAll(
                                        game.getDisplayName(),
                                        "Reset fehlgeschlagen!",
                                        NamedTextColor.RED
                                );

                                Bukkit.broadcastMessage(
                                        ChatColor.RED
                                                + "❌ Fehler beim Zurücksetzen der "
                                                + game.getDisplayName()
                                                + "-Welten."
                                );

                                resetRunning = false;
                                return;
                            }

                            plugin.getPlayerGameDataManager()
                                    .deleteGamePlayerData(
                                            game.getId()
                                    );

                            if (game.getId().equalsIgnoreCase("levelblock")) {

                                runi.myddns.challenges.games.LevelBlock.LevelBlockGame levelBlockGame =
                                        (runi.myddns.challenges.games.LevelBlock.LevelBlockGame) game;

                                levelBlockGame.resetWorldData();
                            }

                            plugin.getGameWorldManager()
                                    .loadGameWorlds(
                                            game.getId()
                                    );

                            if (game.getId().equalsIgnoreCase("levelblock")) {

                                runi.myddns.challenges.games.LevelBlock.LevelBlockGame levelBlockGame =
                                        (runi.myddns.challenges.games.LevelBlock.LevelBlockGame) game;

                                levelBlockGame
                                        .getBorderManager()
                                        .getDataManager()
                                        .load();
                            }

                            game.getWorldSettingsManager()
                                    .applyAll();

                            showTitleToAll(
                                    game.getDisplayName(),
                                    "Reset abgeschlossen!",
                                    NamedTextColor.GREEN
                            );

                            Bukkit.broadcastMessage(
                                    ChatColor.GREEN
                                            + "✔ "
                                            + game.getDisplayName()
                                            + "-Welten wurden neu erstellt."
                            );

                            resetRunning = false;
                        },
                        20L
                );
    }

    public boolean isResetRunning() {
        return resetRunning;
    }

    private void resetPlayers(
            ChallengeGame game
    ) {

        boolean nightVision =
                game.getWorldSettingsManager()
                        .isNightVisionEnabled();

        for (Player player : Bukkit.getOnlinePlayers()) {

            if (!plugin.getGameWorldManager()
                    .isGamePlayer(
                            player,
                            game.getId()
                    )) {

                continue;
            }

            plugin.getPlayerGameDataManager()
                    .clearPlayerState(
                            player
                    );

            if (nightVision) {

                player.addPotionEffect(
                        new PotionEffect(
                                PotionEffectType.NIGHT_VISION,
                                999999,
                                0,
                                false,
                                false,
                                false
                        )
                );
            }

            player.updateInventory();
        }
    }

    private void showTitleToAll(
            String title,
            String subtitle,
            NamedTextColor color
    ) {

        Title screenTitle =
                Title.title(
                        Component.text(
                                title,
                                color
                        ),
                        Component.text(
                                subtitle,
                                color
                        ),
                        Title.Times.times(
                                Duration.ofMillis(500),
                                Duration.ofSeconds(2),
                                Duration.ofMillis(700)
                        )
                );

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showTitle(screenTitle);
        }
    }
}