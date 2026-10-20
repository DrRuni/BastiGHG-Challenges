package runi.myddns.challenges.core.reset;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import runi.myddns.challenges.ChallengeMain;
import runi.myddns.challenges.core.game.ChallengeGame;
import runi.myddns.challenges.core.world.GameWorldDefinition;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;
import java.util.function.Consumer;
import org.bukkit.World;
import runi.myddns.challenges.games.LevelBlock.LevelBlockGame;
import runi.myddns.challenges.games.MobArmyBattle.MobArmyBattleGame;

public class GameResetManager {

    private final ChallengeMain plugin;

    private boolean resetRunning;

    public GameResetManager(ChallengeMain plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("unused")
    public void resetGame(ChallengeGame game) {

        if (game == null) return;
        if (!game.isLoaded()) return;

        if (resetRunning) {

            Bukkit.broadcast(
                    plugin.getLanguageManager()
                            .getComponent("game-reset.already-running")
            );

            return;
        }

        resetRunning = true;

        showTitleToAll(
                game.getDisplayName(),
                plugin.getLanguageManager()
                        .get("game-reset.game.resetting"),
                NamedTextColor.GOLD
        );

        plugin.getLogger().info(
                "Game reset started: " + game.getId()
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
                plugin.getLanguageManager()
                        .get("game-reset.game.finished"),
                NamedTextColor.GREEN
        );

        Bukkit.broadcast(
                plugin.getLanguageManager()
                        .getComponent(
                                "game-reset.game.broadcast-finished",
                                "game",
                                Component.text(game.getDisplayName())
                        )
        );

        resetRunning = false;
    }

    public void resetWorlds(ChallengeGame game) {
        if (game == null || !game.isLoaded()) return;
        if (resetRunning) {
            Bukkit.broadcast(plugin.getLanguageManager().getComponent("game-reset.already-running"));
            return;
        }

        resetRunning = true;
        try {
            showTitleToAll(game.getDisplayName(),
                    plugin.getLanguageManager().get("game-reset.worlds.resetting"), NamedTextColor.GOLD);

            if (game instanceof MobArmyBattleGame mobArmy) {
                mobArmy.getEventManager().resetGame(false);
            }

            resetPlayers(game);

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!plugin.getGameWorldManager().isGamePlayer(player, game.getId())) continue;

                if (!(game instanceof MobArmyBattleGame)) {
                    game.leavePlayer(player);
                }

                if (!player.teleport(plugin.getLobbyWorldManager().getSpawn())) {
                    throw new IllegalStateException("Could not evacuate player " + player.getName());
                }
            }

            Bukkit.broadcast(plugin.getLanguageManager().getComponent(
                    "game-reset.worlds.broadcast-resetting", "game",
                    Component.text(game.getDisplayName())));

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                try {
                    List<GameWorldDefinition> definitions = plugin.getGameWorldManager()
                            .getDefinitions(game.getId());

                    // Jeder einzelne Unload muss erfolgreich sein.
                    for (GameWorldDefinition definition : definitions) {
                        if (!plugin.getGameWorldManager().unloadWorld(definition, true)) {
                            throw new IllegalStateException("Could not unload " + definition.worldName());
                        }
                    }

                    showTitleToAll(game.getDisplayName(),
                            plugin.getLanguageManager().get("game-reset.worlds.recreating"),
                            NamedTextColor.YELLOW);

                    long seed = new Random().nextLong();
                    for (GameWorldDefinition definition : definitions) {
                        boolean ok = definition.isGenerated()
                                ? plugin.getWorldSourceManager().resetGeneratedWorld(definition, seed)
                                : plugin.getWorldSourceManager().resetWorld(definition);
                        if (!ok) throw new IllegalStateException(
                                "Could not recreate " + definition.worldName());
                    }

                    plugin.getPlayerGameDataManager().deleteGamePlayerData(game.getId());

                    if (game instanceof LevelBlockGame levelBlock) {
                        levelBlock.resetWorldData();
                    }

                    for (GameWorldDefinition definition : definitions) {
                        World world = plugin.getGameWorldManager().loadWorld(definition);
                        if (world == null) throw new IllegalStateException(
                                "Could not reload " + definition.worldName());
                    }

                    if (game instanceof LevelBlockGame levelBlock) {
                        levelBlock.getBorderManager().getDataManager().load();
                    }

                    game.getWorldSettingsManager().applyAll();
                    showTitleToAll(game.getDisplayName(),
                            plugin.getLanguageManager().get("game-reset.worlds.finished"),
                            NamedTextColor.GREEN);
                    Bukkit.broadcast(plugin.getLanguageManager().getComponent(
                            "game-reset.worlds.broadcast-finished", "game",
                            Component.text(game.getDisplayName())));
                } catch (Exception ex) {
                    plugin.getLogger().log(java.util.logging.Level.SEVERE,
                            "Complete world reset failed for " + game.getId(), ex);
                    showTitleToAll(game.getDisplayName(),
                            plugin.getLanguageManager().get("game-reset.worlds.failed"),
                            NamedTextColor.RED);
                    Bukkit.broadcast(plugin.getLanguageManager().getComponent(
                            "game-reset.worlds.broadcast-failed", "game",
                            Component.text(game.getDisplayName())));
                } finally {
                    resetRunning = false;
                }
            }, 20L);
        } catch (Exception ex) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE,
                    "Could not start complete reset for " + game.getId(), ex);
            resetRunning = false;
        }
    }

    public void resetSelectedWorlds(
            ChallengeGame game,
            List<GameWorldDefinition> worlds,
            Runnable beforeReset,
            Runnable afterReset
    ) {
        resetSelectedWorlds(game, worlds, beforeReset, finish -> {
            if (afterReset != null) afterReset.run();
            finish.run();
        });
    }

    public void resetSelectedWorlds(
            ChallengeGame game,
            List<GameWorldDefinition> worlds,
            Runnable beforeReset,
            Consumer<Runnable> afterReset
    ) {
        if (game == null || !game.isLoaded() || worlds == null || worlds.isEmpty()) return;
        if (resetRunning) {
            Bukkit.broadcast(plugin.getLanguageManager().getComponent("game-reset.already-running"));
            return;
        }

        // Fremde, doppelte oder nicht zum Spiel gehörende Weltdefinitionen ablehnen.
        Set<GameWorldDefinition> unique = new HashSet<>(worlds);
        if (unique.size() != worlds.size()
                || worlds.stream().anyMatch(w -> w == null || !w.gameId().equalsIgnoreCase(game.getId()))) {
            plugin.getLogger().warning("Invalid selected world reset for " + game.getId());
            return;
        }

        final List<GameWorldDefinition> selectedWorlds = List.copyOf(worlds);
        resetRunning = true;
        try {
            if (beforeReset != null) beforeReset.run();

            // Alle Spieler, die NACH den Spiel-Callbacks noch in betroffenen
            // Welten stehen, in die zentrale Lobby bringen. Kein Inventar-Reset.
            for (GameWorldDefinition definition : selectedWorlds) {
                World world = Bukkit.getWorld(definition.worldName());
                if (world == null) continue;
                for (Player player : List.copyOf(world.getPlayers())) {
                    if (!player.teleport(plugin.getLobbyWorldManager().getSpawn())) {
                        throw new IllegalStateException("Could not evacuate " + player.getName()
                                + " from " + definition.worldName());
                    }
                }
            }

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                try {
                    // Prüfen, ob alle ausgewählten Welten entladen werden können.
                    // Bei Fehler keinen Ordner löschen.
                    for (GameWorldDefinition definition : selectedWorlds) {
                        if (!plugin.getGameWorldManager().unloadWorld(definition, true)) {
                            throw new IllegalStateException("Could not unload " + definition.worldName());
                        }
                    }

                    // Teamwelten eines Resets nutzen denselben Seed.
                    long sharedSeed = new Random().nextLong();
                    for (GameWorldDefinition definition : selectedWorlds) {
                        boolean success = definition.isGenerated()
                                ? plugin.getWorldSourceManager().resetGeneratedWorld(definition, sharedSeed)
                                : plugin.getWorldSourceManager().resetWorld(definition);
                        if (!success) {
                            throw new IllegalStateException("Could not recreate " + definition.worldName());
                        }
                    }

                    for (GameWorldDefinition definition : selectedWorlds) {
                        World world = plugin.getGameWorldManager().loadWorld(definition);
                        if (world == null) {
                            throw new IllegalStateException("Could not reload " + definition.worldName());
                        }
                        plugin.getGameWorldManager().preloadSpawnChunks(world, 2);
                        game.getWorldSettingsManager().applyToWorld(world);
                    }
                    game.getWorldSettingsManager().applyPlayerSettingsToAll();
                    if (afterReset != null) {
                        afterReset.accept(() -> resetRunning = false);
                    } else {
                        resetRunning = false;
                    }
                } catch (Exception ex) {
                    plugin.getLogger().log(java.util.logging.Level.SEVERE,
                            "Selected world reset failed for " + game.getId(), ex);
                    Bukkit.broadcast(plugin.getLanguageManager()
                            .getComponent("game-reset.worlds.failed"));
                    resetRunning = false;
                }
            }, 20L);
        } catch (Exception ex) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE,
                    "Could not start selected world reset for " + game.getId(), ex);
            resetRunning = false;
        }
    }

    @SuppressWarnings("unused")
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