package runi.myddns.challenges.core.display;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import runi.myddns.challenges.ChallengeMain;
import runi.myddns.challenges.core.game.ChallengeGame;
import runi.myddns.challenges.core.game.GameManager;
import runi.myddns.challenges.games.MobArmyBattle.MobArmyBattleGame;

import java.util.HashMap;
import java.util.Map;

import static runi.myddns.challenges.core.utils.DisplayColor.*;

public class LobbyButtonManager implements Listener {

    private final ChallengeMain plugin;
    private final LobbyDisplayManager lobbyDisplayManager;
    private final GameManager gameManager;

    public LobbyButtonManager(ChallengeMain plugin, LobbyDisplayManager lobbyDisplayManager, GameManager gameManager) {
        this.plugin = plugin;
        this.lobbyDisplayManager = lobbyDisplayManager;
        this.gameManager = gameManager;
    }

    private static final double START_X = 1.0;
    private static final double START_Y = 67.0;
    private static final double START_Z = 48.0;

    private static final double BUTTON_Y = START_Y + 0.42;
    private static final double BUTTON_Z = START_Z - 0.20;

    private static final float BUTTON_YAW = 180.0f;
    private static final float BUTTON_WIDTH = 2.40f;
    private static final float BUTTON_HEIGHT = 0.90f;
    private static final float BUTTON_SCALE = 0.78f;

    private static final String PROTECTED_TAG = "challenge_protected";
    private static final String BUTTON_TAG_PREFIX = "challenge_lobby_button_";
    private static final String VISUAL_TAG = "challenge_lobby_button_visual";

    private static final Color NORMAL_BACKGROUND =
            Color.fromARGB(
                    190,
                    6,
                    24,
                    24
            );

    private static final Color CLICK_BACKGROUND =
            Color.fromARGB(
                    220,
                    18,
                    55,
                    52
            );

    private static final String[] BUTTON_NAMES = {
            "SELECT",
            "LOAD",
            "START",
            "SETTINGS",
            "WORLDSETTINGS"
    };

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private final Map<Integer, TextDisplay> buttonDisplays =
            new HashMap<>();

    public void createButtons(World world) {

        removeOldButtons(world);

        buttonDisplays.clear();

        double[] positionsX = {
                START_X + 10.20,
                START_X + 7.75,
                START_X + 5.30,
                START_X + 2.85,
                START_X + 2.85
        };

        double[] positionsY = {
                BUTTON_Y,
                BUTTON_Y,
                BUTTON_Y,
                BUTTON_Y,
                BUTTON_Y + 1.00
        };

        for (int i = 0; i < BUTTON_NAMES.length; i++) {

            createButton(
                    world,
                    i,
                    BUTTON_NAMES[i],
                    positionsX[i],
                    positionsY[i]
            );
        }
    }

    private void createButton(
            World world,
            int index,
            String name,
            double x,
            double y
    ) {

        Location textLocation =
                new Location(
                        world,
                        x,
                        y,
                        BUTTON_Z,
                        BUTTON_YAW,
                        0.0f
                );

        TextDisplay textDisplay =
                world.spawn(
                        textLocation,
                        TextDisplay.class,
                        display -> {

                            display.text(
                                    createNormalText(name)
                            );


                            display.setAlignment(
                                    TextDisplay.TextAlignment.CENTER
                            );

                            display.setBillboard(
                                    Display.Billboard.FIXED
                            );

                            display.setBackgroundColor(
                                    NORMAL_BACKGROUND
                            );

                            display.setShadowed(true);

                            display.setSeeThrough(false);

                            display.setLineWidth(300);

                            display.setBrightness(
                                    new Display.Brightness(
                                            15,
                                            15
                                    )
                            );

                            display.setTransformation(
                                    new Transformation(
                                            new Vector3f(),
                                            new AxisAngle4f(),
                                            new Vector3f(
                                                    BUTTON_SCALE,
                                                    BUTTON_SCALE,
                                                    BUTTON_SCALE
                                            ),
                                            new AxisAngle4f()
                                    )
                            );

                            display.addScoreboardTag(
                                    PROTECTED_TAG
                            );

                            display.addScoreboardTag(
                                    VISUAL_TAG
                            );

                            display.addScoreboardTag(
                                    BUTTON_TAG_PREFIX + index
                            );
                        }
                );

        buttonDisplays.put(
                index,
                textDisplay
        );

        Location interactionLocation =
                new Location(
                        world,
                        x,
                        y,
                        BUTTON_Z - 0.05
                );

        world.spawn(
                interactionLocation,
                Interaction.class,
                interaction -> {

                    interaction.setInteractionWidth(
                            BUTTON_WIDTH
                    );

                    interaction.setInteractionHeight(
                            BUTTON_HEIGHT
                    );


                    interaction.setResponsive(true);

                    interaction.addScoreboardTag(
                            PROTECTED_TAG
                    );

                    interaction.addScoreboardTag(
                            BUTTON_TAG_PREFIX + index
                    );
                }
        );
    }

    @EventHandler
    public void onRightClick(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Interaction interaction)) return;

        Integer button = getButtonIndex(interaction);
        if (button == null) return;

        event.setCancelled(true);

        clickButton(event.getPlayer(), button);
    }

    @EventHandler
    public void onLeftClick(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof Interaction interaction)) return;

        Integer button = getButtonIndex(interaction);
        if (button == null) return;

        event.setCancelled(true);

        clickButton(player, button);
    }

    private void clickButton(Player player, int index) {

        // =========================
        // SELECT
        // =========================

        if (index == 0) {

            ChallengeGame currentGame = gameManager.getSelectedGame();

            if (currentGame != null && currentGame.isLoading()) {
                lobbyDisplayManager.setLoadStatus(
                        "Während des Ladens kann das Game nicht gewechselt werden.",
                        BLOOD_RED
                );

                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (currentGame.isLoading()) {
                        lobbyDisplayManager.setLoadStatus(
                                currentGame.getDisplayName() + " wird geladen...",
                                LOAD_RED
                        );
                    } else if (currentGame.isLoaded()) {
                        lobbyDisplayManager.setLoadReady(currentGame.getDisplayName());
                    }
                }, 40L);

                return;
            }

//            if (plugin.getGameStateManager().isStarted()) {
//                lobbyDisplayManager.setLoadStatus(
//                        "Das laufende Game kann nicht gewechselt werden.",
//                        0xA61B1B
//                );
//
//                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
//                    if (currentGame != null && currentGame.isLoaded()) {
//                        lobbyDisplayManager.setLoadReady(currentGame.getDisplayName());
//                    }
//                }, 40L);
//
//                return;
//            }

            if (currentGame != null && currentGame.isLoaded()) {
                currentGame.unload();
            }

            gameManager.selectNextGame();

            ChallengeGame newGame = gameManager.getSelectedGame();

            if (newGame != null) {
                plugin.getGameStateManager().setSelectedGame(newGame.getId());
                lobbyDisplayManager.setSelectedGame(newGame.getDisplayName());
            }
        }

        // =========================
        // LOAD
        // =========================

        if (index == 1) {

            ChallengeGame game = gameManager.getSelectedGame();

            if (game == null) return;

            if (game.isLoaded()) {
                lobbyDisplayManager.setLoadStatus(
                        game.getDisplayName() + " wurde bereits geladen",
                        0xA61B1B
                );

                plugin.getServer().getScheduler().runTaskLater(
                        plugin,
                        () -> {
                            if (game.isLoaded()) {
                                lobbyDisplayManager.setLoadReady(game.getDisplayName());
                            }
                        },
                        40L
                );

                return;
            }

            gameManager.loadSelectedGame();
        }

        // =========================
        // START
        // =========================

        if (index == 2) {

            ChallengeGame game = gameManager.getSelectedGame();

            if (game == null) {
                lobbyDisplayManager.setLoadStatus(
                        "Bitte zuerst ein Game auswählen.",
                        0xFF5555
                );
                return;
            }

            if (!game.isLoaded()) {
                lobbyDisplayManager.setLoadStatus(
                        plugin.getLanguageManager().get("lobby-display.load-first"),
                        0xFFAA00
                );
                return;
            }

            World lobbyWorld = plugin.getLobbyWorldManager().getLobbyWorld();

            if (lobbyWorld == null) {
                lobbyDisplayManager.setLoadStatus(
                        plugin.getLanguageManager().get("lobby-display.lobby-world-missing"),
                        0xFF5555
                );
                return;
            }

            plugin.getGameStateManager().setStarted(true);
            game.startPlayers(lobbyWorld.getPlayers());
        }

        // =========================
        // SETTINGS
        // =========================

        if (index == 3) {

            ChallengeGame game = gameManager.getSelectedGame();

            if (game == null) {
                lobbyDisplayManager.setLoadStatus(
                        plugin.getLanguageManager().get("lobby-display.no-game-selected"),
                        0xFF5555
                );
                return;
            }

            if (!game.isLoaded()) {
                lobbyDisplayManager.setLoadStatus(
                        plugin.getLanguageManager().get("lobby-display.settings-load-first"),
                        0xFFAA00
                );
                return;
            }

            if (game.getId().equalsIgnoreCase("levelborder")) {
                lobbyDisplayManager.setLoadStatus(
                        "LevelBorder-Einstellungen sind derzeit noch nicht verfügbar.",
                        0xA61B1B
                );

                plugin.getServer().getScheduler().runTaskLater(
                        plugin,
                        () -> {
                            if (game.isLoaded()) {
                                lobbyDisplayManager.setLoadReady(game.getDisplayName());
                            }
                        },
                        40L
                );

                return;
            }

            if (game instanceof MobArmyBattleGame mobArmyBattleGame) {
                mobArmyBattleGame.getOptionenGUI().open(player);
            }
        }

        // =========================
        // WORLDSETTINGS
        // =========================

        if (index == 4) {

            ChallengeGame game = gameManager.getSelectedGame();

            if (game == null) {
                lobbyDisplayManager.setLoadStatus(
                        plugin.getLanguageManager().get("lobby-display.no-game-selected"),
                        0xFF5555
                );
                return;
            }

            if (!game.isLoaded()) {
                lobbyDisplayManager.setLoadStatus(
                        "Bitte zuerst ein Game laden.",
                        0xFFAA00
                );
                return;
            }

            if (game instanceof MobArmyBattleGame mobArmyBattleGame) {
                mobArmyBattleGame.getWorldSettingsGUI().open(player);
                return;
            }

            plugin.getWorldSettingsGUI().open(player);
        }

        // =========================
        // NUR OPTISCHE REAKTION
        // =========================

        TextDisplay display =
                findButtonDisplay(index);

        if (display == null) {

            plugin.getLogger().warning(
                    "Button-TextDisplay "
                            + index
                            + " wurde nicht gefunden."
            );

            return;
        }

        display.setBackgroundColor(
                CLICK_BACKGROUND
        );

        plugin.getServer()
                .getScheduler()
                .runTaskLater(
                        plugin,
                        () -> {

                            if (!display.isValid()) {
                                return;
                            }

                            display.setBackgroundColor(
                                    NORMAL_BACKGROUND
                            );
                        },
                        10L
                );
    }

    private net.kyori.adventure.text.Component createNormalText(
            String name
    ) {

        return miniMessage.deserialize(
                "<#67F5E8>" + name + "</#67F5E8>"
        );
    }

    private Integer getButtonIndex(
            Interaction interaction
    ) {

        for (String tag :
                interaction.getScoreboardTags()) {

            if (!tag.startsWith(
                    BUTTON_TAG_PREFIX
            )) {
                continue;
            }

            try {

                return Integer.parseInt(
                        tag.substring(
                                BUTTON_TAG_PREFIX.length()
                        )
                );

            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        return null;
    }

    private void removeOldButtons(
            World world
    ) {

        Location center =
                new Location(
                        world,
                        START_X + 5.75,
                        START_Y,
                        START_Z
                );

        world.getChunkAt(center).load();

        for (Entity entity :
                world.getNearbyEntities(
                        center,
                        15,
                        5,
                        5
                )) {

            boolean buttonEntity =
                    entity.getScoreboardTags()
                            .stream()
                            .anyMatch(
                                    tag ->
                                            tag.startsWith(
                                                    BUTTON_TAG_PREFIX
                                            )
                            );

            if (buttonEntity
                    || entity.getScoreboardTags()
                    .contains(VISUAL_TAG)) {

                entity.remove();
            }
        }
    }

    private TextDisplay findButtonDisplay(int index) {

        String tag =
                BUTTON_TAG_PREFIX + index;

        for (World world :
                plugin.getServer().getWorlds()) {

            for (Entity entity :
                    world.getEntities()) {

                if (!(entity instanceof TextDisplay display)) {
                    continue;
                }

                if (display.getScoreboardTags()
                        .contains(tag)) {

                    return display;
                }
            }
        }

        return null;
    }
}