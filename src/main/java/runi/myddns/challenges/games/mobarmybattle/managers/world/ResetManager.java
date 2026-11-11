
package runi.myddns.challenges.games.mobarmybattle.managers.world;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import runi.myddns.challenges.games.mobarmybattle.MobArmyBattleGame;

import java.io.File;
import java.util.List;

public class ResetManager {

    private final MobArmyBattleGame game;

    public ResetManager(MobArmyBattleGame game) {
        this.game = game;
    }

    /**
     * Leert die Kisten in beiden Waveauswahl-Zonen.
     * Kistenblöcke und andere Blöcke bleiben unverändert.
     */
    public void clearWaveSelectionChests() {

        File file = new File(game.getDataFolder(), "spawns.yml");

        if (!file.isFile()) {
            game.getPlugin().getLogger().warning(
                    "MobArmyBattle Reset: spawns.yml nicht gefunden."
            );
            return;
        }

        FileConfiguration config =
                YamlConfiguration.loadConfiguration(file);

        String worldName = config.getString("wave-auswahl.world");

        if (worldName == null) {
            game.getPlugin().getLogger().warning(
                    "MobArmyBattle Reset: Waveauswahl-Welt fehlt."
            );
            return;
        }

        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            game.getPlugin().getLogger().warning(
                    "MobArmyBattle Reset: Welt nicht geladen: " + worldName
            );
            return;
        }

        int cleared = 0;

        cleared += clearChestsInZone(world, config, "wave-auswahl.rot");
        cleared += clearChestsInZone(world, config, "wave-auswahl.blau");

        game.getPlugin().getLogger().info(
                "MobArmyBattle Reset: " + cleared
                        + " Kisteninventare in der Waveauswahl geleert."
        );
    }

    private int clearChestsInZone(
            World world,
            FileConfiguration config,
            String path
    ) {

        List<Integer> corner1 = config.getIntegerList(path + ".corner1");
        List<Integer> corner2 = config.getIntegerList(path + ".corner2");

        if (corner1.size() != 3 || corner2.size() != 3) {
            game.getPlugin().getLogger().warning(
                    "MobArmyBattle Reset: Ungültige Corner-Koordinaten: " + path
            );
            return 0;
        }

        int minX = Math.min(corner1.get(0), corner2.get(0));
        int maxX = Math.max(corner1.get(0), corner2.get(0));

        int minY = Math.min(corner1.get(1), corner2.get(1));
        int maxY = Math.max(corner1.get(1), corner2.get(1));

        int minZ = Math.min(corner1.get(2), corner2.get(2));
        int maxZ = Math.max(corner1.get(2), corner2.get(2));

        int cleared = 0;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {

                    Block block = world.getBlockAt(x, y, z);

                    if (block.getState() instanceof Chest chest) {
                        chest.getBlockInventory().clear();
                        cleared++;
                    }
                }
            }
        }

        return cleared;
    }
}

