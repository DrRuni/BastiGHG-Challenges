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
import runi.myddns.challenges.ChallengeMain;
import org.bukkit.entity.Interaction;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static runi.myddns.challenges.core.utils.ColorUtil.gradientText;
import static runi.myddns.challenges.core.utils.DisplayColor.*;

public class LobbyDisplayManager {

    private static final String DISPLAY_TAG = "challenge_lobby_showcase";
    private static final String TITLE_TAG = "challenge_lobby_title";
    private static final String GAME_LABEL_TAG = "challenge_lobby_game_label";
    private static final String GAME_NAME_TAG = "challenge_lobby_game_name";
    private static final String INTERACTION_TAG = "challenge_lobby_display_interaction";
    private static final String PROTECTED_TAG = "challenge_protected";
    private static final String LOAD_CONSOLE_TAG = "challenge_lobby_load_console";
    private static final String LOAD_TITLE_TAG = "challenge_lobby_load_title";

    private static final double START_X = 1.0;
    private static final double START_Y = 67.0;
    private static final double START_Z = 48.0;

    private static final float WIDTH = 11.5f;
    private static final float HEIGHT = 6.0f;

    private static final double TEXT_CENTER_X = START_X + WIDTH / 2.0;

    private static final float OUTER_FRAME = 0.16f;
    private static final float INNER_FRAME = 0.06f;
    private static final float DEPTH = 0.10f;

    private static final float TEXT_Z_OFFSET = -0.08f;

    private static final Material FRAME_MATERIAL = Material.DARK_PRISMARINE;
    private static final Material INNER_FRAME_MATERIAL = Material.WARPED_PLANKS;
    private static final Material ACCENT_MATERIAL = Material.LIME_CONCRETE;
    private static final Material BACKGROUND_MATERIAL = Material.BLACK_CONCRETE;
    private static final Material GLASS_MATERIAL = Material.TINTED_GLASS;

    private final ChallengeMain plugin;
    private final java.util.ArrayDeque<String> loadConsoleLines = new java.util.ArrayDeque<>();
    private TextDisplay titleDisplay;
    private TextDisplay gameLabelDisplay;
    private TextDisplay gameNameDisplay;
    private TextDisplay loadConsoleDisplay;
    private TextDisplay loadTitleDisplay;

    public LobbyDisplayManager(ChallengeMain plugin) {
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

        loadTitleDisplay = findTextDisplay(world, LOAD_TITLE_TAG);

        if (titleDisplay != null
                && gameLabelDisplay != null
                && gameNameDisplay != null) {

            plugin.getLogger().info(
                    "Vorhandenes Lobby-Display übernommen."
            );

            if (loadTitleDisplay == null) {
                createLoadTitle(world);
            }

            if (loadConsoleDisplay == null) {
                createLoadConsole(world);
            }

            protectDisplayEntities(world);

            return;
        }

        removeBrokenDisplay(world);

        createBackground(world);
        createOuterFrame(world);
        createInnerFrame(world);
        createAccentElements(world);
        createTexts(world);
        createLoadTitle(world);
        createLoadConsole(world);

        protectDisplayEntities(world);
    }

    private void createLoadTitle(World world) {
        loadTitleDisplay = createText(
                world,
                TEXT_CENTER_X,
                START_Y + 1.85,
                START_Z + TEXT_Z_OFFSET,
                0.72f,
                Component.empty(),
                1000
        );

        loadTitleDisplay.addScoreboardTag(LOAD_TITLE_TAG);
    }

    public void setLoadUnloaded(String gameName) {
        setLoadStatus(
                plugin.getLanguageManager().get(
                        "lobby-display.unloaded",
                        "game",
                        gameName
                ),
                0xA61B1B
        );
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

        Component title = Component.text("\uE030");

        titleDisplay = createText(
                world,
                TEXT_CENTER_X + 0.0,
                START_Y + 4.20,
                START_Z + TEXT_Z_OFFSET,
                2.00f,
                title,
                1000
        );

        titleDisplay.addScoreboardTag(
                TITLE_TAG
        );

        gameLabelDisplay =
                createText(
                        world,
                        TEXT_CENTER_X + 1.55,
                        START_Y + 2.85,
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
                        TEXT_CENTER_X - 0.65,
                        START_Y + 2.85,
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
        loadConsoleDisplay = createText(
                world,
                TEXT_CENTER_X,
                START_Y + 1.15,
                START_Z + TEXT_Z_OFFSET,
                0.58f,
                Component.empty(),
                1000
        );

        loadConsoleDisplay.addScoreboardTag(LOAD_CONSOLE_TAG);
    }

    public void addLoadConsoleLine(String text) {
        loadConsoleLines.addLast(text);

        while (loadConsoleLines.size() > 3) {
            loadConsoleLines.removeFirst();
        }

        updateLoadConsole();
    }

    private void updateLoadConsole() {
        if (loadConsoleDisplay == null || !loadConsoleDisplay.isValid()) {
            World world = plugin.getLobbyWorldManager().getLobbyWorld();
            if (world == null) return;

            loadConsoleDisplay = findTextDisplay(world, LOAD_CONSOLE_TAG);

            if (loadConsoleDisplay == null || !loadConsoleDisplay.isValid()) {
                createLoadConsole(world);
                return;
            }
        }

        Component console = Component.empty();
        int index = 0;

        for (String line : loadConsoleLines) {
            if (index > 0) console = console.append(Component.newline());

            if (!line.isEmpty()) {
                console = console.append(
                        Component.text(
                                "> " + line,
                                TextColor.color(LIGHT_GREY)
                        )
                );
            }

            index++;
        }

        loadConsoleDisplay.text(console);
    }

    public void clearLoadConsole() {
        loadConsoleLines.clear();

        if (loadConsoleDisplay == null || !loadConsoleDisplay.isValid()) {
            World world = plugin.getLobbyWorldManager().getLobbyWorld();
            if (world == null) return;

            loadConsoleDisplay = findTextDisplay(world, LOAD_CONSOLE_TAG);
        }

        if (loadConsoleDisplay != null && loadConsoleDisplay.isValid()) {
            loadConsoleDisplay.text(Component.empty());
        }
    }

    public void setLoadStatus(String text, int color) {
        if (loadTitleDisplay == null || !loadTitleDisplay.isValid()) {
            World world = plugin.getLobbyWorldManager().getLobbyWorld();

            if (world == null) return;

            loadTitleDisplay = findTextDisplay(world, LOAD_TITLE_TAG);

            if (loadTitleDisplay == null) {
                createLoadTitle(world);

                plugin.getServer().getScheduler().runTaskLater(
                        plugin,
                        () -> setLoadStatus(text, color),
                        1L
                );

                return;
            }
        }

        loadTitleDisplay.text(
                Component.text(
                        text,
                        TextColor.color(color)
                ).decorate(TextDecoration.BOLD)
        );
    }

    public void setLoadReady(String gameName) {
        if (loadTitleDisplay == null || !loadTitleDisplay.isValid()) {
            World world = plugin.getLobbyWorldManager().getLobbyWorld();

            if (world != null) {
                loadTitleDisplay = findTextDisplay(world, LOAD_TITLE_TAG);
            }
        }

        if (loadTitleDisplay != null) {
            loadTitleDisplay.text(
                    Component.text(
                            gameName + " ist geladen",
                            TextColor.color(0x55CC44)
                    ).decorate(TextDecoration.BOLD)
            );
        }

        scrollLoadConsoleOut();
    }

    private void scrollLoadConsoleOut() {
        for (int i = 1; i <= 3; i++) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!loadConsoleLines.isEmpty()) loadConsoleLines.removeFirst();
                loadConsoleLines.addLast("");
                updateLoadConsole();
            }, i * 8L);
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, this::clearLoadConsole, 32L);
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

    public void refreshLanguage() {
        setLoadStatus(
                plugin.getLanguageManager().get("lobby-display.waiting-for-load"),
                0x777777
        );
    }

    public void setSelectedGame(String gameName) {

        plugin.getLogger().info("Display Game setzen auf: " + gameName);

        if (gameNameDisplay == null || !gameNameDisplay.isValid()) {
            World world = plugin.getLobbyWorldManager().getLobbyWorld();
            if (world == null) return;

            gameNameDisplay = findTextDisplay(world, GAME_NAME_TAG);

            if (gameNameDisplay == null || !gameNameDisplay.isValid()) {
                plugin.getLogger().warning("Game-Name-Display wurde nicht gefunden.");
                return;
            }
        }

        Component gameText;

        if (gameName.equalsIgnoreCase("MobArmyBattle")) {
            gameText = gradientText(
                    " " + gameName.toUpperCase(),
                    BRIGHT_RED,
                    BLUE
            );
        } else if (gameName.equalsIgnoreCase("LevelBorder")) {
            gameText = gradientText(
                    gameName.toUpperCase(),
                    LIGHT_BLUE,
                    DEEP_BLUE
            );
        } else if (gameName.equalsIgnoreCase("LevelBlock")) {
            gameText = gradientText(
                    gameName.toUpperCase(),
                    LIME,
                    DARK_GREEN
            );
        } else {
            gameText = gradientText(
                    gameName.toUpperCase(),
                    LIME,
                    DARK_GREEN
            );
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
                                || entity.getScoreboardTags().contains(INTERACTION_TAG)
                                || entity.getScoreboardTags().contains(LOAD_TITLE_TAG);

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
        loadTitleDisplay = null;
    }
}