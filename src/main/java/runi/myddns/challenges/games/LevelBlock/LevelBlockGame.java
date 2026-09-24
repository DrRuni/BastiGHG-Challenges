package runi.myddns.challenges.games.LevelBlock;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.command.PluginCommand;
import runi.myddns.challenges.ChallengeMain;
import runi.myddns.challenges.core.display.LobbyDisplayManager;
import runi.myddns.challenges.core.game.ChallengeGame;
import runi.myddns.challenges.core.timer.GameTimerManager;
import runi.myddns.challenges.core.utils.ConsoleColor;
import runi.myddns.challenges.core.world.GameWorldDefinition;
import runi.myddns.challenges.core.world.GameWorldSettingsManager;
import runi.myddns.challenges.games.LevelBlock.Listeners.BorderBlockListener;
import runi.myddns.challenges.games.LevelBlock.Listeners.PlayerListener;
import runi.myddns.challenges.games.LevelBlock.Manager.BorderManager;
import runi.myddns.challenges.games.LevelBlock.Commands.LevelBlockCommand;
import runi.myddns.challenges.games.LevelBlock.Listeners.MoveListener;
import runi.myddns.challenges.games.LevelBlock.Manager.LevelBlockGameManager;
import runi.myddns.challenges.games.LevelBlock.Manager.TimerManager;

import java.util.List;
import java.util.Collection;

import static runi.myddns.challenges.core.utils.DisplayColor.*;

public class LevelBlockGame implements ChallengeGame {

    private final ChallengeMain plugin;
    private final GameWorldSettingsManager worldSettingsManager;
    private final LobbyDisplayManager lobbyDisplayManager;
    private BorderManager borderManager;
    private MoveListener moveListener;
    private BorderBlockListener borderBlockListener;
    private LevelBlockCommand levelBlockCommand;
    private final LevelBlockGameManager gameManager;
    private PlayerListener playerListener;
    private final GameTimerManager gameTimerManager;
    private final TimerManager timerManager;

    private boolean loaded;
    private boolean loading;

    public LevelBlockGame(ChallengeMain plugin) {
        this.plugin = plugin;
        this.lobbyDisplayManager = plugin.getLobbyDisplayManager();
        this.borderManager = new BorderManager(this);
        this.gameManager = new LevelBlockGameManager(this, borderManager);
        this.gameTimerManager = new GameTimerManager(plugin, getId());
        this.timerManager = new TimerManager(this, gameTimerManager);
        this.worldSettingsManager = new GameWorldSettingsManager(plugin, getId(), List.of(
                                GameWorldDefinition.LEVEL_BLOCK_OVERWORLD.worldName(),
                                GameWorldDefinition.LEVEL_BLOCK_NETHER.worldName(),
                                GameWorldDefinition.LEVEL_BLOCK_END.worldName()));
    }

    @Override
    public String getId() {
        return "levelblock";
    }

    @Override
    public String getDisplayName() {
        return "LevelBlock";
    }

    @Override
    public boolean isLoading() {
        return loading;
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public void openSettings(Player player) {
        plugin.getWorldSettingsGUI().open(player);
    }

    @Override
    public void load() {
        if (loaded || loading) return;

        loading = true;

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(
                    player.getLocation(),
                    Sound.BLOCK_STONE_BUTTON_CLICK_ON,
                    0.35f,
                    0.7f
            );
        }

        lobbyDisplayManager.clearLoadConsole();
        lobbyDisplayManager.setLoadStatus(
                "LevelBlock wird geladen...",
                LOAD_RED
        );

        plugin.getServer().getScheduler().runTaskLater(
                plugin,
                this::runLoadSequence,
                2L
        );
    }

    private void runLoadSequence() {

        runStep(
                "Welten laden...",
                () -> plugin
                        .getGameWorldManager()
                        .loadGameWorlds(getId()),

                () -> runStep(
                        "Welteneinstellungen laden...",
                        () -> {
                            worldSettingsManager.load();
                            worldSettingsManager.applyAll();
                        },

                        () -> runStep(
                                "LevelBlock-Daten laden...",
                                () -> {
                                    borderManager
                                            .getDataManager()
                                            .load();

                                    gameTimerManager.load();
                                },

                                () -> runStep(
                                        "Overworld vorbereiten...",
                                        () -> preloadWorld(
                                                GameWorldDefinition.LEVEL_BLOCK_OVERWORLD,
                                                3
                                        ),

                                        () -> runStep(
                                                "Nether vorbereiten...",
                                                () -> preloadWorld(
                                                        GameWorldDefinition.LEVEL_BLOCK_NETHER,
                                                        2
                                                ),

                                                () -> runStep(
                                                        "End vorbereiten...",
                                                        () -> preloadWorld(
                                                                GameWorldDefinition.LEVEL_BLOCK_END,
                                                                2
                                                        ),

                                                        () -> runStep(
                                                                "Listener registrieren...",
                                                                () -> {
                                                                    moveListener = new MoveListener(this);
                                                                    borderBlockListener = new BorderBlockListener(this);
                                                                    playerListener = new PlayerListener(this);

                                                                    Bukkit.getPluginManager().registerEvents(moveListener, plugin);
                                                                    Bukkit.getPluginManager().registerEvents(borderBlockListener, plugin);
                                                                    Bukkit.getPluginManager().registerEvents(playerListener, plugin);
                                                                },

                                                                () -> runStep(
                                                                        "Commands registrieren...",
                                                                        () -> {
                                                                            levelBlockCommand = new LevelBlockCommand(this);

                                                                            PluginCommand command = plugin.getCommand("levelblock");

                                                                            if (command != null) {
                                                                                command.setExecutor(levelBlockCommand);
                                                                                command.setTabCompleter(levelBlockCommand);
                                                                            }
                                                                        },

                                                                        () -> runStep(
                                                                                "Border vorbereiten...",
                                                                                borderManager::startRenderer,
                                                                                this::finishLoading
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }

    @Override
    public void shutdown() {
        gameTimerManager.pause();
        timerManager.stop();
    }

    @Override
    public void unload() {
        if (!loaded) return;

        borderManager.stopRenderer();

        if (moveListener != null) HandlerList.unregisterAll(moveListener);
        if (borderBlockListener != null) HandlerList.unregisterAll(borderBlockListener);
        if (playerListener != null) HandlerList.unregisterAll(playerListener);

        gameTimerManager.pause();
        timerManager.stop();
        moveListener = null;
        borderBlockListener = null;
        playerListener = null;

        PluginCommand command = plugin.getCommand("levelblock");

        if (command != null) {
            command.setExecutor(null);
            command.setTabCompleter(null);
        }

        levelBlockCommand = null;

        loading = false;
        loaded = false;

        plugin.getGameStateManager().setStarted(false);
        plugin.getGameStateManager().setLoaded(false);

        lobbyDisplayManager.setLoadUnloaded(getDisplayName());
    }

    private void preloadWorld(
            GameWorldDefinition definition,
            int radius
    ) {

        World world = plugin.getGameWorldManager().loadWorld(definition);

        if (world == null) {
            throw new IllegalStateException(
                    "Welt konnte nicht geladen werden: "
                            + definition.worldName()
            );
        }

        int centerX =
                world.getSpawnLocation().getBlockX() >> 4;

        int centerZ =
                world.getSpawnLocation().getBlockZ() >> 4;

        for (int x = -radius; x <= radius; x++) {

            for (int z = -radius; z <= radius; z++) {

                world.getChunkAt(
                        centerX + x,
                        centerZ + z
                ).load();
            }
        }
    }

    private void runStep(
            String status,
            Runnable action,
            Runnable next
    ) {

        lobbyDisplayManager.addLoadConsoleLine(status);

        try {

            action.run();

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(
                        player.getLocation(),
                        Sound.BLOCK_STONE_BUTTON_CLICK_OFF,
                        0.25f,
                        1.2f
                );
            }

        } catch (Exception ex) {

            loading = false;

            lobbyDisplayManager.setLoadStatus(
                    "FEHLER: " + status,
                    BRIGHT_RED
            );

            plugin.getLogger().severe(
                    "Fehler beim Laden von LevelBlock: "
                            + status
            );

            ex.printStackTrace();

            return;
        }

        plugin.getServer().getScheduler().runTaskLater(
                plugin,
                next,
                15L
        );
    }


    private void finishLoading() {

        loaded = true;
        loading = false;

        plugin.getGameStateManager().setLoaded(true);
        timerManager.start();
        lobbyDisplayManager.setLoadReady(
                getDisplayName()
        );

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(
                    player.getLocation(),
                    Sound.BLOCK_NOTE_BLOCK_PLING,
                    0.6f,
                    1.4f
            );
        }

        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage(
                ConsoleColor.DARK_GOLDEN_LIME
                        + "  ═══════════════  LevelBlock V1.0 ═══════════════"
                        + ConsoleColor.RESET
        );
        Bukkit.getConsoleSender().sendMessage(
                ConsoleColor.DARK_GOLDEN_LIME
                        + "                      geladen"
                        + ConsoleColor.RESET
        );
        Bukkit.getConsoleSender().sendMessage("");
    }

    @Override
    public void startPlayers(
            Collection<? extends Player> players
    ) {

        if (!loaded) return;

        for (Player player : players) {
            joinActiveGame(player);
        }
    }


    @Override
    public void joinActiveGame(Player player) {

        World world =
                plugin.getGameWorldManager().loadWorld(
                        GameWorldDefinition.LEVEL_BLOCK_OVERWORLD
                );

        if (world == null) {

            plugin.getLogger().warning(
                    "LevelBlock-Welt konnte nicht geladen werden."
            );

            return;
        }

        Location targetLocation =
                plugin.getPlayerGameDataManager()
                        .getSavedLocation(
                                player.getUniqueId(),
                                getId()
                        );

        if (targetLocation == null) {

            targetLocation =
                    borderManager
                            .getDataManager()
                            .getStartLocation(
                                    world
                            );
        }

        if (targetLocation == null) {
            targetLocation =
                    world.getSpawnLocation();
        }

        player.teleport(
                targetLocation
        );

        if (gameManager.isStarted(world)) {
            gameTimerManager.resume();
        }

        plugin.getServer()
                .getScheduler()
                .runTaskLater(
                        plugin,
                        () -> {

                            borderManager
                                    .getDisplayManager()
                                    .updatePlayerView(
                                            player,
                                            player.getLocation()
                                    );

                            borderManager
                                    .getLineManager()
                                    .updatePlayerView(
                                            player
                                    );
                        },
                        1L
                );
    }

    @Override
    public void leavePlayer(Player player) {

        if (player == null) return;

        borderManager.removePlayerView(
                player
        );

        timerManager.clear(
                player
        );

        if (!hasOtherPlayers(player)) {
            gameTimerManager.pause();
        }

        World lobby =
                plugin.getLobbyWorldManager()
                        .getLobbyWorld();

        if (lobby == null) return;

        player.teleport(
                plugin.getLobbyWorldManager()
                        .getSpawn()
        );
    }

    @Override
    public boolean hasPlayers() {

        return hasPlayersInWorld(
                GameWorldDefinition.LEVEL_BLOCK_OVERWORLD
        )
                || hasPlayersInWorld(
                GameWorldDefinition.LEVEL_BLOCK_NETHER
        )
                || hasPlayersInWorld(
                GameWorldDefinition.LEVEL_BLOCK_END
        );
    }


    private boolean hasPlayersInWorld(
            GameWorldDefinition definition
    ) {

        World world =
                Bukkit.getWorld(
                        definition.worldName()
                );

        return world != null
                && !world.getPlayers().isEmpty();
    }


    @Override
    public boolean hasOtherPlayers(
            Player ignoredPlayer
    ) {

        for (Player player : Bukkit.getOnlinePlayers()) {

            if (player.equals(ignoredPlayer)) continue;

            if (isLevelBlockPlayer(player)) {
                return true;
            }
        }

        return false;
    }

    public boolean isLevelBlockPlayer(
            Player player
    ) {

        if (player == null) return false;

        String worldName =
                player.getWorld().getName();

        return worldName.equalsIgnoreCase(
                GameWorldDefinition.LEVEL_BLOCK_OVERWORLD.worldName()
        )
                || worldName.equalsIgnoreCase(
                GameWorldDefinition.LEVEL_BLOCK_NETHER.worldName()
        )
                || worldName.equalsIgnoreCase(
                GameWorldDefinition.LEVEL_BLOCK_END.worldName()
        );
    }

    @Override
    public boolean canUnload() {
        return !hasPlayers();
    }

    @Override
    public GameWorldSettingsManager getWorldSettingsManager() {
        return worldSettingsManager;
    }
    public ChallengeMain getPlugin() {
        return plugin;
    }
    public BorderManager getBorderManager() { return borderManager;}
    public LevelBlockGameManager getLevelBlockGameManager() { return gameManager;}
    public GameTimerManager getTimerManager() {return gameTimerManager;}
    public TimerManager getTimerDisplayManager() {return timerManager;}
}