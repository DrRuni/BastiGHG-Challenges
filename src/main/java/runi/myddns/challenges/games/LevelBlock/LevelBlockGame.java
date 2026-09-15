package runi.myddns.challenges.games.LevelBlock;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import runi.myddns.challenges.ChallengeMain;
import runi.myddns.challenges.core.display.LobbyDisplayManager;
import runi.myddns.challenges.core.game.ChallengeGame;
import runi.myddns.challenges.core.utils.ConsoleColor;
import runi.myddns.challenges.core.world.GameWorldDefinition;

import java.util.Collection;

import static runi.myddns.challenges.core.utils.DisplayColor.*;

public class LevelBlockGame implements ChallengeGame {

    private final ChallengeMain plugin;
    private final LobbyDisplayManager lobbyDisplayManager;

    private boolean loaded;
    private boolean loading;

    public LevelBlockGame(ChallengeMain plugin) {
        this.plugin = plugin;
        this.lobbyDisplayManager = plugin.getLobbyDisplayManager();
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
                () -> plugin.getGameWorldManager().loadGameWorlds(getId()),
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
                                        this::finishLoading
                                )
                        )
                )
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
    public void unload() {
        if (!loaded) return;

        loading = false;
        loaded = false;

        plugin.getGameStateManager().setStarted(false);
        plugin.getGameStateManager().setLoaded(false);

        lobbyDisplayManager.setLoadUnloaded(
                getDisplayName()
        );
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

        player.teleport(
                world.getSpawnLocation()
        );
    }


    @Override
    public void leavePlayer(Player player) {

        if (player == null) return;

        World lobby =
                plugin.getLobbyWorldManager().getLobbyWorld();

        if (lobby == null) return;

        player.teleport(
                plugin.getLobbyWorldManager().getSpawn()
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


    public ChallengeMain getPlugin() {
        return plugin;
    }
}