package runi.myddns.challenges;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import runi.myddns.challenges.core.commands.GameSettingsCommand;
import runi.myddns.challenges.core.commands.LanguageCommand;
import runi.myddns.challenges.core.commands.LobbyCommand;
import runi.myddns.challenges.core.display.LobbyButtonManager;
import runi.myddns.challenges.core.display.LobbyDisplayManager;
import runi.myddns.challenges.core.files.PluginFileManager;
import runi.myddns.challenges.core.game.*;
import runi.myddns.challenges.core.gui.LanguageSelectionGUI;
import runi.myddns.challenges.core.gui.WorldSettingsGUI;
import runi.myddns.challenges.core.language.LanguageManager;
import runi.myddns.challenges.core.player.PlayerGameDataListener;
import runi.myddns.challenges.core.player.PlayerGameDataManager;
import runi.myddns.challenges.core.server.ServerIconManager;
import runi.myddns.challenges.core.utils.ConsoleColor;
import runi.myddns.challenges.core.world.*;
import runi.myddns.challenges.core.world.lobby.LobbyWorldManager;
import runi.myddns.challenges.games.LevelBlock.LevelBlockGame;
import runi.myddns.challenges.games.LevelBorder.LevelBorderGame;
import runi.myddns.challenges.games.MobArmyBattle.MobArmyBattleGame;
import runi.myddns.challenges.listener.LobbyRespawnListener;
import runi.myddns.challenges.listener.PlayerJoinListener;
import runi.myddns.challenges.listener.UltraUltraHardcoreListener;

import static runi.myddns.challenges.core.utils.DisplayColor.*;

public final class ChallengeMain extends JavaPlugin {

    private WorldSourceManager worldSourceManager;
    private GameWorldManager gameWorldManager;
    private LobbyWorldManager lobbyWorldManager;
    private LobbyDisplayManager lobbyDisplayManager;
    private LanguageManager languageManager;
    private LanguageSelectionGUI languageSelectionGUI;
    private GameManager gameManager;
    private GameStateManager gameStateManager;
    private PlayerGameDataManager playerGameDataManager;
    private WorldSettingsGUI worldSettingsGUI;

    @Override
    public void onLoad() {
        printLoadingMessage();
    }

    @Override
    public void onEnable() {
        ServerIconManager serverIconManager = new ServerIconManager(this);

        new PluginFileManager(this).checkFilesOnStartup();

        setupWorlds();
        setupLanguage();
        setupLobby();
        setupGames();

        worldSettingsGUI = new WorldSettingsGUI(this);

        LobbyButtonManager lobbyButtonManager = new LobbyButtonManager(this, lobbyDisplayManager, gameManager);

        playerGameDataManager = new PlayerGameDataManager(this);

        registerListeners(serverIconManager, lobbyButtonManager);
        createLobbyDisplay(lobbyButtonManager);
        registerCommands();

        printReadyMessage();
    }

    private void setupWorlds() {
        worldSourceManager = new WorldSourceManager(this);
        gameWorldManager = new GameWorldManager(this, worldSourceManager);

        worldSourceManager.prepareAllWorlds();

        lobbyWorldManager = new LobbyWorldManager(this);
        lobbyWorldManager.loadWorld();
    }

    private void setupLanguage() {
        languageManager = new LanguageManager(this);
        languageSelectionGUI = new LanguageSelectionGUI(this);
    }

    private void setupLobby() {
        lobbyDisplayManager = new LobbyDisplayManager(this);
    }

    private void setupGames() {
        gameStateManager = new GameStateManager(this);

        gameManager = new GameManager();
        gameManager.registerGame(new MobArmyBattleGame(this, lobbyDisplayManager));
        gameManager.registerGame(new LevelBorderGame(this));
        gameManager.registerGame(new LevelBlockGame(this));

        gameStateManager.resetRuntimeState();

        String savedGameId = gameStateManager.getSelectedGame();

        if (!savedGameId.equalsIgnoreCase("none")) gameManager.selectGame(savedGameId);
    }

    private void registerListeners(ServerIconManager serverIconManager, LobbyButtonManager lobbyButtonManager) {
        getServer().getPluginManager().registerEvents(languageSelectionGUI, this);
        getServer().getPluginManager().registerEvents(lobbyButtonManager, this);
        getServer().getPluginManager().registerEvents(worldSettingsGUI, this);
        getServer().getPluginManager().registerEvents(new LobbyRespawnListener(lobbyWorldManager), this);
        getServer().getPluginManager().registerEvents(serverIconManager, this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerGameDataListener(this), this);
        getServer().getPluginManager().registerEvents(new UltraUltraHardcoreListener(this), this);
    }

    private void createLobbyDisplay(LobbyButtonManager lobbyButtonManager) {
        getServer().getScheduler().runTaskLater(this, () -> {
            lobbyDisplayManager.createShowcaseDisplay(lobbyWorldManager.getLobbyWorld());
            lobbyButtonManager.createButtons(lobbyWorldManager.getLobbyWorld());

            ChallengeGame selectedGame = gameManager.getSelectedGame();

            if (selectedGame != null) {
                lobbyDisplayManager.setSelectedGame(selectedGame.getDisplayName());
            }

            lobbyDisplayManager.clearLoadConsole();

            getServer().getScheduler().runTaskLater(this, () ->
                    lobbyDisplayManager.setLoadStatus("Warte auf Laden...", GREY), 5L);

        }, 2L);
    }

    private void registerCommands() {
        if (getCommand("language") != null) getCommand("language").setExecutor(new LanguageCommand(this));
        if (getCommand("lobby") != null) getCommand("lobby").setExecutor(new LobbyCommand(this));
        if (getCommand("gamesettings") != null) getCommand("gamesettings").setExecutor(new GameSettingsCommand(this));
    }

    @Override
    public void onDisable() {

        if (gameManager != null) {

            ChallengeGame game =
                    gameManager.getSelectedGame();

            if (playerGameDataManager != null) {
                playerGameDataManager.saveOnlinePlayers();
            }

            if (game != null && game.isLoaded()) {
                game.shutdown();
            }
        }

        if (lobbyDisplayManager != null) {
            lobbyDisplayManager.removeAllDisplayEntities();
        }
    }

    private void printLoadingMessage() {
        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage(ConsoleColor.COPPER + "  ═══════════════  BastiGHG Challenges  ═══════════════" + ConsoleColor.RESET);
        Bukkit.getConsoleSender().sendMessage(ConsoleColor.COPPER + "  ═══════════════════  Fan Project  ═══════════════════" + ConsoleColor.RESET);
        Bukkit.getConsoleSender().sendMessage(ConsoleColor.COPPER + "                          V1.0" + ConsoleColor.RESET);
        Bukkit.getConsoleSender().sendMessage(ConsoleColor.COPPER + "                     L O A D I N G" + ConsoleColor.RESET);
        Bukkit.getConsoleSender().sendMessage("");
    }

    private void printReadyMessage() {
        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage(ConsoleColor.DARK_GOLDEN_LIME + "  ═══════════════  BastiGHG Challenges  ═══════════════" + ConsoleColor.RESET);
        Bukkit.getConsoleSender().sendMessage(ConsoleColor.DARK_GOLDEN_LIME + "  ═══════════════════  Fan Project  ═══════════════════" + ConsoleColor.RESET);
        Bukkit.getConsoleSender().sendMessage(ConsoleColor.DARK_GOLDEN_LIME + "                          V1.0" + ConsoleColor.RESET);
        Bukkit.getConsoleSender().sendMessage(ConsoleColor.DARK_GOLDEN_LIME + "                       R E A D Y" + ConsoleColor.RESET);
        Bukkit.getConsoleSender().sendMessage("");
    }

    public GameManager getGameManager() { return gameManager; }
    public LobbyWorldManager getLobbyWorldManager() { return lobbyWorldManager; }
    public LobbyDisplayManager getLobbyDisplayManager() { return lobbyDisplayManager; }
    public LanguageManager getLanguageManager() { return languageManager; }
    public LanguageSelectionGUI getLanguageSelectionGUI() { return languageSelectionGUI; }
    public GameStateManager getGameStateManager() { return gameStateManager; }
    public PlayerGameDataManager getPlayerGameDataManager() { return playerGameDataManager; }
    public WorldSourceManager getWorldSourceManager() { return worldSourceManager; }
    public GameWorldManager getGameWorldManager() { return gameWorldManager; }
    public WorldSettingsGUI getWorldSettingsGUI() { return worldSettingsGUI;}
}