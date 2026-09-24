package runi.myddns.challenges.games.LevelBlock.Manager;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import runi.myddns.challenges.games.LevelBlock.LevelBlockGame;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class LevelBlockDataManager {

    private final LevelBlockGame game;

    private final File dataFile;
    private FileConfiguration config;
    private boolean eventStarted;

    private final Map<String, Location> startLocations =
            new HashMap<>();

    private final Map<String, Set<LevelBlockPos>> unlockedBlocks =
            new HashMap<>();


    public LevelBlockDataManager(
            LevelBlockGame game
    ) {

        this.game = game;

        this.dataFile =
                new File(
                        game.getPlugin().getDataFolder(),
                        "games/levelblock/gamedata.yml"
                );

        load();
    }

    public void load() {

        startLocations.clear();
        unlockedBlocks.clear();

        config = YamlConfiguration.loadConfiguration(dataFile);
        eventStarted = config.getBoolean("event-started", false);

        loadWorld(
                "world_levelblock"
        );

        loadWorld(
                "world_levelblock_nether"
        );

        loadWorld(
                "world_levelblock_the_end"
        );
    }

    private void loadWorld(
            String worldName
    ) {

        String path =
                "worlds."
                        + worldName;

        String startPath =
                path + ".start";


        if (
                config.isInt(
                        startPath + ".x"
                )
                        && config.isInt(
                        startPath + ".y"
                )
                        && config.isInt(
                        startPath + ".z"
                )
        ) {

            World world =
                    game.getPlugin()
                            .getServer()
                            .getWorld(
                                    worldName
                            );


            if (world != null) {

                Location location =
                        new Location(
                                world,
                                config.getInt(
                                        startPath + ".x"
                                ),
                                config.getInt(
                                        startPath + ".y"
                                ),
                                config.getInt(
                                        startPath + ".z"
                                )
                        );


                startLocations.put(
                        worldName,
                        location
                );
            }
        }

        Set<LevelBlockPos> blocks =
                new HashSet<>();

        List<String> savedBlocks =
                config.getStringList(
                        path + ".unlocked"
                );

        for (String entry : savedBlocks) {

            if (entry == null
                    || entry.isBlank()) {

                continue;
            }


            String[] split =
                    entry.split(
                            ":"
                    );


            if (split.length != 2) {
                continue;
            }


            try {

                int x =
                        Integer.parseInt(
                                split[0]
                        );

                int z =
                        Integer.parseInt(
                                split[1]
                        );


                blocks.add(
                        new LevelBlockPos(
                                x,
                                z
                        )
                );


            } catch (NumberFormatException ignored) {}
        }


        unlockedBlocks.put(
                worldName,
                blocks
        );
    }

    public boolean isEventStarted() {
        return eventStarted;
    }

    public void setEventStarted(boolean eventStarted) {
        this.eventStarted = eventStarted;

        config.set(
                "event-started",
                eventStarted
        );

        saveFile();
    }

    public void saveStartLocation(
            World world,
            Location location
    ) {

        if (world == null
                || location == null) {

            return;
        }


        String worldName =
                world.getName();


        String path =
                "worlds."
                        + worldName
                        + ".start";


        config.set(
                path + ".x",
                location.getBlockX()
        );

        config.set(
                path + ".y",
                location.getBlockY()
        );

        config.set(
                path + ".z",
                location.getBlockZ()
        );


        startLocations.put(
                worldName,
                location.clone()
        );


        saveFile();
    }

    public void saveUnlockedBlock(
            World world,
            int x,
            int z
    ) {

        if (world == null) {
            return;
        }


        String worldName =
                world.getName();


        Set<LevelBlockPos> blocks =
                unlockedBlocks.computeIfAbsent(
                        worldName,
                        ignored -> new HashSet<>()
                );


        LevelBlockPos pos =
                new LevelBlockPos(
                        x,
                        z
                );

        if (!blocks.add(pos)) {
            return;
        }


        String path =
                "worlds."
                        + worldName
                        + ".unlocked";


        List<String> savedBlocks =
                new ArrayList<>(
                        config.getStringList(
                                path
                        )
                );


        String entry =
                x + ":" + z;


        if (!savedBlocks.contains(entry)) {

            savedBlocks.add(
                    entry
            );

            config.set(
                    path,
                    savedBlocks
            );


            saveFile();
        }
    }

    public Location getStartLocation(
            World world
    ) {

        if (world == null) {
            return null;
        }


        Location location =
                startLocations.get(
                        world.getName()
                );


        if (location == null) {
            return null;
        }


        return location.clone();
    }


    public Set<LevelBlockPos> getUnlockedBlocks(
            World world
    ) {

        if (world == null) {

            return Collections.emptySet();
        }


        Set<LevelBlockPos> blocks =
                unlockedBlocks.get(
                        world.getName()
                );


        if (blocks == null) {

            return Collections.emptySet();
        }


        return Collections.unmodifiableSet(
                blocks
        );
    }


    public boolean isUnlocked(
            World world,
            int x,
            int z
    ) {

        if (world == null) {
            return false;
        }


        Set<LevelBlockPos> blocks =
                unlockedBlocks.get(
                        world.getName()
                );


        if (blocks == null) {
            return false;
        }


        return blocks.contains(
                new LevelBlockPos(
                        x,
                        z
                )
        );
    }

    public void resetWorld(
            World world
    ) {

        if (world == null) {
            return;
        }


        String worldName =
                world.getName();


        startLocations.remove(
                worldName
        );

        unlockedBlocks.remove(
                worldName
        );


        String path =
                "worlds."
                        + worldName;


        config.set(
                path + ".start.x",
                null
        );

        config.set(
                path + ".start.y",
                null
        );

        config.set(
                path + ".start.z",
                null
        );

        config.set(
                path + ".unlocked",
                new ArrayList<>()
        );

        saveFile();
    }

    private void saveFile() {

        try {

            config.save(
                    dataFile
            );

        } catch (IOException e) {

            game.getPlugin()
                    .getLogger()
                    .severe(
                            "gamedata.yml konnte nicht gespeichert werden: "
                                    + e.getMessage()
                    );
        }
    }
}