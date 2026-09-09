package runi.myddns.challenges.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import runi.myddns.challenges.ChallengeMain;

public class PlayerJoinListener implements Listener {

    private final ChallengeMain plugin;

    private static final String RESOURCE_PACK_URL =
            "https://github.com/DrRuni/BastiGHG-Challenges/releases/download/V0.1/BastiGHG-Challenges-Fan-Projekt.zip";
    private static final String RESOURCE_PACK_SHA1 =
            "5e30419143534a7370f722dbed0310a5487bade9";

    public PlayerJoinListener(ChallengeMain plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onResourcePackStatus(PlayerResourcePackStatusEvent event) {
        plugin.getLogger().info(
                "Resourcepack-Status von " +
                        event.getPlayer().getName() +
                        ": " +
                        event.getStatus()
        );
    }



    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        player.teleport(plugin.getLobbyWorldManager().getSpawn());

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;

            Component prompt = Component.text()
                    .append(Component.text("BastiGHG Challenges\n").color(NamedTextColor.GOLD))
                    .append(Component.text("Optional resource pack for ").color(NamedTextColor.GRAY))
                    .append(Component.text("custom graphics and icons.").color(NamedTextColor.AQUA))
                    .build();

            player.setResourcePack(RESOURCE_PACK_URL, RESOURCE_PACK_SHA1, true, prompt);
        }, 20L);

        if (player.isOp() && !plugin.getLanguageManager().hasLanguage()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;
                if (plugin.getLanguageManager().hasLanguage()) return;

                plugin.getLanguageSelectionGUI().open(player);
            }, 80L);
        }
    }
}
