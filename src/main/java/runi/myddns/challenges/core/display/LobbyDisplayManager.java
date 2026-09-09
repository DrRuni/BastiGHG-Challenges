package runi.myddns.challenges.core.display;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Interaction;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class LobbyDisplayManager {

    private static final String DISPLAY_TAG = "challenge_lobby_showcase";
    private static final String TITLE_TAG = "challenge_lobby_title";
    private static final String GAME_LABEL_TAG = "challenge_lobby_game_label";
    private static final String GAME_NAME_TAG = "challenge_lobby_game_name";
    private static final String INTERACTION_TAG = "challenge_lobby_display_interaction";
    private static final String PROTECTED_TAG = "challenge_protected";
    private static final String LOAD_CONSOLE_TAG = "challenge_lobby_load_console";

    private static final double START_X = 1.0;
    private static final double START_Y = 67.0;
    private static final double START_Z = 48.0;

    private static final float WIDTH = 11.5f;
    private static final float HEIGHT = 6.0f;

    private static final double TEXT_CENTER_X = START_X + WIDTH / 2.0;
    private static final double TEXT_CENTER_OFFSET_X = 0.0;

    private static final float OUTER_FRAME = 0.16f;
    private static final float INNER_FRAME = 0.06f;
    private static final float DEPTH = 0.10f;

    private static final float TEXT_Z_OFFSET = -0.08f;

    private static final Material FRAME_MATERIAL = Material.DARK_PRISMARINE;
    private static final Material INNER_FRAME_MATERIAL = Material.WARPED_PLANKS;
    private static final Material ACCENT_MATERIAL = Material.LIME_CONCRETE;
    private static final Material BACKGROUND_MATERIAL = Material.BLACK_CONCRETE;
    private static final Material GLASS_MATERIAL = Material.TINTED_GLASS;

    private final JavaPlugin plugin;
    private TextDisplay titleDisplay;
    private TextDisplay gameLabelDisplay;
    private TextDisplay gameNameDisplay;
    private TextDisplay loadConsoleDisplay;

    public LobbyDisplayManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void createShowcaseDisplay(World world) {

        Location center =
                new Location(
                        world,
                        START_X + WIDTH / 2.0,
                        START_Y + HEIGHT / 2.0,
                        START_Z
                );

        world.getChunkAt(center).load();


        // =============================
        // EXISTIERENDES DISPLAY SUCHEN
        // =============================

        titleDisplay =
                findTextDisplay(
                        world,
                        TITLE_TAG
                );

        gameLabelDisplay =
                findTextDisplay(
                        world,
                        GAME_LABEL_TAG
                );

        gameNameDisplay =
                findTextDisplay(
                        world,
                        GAME_NAME_TAG
                );

        loadConsoleDisplay =
                findTextDisplay(
                        world,
                        LOAD_CONSOLE_TAG
                );


        // Alle wichtigen Teile gefunden?
        if (titleDisplay != null
                && gameLabelDisplay != null
                && gameNameDisplay != null) {

            plugin.getLogger().info(
                    "Vorhandenes Lobby-Display übernommen."
            );

            if (loadConsoleDisplay == null) {
                createLoadConsole(world);
            }

            protectDisplayEntities(world);

            return;
        }


        // =============================
        // KEIN VOLLSTÄNDIGES DISPLAY
        // =============================

        plugin.getLogger().info(
                "Kein vollständiges Lobby-Display gefunden. Erstelle neu."
        );

        removeBrokenDisplay(world);

        createBackground(world);
        createOuterFrame(world);
        createInnerFrame(world);
        createAccentElements(world);
        createTexts(world);
        createLoadConsole(world);

        protectDisplayEntities(world);
    }

    public void setLoadUnloaded(String gameName) {
        setLoadStatus(gameName + " entladen", 0xA61B1B);
    }

    private TextDisplay findTextDisplay(
            World world,
            String tag
    ) {

        TextDisplay found = null;

        for (Entity entity : world.getEntities()) {

            if (!(entity instanceof TextDisplay display)) {
                continue;
            }

            if (!display.getScoreboardTags()
                    .contains(tag)) {
                continue;
            }

            if (found == null) {

                found = display;

            } else {

                display.remove();

                plugin.getLogger().warning(
                        "Doppeltes Display entfernt: "
                                + tag
                );
            }
        }

        return found;
    }

    private void removeBrokenDisplay(World world) {

        for (Entity entity : world.getEntities()) {

            boolean showcase =
                    entity.getScoreboardTags()
                            .contains(DISPLAY_TAG);

            boolean title =
                    entity.getScoreboardTags()
                            .contains(TITLE_TAG);

            boolean gameLabel =
                    entity.getScoreboardTags()
                            .contains(GAME_LABEL_TAG);

            boolean gameName =
                    entity.getScoreboardTags()
                            .contains(GAME_NAME_TAG);

            if (showcase
                    || title
                    || gameLabel
                    || gameName) {

                entity.remove();
            }
        }
    }

    private void createBackground(World world) {

        createBlock(
                world,
                START_X + 0.18,
                START_Y + 0.18,
                START_Z + 0.06,
                WIDTH - 0.36f,
                HEIGHT - 0.36f,
                0.08f,
                BACKGROUND_MATERIAL
        );

        createBlock(
                world,
                START_X + 0.24,
                START_Y + 0.24,
                START_Z - 0.01,
                WIDTH - 0.48f,
                HEIGHT - 0.48f,
                0.025f,
                GLASS_MATERIAL
        );
    }

    private void createOuterFrame(World world) {

        createBlock(
                world,
                START_X,
                START_Y,
                START_Z,
                WIDTH,
                OUTER_FRAME,
                DEPTH,
                FRAME_MATERIAL
        );

        createBlock(
                world,
                START_X,
                START_Y + HEIGHT - OUTER_FRAME,
                START_Z,
                WIDTH,
                OUTER_FRAME,
                DEPTH,
                FRAME_MATERIAL
        );

        createBlock(
                world,
                START_X,
                START_Y,
                START_Z,
                OUTER_FRAME,
                HEIGHT,
                DEPTH,
                FRAME_MATERIAL
        );

        createBlock(
                world,
                START_X + WIDTH - OUTER_FRAME,
                START_Y,
                START_Z,
                OUTER_FRAME,
                HEIGHT,
                DEPTH,
                FRAME_MATERIAL
        );
    }

    private void createInnerFrame(World world) {

        createBlock(
                world,
                START_X + 0.24,
                START_Y + 0.27,
                START_Z - 0.035,
                WIDTH - 0.48f,
                INNER_FRAME,
                0.035f,
                INNER_FRAME_MATERIAL
        );

        createBlock(
                world,
                START_X + 0.24,
                START_Y + HEIGHT - 0.33,
                START_Z - 0.035,
                WIDTH - 0.48f,
                INNER_FRAME,
                0.035f,
                INNER_FRAME_MATERIAL
        );
    }

    private void createAccentElements(World world) {

        createBlock(
                world,
                START_X + 0.36,
                START_Y + 0.42,
                START_Z - 0.06,
                WIDTH - 0.72f,
                0.035f,
                0.035f,
                ACCENT_MATERIAL
        );

        createBlock(
                world,
                START_X + 0.36,
                START_Y + HEIGHT - 0.49,
                START_Z - 0.06,
                2.4f,
                0.035f,
                0.035f,
                ACCENT_MATERIAL
        );

        createBlock(
                world,
                START_X + 0.34,
                START_Y + 0.60,
                START_Z - 0.06,
                0.04f,
                HEIGHT - 1.20f,
                0.035f,
                ACCENT_MATERIAL
        );

        createBlock(
                world,
                START_X + WIDTH - 0.38,
                START_Y + 0.60,
                START_Z - 0.06,
                0.04f,
                1.30f,
                0.035f,
                ACCENT_MATERIAL
        );
    }

    private void createTexts(World world) {

        Component welcomeTop =
                Component.text(
                        "WILLKOMMEN ZU",
                        TextColor.color(0x20D5C2)
                ).decorate(TextDecoration.BOLD);

        createText(
                world,
                TEXT_CENTER_X + TEXT_CENTER_OFFSET_X,
                START_Y + 5.15,
                START_Z + TEXT_Z_OFFSET,
                0.70f,
                welcomeTop,
                800
        );


        Component title =
                gradientText(
                        "BASTIGHG'S CHALLENGES",
                        0x00BFAF,
                        0xB8FF32
                );

        titleDisplay =
                createText(
                        world,
                        TEXT_CENTER_X + TEXT_CENTER_OFFSET_X,
                        START_Y + 4.20,
                        START_Z + TEXT_Z_OFFSET,
                        1.48f,
                        title,
                        1000
                );

        titleDisplay.addScoreboardTag(
                TITLE_TAG
        );

        Component fanProject =
                Component.text(
                        "FAN PROJECT",
                        TextColor.color(0x67F5E8)
                ).decorate(TextDecoration.BOLD);

        createText(
                world,
                TEXT_CENTER_X + TEXT_CENTER_OFFSET_X,
                START_Y + 3.35,
                START_Z + TEXT_Z_OFFSET,
                1.02f,
                fanProject,
                700
        );

        gameLabelDisplay =
                createText(
                        world,
                        TEXT_CENTER_X + 2.85,
                        START_Y + 1.95,
                        START_Z + TEXT_Z_OFFSET,
                        1.02f,
                        Component.text(
                                "GAME //",
                                TextColor.color(0x20D5C2)
                        ).decorate(
                                TextDecoration.BOLD
                        ),
                        500
                );

        gameLabelDisplay.addScoreboardTag(
                GAME_LABEL_TAG
        );

        gameNameDisplay =
                createText(
                        world,
                        TEXT_CENTER_X + 1.15,
                        START_Y + 1.95,
                        START_Z + TEXT_Z_OFFSET,
                        1.20f,
                        Component.text(
                                "NONE",
                                TextColor.color(0xB8FF32)
                        ).decorate(
                                TextDecoration.BOLD
                        ),
                        900
                );

        gameNameDisplay.addScoreboardTag(
                GAME_NAME_TAG
        );
    }

    private void createLoadConsole(World world) {

        loadConsoleDisplay =
                createText(
                        world,

                        // mittig unter GAME
                        TEXT_CENTER_X,
                        START_Y + 0.90,
                        START_Z + TEXT_Z_OFFSET,

                        0.58f,

                        Component.text(
                                "> WARTET AUF LOAD",
                                TextColor.color(0x777777)
                        ),

                        1000
                );

        loadConsoleDisplay.addScoreboardTag(
                LOAD_CONSOLE_TAG
        );
    }

    public void setLoadStatus(
            String text,
            int color
    ) {

        if (loadConsoleDisplay == null
                || !loadConsoleDisplay.isValid()) {

            World world =
                    plugin.getServer()
                            .getWorld(
                                    "bastighg_challenges_lobby"
                            );

            if (world != null) {
                loadConsoleDisplay =
                        findTextDisplay(
                                world,
                                LOAD_CONSOLE_TAG
                        );
            }
        }

        if (loadConsoleDisplay == null) {
            return;
        }

        loadConsoleDisplay.text(
                Component.text(
                        "> " + text,
                        TextColor.color(color)
                )
        );
    }

    public void setLoadReady(
            String gameName
    ) {

        if (loadConsoleDisplay == null) {
            return;
        }

        loadConsoleDisplay.text(
                Component.empty()
                        .append(
                                Component.text(
                                        "● ",
                                        TextColor.color(0xB8FF32)
                                )
                        )
                        .append(
                                Component.text(
                                        gameName + " READY",
                                        TextColor.color(0xB8FF32)
                                ).decorate(
                                        TextDecoration.BOLD
                                )
                        )
        );
    }

    private TextDisplay createText(
            World world,
            double x,
            double y,
            double z,
            float scale,
            Component text,
            int lineWidth
    ) {

        Location location =
                new Location(
                        world,
                        x,
                        y,
                        z,
                        180.0f,
                        0.0f
                );

        TextDisplay display =
                world.spawn(
                        location,
                        TextDisplay.class
                );

        display.addScoreboardTag(
                DISPLAY_TAG
        );

        display.setBillboard(
                Display.Billboard.FIXED
        );

        display.setAlignment(
                TextDisplay.TextAlignment.CENTER
        );

        display.setLineWidth(lineWidth);

        display.setDefaultBackground(false);

        display.setBackgroundColor(
                Color.fromARGB(
                        0,
                        0,
                        0,
                        0
                )
        );

        display.setShadowed(true);

        display.setSeeThrough(false);

        display.setBrightness(
                new Display.Brightness(
                        15,
                        15
                )
        );

        display.text(text);

        display.setTransformation(
                new Transformation(
                        new Vector3f(),
                        new Quaternionf(),
                        new Vector3f(
                                scale,
                                scale,
                                scale
                        ),
                        new Quaternionf()
                )
        );

        return display;
    }

    private void createBlock(
            World world,
            double x,
            double y,
            double z,
            float scaleX,
            float scaleY,
            float scaleZ,
            Material material
    ) {

        BlockDisplay display =
                world.spawn(
                        new Location(
                                world,
                                x,
                                y,
                                z
                        ),
                        BlockDisplay.class
                );

        display.addScoreboardTag(
                DISPLAY_TAG
        );

        display.setBlock(
                material.createBlockData()
        );

        display.setBrightness(
                new Display.Brightness(
                        15,
                        15
                )
        );

        display.setTransformation(
                new Transformation(
                        new Vector3f(),
                        new Quaternionf(),
                        new Vector3f(
                                scaleX,
                                scaleY,
                                scaleZ
                        ),
                        new Quaternionf()
                )
        );
    }

    private Component gradientText(
            String text,
            int startColor,
            int endColor
    ) {

        Component result =
                Component.empty();

        int length =
                Math.max(
                        1,
                        text.length() - 1
                );

        int startR = (startColor >> 16) & 0xFF;
        int startG = (startColor >> 8) & 0xFF;
        int startB = startColor & 0xFF;

        int endR = (endColor >> 16) & 0xFF;
        int endG = (endColor >> 8) & 0xFF;
        int endB = endColor & 0xFF;

        for (int i = 0; i < text.length(); i++) {

            double factor =
                    (double) i / length;

            int red =
                    (int) Math.round(
                            startR
                                    + (endR - startR)
                                    * factor
                    );

            int green =
                    (int) Math.round(
                            startG
                                    + (endG - startG)
                                    * factor
                    );

            int blue =
                    (int) Math.round(
                            startB
                                    + (endB - startB)
                                    * factor
                    );

            result = result.append(
                    Component.text(
                            String.valueOf(
                                    text.charAt(i)
                            ),
                            TextColor.color(
                                    red,
                                    green,
                                    blue
                            )
                    ).decorate(
                            TextDecoration.BOLD
                    )
            );
        }

        return result;
    }

    public void setSelectedGame(String gameName) {

        plugin.getLogger().info(
                "Display Game setzen auf: " + gameName
        );

        if (gameNameDisplay == null) {
            plugin.getLogger().warning(
                    "Game-Name-Display ist NULL."
            );
            return;
        }

        Component gameText;

        if (gameName.equalsIgnoreCase("MobArmyWars")) {
            gameText = gradientText(
                    "  " + gameName.toUpperCase(),
                    0xFF3333,
                    0x3366FF
            );
        } else {
            gameText = Component.text(
                    "  " + gameName.toUpperCase(),
                    TextColor.color(0xFF4FD8)
            ).decorate(TextDecoration.BOLD);
        }

        gameNameDisplay.text(gameText);
    }

    private void protectDisplayEntities(
            World world
    ) {

        Location center =
                new Location(
                        world,
                        START_X + WIDTH / 2.0,
                        START_Y + HEIGHT / 2.0,
                        START_Z
                );

        for (Entity entity :
                world.getNearbyEntities(
                        center,
                        15,
                        10,
                        5
                )) {

            if (entity instanceof Display
                    || entity instanceof Interaction) {

                entity.addScoreboardTag(
                        PROTECTED_TAG
                );
            }
        }
    }

    public void removeAllDisplayEntities() {
        for (World world : plugin.getServer().getWorlds()) {
            for (Entity entity : world.getEntities()) {

                boolean lobbyDisplay =
                        entity.getScoreboardTags().contains(DISPLAY_TAG)
                                || entity.getScoreboardTags().contains(TITLE_TAG)
                                || entity.getScoreboardTags().contains(GAME_LABEL_TAG)
                                || entity.getScoreboardTags().contains(GAME_NAME_TAG)
                                || entity.getScoreboardTags().contains(LOAD_CONSOLE_TAG)
                                || entity.getScoreboardTags().contains(INTERACTION_TAG);

                boolean lobbyButton =
                        entity.getScoreboardTags().contains("challenge_lobby_button_visual")
                                || entity.getScoreboardTags().stream()
                                .anyMatch(tag -> tag.startsWith("challenge_lobby_button_"));

                if (lobbyDisplay || lobbyButton) {
                    entity.remove();
                }
            }
        }

        titleDisplay = null;
        gameLabelDisplay = null;
        gameNameDisplay = null;
        loadConsoleDisplay = null;
    }
}