package runi.myddns.challenges.games.MobArmyBattle.Managers.World;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import runi.myddns.challenges.games.MobArmyBattle.MobArmyBattleGame;
import runi.myddns.challenges.core.utils.ConsoleColor;
import runi.myddns.challenges.core.world.GameWorldDefinition;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class WorldManager {

    private final MobArmyBattleGame game;

    private static final String WORLD_LOBBY = "world_mobarmy_lobby";
    private static final String WORLD_ARENA = "world_mobarmy_arena";

    private boolean isWorldResetRunning = false;

    public WorldManager(MobArmyBattleGame game) {
        this.game = game;
    }

    public void resetArenaWorld() {

        List<Player> players = getPlayersInWorld(WORLD_ARENA);
        game.getArenaManager().resetArena();

        for (Player p : players) {

            if (!p.isOnline()) {
                continue;
            }

            TeleportManager.teleport(
                    game,
                    p,
                    WORLD_LOBBY
            );

            Bukkit.getScheduler().runTaskLater(
                    game.getPlugin(),
                    () -> {

                        if (!p.isOnline()) {
                            return;
                        }

                        p.sendActionBar(
                                lang("world-manager.arena-resetting")
                        );

                        p.playSound(
                                p.getLocation(),
                                Sound.ENTITY_ENDERMAN_TELEPORT,
                                1.0f,
                                0.8f
                        );
                    },
                    10L
            );
        }

        // Kurz warten, damit alle Spieler sicher aus der Arena raus sind
        Bukkit.getScheduler().runTaskLater(
                game.getPlugin(),
                () -> {

                    boolean reset =
                            game.getPlugin()
                                    .getWorldSourceManager()
                                    .resetWorld(
                                            GameWorldDefinition.MOB_ARMY_ARENA
                                    );

                    if (!reset) {

                        game.getPlugin()
                                .getLogger()
                                .severe(
                                        "Arena-Welt konnte nicht zurückgesetzt werden."
                                );

                        showResetTitle(
                                "world-manager.reset-title.arena-failed",
                                NamedTextColor.RED
                        );

                        endWorldReset();
                        return;
                    }

                    // Arena wieder laden
                    Bukkit.getScheduler().runTaskLater(
                            game.getPlugin(),
                            () -> {

                                World arena =
                                        game.getPlugin()
                                                .getGameWorldManager()
                                                .loadWorld(
                                                        GameWorldDefinition.MOB_ARMY_ARENA
                                                );

                                game.getPlugin()
                                        .getGameWorldManager()
                                        .preloadSpawnChunks(
                                                arena,
                                                2
                                        );

                                game.getWorldSettingsManager()
                                        .applyAll();

                                game.getArenaConfig()
                                        .reload();

                                // Noch kurz warten, dann die vorherigen Arena-Spieler zurück
                                Bukkit.getScheduler().runTaskLater(
                                        game.getPlugin(),
                                        () -> {

                                            for (Player player : Bukkit.getOnlinePlayers()) {

                                                player.sendMessage(
                                                        Component.empty()
                                                );

                                                player.sendMessage(
                                                        lang(
                                                                "world-manager.arena-reset-complete"
                                                        )
                                                );

                                                player.sendMessage(
                                                        Component.empty()
                                                );

                                                player.sendMessage(
                                                        lang(
                                                                "world-manager.progress-still-loaded-1"
                                                        )
                                                );

                                                player.sendMessage(
                                                        lang(
                                                                "world-manager.progress-still-loaded-2"
                                                        )
                                                );

                                                player.sendMessage(
                                                        lang(
                                                                "world-manager.progress-still-loaded-3"
                                                        )
                                                );

                                                player.sendMessage(
                                                        Component.empty()
                                                );
                                            }

                                            Bukkit.getConsoleSender()
                                                    .sendMessage("");

                                            Bukkit.getConsoleSender()
                                                    .sendMessage(
                                                            ConsoleColor.LIME
                                                                    + "        ARENA-Welt neu erstellt"
                                                                    + ConsoleColor.RESET
                                                    );

                                            Bukkit.getConsoleSender()
                                                    .sendMessage("");

                                            showResetTitle(
                                                    "world-manager.reset-title.arena-ready",
                                                    NamedTextColor.GREEN
                                            );

                                            endWorldReset();
                                        },
                                        40L
                                );
                            },
                            40L
                    );
                },
                60L
        );
    }

    public void resetTeamWorlds() {
        List<Player> rotPlayers = new ArrayList<>();
        rotPlayers.addAll(getPlayersInWorld("world_rot"));
        rotPlayers.addAll(getPlayersInWorld("world_rot_nether"));

        List<Player> blauPlayers = new ArrayList<>();
        blauPlayers.addAll(getPlayersInWorld("world_blau"));
        blauPlayers.addAll(getPlayersInWorld("world_blau_nether"));

        game.getPlugin().getGameResetManager().resetSelectedWorlds(
                game,
                List.of(
                        GameWorldDefinition.MOB_ARMY_ROT,
                        GameWorldDefinition.MOB_ARMY_ROT_NETHER,
                        GameWorldDefinition.MOB_ARMY_BLAU,
                        GameWorldDefinition.MOB_ARMY_BLAU_NETHER
                ),
                () -> {
                    // Erst nach Erwerb der Core-Reset-Sperre Spieler evakuieren.
                    for (Player p : merge(rotPlayers, blauPlayers)) {
                        if (!p.isOnline()) continue;
                        TeleportManager.teleport(game, p, WORLD_LOBBY);
                        Bukkit.getScheduler().runTaskLater(game.getPlugin(), () -> {
                            if (!p.isOnline()) return;
                            p.sendActionBar(lang("world-manager.team-worlds-resetting"));
                            p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.8f);
                        }, 10L);
                    }
                    game.getPortalManager().clearPortalWorldData();
                },
                finish -> Bukkit.getScheduler().runTaskLater(game.getPlugin(), () -> {
                    try {
                        for (Player p : rotPlayers) {
                            if (p.isOnline()) TeleportManager.teleport(game, p, "world_rot");
                        }
                        for (Player p : blauPlayers) {
                            if (p.isOnline()) TeleportManager.teleport(game, p, "world_blau");
                        }
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.sendMessage(Component.empty());
                            player.sendMessage(lang("world-manager.team-worlds-reset-complete"));
                        }
                        Bukkit.getConsoleSender().sendMessage("");
                        Bukkit.getConsoleSender().sendMessage(
                                ConsoleColor.LIME + "        TEAM-Welten neu erstellt" + ConsoleColor.RESET);
                        Bukkit.getConsoleSender().sendMessage("");
                        showResetTitle("world-manager.reset-title.teamworlds-ready", NamedTextColor.GREEN);
                    } finally {
                        finish.run();
                    }
                }, 60L)
        );
    }

    public void unloadWorlds() {
        game.getPlugin().getGameWorldManager().unloadGameWorlds(game.getId(), true);
    }

    private List<Player> getPlayersInWorld(String worldName) {
        World world = Bukkit.getWorld(worldName);
        return world != null ? new ArrayList<>(world.getPlayers()) : new ArrayList<>();
    }

    private List<Player> merge(List<Player> a, List<Player> b) {
        List<Player> all = new ArrayList<>(a);
        all.addAll(b);
        return all;
    }

    public void preloadTeamWorlds() {
        game.getPlugin().getGameWorldManager().preloadSpawnChunks(Bukkit.getWorld("world_rot"), 2);
        game.getPlugin().getGameWorldManager().preloadSpawnChunks(Bukkit.getWorld("world_blau"), 2);
        game.getPlugin().getGameWorldManager().preloadSpawnChunks(Bukkit.getWorld("world_rot_nether"), 2);
        game.getPlugin().getGameWorldManager().preloadSpawnChunks(Bukkit.getWorld("world_blau_nether"), 2);
    }

    public synchronized boolean isWorldResetBlocked() {
        if (isWorldResetRunning) {
            return true;
        }

        isWorldResetRunning = true;
        return false;
    }

    private void endWorldReset() {
        isWorldResetRunning = false;
    }

    private void showResetTitle(
            String subtitleKey,
            NamedTextColor color
    ) {

        Title screenTitle = Title.title(
                lang("world-manager.reset-title.title").color(color),
                lang(subtitleKey).color(color),
                Title.Times.times(
                        Duration.ofMillis(500),
                        Duration.ofSeconds(3),
                        Duration.ofMillis(800)
                )
        );

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showTitle(screenTitle);
        }
    }

    private Component lang(String path) {
        return game.getLanguageManager()
                .getComponent(path);
    }
}