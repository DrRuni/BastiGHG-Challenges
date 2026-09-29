package runi.myddns.challenges.core.player;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class PlayerEnderChestManager {

    public void save(
            Player player,
            FileConfiguration config
    ) {

        if (player == null
                || config == null) {

            return;
        }

        config.set(
                "enderchest",
                Arrays.asList(
                        player.getEnderChest()
                                .getContents()
                )
        );
    }

    public void load(
            Player player,
            FileConfiguration config
    ) {

        if (player == null
                || config == null) {

            return;
        }

        List<?> contents =
                config.getList(
                        "enderchest"
                );

        clear(
                player
        );

        if (contents == null) {
            return;
        }

        player.getEnderChest()
                .setContents(
                        contents.toArray(
                                new ItemStack[0]
                        )
                );
    }

    public void clear(
            Player player
    ) {

        if (player == null) {
            return;
        }

        player.getEnderChest()
                .clear();
    }
}