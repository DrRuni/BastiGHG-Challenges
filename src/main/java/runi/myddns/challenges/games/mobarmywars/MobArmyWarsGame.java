package runi.myddns.challenges.games.mobarmywars;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import runi.myddns.challenges.ChallengeMain;
import runi.myddns.challenges.core.display.LobbyDisplayManager;
import runi.myddns.challenges.core.game.ChallengeGame;
import runi.myddns.challenges.games.mobarmywars.GUIs.OptionsGUI;
import runi.myddns.challenges.core.language.LanguageManager;
import runi.myddns.challenges.core.utils.ConsoleColor;
import runi.myddns.challenges.games.mobarmywars.Commands.*;
import runi.myddns.challenges.games.mobarmywars.GUIs.*;
import runi.myddns.challenges.games.mobarmywars.Listeners.*;
import runi.myddns.challenges.games.mobarmywars.Managers.Event.*;
import runi.myddns.challenges.games.mobarmywars.Managers.World.*;
import runi.myddns.challenges.games.mobarmywars.Utils.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MobArmyWarsGame implements ChallengeGame {

    private final ChallengeMain plugin;
    private final LobbyDisplayManager lobbyDisplayManager;
    private final List<Listener> registeredListeners = new ArrayList<>();

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
    private PortalManager portalManager;
    private PlayerEffectManager playerEffectManager;
    private ArenaConfig arenaConfig;
    public OptionsGUI optionenGUI;
    public TimerGUI timerGUI;
    public SetupGUI eventSettingsGUI;
    public TeleportGUI mobArmySettingsGUI;
    public TeamSelectionGUI teamSelectionGUI;
    public BundleGUI bundleGUI;
    public RandomizerExclusionGUI spawnEggGUI;
    private ArenaSettingsGUI arenaSettingsGUI;
    private WorldSettingsGUI worldSettingsGUI;
    private PlayerGUI playerGUI;
    private PlayerActionGUI playerActionGUI;
    private TeamSettingsGUI teamSettingsGUI;
    private TeamScoreboardManager teamScoreboardManager;
    private ScoreboardSwitcher scoreboardSwitcher;
    private ArenaCompassManager arenaCompassManager;
    private TeamEquipmentManager teamEquipmentManager;
    private TeamEquipmentGUI teamEquipmentGUI;
    private PlayerJoinListener playerJoinListener;
    private ChestRandomizerManager chestRandomizerManager;

    private boolean loaded;
    private boolean loading;

    public MobArmyWarsGame(ChallengeMain plugin, LobbyDisplayManager lobbyDisplayManager) {
        this.plugin = plugin;
        this.lobbyDisplayManager = lobbyDisplayManager;
    }

    @Override
    public String getId() {
        return "mobarmywars";
    }

    @Override
    public String getDisplayName() {
        return "MobArmyWars";
    }

    public File getDataFolder() {
        return new File(plugin.getDataFolder(), "games/mobarmywars");
    }

    @Override
    public void load() {
        if (loaded || loading) return;

        loading = true;

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.35f, 0.7f);
        }

        lobbyDisplayManager.setLoadStatus("MobArmyWars wird geladen...", 0xFF5555);
        runLoadSequence();
    }

    private void runLoadSequence() {
        runStep(
                "Welten prüfen...",
                () -> {
                    worldSettings = new WorldSettings(this);
                    worldManager = new WorldManager(this);
                    worldManager.checkWorldsOnStartup();
                },
                () -> runStep(
                        "Manager initialisieren...",
                        this::initializeMobArmyWars,
                        () -> runStep(
                                "Listener registrieren...",
                                this::registerListeners,
                                () -> runStep(
                                        "Befehle registrieren...",
                                        this::registerCommands,
                                        () -> runStep(
                                                "Waves laden...",
                                                () -> {
                                                    waveStorage.loadWaves();
                                                    scheduleArenaReload();
                                                },
                                                () -> runStep(
                                                        "Teams laden...",
                                                        teamManager::loadTeams,
                                                        () -> runStep(
                                                                "Scoreboard vorbereiten...",
                                                                teamScoreboardManager::rebuildBoard,
                                                                () -> runStep(
                                                                        "Timer vorbereiten...",
                                                                        () -> {
                                                                            timerManager.ensureBossBarExists();
                                                                            timerManager.updatePauseState();
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

    private void initializeMobArmyWars() {
        playerEffectManager = new PlayerEffectManager(this);
        blockRandomizerManager = new BlockRandomizerManager(this);
        arenaConfig = new ArenaConfig(plugin);
        arenaManager = new ArenaEventManager(this);
        teamManager = new TeamManager(this);
        arenaBuildProtectionManager = new ArenaBuildProtectionManager(this);
        arenaBuildProtectionManager.loadSpawnProtectionAreas();

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

        optionenGUI = new OptionsGUI(this, plugin.getLanguageManager());
        timerGUI = new TimerGUI(this, timerManager);
        eventSettingsGUI = new SetupGUI(this);
        mobArmySettingsGUI = new TeleportGUI(this);
        teamSelectionGUI = new TeamSelectionGUI(this, teamManager);
        bundleGUI = new BundleGUI(this, teamManager);
        spawnEggGUI = new RandomizerExclusionGUI(blockRandomizerManager, this);
        arenaSettingsGUI = new ArenaSettingsGUI(this);
        worldSettingsGUI = new WorldSettingsGUI(this, blockRandomizerManager);
        playerGUI = new PlayerGUI(this);
        playerActionGUI = new PlayerActionGUI(this);
        teamSettingsGUI = new TeamSettingsGUI(this);
        teamEquipmentGUI = new TeamEquipmentGUI(this);
        playerJoinListener = new PlayerJoinListener(this);
    }

    private void registerListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, plugin);
        registeredListeners.add(listener);
    }

    private void registerListeners() {
        registerListener(new PauseListener(this));
        registerListener(playerJoinListener);
        registerListener(new PlayerRespawnListener(this));
        registerListener(new PortalListener(this));
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
        registerListener(new ButtonManager(this));
        registerListener(new ArenaMobTargetListener(this));
        registerListener(new BundleListener(this, bundleGUI, teamManager, bundleManager));
        registerListener(arenaSettingsGUI);
        registerListener(worldSettingsGUI);
        registerListener(new UltraHardcoreListener(this));
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
        registerCommand("mobarmy", resumeCmd, resumeCmd);

        OptionenCommand optionenCommand = new OptionenCommand(this);
        registerCommand("optionen", optionenCommand, optionenCommand);

        TeamCommand teamCmd = new TeamCommand(this);
        registerCommand("team", teamCmd, teamCmd);

        MobStatusCommand mobStatusCommand = new MobStatusCommand(this, mobSaveManager, teamManager);
        registerCommand("mobstatus", mobStatusCommand, mobStatusCommand);

        registerCommand("arenasummary", new ArenaSummaryCommand(this), null);

        SetPhaseCommand setPhaseCommand = new SetPhaseCommand(this);
        registerCommand("setphase", setPhaseCommand, setPhaseCommand);

        ResetCommand resetCommand = new ResetCommand(this);
        registerCommand("reset", resetCommand, resetCommand);

        InfoCommand infoCommand = new InfoCommand(this);
        registerCommand("info", infoCommand, null);
    }

    @Override
    public boolean canUnload() {
        return eventResume != null && eventResume.loadPhase() == ResumeManager.PHASE_LOBBY && !hasPlayers();
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

        for (Listener listener : registeredListeners) {
            HandlerList.unregisterAll(listener);
        }

        registeredListeners.clear();

        if (worldManager != null) worldManager.unloadWorlds();

        loaded = false;

        plugin.getGameStateManager().setStarted(false);
        plugin.getGameStateManager().setLoaded(false);

        lobbyDisplayManager.setLoadUnloaded(getDisplayName());
    }

    @Override
    public void joinActiveGame(Player player) {

        boolean restored = eventResume.restorePlayerPosition(player);

        if (!restored) {
            TeleportManager.teleport(this, player, "world_mobarmy_lobby");
        }

        playerEffectManager.applyNightVision(player);

        playerJoinListener.showWelcomeSequence(player);
        playerJoinListener.showHelpHint(player);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;

            timerManager.ensureBossBarExists();
            timerManager.addPlayerToBossBar(player);
            timerManager.updatePauseState();

            teamScoreboardManager.updateBoard();
            scoreboardSwitcher.switchToTeam(player);

        }, 20L * 7);
    }

    @Override
    public boolean hasPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            String worldName = player.getWorld().getName();

            if (worldName.equals("world_mobarmy_lobby")
                    || worldName.equals("world_mobarmy_arena")
                    || worldName.equals("world_rot")
                    || worldName.equals("world_blau")
                    || worldName.equals("world_rot_nether")
                    || worldName.equals("world_blau_nether")) {
                return true;
            }
        }

        return false;
    }

    private void scheduleArenaReload() {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try {
                arenaConfig.reload();
                arenaBuildProtectionManager.loadSpawnProtectionAreas();
            } catch (Exception ex) {
                plugin.getLogger().log(
                        java.util.logging.Level.SEVERE,
                        "Failed to reload build protection.",
                        ex
                );
            }
        }, 20L);
    }

    private void registerCommand(String name, org.bukkit.command.CommandExecutor executor, org.bukkit.command.TabCompleter completer) {
        var command = plugin.getCommand(name);

        if (command == null) {
            plugin.getLogger().warning("Command nicht in plugin.yml gefunden: /" + name);
            return;
        }

        command.setExecutor(executor);

        if (completer != null) command.setTabCompleter(completer);
    }

    private void runStep(String status, Runnable action, Runnable next) {
        lobbyDisplayManager.setLoadStatus(status, 0xFFAA00);

        try {
            action.run();

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_OFF, 0.25f, 1.2f);
            }

        } catch (Exception ex) {
            loading = false;
            lobbyDisplayManager.setLoadStatus("FEHLER: " + status, 0xFF3333);
            plugin.getLogger().severe("Fehler beim Laden von MobArmyWars: " + status);
            ex.printStackTrace();
            return;
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, next, 15L);
    }

    private void finishLoading() {
        loaded = true;
        loading = false;
        plugin.getGameStateManager().setLoaded(true);

        lobbyDisplayManager.setLoadReady(getDisplayName());

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.6f, 1.4f);
        }

        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage(ConsoleColor.DARK_GOLDEN_LIME + "  ═══════════════  MobArmyWars - V1.7 ═══════════════" + ConsoleColor.RESET);
        Bukkit.getConsoleSender().sendMessage(ConsoleColor.DARK_GOLDEN_LIME + "                       geladen"                        + ConsoleColor.RESET);
        Bukkit.getConsoleSender().sendMessage("");
    }

    @Override
    public boolean hasOtherPlayers(Player ignoredPlayer) {
        for (Player player : Bukkit.getOnlinePlayers()) {

            if (player.equals(ignoredPlayer)) continue;

            String worldName = player.getWorld().getName();

            if (worldName.equals("world_mobarmy_lobby")
                    || worldName.equals("world_mobarmy_arena")
                    || worldName.equals("world_rot")
                    || worldName.equals("world_blau")
                    || worldName.equals("world_rot_nether")
                    || worldName.equals("world_blau_nether")) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void startPlayers(Collection<? extends Player> players) {
        if (!loaded) return;

        for (Player player : players) {

            TeleportManager.teleport(this, player, "world_mobarmy_lobby");

            playerEffectManager.applyNightVision(player);

            playerJoinListener.showWelcomeSequence(player);
            playerJoinListener.showHelpHint(player);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;

                timerManager.ensureBossBarExists();
                timerManager.addPlayerToBossBar(player);
                timerManager.updatePauseState();

                teamScoreboardManager.updateBoard();
                scoreboardSwitcher.switchToTeam(player);

            }, 20L * 7);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (worldManager != null) {
                worldManager.preloadTeamWorlds();
            }
        }, 80L);
    }

    @Override
    public void leavePlayer(Player player) {
        if (scoreboardSwitcher != null) {
            scoreboardSwitcher.removePlayer(player);
        }

        if (timerManager != null) {
            timerManager.removeBossBarFor(player);
        }
    }

    @Override
    public boolean isLoaded() {
        return loaded;
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
    public PlayerEffectManager getPlayerEffectManager() { return playerEffectManager; }
    public TeamScoreboardManager getTeamScoreboardManager() { return teamScoreboardManager; }
    public ScoreboardSwitcher getScoreboardSwitcher() { return scoreboardSwitcher; }
    public ArenaSettingsGUI getArenaSettingsGUI() { return arenaSettingsGUI; }
    public WorldSettingsGUI getWorldSettingsGUI() { return worldSettingsGUI; }
    public ArenaCompassManager getArenaCompassManager() { return arenaCompassManager; }
    public PlayerGUI getPlayerGUI() { return playerGUI; }
    public PlayerActionGUI getPlayerActionGUI() { return playerActionGUI; }
    public TeamSettingsGUI getTeamSettingsGUI() { return teamSettingsGUI; }
    public TeamEquipmentManager getTeamEquipmentManager() { return teamEquipmentManager; }
    public TeamEquipmentGUI getTeamEquipmentGUI() { return teamEquipmentGUI; }
    public PlayerJoinListener getPlayerJoinListener() { return playerJoinListener; }
    public LanguageManager getLanguageManager() { return plugin.getLanguageManager();
    }
}