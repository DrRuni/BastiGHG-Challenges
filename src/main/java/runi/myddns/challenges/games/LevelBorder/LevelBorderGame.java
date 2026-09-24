package runi.myddns.challenges.games.LevelBorder;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import runi.myddns.challenges.ChallengeMain;
import runi.myddns.challenges.core.game.ChallengeGame;
import runi.myddns.challenges.core.utils.ConsoleColor;
import runi.myddns.challenges.core.world.GameWorldDefinition;
import runi.myddns.challenges.core.world.GameWorldSettingsManager;
import runi.myddns.challenges.games.LevelBorder.Commands.LevelBorderCommand;
import runi.myddns.challenges.games.LevelBorder.Commands.ScoreboardCommand;
import runi.myddns.challenges.games.LevelBorder.Listeners.PlayerListener;
import runi.myddns.challenges.games.LevelBorder.Listeners.PortalListener;
import runi.myddns.challenges.games.LevelBorder.Manager.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LevelBorderGame implements ChallengeGame {

    private final ChallengeMain plugin;
    private final GameWorldSettingsManager worldSettingsManager;
    private final List<Listener> registeredListeners = new ArrayList<>();

    private boolean loaded = false;

    private File configFile;
    private FileConfiguration config;
    private boolean loading = false;

    private BorderDataManager dataManager;
    private LevelBorderManager borderManager;
    private ScoreboardManager scoreboardManager;
    private TimerManager timerManager;
    private PortalManager portalManager;
    private MobSpawnManager mobSpawnManager;

    public LevelBorderGame(ChallengeMain plugin) {
        this.plugin = plugin;

        this.worldSettingsManager =
                new GameWorldSettingsManager(plugin, getId(), List.of(
                        GameWorldDefinition.LEVEL_BORDER_OVERWORLD.worldName(),
                        GameWorldDefinition.LEVEL_BORDER_NETHER.worldName(),
                        GameWorldDefinition.LEVEL_BORDER_END.worldName()));
    }

    private void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "games/levelborder/config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    @Override
    public String getId() {
        return "levelborder";
    }

    @Override
    public String getDisplayName() {
        return "LevelBorder";
    }

    @Override
    public boolean isLoading() {return loading;}

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

        plugin.getLobbyDisplayManager().clearLoadConsole();
        plugin.getLobbyDisplayManager().setLoadStatus("LevelBorder wird geladen...", 0xC47A6B);
        plugin.getServer().getScheduler().runTaskLater(
                plugin,
                this::runLoadSequence,
                2L
        );
    }

    private void runLoadSequence() {
        runStep(
                "Welten laden...",
                () -> {
                    loadConfig();

                    plugin.getGameWorldManager().loadGameWorlds(getId());
                    worldSettingsManager.load();
                    worldSettingsManager.applyAll();

                    World world = Bukkit.getWorld(
                            GameWorldDefinition.LEVEL_BORDER_OVERWORLD.worldName()
                    );

                    if (world != null) {
                        plugin.getGameWorldManager().preloadSpawnChunks(world, 3);
                    }
                },
                () -> runStep(
                        "Manager initialisieren...",
                        this::initializeManagers,
                        () -> runStep(
                                "Listener registrieren...",
                                this::registerListeners,
                                () -> runStep(
                                        "Befehle registrieren...",
                                        this::registerCommands,
                                        () -> runStep(
                                                "LevelBorder vorbereiten...",
                                                () -> {
                                                    portalManager.loadAllPortals();
                                                    scoreboardManager.startUpdater();
                                                    mobSpawnManager.start();
                                                },
                                                this::finishLoading
                                        )
                                )
                        )
                )
        );
    }

    private void initializeManagers() {
        dataManager = new BorderDataManager(this);
        scoreboardManager = new ScoreboardManager(this, dataManager);
        timerManager = new TimerManager(this, dataManager);
        borderManager = new LevelBorderManager(this, dataManager, scoreboardManager);
        portalManager = new PortalManager(this, dataManager);
        mobSpawnManager = new MobSpawnManager(this, dataManager, borderManager);
    }

    private void registerListeners() {
        registerListener(new PortalListener(this, portalManager, dataManager));
        registerListener(new PlayerListener(this, borderManager, scoreboardManager, timerManager));
    }

    private void finishLoading() {
        loaded = true;
        loading = false;
        plugin.getGameStateManager().setLoaded(true);

        plugin.getLobbyDisplayManager().setLoadReady(getDisplayName());

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.6f, 1.4f);
        }

        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage(ConsoleColor.DARK_GOLDEN_LIME + "  ═══════════════  LevelBorder - V1.1 ═══════════════" + ConsoleColor.RESET);
        Bukkit.getConsoleSender().sendMessage(ConsoleColor.DARK_GOLDEN_LIME + "                       geladen" + ConsoleColor.RESET);
        Bukkit.getConsoleSender().sendMessage("");
    }

    private void registerCommands() {
        LevelBorderCommand levelBorderCommand = new LevelBorderCommand(this, borderManager, scoreboardManager, timerManager, portalManager);

        if (plugin.getCommand("levelborder") != null) {
            plugin.getCommand("levelborder").setExecutor(levelBorderCommand);
            plugin.getCommand("levelborder").setTabCompleter(levelBorderCommand);
        }

        ScoreboardCommand scoreboardCommand = new ScoreboardCommand(scoreboardManager);

        if (plugin.getCommand("lbscore") != null) {
            plugin.getCommand("lbscore").setExecutor(scoreboardCommand);
            plugin.getCommand("lbscore").setTabCompleter(scoreboardCommand);
        }
    }

    private void registerListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, plugin);
        registeredListeners.add(listener);
    }

    @Override
    public void shutdown() {
        if (timerManager != null) {
            timerManager.stop();
        }
    }

    @Override
    public void unload() {
        if (!loaded) return;

        if (mobSpawnManager != null) mobSpawnManager.stop();
        if (timerManager != null) timerManager.stop();

        if (scoreboardManager != null) {
            scoreboardManager.stopUpdater();
            scoreboardManager.reset();
        }

        for (Listener listener : registeredListeners) HandlerList.unregisterAll(listener);
        registeredListeners.clear();

        plugin.getGameWorldManager().unloadGameWorlds(getId(), true);

        mobSpawnManager = null;
        portalManager = null;
        timerManager = null;
        scoreboardManager = null;
        borderManager = null;
        dataManager = null;

        loaded = false;
        loading = false;

        plugin.getGameStateManager().setLoaded(false);
        plugin.getGameStateManager().setStarted(false);
        plugin.getLobbyDisplayManager().setLoadUnloaded(getDisplayName());

        plugin.getLogger().info("LevelBorder wurde entladen.");
    }

    private void runStep(String status, Runnable action, Runnable next) {
        plugin.getLobbyDisplayManager().addLoadConsoleLine(status);

        try {
            action.run();

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_OFF, 0.25f, 1.2f);
            }

        } catch (Exception ex) {
            loading = false;
            plugin.getLobbyDisplayManager().setLoadStatus("FEHLER: " + status, 0xFF3333);
            plugin.getLogger().severe("Fehler beim Laden von LevelBorder: " + status);
            ex.printStackTrace();
            return;
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, next, 15L);
    }

    @Override
    public void startPlayers(Collection<? extends Player> players) {
        if (!loaded) return;

        for (Player player : players) {
            joinActiveGame(player);
        }
    }

    @Override
    public void joinActiveGame(Player player) {
        if (!loaded) return;

        World world = Bukkit.getWorld(
                GameWorldDefinition.LEVEL_BORDER_OVERWORLD.worldName()
        );

        if (world == null) return;

        Location targetLocation =
                plugin.getPlayerGameDataManager()
                        .getSavedLocation(
                                player.getUniqueId(),
                                getId()
                        );

        if (targetLocation == null) {
            targetLocation =
                    world.getSpawnLocation();
        }

        player.teleport(
                targetLocation
        );
    }

    @Override
    public void leavePlayer(Player player) {
        if (player == null) return;

        scoreboardManager.removePlayer(player);
    }

    @Override
    public boolean hasPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isLevelBorderPlayer(player)) return true;
        }

        return false;
    }

    @Override
    public boolean hasOtherPlayers(Player ignoredPlayer) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.equals(ignoredPlayer)) continue;
            if (isLevelBorderPlayer(player)) return true;
        }

        return false;
    }

    @Override
    public boolean canUnload() {
        return !hasPlayers();
    }

    @Override
    public GameWorldSettingsManager getWorldSettingsManager() {
        return worldSettingsManager;
    }

    public boolean isLevelBorderWorld(World world) {
        return plugin.getGameWorldManager().isGameWorld(world, getId());
    }

    public boolean isLevelBorderPlayer(Player player) {
        return plugin.getGameWorldManager().isGamePlayer(player, getId());
    }

    public FileConfiguration getConfig() {return config;}
    public ChallengeMain getPlugin() {return plugin;}
}