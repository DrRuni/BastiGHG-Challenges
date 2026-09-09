package runi.myddns.challenges.core.world;

import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class LobbyWorldManager {

    private static final String WORLD_NAME = "BastiGHG_Challenges_Lobby";

    private static final NamespacedKey WORLD_KEY = NamespacedKey.minecraft("bastighg_challenges_lobby");

    private static final String ZIP_RESOURCE = "core/worlds/BastiGHG_Challenges_Lobby.zip";

    private final JavaPlugin plugin;

    private World lobbyWorld;

    public LobbyWorldManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadWorld() {

        World loadedWorld = Bukkit.getWorld(WORLD_KEY);

        if (loadedWorld == null) {
            loadedWorld = Bukkit.getWorld(WORLD_NAME);
        }

        if (loadedWorld != null) {

            plugin.getLogger().info(
                    "Lobby-Welt ist bereits geladen."
            );

            lobbyWorld = loadedWorld;

            configureWorld();
            setSpawn();

            return;
        }

        Path serverRoot =
                Bukkit.getWorldContainer()
                        .toPath()
                        .toAbsolutePath()
                        .normalize();

        Path legacyWorldFolder =
                serverRoot.resolve(WORLD_NAME);

        Path migratedWorldFolder =
                serverRoot
                        .resolve("world")
                        .resolve("dimensions")
                        .resolve("minecraft")
                        .resolve("bastighg_challenges_lobby");

        if (Files.isDirectory(migratedWorldFolder)) {

            plugin.getLogger().info(
                    "Vorhandene Paper-26.2-Lobby-Welt gefunden."
            );

            loadMigratedWorld();

            return;
        }

        if (Files.isDirectory(legacyWorldFolder)) {

            plugin.getLogger().info(
                    "Vorhandene Legacy-Lobby-Welt gefunden. "
                            + "Paper darf sie jetzt migrieren."
            );

            loadLegacyWorld();

            return;
        }

        plugin.getLogger().info(
                "Keine Lobby-Welt vorhanden."
        );

        plugin.getLogger().info(
                "Entpacke " + ZIP_RESOURCE + "..."
        );

        try {

            extractWorldZip(legacyWorldFolder);

        } catch (IOException exception) {

            plugin.getLogger().log(
                    Level.SEVERE,
                    "Lobby-Welt konnte nicht entpackt werden.",
                    exception
            );

            return;
        }

        loadLegacyWorld();
    }

    private void loadMigratedWorld() {

        WorldCreator creator =
                WorldCreator.ofKey(WORLD_KEY);

        creator.environment(
                World.Environment.NORMAL
        );

        creator.generator(
                new LobbyWorldGenerator()
        );

        lobbyWorld =
                creator.createWorld();

        finishWorldLoading();
    }

    private void loadLegacyWorld() {

        WorldCreator creator =
                new WorldCreator(WORLD_NAME);

        creator.environment(
                World.Environment.NORMAL
        );

        creator.generator(
                new LobbyWorldGenerator()
        );

        lobbyWorld =
                creator.createWorld();

        finishWorldLoading();
    }

    private void finishWorldLoading() {

        if (lobbyWorld == null) {

            plugin.getLogger().severe(
                    "Lobby-Welt konnte nicht geladen werden."
            );

            return;
        }

        configureWorld();
        setSpawn();

        plugin.getLogger().info(
                "Lobby-Welt erfolgreich geladen: "
                        + lobbyWorld.getKey()
        );
    }

    private void configureWorld() {

        if (lobbyWorld == null) {
            return;
        }

        lobbyWorld.setDifficulty(
                Difficulty.PEACEFUL
        );

        lobbyWorld.setGameRule(
                GameRules.SPAWN_MOBS,
                false
        );

        lobbyWorld.setGameRule(
                GameRules.SPAWN_PATROLS,
                false
        );

        lobbyWorld.setGameRule(
                GameRules.SPAWN_WANDERING_TRADERS,
                false
        );

        lobbyWorld.setGameRule(
                GameRules.SPAWN_PHANTOMS,
                false
        );

        lobbyWorld.setTime(6000);

        lobbyWorld.setStorm(false);
    }

    private void setSpawn() {

        if (lobbyWorld == null) {
            return;
        }

        lobbyWorld.setSpawnLocation(
                new Location(
                        lobbyWorld,
                        0.5,
                        67.0,
                        4.5,
                        0.0f,
                        0.0f
                )
        );
    }

    public Location getSpawn() {

        if (lobbyWorld == null) {
            throw new IllegalStateException(
                    "Lobby-Welt wurde noch nicht geladen."
            );
        }

        return new Location(
                lobbyWorld,
                0.5,
                67.0,
                4.5,
                0.0f,
                0.0f
        );
    }

    public World getLobbyWorld() {
        return lobbyWorld;
    }

    private void extractWorldZip(
            Path targetDirectory
    ) throws IOException {

        InputStream resourceStream =
                plugin.getResource(
                        ZIP_RESOURCE
                );

        if (resourceStream == null) {

            throw new FileNotFoundException(
                    "ZIP nicht in der Plugin-JAR gefunden: "
                            + ZIP_RESOURCE
            );
        }

        Path normalizedTarget =
                targetDirectory
                        .toAbsolutePath()
                        .normalize();

        Files.createDirectories(
                normalizedTarget
        );

        try (
                resourceStream;
                ZipInputStream zipInputStream =
                        new ZipInputStream(
                                resourceStream
                        )
        ) {

            ZipEntry entry;

            while (
                    (entry =
                            zipInputStream.getNextEntry())
                            != null
            ) {

                Path target =
                        normalizedTarget
                                .resolve(
                                        entry.getName()
                                )
                                .normalize();

                if (!target.startsWith(
                        normalizedTarget
                )) {

                    throw new IOException(
                            "Ungültiger ZIP-Eintrag: "
                                    + entry.getName()
                    );
                }

                if (entry.isDirectory()) {

                    Files.createDirectories(
                            target
                    );

                } else {

                    Path parent =
                            target.getParent();

                    if (parent != null) {

                        Files.createDirectories(
                                parent
                        );
                    }

                    Files.copy(
                            zipInputStream,
                            target,
                            StandardCopyOption.REPLACE_EXISTING
                    );
                }

                zipInputStream.closeEntry();
            }
        }

        plugin.getLogger().info("Lobby-ZIP erfolgreich entpackt.");
    }
}