package runi.myddns.challenges.games.chunkrandomizer;

import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitTask;
import runi.myddns.challenges.ChallengeMain;
import runi.myddns.challenges.core.game.ChallengeGame;
import runi.myddns.challenges.core.timer.GameTimerManager;
import runi.myddns.challenges.core.utils.ConsoleColor;
import runi.myddns.challenges.core.world.GameWorldDefinition;
import runi.myddns.challenges.core.world.GameWorldSettingsManager;
import runi.myddns.challenges.games.chunkrandomizer.commands.ChunkRandomizerCommand;
import runi.myddns.challenges.games.chunkrandomizer.listeners.ChunkRandomizerPlayerListener;
import runi.myddns.challenges.games.chunkrandomizer.managers.*;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

public class ChunkRandomizerGame implements ChallengeGame {

    private final ChallengeMain plugin;
    private final GameWorldSettingsManager worldSettingsManager;
    private final ChunkRandomizerManager randomizerManager;
    private final ChunkRandomizerDataManager dataManager;
    private final GameTimerManager timerManager;
    private final TimerManager timerDisplayManager;
    private final ChunkRandomizerMobManager mobManager;
    private ChunkRandomizerPlayerListener playerListener;

    private boolean loaded = false;
    private boolean loading = false;

    private BukkitTask loadTask;

    private final ChunkRandomizerBorderManager borderManager;
    private final ChunkRandomizerStartManager startManager;

    public ChunkRandomizerGame(ChallengeMain plugin) {
        this.plugin = plugin;
        this.borderManager = new ChunkRandomizerBorderManager();
        this.startManager = new ChunkRandomizerStartManager(plugin, borderManager);
        this.dataManager = new ChunkRandomizerDataManager(plugin);
        this.mobManager = new ChunkRandomizerMobManager(plugin, startManager, dataManager, this::onChallengeMobDefeated);
        this.timerManager = new GameTimerManager(plugin, getId());
        this.timerDisplayManager = new TimerManager(plugin, timerManager, startManager);
        this.randomizerManager = new ChunkRandomizerManager(plugin, borderManager, startManager, dataManager, this::startNextChunk);

        this.worldSettingsManager =
                new GameWorldSettingsManager(plugin, getId(), List.of(
                        GameWorldDefinition.CHUNK_RANDOMIZER_OVERWORLD.worldName(),
                        GameWorldDefinition.CHUNK_RANDOMIZER_NETHER.worldName(),
                        GameWorldDefinition.CHUNK_RANDOMIZER_END.worldName()
                ));
    }

    @Override
    public String getId() {
        return "chunkrandomizer";
    }

    @Override
    public String getDisplayName() {
        return "ChunkRandomizer";
    }

    public String getDisplayVersion() {
        try (var stream = plugin.getResource("plugin.yml")) {
            if (stream == null) return "V????";

            var config = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(stream, StandardCharsets.UTF_8)
            );

            return "V" + config.getString(
                    "game-versions.chunkrandomizer",
                    "????"
            );

        } catch (java.io.IOException e) {
            return "V????";
        }
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
    public GameWorldSettingsManager getWorldSettingsManager() {
        return worldSettingsManager;
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

        plugin.getLobbyDisplayManager().clearLoadConsole();
        plugin.getLobbyDisplayManager().setLoadStatus(
                "ChunkRandomizer wird geladen ...",
                0xFF8800
        );

        loadTask = Bukkit.getScheduler().runTaskLater(
                plugin,
                this::runLoadSequence,
                2L
        );
    }

    private void runLoadSequence() {

        runStep(
                "ChunkRandomizer: Welten laden",
                () -> {
                    plugin.getGameWorldManager().loadGameWorlds(getId());

                    for (GameWorldDefinition definition :
                            plugin.getGameWorldManager().getDefinitions(getId())) {

                        if (Bukkit.getWorld(definition.worldName()) == null) {
                            throw new IllegalStateException(
                                    "Welt konnte nicht geladen werden: "
                                            + definition.worldName()
                            );
                        }
                    }
                },

                () -> runStep(
                        "ChunkRandomizer: Welteinstellungen laden",
                        () -> {
                            worldSettingsManager.load();
                            worldSettingsManager.applyAll();
                        },

                        () -> runStep(
                                "ChunkRandomizer: Spieldaten laden",
                                () -> {
                                    dataManager.load();
                                    timerManager.load();
                                },

                                () -> runStep(
                                        "ChunkRandomizer: Overworld vorbereiten",
                                        () -> preloadWorld(
                                                GameWorldDefinition.CHUNK_RANDOMIZER_OVERWORLD,
                                                3
                                        ),

                                        () -> runStep(
                                                "ChunkRandomizer: Nether vorbereiten",
                                                () -> preloadWorld(
                                                        GameWorldDefinition.CHUNK_RANDOMIZER_NETHER,
                                                        2
                                                ),

                                                () -> runStep(
                                                        "ChunkRandomizer: End vorbereiten",
                                                        () -> preloadWorld(
                                                                GameWorldDefinition.CHUNK_RANDOMIZER_END,
                                                                2
                                                        ),

                                                        () -> runStep(
                                                                "ChunkRandomizer: Event wiederherstellen",
                                                                () -> {
                                                                    boolean restored =
                                                                            startManager.restore(dataManager);

                                                                    if (restored) {

                                                                        dataManager.restoreOriginalBorder(borderManager);

                                                                        // Alte Challenge-Border nach einem Crash entfernen,
                                                                        // falls das Mob bereits besiegt wurde.
                                                                        dataManager.restoreInactiveBorder();

                                                                        if (dataManager.getData()
                                                                                .getBoolean("border.active", false)) {

                                                                            if (!mobManager.restoreMob()) {
                                                                                plugin.getLogger().warning(
                                                                                        "ChunkRandomizer: Challenge-Mob konnte nicht wiederhergestellt werden!"
                                                                                );
                                                                            }
                                                                        }

                                                                        timerManager.resume();
                                                                        timerDisplayManager.start();
                                                                    }
                                                                },

                                                                () -> runStep(
                                                                        "ChunkRandomizer: Listener registrieren",
                                                                        () -> {
                                                                            Bukkit.getPluginManager()
                                                                                    .registerEvents(randomizerManager, plugin);

                                                                            Bukkit.getPluginManager()
                                                                                    .registerEvents(mobManager, plugin);

                                                                            playerListener = new ChunkRandomizerPlayerListener(this);

                                                                            Bukkit.getPluginManager()
                                                                                    .registerEvents(playerListener, plugin);

                                                                            var command = plugin.getCommand("chunkrandomizer");

                                                                            if (command != null) {
                                                                                ChunkRandomizerCommand executor =
                                                                                        new ChunkRandomizerCommand(plugin, this);

                                                                                command.setExecutor(executor);
                                                                                command.setTabCompleter(executor);
                                                                            }
                                                                        },
                                                                        this::finishLoading
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private void runStep(
            String status,
            Runnable action,
            Runnable next
    ) {
        plugin.getLobbyDisplayManager().addLoadConsoleLine(status);

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

            plugin.getLobbyDisplayManager().setLoadStatus(
                    "ChunkRandomizer: Ladefehler",
                    0xFF3333
            );

            plugin.getLogger().log(
                    Level.SEVERE,
                    "ChunkRandomizer Ladefehler: " + status,
                    ex
            );

            shutdown();

            plugin.getGameWorldManager().unloadGameWorlds(
                    getId(),
                    true
            );
            return;
        }

        loadTask = Bukkit.getScheduler().runTaskLater(
                plugin,
                next,
                15L
        );
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

        int centerX = world.getSpawnLocation().getBlockX() >> 4;
        int centerZ = world.getSpawnLocation().getBlockZ() >> 4;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                world.getChunkAt(
                        centerX + x,
                        centerZ + z
                ).load();
            }
        }
    }

    private void finishLoading() {
        loadTask = null;

        loaded = true;
        loading = false;

        plugin.getGameStateManager().setLoaded(true);

        plugin.getLobbyDisplayManager().setLoadReady(
                getDisplayName()
        );

        plugin.getLobbyDisplayManager().sendGameInfoHint(this);

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
                        + "  ═══════════════  ChunkRandomizer - "
                        + getDisplayVersion()
                        + " ═══════════════"
                        + ConsoleColor.RESET
        );
        Bukkit.getConsoleSender().sendMessage(
                ConsoleColor.DARK_GOLDEN_LIME
                        + "                          loaded"
                        + ConsoleColor.RESET
        );
        Bukkit.getConsoleSender().sendMessage("");
    }


    @Override
    public void shutdown() {
        timerDisplayManager.stop();
        dataManager.save();
        timerManager.pause();
        startManager.stop();
        mobManager.shutdown();

        if (playerListener != null) {
            HandlerList.unregisterAll(playerListener);
            playerListener = null;
        }

        HandlerList.unregisterAll(mobManager);
        HandlerList.unregisterAll(randomizerManager);

        randomizerManager.reset();
    }

    @Override
    public void unload() {
        if (!loaded && !loading) return;
        if (!canUnload()) return;

        if (loadTask != null) {
            loadTask.cancel();
            loadTask = null;
        }

        shutdown();

        var command = plugin.getCommand("chunkrandomizer");

        if (command != null) {
            command.setExecutor(null);
            command.setTabCompleter(null);
        }

        plugin.getGameWorldManager().unloadGameWorlds(
                getId(),
                true
        );

        loaded = false;
        loading = false;

        plugin.getGameStateManager().setLoaded(false);
        plugin.getGameStateManager().setStarted(false);

        plugin.getLobbyDisplayManager().setLoadUnloaded(
                getDisplayName()
        );

        plugin.getLogger().info("ChunkRandomizer unloaded.");
    }

    @Override
    public void startPlayers(Collection<? extends Player> players) {
        if (!loaded) return;

        for (Player player : players) {
            joinActiveGame(player);
        }
    }


    public boolean startChallenge(Player initiator) {

        if (!loaded || initiator == null) return false;

        if (dataManager.isStarted()) {
            initiator.sendMessage(
                    "§cChunkRandomizer wurde bereits gestartet!"
            );
            return false;
        }

        Location location = initiator.getLocation();

        boolean started = startManager.start(initiator);

        if (started) {

            dataManager.saveOriginalBorder(borderManager);
            dataManager.saveGameStart(location);
            dataManager.saveBorder(location.getWorld());
            randomizerManager.processChunk(location);

            if (!mobManager.spawnMob(location)) {

                borderManager.removeBorder();

                dataManager.getData().set("border.active", false);
                dataManager.save();

                plugin.getLogger().warning(
                        "ChunkRandomizer: Start-Mob konnte nicht gespawnt werden."
                );
            }

            timerManager.resume();
            timerDisplayManager.start();
        }

        return started;
    }

    @Override
    public void joinActiveGame(Player player) {
        if (!loaded || player == null) return;

        World world = Bukkit.getWorld(
                GameWorldDefinition.CHUNK_RANDOMIZER_OVERWORLD.worldName()
        );

        if (world == null) return;

        Location targetLocation =
                plugin.getPlayerGameDataManager()
                        .getSavedLocation(
                                player.getUniqueId(),
                                getId()
                        );

        if (targetLocation == null
                || targetLocation.getWorld() == null
                || !plugin.getGameWorldManager().isGameWorld(
                targetLocation.getWorld(),
                getId()
        )) {

            targetLocation = world.getSpawnLocation();
        }

        if (player.teleport(targetLocation)) {
            player.setGameMode(GameMode.SURVIVAL);

            if (playerListener != null) {
                playerListener.showWelcomeSequence(player);
            }
        }
    }

    public void resetGameData() {

        // Timeranzeige und Spielzeit stoppen
        timerDisplayManager.stop();
        timerManager.pause();

        for (Player player : Bukkit.getOnlinePlayers()) {

            if (plugin.getGameWorldManager()
                    .isGamePlayer(player, getId())) {

                plugin.getPlayerGameDataManager()
                        .clearPlayerState(player);

                player.updateInventory();
            }
        }

        // Challenge-Mob entfernen
        mobManager.reset();

        // WorldBorder entfernen und Event stoppen
        startManager.stop();

        // Bereits umgewandelte Chunks zurücksetzen
        randomizerManager.reset();

        // Gespeicherte Spieldaten zurücksetzen
        dataManager.reset();

        // Gespeicherte Inventare und Spielerzustände zurücksetzen
        plugin.getPlayerGameDataManager()
                .resetGamePlayerData(getId());

        // Timer wieder bei 0 laden
        timerManager.load();

        plugin.getLogger().info(
                "ChunkRandomizer: GameData vollständig zurückgesetzt."
        );
    }

    @Override
    public void leavePlayer(Player player) {
        // Später: spielerspezifische Chunk- und Border-Zustände entfernen.
    }

    @Override
    public boolean hasNoPlayers() {
        return Bukkit.getOnlinePlayers().stream()
                .noneMatch(player ->
                        plugin.getGameWorldManager()
                                .isGamePlayer(player, getId())
                );
    }

    @Override
    public boolean hasOtherPlayers(Player ignoredPlayer) {
        return Bukkit.getOnlinePlayers().stream()
                .anyMatch(player ->
                        !player.equals(ignoredPlayer)
                                && plugin.getGameWorldManager()
                                .isGamePlayer(player, getId())
                );
    }

    @Override
    public boolean canUnload() {
        return hasNoPlayers();
    }

    private void onChallengeMobDefeated() {

        if (!startManager.isStarted()) return;

        borderManager.removeBorder();

        dataManager.getData().set("border.active", false);
        dataManager.save();

        plugin.getLogger().info(
                "ChunkRandomizer: Mob besiegt - WorldBorder freigegeben."
        );
    }

    private void startNextChunk(Location location) {

        if (!startManager.isStarted() || !dataManager.isStarted()) {
            return;
        }

        if (borderManager.isActive()) return;

        borderManager.createBorder(location);

        randomizerManager.processChunk(location);

        if (mobManager.spawnMob(location)) {

            dataManager.saveBorder(location.getWorld());

        } else {

            borderManager.removeBorder();

            dataManager.getData().set("border.active", false);
            dataManager.save();
        }
    }

    public ChunkRandomizerDataManager getDataManager() { return dataManager;}
    public GameTimerManager getTimerManager() { return timerManager;}
    public ChallengeMain getPlugin() { return plugin;}
}


