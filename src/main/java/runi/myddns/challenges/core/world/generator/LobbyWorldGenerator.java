package runi.myddns.challenges.core.world.generator;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.util.noise.SimplexNoiseGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class LobbyWorldGenerator extends ChunkGenerator {

    private static final int WATER_LEVEL = 62;

    private static final int MIN_DEPTH = 4;
    private static final int MAX_DEPTH = 12;
    private static final int SAND_LAYERS = 3;

    private static final Biome LOBBY_BIOME = Biome.WARM_OCEAN;

    private static final Material[] CORAL_BLOCKS = {
            Material.TUBE_CORAL_BLOCK,
            Material.BRAIN_CORAL_BLOCK,
            Material.BUBBLE_CORAL_BLOCK,
            Material.FIRE_CORAL_BLOCK,
            Material.HORN_CORAL_BLOCK
    };

    private static final Material[] CORALS = {
            Material.TUBE_CORAL,
            Material.BRAIN_CORAL,
            Material.BUBBLE_CORAL,
            Material.FIRE_CORAL,
            Material.HORN_CORAL
    };

    @Override
    public void generateNoise(
            @NotNull WorldInfo worldInfo,
            @NotNull Random random,
            int chunkX,
            int chunkZ,
            @NotNull ChunkData chunkData
    ) {

        int minY = chunkData.getMinHeight();

        SimplexNoiseGenerator noise =
                new SimplexNoiseGenerator(worldInfo.getSeed());

        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {

                int worldX = chunkX * 16 + localX;
                int worldZ = chunkZ * 16 + localZ;

                double large = noise.noise(
                        worldX * 0.006,
                        worldZ * 0.006
                );

                double medium = noise.noise(
                        worldX * 0.018,
                        worldZ * 0.018
                );

                double detail = noise.noise(
                        worldX * 0.055,
                        worldZ * 0.055
                );

                double terrain =
                        large * 0.65
                                + medium * 0.25
                                + detail * 0.10;

                double normalized =
                        (terrain + 1.0) / 2.0;

                int depth =
                        MIN_DEPTH
                                + (int) Math.round(
                                normalized * (MAX_DEPTH - MIN_DEPTH)
                        );

                depth = Math.clamp(depth, MIN_DEPTH, MAX_DEPTH);

                int floorY = WATER_LEVEL - depth;

                chunkData.setBlock(
                        localX,
                        minY,
                        localZ,
                        Material.BEDROCK
                );

                int sandStart = floorY - SAND_LAYERS + 1;

                for (int y = minY + 1; y < sandStart; y++) {
                    chunkData.setBlock(
                            localX,
                            y,
                            localZ,
                            Material.STONE
                    );
                }

                for (int y = sandStart; y <= floorY; y++) {
                    chunkData.setBlock(
                            localX,
                            y,
                            localZ,
                            Material.SAND
                    );
                }

                for (int y = floorY + 1; y <= WATER_LEVEL; y++) {
                    chunkData.setBlock(
                            localX,
                            y,
                            localZ,
                            Material.WATER
                    );
                }

                // =================================================
                // SEEGRAS / SEEGURKEN
                // =================================================

                double plantNoise = noise.noise(
                        worldX * 0.09 + 500,
                        worldZ * 0.09 + 500
                );

                if (plantNoise > 0.35) {

                    if (random.nextDouble() < 0.35) {
                        chunkData.setBlock(
                                localX,
                                floorY + 1,
                                localZ,
                                Material.SEAGRASS
                        );
                    }

                } else if (plantNoise < -0.55) {

                    if (random.nextDouble() < 0.08) {
                        chunkData.setBlock(
                                localX,
                                floorY + 1,
                                localZ,
                                Material.SEA_PICKLE
                        );
                    }
                }

                // =================================================
                // KORALLEN
                // =================================================

                double coralNoise = noise.noise(
                        worldX * 0.045 + 1200,
                        worldZ * 0.045 + 1200
                );

                if (coralNoise > 0.62 && random.nextDouble() < 0.12) {

                    Material coralBlock =
                            CORAL_BLOCKS[random.nextInt(CORAL_BLOCKS.length)];

                    chunkData.setBlock(
                            localX,
                            floorY,
                            localZ,
                            coralBlock
                    );

                    if (floorY + 1 < WATER_LEVEL) {

                        if (random.nextDouble() < 0.65) {
                            Material coral =
                                    CORALS[random.nextInt(CORALS.length)];

                            chunkData.setBlock(
                                    localX,
                                    floorY + 1,
                                    localZ,
                                    coral
                            );
                        }

                        if (random.nextDouble() < 0.20) {
                            chunkData.setBlock(
                                    localX,
                                    floorY + 1,
                                    localZ,
                                    Material.SEA_PICKLE
                            );
                        }
                    }
                }
            }
        }
    }

    @Override
    public @Nullable BiomeProvider getDefaultBiomeProvider(
            @NotNull WorldInfo worldInfo
    ) {

        return new BiomeProvider() {

            @Override
            public @NotNull Biome getBiome(
                    @NotNull WorldInfo worldInfo,
                    int x,
                    int y,
                    int z
            ) {
                return LOBBY_BIOME;
            }

            @Override
            public @NotNull List<Biome> getBiomes(
                    @NotNull WorldInfo worldInfo
            ) {
                return List.of(LOBBY_BIOME);
            }
        };
    }

    @Override
    public boolean shouldGenerateMobs() {
        return true;
    }
}