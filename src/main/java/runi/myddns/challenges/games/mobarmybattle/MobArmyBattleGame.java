package runi.myddns.challenges.games.mobarmybattle;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
import runi.myddns.challenges.ChallengeMain;
import runi.myddns.challenges.core.display.LobbyDisplayManager;
import runi.myddns.challenges.core.game.ChallengeGame;
import runi.myddns.challenges.core.language.LanguageManager;
import runi.myddns.challenges.core.utils.ConsoleColor;
import runi.myddns.challenges.core.world.GameWorldSettingsManager;
import runi.myddns.challenges.games.mobarmybattle.commands.*;
import runi.myddns.challenges.games.mobarmybattle.guis.*;
import runi.myddns.challenges.games.mobarmybattle.listeners.*;
import runi.myddns.challenges.games.mobarmybattle.managers.event.*;
import runi.myddns.challenges.games.mobarmybattle.managers.world.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MobArmyBattleGame implements ChallengeGame {

    private final ChallengeMain plugin;
    private final LobbyDisplayManager lobbyDisplayManager;
    private final List<Listener> registeredListeners = new ArrayList<>();
    private final List<BukkitTask> gameTasks = new ArrayList<>();
    private final LanguageManager languageManager;

    private WorldManager worldManager;
    public TimerManager timerManager;
    public TeamManager teamManager;
    public BlockRandomizerManager blockRandomizerManager;
    public WaveManager waveManager;
    private MobSaveManager mobSaveManager;
    public WaveStorage waveStorage;
    public ArenaEventManager arenaManager;
    public EventManager eventManager;
    public ArenaScoreboardManager scoreboardManager;
    public MobSaveListener mobSaveListener;
    public BundleManager bundleManager;
    public ArenaBuildProtectionManager arenaBuildProtectionManager;
    private ResumeManager eventResume;
    private WorldSettings worldSettings;
    private final GameWorldSettingsManager worldSettingsManager;
    private PortalManager portalManager;
    private ArenaConfig arenaConfig;
    public OptionsGUI optionenGUI;
    public TimerGUI timerGUI;
    public SetupGUI eventSettingsGUI;
    public TeleportGUI mobArmySettingsGUI;
    public TeamSelectionGUI teamSelectionGUI;
    public BundleGUI bundleGUI;
    public RandomizerExclusionGUI spawnEggGUI;
    private ArenaSettingsGUI arenaSettingsGUI;
    private PlayerGUI playerGUI;
    private PlayerActionGUI playerActionGUI;
    private TeamSettingsGUI teamSettingsGUI;
    private TeamScoreboardManager teamScoreboardManager;
    private ScoreboardSwitcher scoreboardSwitcher;
    private ArenaCompassManager arenaCompassManager;
    private TeamEquipmentManager teamEquipmentManager;
    private TeamEquipmentGUI teamEquipmentGUI;
    private PlayerListener playerListener;
    private ChestRandomizerManager chestRandomizerManager;
    private ButtonManager buttonManager;

    private boolean loaded;
    private boolean loading;

    public MobArmyBattleGame(
            ChallengeMain plugin,
            LobbyDisplayManager lobbyDisplayManager
    ) {
        this.plugin = plugin;
        this.lobbyDisplayManager = lobbyDisplayManager;
        this.languageManager =
                plugin.getLanguageManager()
                        .forGroup(getId());

        this.worldSettingsManager =
                new GameWorldSettingsManager(plugin, getId(), List.of(
                        "world_mobarmy_lobby",
                        "world_mobarmy_arena",
                        "world_rot",
                        "world_rot_nether",
                        "world_blau",
                        "world_blau_nether"
                ));

        this.worldSettingsManager.setMobSpawningForcedOffWorlds(
                List.of(
                        "world_mobarmy_lobby",
                        "world_mobarmy_arena"
                )
        );
    }

    public File getDataFolder() {
        return new File(plugin.getDataFolder(), "games/mobarmybattle");
    }

    @Override
    public String getId() {
        return "mobarmybattle";
    }

    @Override
    public String getDisplayName() {
        return "MobArmyBattle";
    }


    @Override
    public boolean isLoading() {return loading;}

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public void openSettings(Player player) {
        getOptionenGUI().open(player);
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
                languageManager.get("loading.starting"),
                0xC47A6B
        );

        gameTasks.add(
                plugin.getServer().getScheduler().runTaskLater(
                        plugin,
                        this::runLoadSequence,
                        2L
                )
        );
    }

    private void runLoadSequence() {

        runStep(
                languageManager.get("loading.worlds"),
                () -> {

                    worldSettingsManager.load();
                    worldSettings = new WorldSettings(this);
                    worldManager = new WorldManager(this);

                    plugin.getGameWorldManager()
                            .loadGameWorlds(getId());

                    worldSettingsManager.applyAll();

                },
                () -> runStep(
                        languageManager.get("loading.managers"),
                        this::initializeMobArmyBattle,

                        () -> runStep(
                                languageManager.get("loading.listeners"),
                                this::registerListeners,

                                () -> runStep(
                                        languageManager.get("loading.commands"),
                                        this::registerCommands,

                                        () -> runStep(
                                                languageManager.get("loading.waves"),
                                                () -> {
                                                    waveStorage.loadWaves();
                                                    scheduleArenaReload();
                                                },

                                                () -> runStep(
                                                        languageManager.get("loading.teams"),
                                                        teamManager::loadTeams,

                                                        () -> runStep(
                                                                languageManager.get("loading.scoreboard"),
                                                                teamScoreboardManager::rebuildBoard,

                                                                () -> runStep(
                                                                        languageManager.get("loading.timer"),
                                                                        () -> {

                                                                            timerManager
                                                                                    .ensureBossBarExists();

                                                                            timerManager
                                                                                    .updatePauseState();
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

    private void initializeMobArmyBattle() {
        blockRandomizerManager = new BlockRandomizerManager(this);
        arenaConfig = new ArenaConfig(this);
        arenaManager = new ArenaEventManager(this);
        teamManager = new TeamManager(this);
        arenaBuildProtectionManager = new ArenaBuildProtectionManager(this);
        mobSaveManager = new MobSaveManager(this, teamManager);
        waveManager = new WaveManager(mobSaveManager);
        waveStorage = new WaveStorage(plugin, getDataFolder(), waveManager);
        scoreboardManager = arenaManager.getScoreboardManager();
        waveManager.setScoreboardManager(scoreboardManager);
        eventResume = new ResumeManager(this);
        timerManager = new TimerManager(this);
        mobSaveManager.setTimerManager(timerManager);
        mobSaveListener = new MobSaveListener(plugin, mobSaveManager);
        bundleManager = new BundleManager(this);
        bundleManager.setTeamManager(teamManager);
        eventManager = new EventManager(this, mobSaveManager);
        portalManager = new PortalManager(this);
        portalManager.loadAllPortals();
        teamScoreboardManager = new TeamScoreboardManager(this);
        scoreboardSwitcher = new ScoreboardSwitcher(plugin, teamScoreboardManager, scoreboardManager);
        arenaCompassManager = new ArenaCompassManager(this);
        teamEquipmentManager = new TeamEquipmentManager(this);
        chestRandomizerManager = new ChestRandomizerManager(this, blockRandomizerManager);
        optionenGUI = new OptionsGUI(this, getLanguageManager());
        timerGUI = new TimerGUI(this, timerManager);
        eventSettingsGUI = new SetupGUI(this);
        mobArmySettingsGUI = new TeleportGUI(this);
        teamSelectionGUI = new TeamSelectionGUI(this, teamManager);
        bundleGUI = new BundleGUI(this);
        spawnEggGUI = new RandomizerExclusionGUI(blockRandomizerManager, this);
        arenaSettingsGUI = new ArenaSettingsGUI(this);
        playerGUI = new PlayerGUI(this);
        playerActionGUI = new PlayerActionGUI(this);
        teamSettingsGUI = new TeamSettingsGUI(this);
        teamEquipmentGUI = new TeamEquipmentGUI(this);
        playerListener = new PlayerListener(this);
        buttonManager = new ButtonManager(this);
    }

    private void registerListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, plugin);
        registeredListeners.add(listener);
    }

    private void registerListeners() {
        registerListener(new PauseListener(this));
        registerListener(playerListener);
        registerListener(new PlayerRespawnListener(this));
        registerListener(new PortalListener(this));
        registerListener(arenaBuildProtectionManager);
        registerListener(blockRandomizerManager);
        registerListener(mobSaveListener);
        registerListener(timerManager);
        registerListener(teamManager);
        registerListener(optionenGUI);
        registerListener(timerGUI);
        registerListener(eventSettingsGUI);
        registerListener(mobArmySettingsGUI);
        registerListener(teamSelectionGUI);
        registerListener(spawnEggGUI);
        registerListener(buttonManager);
        registerListener(new ArenaMobTargetListener(this));
        registerListener(new BundleListener(this, bundleGUI, teamManager, bundleManager));
        registerListener(arenaSettingsGUI);
        registerListener(arenaCompassManager);
        registerListener(playerGUI);
        registerListener(playerActionGUI);
        registerListener(teamSettingsGUI);
        registerListener(teamEquipmentGUI);
        registerListener(new ScoreboardSwitchListener(this));
        registerListener(chestRandomizerManager);
    }

    private void registerCommands() {
        ResumeCommand resumeCmd = new ResumeCommand(this);
        registerCommand("resume", resumeCmd, resumeCmd);

        MobArmyCommand mobArmyCommand = new MobArmyCommand(this);
        registerCommand("mobarmy", mobArmyCommand, mobArmyCommand);

        TeamCommand teamCmd = new TeamCommand(this);
        registerCommand("team", teamCmd, teamCmd);

        MobStatusCommand mobStatusCommand = new MobStatusCommand(this, mobSaveManager, teamManager);
        registerCommand("mobstatus", mobStatusCommand, mobStatusCommand);

        registerCommand("arenasummary", new ArenaSummaryCommand(this), null);

        SetPhaseCommand setPhaseCommand = new SetPhaseCommand(this);
        registerCommand("setphase", setPhaseCommand, setPhaseCommand);
    }

    @Override
    public boolean canUnload() {
        return hasNoPlayers();
    }

    @Override
    public void shutdown() {
        if (timerManager != null) {
            getEventResume().saveTimerState(
                    timerManager.getTimeInSeconds(),
                    timerManager.isForward()
            );

            timerManager.pauseTimer();
        }
    }

    @Override
    public void unload() {
        if (!loaded) return;

        loading = false;

        if (timerManager != null) timerManager.stopTimer();

        if (timerManager != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                timerManager.removeBossBarFor(player);
            }
        }

        if (scoreboardSwitcher != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                scoreboardSwitcher.removePlayer(player);
            }
        }

        if (arenaManager != null) {
            arenaManager.shutdown();
        }

        for (Listener listener : registeredListeners) {
            HandlerList.unregisterAll(listener);
        }

        registeredListeners.clear();

        for (BukkitTask task : gameTasks) {
            task.cancel();
        }
        gameTasks.clear();

        if (worldManager != null) worldManager.unloadWorlds();

        loaded = false;

        plugin.getGameStateManager().setStarted(false);
        plugin.getGameStateManager().setLoaded(false);

        lobbyDisplayManager.setLoadUnloaded(getDisplayName());
    }

    @Override
    public void joinActiveGame(Player player) {

        if (!eventResume.isEventStarted()) {

            TeleportManager.teleport(
                    this,
                    player,
                    "world_mobarmy_lobby"
            );

            prepareJoinedPlayer(player);
            return;
        }

        if (!teamManager.isRegisteredParticipant(player)) {

            TeleportManager.teleport(
                    this,
                    player,
                    "world_mobarmy_lobby"
            );

            prepareJoinedPlayer(player);
            sendResumeHint(player);
            return;
        }

        int phase = eventResume.loadPhase();

        switch (phase) {

            case ResumeManager.PHASE_TEAMWELT -> joinTeamWorld(player);

            case ResumeManager.PHASE_WAVEAUSWAHL -> {

                TeleportManager.teleportToWaveSelection(
                        this,
                        player
                );

                player.setGameMode(
                        org.bukkit.GameMode.SURVIVAL
                );

                player.setInvulnerable(false);
            }

            case ResumeManager.PHASE_ARENA -> {

                TeleportManager.teleportToArena(
                        this,
                        player
                );

                player.setGameMode(
                        org.bukkit.GameMode.SURVIVAL
                );

                player.setInvulnerable(false);
            }

            default -> TeleportManager.teleport(
                    this,
                    player,
                    "world_mobarmy_lobby"
            );
        }

        prepareJoinedPlayer(player);
        sendResumeHint(player);
    }

    public String getDisplayVersion() {
        try (var stream = plugin.getResource("plugin.yml")) {
            if (stream == null) return "V????";

            var config = org.bukkit.configuration.file.YamlConfiguration
                    .loadConfiguration(
                            new java.io.InputStreamReader(
                                    stream,
                                    java.nio.charset.StandardCharsets.UTF_8
                            )
                    );

            return "V" + config.getString(
                    "game-versions.mobarmybattle",
                    "????"
            );
        } catch (java.io.IOException e) {
            return "V????";
        }
    }

    private void sendResumeHint(Player player) {

        if (!eventResume.isEventStarted()
                || !eventResume.isEventPaused()) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> {

                    if (!player.isOnline()) {
                        return;
                    }

                    player.sendMessage(
                            languageManager.getComponent(
                                    "resume.paused-hint"
                            )
                    );

                },
                60L
        );
    }

    private void joinTeamWorld(Player player) {

        String team =
                teamManager.getPlayerTeam(player);

        String worldName;

        if ("Rot".equalsIgnoreCase(team)) {

            worldName = "world_rot";

        } else if ("Blau".equalsIgnoreCase(team)) {

            worldName = "world_blau";

        } else {

            TeleportManager.teleport(
                    this,
                    player,
                    "world_mobarmy_lobby"
            );

            return;
        }

        org.bukkit.World teamWorld =
                Bukkit.getWorld(worldName);

        if (teamWorld == null) {
            return;
        }

        boolean restored =
                eventResume.restorePlayerPosition(player);

        if (!restored
                || !player.getWorld()
                .getName()
                .toLowerCase()
                .startsWith(
                        team.equalsIgnoreCase("Rot")
                                ? "world_rot"
                                : "world_blau"
                )) {

            player.teleport(
                    teamWorld.getSpawnLocation()
            );
        }

        player.setGameMode(
                org.bukkit.GameMode.SURVIVAL
        );

        player.setInvulnerable(false);
    }

    private void prepareJoinedPlayer(Player player) {

        worldSettingsManager
                .applyPlayerSettings(player);

        playerListener
                .showWelcomeSequence(player);

        gameTasks.add(
                Bukkit.getScheduler()
                        .runTaskLater(
                                plugin,
                                () -> {

                                    if (!player.isOnline()) {
                                        return;
                                    }

                                    timerManager
                                            .ensureBossBarExists();

                                    timerManager
                                            .addPlayerToBossBar(player);

                                    timerManager
                                            .updatePauseState();

                                    teamScoreboardManager
                                            .updateBoard();

                                    scoreboardSwitcher
                                            .switchToTeam(player);
                                },
                                20L * 7
                        )
        );
    }

    public boolean isMobArmyPlayer(Player player) {

        if (player == null) {
            return false;
        }

        String world = player.getWorld().getName();

        return world.equalsIgnoreCase("world_mobarmy_lobby")
                || world.equalsIgnoreCase("world_mobarmy_arena")
                || world.equalsIgnoreCase("world_rot")
                || world.equalsIgnoreCase("world_rot_nether")
                || world.equalsIgnoreCase("world_blau")
                || world.equalsIgnoreCase("world_blau_nether");
    }

    @Override
    public boolean hasNoPlayers() {

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isMobArmyPlayer(player)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean hasOtherPlayers(Player ignoredPlayer) {

        for (Player player : Bukkit.getOnlinePlayers()) {

            if (player.equals(ignoredPlayer)) {
                continue;
            }

            if (isMobArmyPlayer(player)) {
                return true;
            }
        }

        return false;
    }

    private void scheduleArenaReload() {
        gameTasks.add(
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    try {
                        arenaConfig.reload();
                    } catch (Exception ex) {
                        plugin.getLogger().log(
                                java.util.logging.Level.SEVERE,
                                "Failed to reload build protection.",
                                ex
                        );
                    }
                }, 20L)
        );
    }

    private void registerCommand(String name, org.bukkit.command.CommandExecutor executor, org.bukkit.command.TabCompleter completer) {
        var command = plugin.getCommand(name);

        if (command == null) {
            plugin.getLogger().warning(
                    "Command not found in plugin.yml: /" + name
            );
            return;
        }

        command.setExecutor(executor);

        if (completer != null) command.setTabCompleter(completer);
    }

    private void runStep(String status, Runnable action, Runnable next) {
        lobbyDisplayManager.addLoadConsoleLine(status);

        try {
            action.run();

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_OFF, 0.25f, 1.2f);
            }

        } catch (Exception ex) {
            loading = false;
            lobbyDisplayManager.setLoadStatus(
                    languageManager.get(
                            "loading.error",
                            "status",
                            status
                    ),
                    0xFF3333
            );
            plugin.getLogger().log(
                    java.util.logging.Level.SEVERE,
                    "Error while loading MobArmyBattle: " + status,
                    ex
            );
            return;
        }

        gameTasks.add(
                plugin.getServer().getScheduler().runTaskLater(
                        plugin,
                        next,
                        15L
                )
        );
    }

    private void finishLoading() {
        loaded = true;
        loading = false;
        plugin.getGameStateManager().setLoaded(true);

        lobbyDisplayManager.setLoadReady(getDisplayName());
        lobbyDisplayManager.sendGameInfoHint(this);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.6f, 1.4f);
        }

        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage(ConsoleColor.DARK_GOLDEN_LIME + "  ═══════════════  MobArmyBattle - " + getDisplayVersion() + " ═══════════════" + ConsoleColor.RESET);
        Bukkit.getConsoleSender().sendMessage(ConsoleColor.DARK_GOLDEN_LIME + "                              loaded"                        + ConsoleColor.RESET);
        Bukkit.getConsoleSender().sendMessage("");
    }

    public void startPlayers(Collection<? extends Player> players) {

        if (!loaded) return;

        for (Player player : players) {

            joinActiveGame(player);
        }

        gameTasks.add(
                Bukkit.getScheduler().runTaskLater(
                        plugin,
                        () -> {
                            if (worldManager != null) {
                                worldManager.preloadTeamWorlds();
                            }
                        },
                        80L
                )
        );
    }

    @Override
    public void leavePlayer(Player player) {

        if (player != null
                && isMobArmyPlayer(player)
                && eventResume != null) {

            eventResume.savePlayerLastLocation(player);
        }

        if (scoreboardSwitcher != null) {
            scoreboardSwitcher.removePlayer(player);
        }

        if (timerManager != null) {
            timerManager.removeBossBarFor(player);
        }

        if (arenaManager != null) {
            arenaManager.removePlayer(player);
        }
    }

    @Override
    public GameWorldSettingsManager getWorldSettingsManager() {
        return worldSettingsManager;
    }

    public ChallengeMain getPlugin() { return plugin; }
    public TeamManager getTeamManager() { return teamManager; }
    public TimerManager getTimerManager() { return timerManager; }
    public BlockRandomizerManager getBlockRandomizerManager() { return blockRandomizerManager; }
    public WaveManager getWaveManager() { return waveManager; }
    public WaveStorage getWaveStorage() { return waveStorage; }
    public ArenaEventManager getArenaManager() { return arenaManager; }
    public EventManager getEventManager() { return eventManager; }
    public BundleManager getBundleManager() { return bundleManager; }
    public ArenaBuildProtectionManager getArenaBuildProtectionManager() { return arenaBuildProtectionManager; }
    public OptionsGUI getOptionenGUI() { return optionenGUI; }
    public TimerGUI getTimerGUI() { return timerGUI; }
    public SetupGUI getEventSettingsGUI() { return eventSettingsGUI; }
    public TeleportGUI getMobArmySettingsGUI() { return mobArmySettingsGUI; }
    public TeamSelectionGUI getTeamSelectionGUI() { return teamSelectionGUI; }
    public BundleGUI getBundleGUI() { return bundleGUI; }
    public RandomizerExclusionGUI getSpawnEggGUI() { return spawnEggGUI; }
    public ResumeManager getEventResume() { return eventResume; }
    public WorldManager getWorldManager() { return worldManager; }
    public ArenaConfig getArenaConfig() { return arenaConfig; }
    public MobSaveManager getMobSaveManager() { return mobSaveManager; }
    public WorldSettings getWorldSettings() { return worldSettings; }
    public PortalManager getPortalManager() { return portalManager; }
    public TeamScoreboardManager getTeamScoreboardManager() { return teamScoreboardManager; }
    public ScoreboardSwitcher getScoreboardSwitcher() { return scoreboardSwitcher; }
    public ArenaSettingsGUI getArenaSettingsGUI() { return arenaSettingsGUI; }
    public ArenaCompassManager getArenaCompassManager() { return arenaCompassManager; }
    public PlayerGUI getPlayerGUI() { return playerGUI; }
    public PlayerActionGUI getPlayerActionGUI() { return playerActionGUI; }
    public TeamSettingsGUI getTeamSettingsGUI() { return teamSettingsGUI; }
    public TeamEquipmentManager getTeamEquipmentManager() { return teamEquipmentManager; }
    public TeamEquipmentGUI getTeamEquipmentGUI() { return teamEquipmentGUI; }
    @SuppressWarnings("unused")
    public PlayerListener getPlayerJoinListener() { return playerListener; }
    public LanguageManager getLanguageManager() {return languageManager;}
    public ButtonManager getButtonManager() {return buttonManager;}
}