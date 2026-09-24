package runi.myddns.challenges.games.MobArmyBattle.Managers.World;

import org.bukkit.*;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import runi.myddns.challenges.games.MobArmyBattle.MobArmyBattleGame;
import runi.myddns.challenges.core.utils.ConsoleColor;
import runi.myddns.challenges.core.world.GameWorldDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WorldManager {

    private final MobArmyBattleGame game;

    private static final String WORLD_LOBBY = "world_mobarmy_lobby";
    private static final String WORLD_ARENA = "world_mobarmy_arena";

    private long teamSeed;
    private boolean isWorldResetRunning = false;

    public WorldManager(MobArmyBattleGame game) {
        this.game = game;
    }

    public void checkWorldsOnStartup() {
        checkTeamWorlds();
        loadWorlds();
        game.getWorldSettingsManager().applyAll();
    }

    public void resetLobbyWorld() {
        List<Player> players = getPlayersInWorld(WORLD_LOBBY);

        for (Player p : players) {
            if (!p.isOnline()) continue;

            TeleportManager.teleport(game, p, WORLD_ARENA);

            Bukkit.getScheduler().runTaskLater(game.getPlugin(), () -> {
                if (p.isOnline()) {
                    p.sendActionBar(lang("world-manager.lobby-resetting"));
                    p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.8f);
                }
            }, 10L);
        }

        Bukkit.getScheduler().runTaskLater(game.getPlugin(), () -> {
            boolean reset = game.getPlugin().getWorldSourceManager().resetWorld(GameWorldDefinition.MOB_ARMY_LOBBY);

            if (!reset) {
                game.getPlugin().getLogger().severe("Lobby-Welt konnte nicht zurückgesetzt werden.");
                endWorldReset();
                return;
            }

            Bukkit.getScheduler().runTaskLater(game.getPlugin(), () -> {
                World lobby = game.getPlugin().getGameWorldManager().loadWorld(GameWorldDefinition.MOB_ARMY_LOBBY);
                game.getPlugin().getGameWorldManager().preloadSpawnChunks(lobby, 3);
                game.getWorldSettingsManager().applyAll();

                Bukkit.getScheduler().runTaskLater(game.getPlugin(), () -> {
                    for (Player p : players) {
                        if (p.isOnline()) TeleportManager.teleport(game, p, WORLD_LOBBY);
                    }

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.sendMessage(lang("world-manager.lobby-reset-complete"));
                        player.sendMessage(Component.empty());
                        player.sendMessage(lang("world-manager.progress-still-loaded-1"));
                        player.sendMessage(lang("world-manager.progress-still-loaded-2"));
                        player.sendMessage(lang("world-manager.progress-still-loaded-3"));
                        player.sendMessage(Component.empty());
                    }

                    Bukkit.getConsoleSender().sendMessage("");
                    Bukkit.getConsoleSender().sendMessage(ConsoleColor.LIME + "        LOBBY-Welt neu erstellt" + ConsoleColor.RESET);
                    Bukkit.getConsoleSender().sendMessage("");

                    endWorldReset();
                }, 40L);
            }, 40L);
        }, 60L);
    }

    public void resetArenaWorld() {
        List<Player> players = getPlayersInWorld(WORLD_ARENA);

        for (Player p : players) {
            if (!p.isOnline()) continue;

            TeleportManager.teleport(game, p, WORLD_LOBBY);

            Bukkit.getScheduler().runTaskLater(game.getPlugin(), () -> {
                if (p.isOnline()) {
                    p.sendActionBar(lang("world-manager.arena-resetting"));
                    p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.8f);
                }
            }, 10L);
        }

        Bukkit.getScheduler().runTaskLater(game.getPlugin(), () -> {
            boolean reset = game.getPlugin().getWorldSourceManager().resetWorld(GameWorldDefinition.MOB_ARMY_ARENA);

            if (!reset) {
                game.getPlugin().getLogger().severe("Arena-Welt konnte nicht zurückgesetzt werden.");
                endWorldReset();
                return;
            }

            Bukkit.getScheduler().runTaskLater(game.getPlugin(), () -> {
                World arena = game.getPlugin().getGameWorldManager().loadWorld(GameWorldDefinition.MOB_ARMY_ARENA);
                game.getPlugin().getGameWorldManager().preloadSpawnChunks(arena, 2);

                game.getWorldSettingsManager().applyAll();
                game.getArenaConfig().reload();

                Bukkit.getScheduler().runTaskLater(game.getPlugin(), () -> {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.sendMessage(Component.empty());
                        player.sendMessage(lang("world-manager.arena-reset-complete"));
                        player.sendMessage(Component.empty());
                        player.sendMessage(lang("world-manager.progress-still-loaded-1"));
                        player.sendMessage(lang("world-manager.progress-still-loaded-2"));
                        player.sendMessage(lang("world-manager.progress-still-loaded-3"));
                        player.sendMessage(Component.empty());
                    }

                    Bukkit.getConsoleSender().sendMessage("");
                    Bukkit.getConsoleSender().sendMessage(ConsoleColor.LIME + "        ARENA-Welt neu erstellt" + ConsoleColor.RESET);
                    Bukkit.getConsoleSender().sendMessage("");

                    endWorldReset();
                }, 40L);
            }, 40L);
        }, 60L);
    }

    private void checkTeamWorlds() {

        boolean rotWorld = game.getPlugin().getWorldSourceManager().worldExists(GameWorldDefinition.MOB_ARMY_ROT);
        boolean rotNether = game.getPlugin().getWorldSourceManager().worldExists(GameWorldDefinition.MOB_ARMY_ROT_NETHER);
        boolean blauWorld = game.getPlugin().getWorldSourceManager().worldExists(GameWorldDefinition.MOB_ARMY_BLAU);
        boolean blauNether = game.getPlugin().getWorldSourceManager().worldExists(GameWorldDefinition.MOB_ARMY_BLAU_NETHER);
        boolean anyWorldExists = rotWorld || rotNether || blauWorld || blauNether;

        if (!anyWorldExists) {
            teamSeed = new Random().nextLong();
        }

        // ROT Overworld
        if (!rotWorld) {
            Bukkit.getConsoleSender().sendMessage(
                    ConsoleColor.RED +
                            "        Teamwelt ROT wird erzeugt!" +
                            ConsoleColor.RESET);
            Bukkit.getConsoleSender().sendMessage("");
            game.getPlugin().getWorldSourceManager().generateWorld(GameWorldDefinition.MOB_ARMY_ROT, teamSeed);
        } else {
            Bukkit.getConsoleSender().sendMessage(
                    ConsoleColor.LIME +
                            "        Teamwelt - " +
                            ConsoleColor.RED + "ROT" +
                            ConsoleColor.LIME + " vorhanden!" +
                            ConsoleColor.RESET);
            Bukkit.getConsoleSender().sendMessage("");
        }

        // ROT Nether
        if (!rotNether) {
            Bukkit.getConsoleSender().sendMessage(
                    ConsoleColor.RED +
                            "        Netherwelt ROT wird erzeugt!" +
                            ConsoleColor.RESET);
            Bukkit.getConsoleSender().sendMessage("");
            game.getPlugin().getWorldSourceManager().generateWorld(GameWorldDefinition.MOB_ARMY_ROT_NETHER, teamSeed);
        } else {
            Bukkit.getConsoleSender().sendMessage(
                    ConsoleColor.LIME +
                            "        Netherwelt - " +
                            ConsoleColor.RED + "ROT" +
                            ConsoleColor.LIME + " vorhanden!" +
                            ConsoleColor.RESET);
            Bukkit.getConsoleSender().sendMessage("");
        }

        // BLAU Overworld
        if (!blauWorld) {
            Bukkit.getConsoleSender().sendMessage(
                    ConsoleColor.DARK_AQUA +
                            "        Teamwelt BLAU wird erzeugt!" +
                            ConsoleColor.RESET);
            Bukkit.getConsoleSender().sendMessage("");
            game.getPlugin().getWorldSourceManager().generateWorld(GameWorldDefinition.MOB_ARMY_BLAU, teamSeed);
        } else {
            Bukkit.getConsoleSender().sendMessage(
                    ConsoleColor.LIME +
                            "        Teamwelt - " +
                            ConsoleColor.DARK_AQUA + "BLAU " +
                            ConsoleColor.LIME + "vorhanden!" +
                            ConsoleColor.RESET);
            Bukkit.getConsoleSender().sendMessage("");
        }

        // BLAU Nether
        if (!blauNether) {
            Bukkit.getConsoleSender().sendMessage(
                    ConsoleColor.DARK_AQUA +
                            "        Netherwelt BLAU wird erzeugt!" +
                            ConsoleColor.RESET);
            Bukkit.getConsoleSender().sendMessage("");
            game.getPlugin().getWorldSourceManager().generateWorld(GameWorldDefinition.MOB_ARMY_BLAU_NETHER, teamSeed);
        } else {
            Bukkit.getConsoleSender().sendMessage(
                    ConsoleColor.LIME +
                            "        Netherwelt - " +
                            ConsoleColor.DARK_AQUA + "BLAU " +
                            ConsoleColor.LIME + "vorhanden!" +
                            ConsoleColor.RESET);
            Bukkit.getConsoleSender().sendMessage("");
        }

        Bukkit.getConsoleSender().sendMessage("");
    }

    public void resetTeamWorlds() {

        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage(
                ConsoleColor.BLOOD_ORANGE +
                        "        Team-Welten werden ZURÜCKGESETZT!" + ConsoleColor.RESET);
        Bukkit.getConsoleSender().sendMessage("");

        List<Player> rotPlayers = new ArrayList<>();
        rotPlayers.addAll(getPlayersInWorld("world_rot"));
        rotPlayers.addAll(getPlayersInWorld("world_rot_nether"));

        List<Player> blauPlayers = new ArrayList<>();
        blauPlayers.addAll(getPlayersInWorld("world_blau"));
        blauPlayers.addAll(getPlayersInWorld("world_blau_nether"));

        for (Player p : merge(rotPlayers, blauPlayers)) {
            if (p.isOnline()) {
                TeleportManager.teleport(game, p, "world_mobarmy_lobby");

                Bukkit.getScheduler().runTaskLater(game.getPlugin(), () -> {
                    if (p.isOnline()) {
                        p.sendActionBar(
                                lang("world-manager.team-worlds-resetting")
                        );
                        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.8f);
                    }
                }, 10L);
            }
        }

        Bukkit.getScheduler().runTaskLater(game.getPlugin(), () -> {

            game.getPortalManager().clearPortalWorldData();

            teamSeed = new Random().nextLong();

            game.getPlugin().getWorldSourceManager().resetGeneratedWorld(GameWorldDefinition.MOB_ARMY_ROT, teamSeed);
            game.getPlugin().getWorldSourceManager().resetGeneratedWorld(GameWorldDefinition.MOB_ARMY_ROT_NETHER, teamSeed);
            game.getPlugin().getWorldSourceManager().resetGeneratedWorld(GameWorldDefinition.MOB_ARMY_BLAU, teamSeed);
            game.getPlugin().getWorldSourceManager().resetGeneratedWorld(GameWorldDefinition.MOB_ARMY_BLAU_NETHER, teamSeed);

            Bukkit.getScheduler().runTaskLater(game.getPlugin(), () -> {

                    loadWorldsWithPreload();

                game.getWorldSettingsManager().applyToWorld(Bukkit.getWorld("world_rot"));
                game.getWorldSettingsManager().applyToWorld(Bukkit.getWorld("world_blau"));
                game.getWorldSettingsManager().applyToWorld(Bukkit.getWorld("world_rot_nether"));
                game.getWorldSettingsManager().applyToWorld(Bukkit.getWorld("world_blau_nether"));

                    Bukkit.getScheduler().runTaskLater(game.getPlugin(), () -> {

                        for (Player p : rotPlayers) {
                            if (p.isOnline()) {
                                TeleportManager.teleport(game, p, "world_rot");
                            }
                        }

                        for (Player p : blauPlayers) {
                            if (p.isOnline()) {
                                TeleportManager.teleport(game, p, "world_blau");
                            }
                        }

                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.sendMessage(Component.empty());
                            player.sendMessage(
                                    lang("world-manager.team-worlds-reset-complete")
                            );
                        }
                        Bukkit.getConsoleSender().sendMessage("");
                        Bukkit.getConsoleSender().sendMessage(
                                ConsoleColor.LIME + "        TEAM-Welten neu erstellt" + ConsoleColor.RESET);
                        Bukkit.getConsoleSender().sendMessage("");

                        endWorldReset();
                    }, 60L); // Spieler zurück
            }, 60L); // Worlds laden
        }, 20L); // Teleports
    }

    private void loadWorlds() {
        World lobby = game.getPlugin().getGameWorldManager().loadWorld(GameWorldDefinition.MOB_ARMY_LOBBY);
        game.getPlugin().getGameWorldManager().preloadSpawnChunks(lobby, 3);

        World arena = game.getPlugin().getGameWorldManager().loadWorld(GameWorldDefinition.MOB_ARMY_ARENA);
        game.getPlugin().getGameWorldManager().preloadSpawnChunks(arena, 2);

        game.getPlugin().getGameWorldManager().loadWorld(GameWorldDefinition.MOB_ARMY_ROT);
        game.getPlugin().getGameWorldManager().loadWorld(GameWorldDefinition.MOB_ARMY_BLAU);
        game.getPlugin().getGameWorldManager().loadWorld(GameWorldDefinition.MOB_ARMY_ROT_NETHER);
        game.getPlugin().getGameWorldManager().loadWorld(GameWorldDefinition.MOB_ARMY_BLAU_NETHER);
    }

    public void unloadWorlds() {
        game.getPlugin().getGameWorldManager().unloadGameWorlds(game.getId(), true);
    }

    private void loadWorldsWithPreload() {
        World rot = game.getPlugin().getGameWorldManager().loadWorld(GameWorldDefinition.MOB_ARMY_ROT);
        game.getPlugin().getGameWorldManager().preloadSpawnChunks(rot, 2);

        World blau = game.getPlugin().getGameWorldManager().loadWorld(GameWorldDefinition.MOB_ARMY_BLAU);
        game.getPlugin().getGameWorldManager().preloadSpawnChunks(blau, 2);

        World rotNether = game.getPlugin().getGameWorldManager().loadWorld(GameWorldDefinition.MOB_ARMY_ROT_NETHER);
        game.getPlugin().getGameWorldManager().preloadSpawnChunks(rotNether, 1);

        World blauNether = game.getPlugin().getGameWorldManager().loadWorld(GameWorldDefinition.MOB_ARMY_BLAU_NETHER);
        game.getPlugin().getGameWorldManager().preloadSpawnChunks(blauNether, 1);
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

    private Component lang(String path) {
        return game.getLanguageManager()
                .getComponent(path);
    }
}