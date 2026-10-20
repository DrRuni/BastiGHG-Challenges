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

        if (player == null || config == null) {
            return;
        }

        List<?> contents = config.getList("enderchest");

        if (contents == null) {
            clear(player);
            return;
        }

        ItemStack[] items = new ItemStack[
                player.getEnderChest().getSize()
                ];

        for (int i = 0; i < Math.min(contents.size(), items.length); i++) {

            Object object = contents.get(i);

            if (object instanceof ItemStack item) {
                items[i] = item;
            }
        }

        player.getEnderChest().setContents(items);
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