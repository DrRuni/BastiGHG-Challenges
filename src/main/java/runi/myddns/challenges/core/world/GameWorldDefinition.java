package runi.myddns.challenges.core.world;

import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import runi.myddns.challenges.core.world.generator.LobbyWorldGenerator;

import java.util.List;
import java.util.function.Supplier;

public record GameWorldDefinition(
        String gameId,
        String worldId,
        String worldName,
        World.Environment environment,
        SourceType sourceType,
        String zipFile,
        Supplier<ChunkGenerator> generator
) {

    private static final String WORLD_ZIP = "core/worlds/BastiGHG_Challenges_Worlds.zip";
    public enum SourceType {
        GENERATED,
        ZIP
    }

    // ============================================================
    // CHALLENGES LOBBY
    // ============================================================

    public static final GameWorldDefinition LOBBY = new GameWorldDefinition(
            "core",
            "lobby",
            "world_challenges_lobby",
            World.Environment.NORMAL,
            SourceType.ZIP,
            WORLD_ZIP,
            LobbyWorldGenerator::new
    );

    // ============================================================
    // MOB ARMY BATTLE
    // ============================================================

    public static final GameWorldDefinition MOB_ARMY_LOBBY = new GameWorldDefinition(
            "mobarmybattle",
            "lobby",
            "world_mobarmy_lobby",
            World.Environment.NORMAL,
            SourceType.ZIP,
            WORLD_ZIP,
            null
    );

    public static final GameWorldDefinition MOB_ARMY_ARENA = new GameWorldDefinition(
            "mobarmybattle",
            "arena",
            "world_mobarmy_arena",
            World.Environment.NORMAL,
            SourceType.ZIP,
            WORLD_ZIP,
            null
    );

    public static final GameWorldDefinition MOB_ARMY_ROT = new GameWorldDefinition(
            "mobarmybattle",
            "rot",
            "world_rot",
            World.Environment.NORMAL,
            SourceType.GENERATED,
            null,
            null
    );

    public static final GameWorldDefinition MOB_ARMY_ROT_NETHER = new GameWorldDefinition(
            "mobarmybattle",
            "rot_nether",
            "world_rot_nether",
            World.Environment.NETHER,
            SourceType.GENERATED,
            null,
            null
    );

    public static final GameWorldDefinition MOB_ARMY_BLAU = new GameWorldDefinition(
            "mobarmybattle",
            "blau",
            "world_blau",
            World.Environment.NORMAL,
            SourceType.GENERATED,
            null,
            null
    );

    public static final GameWorldDefinition MOB_ARMY_BLAU_NETHER = new GameWorldDefinition(
            "mobarmybattle",
            "blau_nether",
            "world_blau_nether",
            World.Environment.NETHER,
            SourceType.GENERATED,
            null,
            null
    );

    // ============================================================
    // LEVELBORDER
    // ============================================================

    public static final GameWorldDefinition LEVEL_BORDER_OVERWORLD = new GameWorldDefinition(
            "levelborder",
            "overworld",
            "world_levelborder",
            World.Environment.NORMAL,
            SourceType.GENERATED,
            null,
            null
    );

    public static final GameWorldDefinition LEVEL_BORDER_NETHER = new GameWorldDefinition(
            "levelborder",
            "nether",
            "world_levelborder_nether",
            World.Environment.NETHER,
            SourceType.GENERATED,
            null,
            null
    );

    public static final GameWorldDefinition LEVEL_BORDER_END = new GameWorldDefinition(
            "levelborder",
            "end",
            "world_levelborder_the_end",
            World.Environment.THE_END,
            SourceType.GENERATED,
            null,
            null
    );

    // ============================================================
    // LEVELBLOCK
    // ============================================================

    public static final GameWorldDefinition LEVEL_BLOCK_OVERWORLD = new GameWorldDefinition(
            "levelblock",
            "overworld",
            "world_levelblock",
            World.Environment.NORMAL,
            SourceType.GENERATED,
            null,
            null
    );

    public static final GameWorldDefinition LEVEL_BLOCK_NETHER = new GameWorldDefinition(
            "levelblock",
            "nether",
            "world_levelblock_nether",
            World.Environment.NETHER,
            SourceType.GENERATED,
            null,
            null
    );

    public static final GameWorldDefinition LEVEL_BLOCK_END = new GameWorldDefinition(
            "levelblock",
            "end",
            "world_levelblock_the_end",
            World.Environment.THE_END,
            SourceType.GENERATED,
            null,
            null
    );

    public static final List<GameWorldDefinition> ALL = List.of(
            LOBBY,

            LEVEL_BORDER_OVERWORLD,
            LEVEL_BORDER_NETHER,
            LEVEL_BORDER_END,

            LEVEL_BLOCK_OVERWORLD,
            LEVEL_BLOCK_NETHER,
            LEVEL_BLOCK_END,

            MOB_ARMY_LOBBY,
            MOB_ARMY_ARENA,
            MOB_ARMY_ROT,
            MOB_ARMY_ROT_NETHER,
            MOB_ARMY_BLAU,
            MOB_ARMY_BLAU_NETHER
    );

    public boolean isGenerated() {
        return sourceType == SourceType.GENERATED;
    }

    public boolean isZip() {
        return sourceType == SourceType.ZIP;
    }
}