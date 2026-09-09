package runi.myddns.challenges.core.display;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
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

import java.util.HashMap;
import java.util.Map;

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
            "SETTINGS"
    };

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private final Map<Integer, TextDisplay> buttonDisplays =
            new HashMap<>();

    public void createButtons(World world) {

        removeOldButtons(world);

        buttonDisplays.clear();

        double[] positions = {
                START_X + 10.20, // SELECT - visuell links
                START_X + 7.75,  // LOAD
                START_X + 5.30,  // START
                START_X + 2.85   // SETTINGS - visuell rechts
        };

        for (int i = 0; i < BUTTON_NAMES.length; i++) {

            createButton(
                    world,
                    i,
                    BUTTON_NAMES[i],
                    positions[i]
            );
        }
    }

    private void createButton(
            World world,
            int index,
            String name,
            double x
    ) {

        Location textLocation =
                new Location(
                        world,
                        x,
                        BUTTON_Y,
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
                        BUTTON_Y,
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
    public void onRightClick(
            PlayerInteractEntityEvent event
    ) {

        if (!(event.getRightClicked()
                instanceof Interaction interaction)) {
            return;
        }

        Integer button =
                getButtonIndex(interaction);

        if (button == null) {
            return;
        }

        event.setCancelled(true);

        clickButton(button);
    }

    @EventHandler
    public void onLeftClick(
            EntityDamageByEntityEvent event
    ) {

        if (!(event.getDamager()
                instanceof Player)) {
            return;
        }

        if (!(event.getEntity()
                instanceof Interaction interaction)) {
            return;
        }

        Integer button =
                getButtonIndex(interaction);

        if (button == null) {
            return;
        }

        event.setCancelled(true);

        clickButton(button);
    }

    private void clickButton(int index) {

        plugin.getLogger().info(
                "BUTTON CLICK: " + index
        );


        // =========================
        // SELECT
        // =========================

        if (index == 0) {
            plugin.getLogger().info("SELECT wurde gedrückt");

            ChallengeGame currentGame = gameManager.getSelectedGame();

            if (currentGame != null && currentGame.isLoaded()) {
                if (!currentGame.canUnload()) {
                    plugin.getLogger().info("Game kann noch nicht gewechselt werden.");
                    return;
                }

                currentGame.unload();
            }

            gameManager.selectNextGame();

            ChallengeGame newGame = gameManager.getSelectedGame();

            if (newGame != null) {
                plugin.getGameStateManager().setSelectedGame(newGame.getId());
            }
        }


        // =========================
        // LOAD
        // =========================

        if (index == 1) {

            plugin.getLogger().info(
                    "LOAD wurde gedrückt"
            );

            gameManager.loadSelectedGame();
        }


        // =========================
        // START
        // =========================

        if (index == 2) {

            plugin.getLogger().info(
                    "START wurde gedrückt"
            );

            ChallengeGame game =
                    gameManager.getSelectedGame();

            if (game == null) {
                return;
            }

            if (!game.isLoaded()) {
                return;
            }

            World lobbyWorld =
                    Bukkit.getWorld(
                            "BastiGHG_Challenges_Lobby"
                    );

            if (lobbyWorld == null) {
                return;
            }

            plugin.getGameStateManager().setStarted(true);
            game.startPlayers(lobbyWorld.getPlayers());
        }


        // =========================
        // SETTINGS
        // =========================

        if (index == 3) {

            plugin.getLogger().info(
                    "SETTINGS wurde gedrückt"
            );

            // später
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