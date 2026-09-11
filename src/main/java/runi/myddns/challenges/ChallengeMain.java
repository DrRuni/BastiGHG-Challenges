package runi.myddns.challenges;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import runi.myddns.challenges.core.commands.LanguageCommand;
import runi.myddns.challenges.core.commands.LobbyCommand;
import runi.myddns.challenges.core.display.LobbyButtonManager;
import runi.myddns.challenges.core.display.LobbyDisplayManager;
import runi.myddns.challenges.core.files.PluginFileManager;
import runi.myddns.challenges.core.game.ChallengeGame;
import runi.myddns.challenges.core.game.GameManager;
import runi.myddns.challenges.core.game.GameStateManager;
import runi.myddns.challenges.core.language.LanguageManager;
import runi.myddns.challenges.core.server.ServerIconManager;
import runi.myddns.challenges.core.world.LobbyWorldManager;
import runi.myddns.challenges.core.utils.ConsoleColor;
import runi.myddns.challenges.core.gui.LanguageSelectionGUI;
import runi.myddns.challenges.games.mobarmywars.MobArmyWarsGame;
import runi.myddns.challenges.listener.LobbyRespawnListener;
import runi.myddns.challenges.listener.PlayerJoinListener;

public final class ChallengeMain extends JavaPlugin {

    private LobbyWorldManager lobbyWorldManager;
    private LobbyDisplayManager lobbyDisplayManager;
    private LanguageManager languageManager;
    private LanguageSelectionGUI languageSelectionGUI;
    private GameManager gameManager;
    private GameStateManager gameStateManager;

    @Override
    public void onLoad() {

        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage(
                ConsoleColor.COPPER + "  ═══════════════  BastiGHG Challenges  ═══════════════" + ConsoleColor.RESET);
        Bukkit.getConsoleSender().sendMessage(
                ConsoleColor.COPPER + "  ═══════════════════  Fan Project  ═══════════════════" + ConsoleColor.RESET);
        Bukkit.getConsoleSender().sendMessage(
                ConsoleColor.COPPER + "                          V1.0" + ConsoleColor.RESET);
        Bukkit.getConsoleSender().sendMessage(
                ConsoleColor.COPPER + "                     L O A D I N G" + ConsoleColor.RESET);
        Bukkit.getConsoleSender().sendMessage("");

    }

    @Override
    public void onEnable() {

        ServerIconManager serverIconManager = new ServerIconManager(this);

        PluginFileManager pluginFileManager = new PluginFileManager(this);
        pluginFileManager.checkFilesOnStartup();

        languageManager = new LanguageManager(this);
        languageSelectionGUI = new LanguageSelectionGUI(this);

        lobbyWorldManager = new LobbyWorldManager(this);
        lobbyWorldManager.loadWorld();

        lobbyDisplayManager = new LobbyDisplayManager(this);

        gameStateManager = new GameStateManager(this);

        gameManager = new GameManager();
        gameManager.registerGame(new MobArmyWarsGame(this, lobbyDisplayManager));

        LobbyButtonManager lobbyButtonManager = new LobbyButtonManager(this, lobbyDisplayManager, gameManager);

        getServer().getPluginManager().registerEvents(languageSelectionGUI, this);
        getServer().getPluginManager().registerEvents(lobbyButtonManager, this);
        getServer().getPluginManager().registerEvents(new LobbyRespawnListener(lobbyWorldManager), this);
        getServer().getPluginManager().registerEvents(serverIconManager, this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getScheduler().runTaskLater(this, () -> {

            lobbyDisplayManager.createShowcaseDisplay(lobbyWorldManager.getLobbyWorld());

            lobbyButtonManager.createButtons(lobbyWorldManager.getLobbyWorld());

            ChallengeGame selectedGame = gameManager.getSelectedGame();

            if (selectedGame != null) {
                lobbyDisplayManager.setSelectedGame(selectedGame.getDisplayName());
            }

        }, 2L);

        LanguageCommand languageCommand = new LanguageCommand(this);

        if (getCommand("language") != null) {
            getCommand("language").setExecutor(languageCommand);
        }

        LobbyCommand lobbyCommand = new LobbyCommand(this);

        if (getCommand("lobby") != null) {
            getCommand("lobby").setExecutor(lobbyCommand);
        }

        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage(
                ConsoleColor.DARK_GOLDEN_LIME + "  ═══════════════  BastiGHG Challenges  ═══════════════" + ConsoleColor.RESET);
        Bukkit.getConsoleSender().sendMessage(
                ConsoleColor.DARK_GOLDEN_LIME + "  ═══════════════════  Fan Project  ═══════════════════" + ConsoleColor.RESET);
        Bukkit.getConsoleSender().sendMessage(
                ConsoleColor.DARK_GOLDEN_LIME + "                          V1.0" + ConsoleColor.RESET);
        Bukkit.getConsoleSender().sendMessage(
                ConsoleColor.DARK_GOLDEN_LIME + "                       R E A D Y" + ConsoleColor.RESET);
        Bukkit.getConsoleSender().sendMessage("");
    }

    @Override
    public void onDisable() {
        if (lobbyDisplayManager != null) {
            lobbyDisplayManager.removeAllDisplayEntities();
        }
    }

    public GameManager getGameManager() { return gameManager; }
    public LobbyWorldManager getLobbyWorldManager() {
        return lobbyWorldManager;
    }
    public LobbyDisplayManager getLobbyDisplayManager() { return lobbyDisplayManager;}
    public LanguageManager getLanguageManager() { return languageManager; }
    public LanguageSelectionGUI getLanguageSelectionGUI() { return languageSelectionGUI; }
    public GameStateManager getGameStateManager() { return gameStateManager; }
}