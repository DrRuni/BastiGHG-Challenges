package runi.myddns.challenges.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import runi.myddns.challenges.ChallengeMain;
import runi.myddns.challenges.core.utils.ConsoleColor;

public class PlayerJoinListener implements Listener {

    private final ChallengeMain plugin;

    private static final String RESOURCE_PACK_URL =
            "https://github.com/DrRuni/BastiGHG-Challenges/releases/download/v1.0.0/BastiGHG-Challenges-Fan-Projekt.zip";

    private static final String RESOURCE_PACK_SHA1 =
            "5EBA6902268830FC97376DE75CAF300D1DF5663E";

    public PlayerJoinListener(ChallengeMain plugin) {
        this.plugin = plugin;
    }

    private void sendWelcome(Player player) {

        player.sendMessage(
                plugin.getLanguageManager()
                        .getComponent("player-join.welcome")
        );
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        offerResourcePack(player);

        player.teleport(
                plugin.getLobbyWorldManager()
                        .getSpawn()
        );

        player.setGameMode(GameMode.SURVIVAL);

        if (player.isOp()
                && !plugin.getLanguageManager().hasLanguage()) {

            Bukkit.getScheduler().runTaskLater(
                    plugin,
                    () -> {

                        if (!player.isOnline()) {
                            return;
                        }

                        if (plugin.getLanguageManager().hasLanguage()) {
                            return;
                        }

                        plugin.getLanguageSelectionGUI()
                                .open(player);

                    },
                    80L
            );
        }

        Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> {

                    if (player.isOnline()) {
                        sendWelcome(player);
                    }
                },
                40L
        );
    }

    private void offerResourcePack(Player player) {

        Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> {

                    if (!player.isOnline()) {
                        return;
                    }

                    Component prompt =
                            Component.text()
                                    .append(
                                            Component.text(
                                                    plugin.getLanguageManager()
                                                            .get("player-join.resource-pack.title")
                                            ).color(
                                                    NamedTextColor.GOLD
                                            )
                                    )
                                    .append(
                                            Component.text("\n")
                                    )
                                    .append(
                                            Component.text(
                                                    plugin.getLanguageManager()
                                                            .get("player-join.resource-pack.line-1")
                                            ).color(
                                                    NamedTextColor.GRAY
                                            )
                                    )
                                    .append(
                                            Component.text(" ")
                                    )
                                    .append(
                                            Component.text(
                                                    plugin.getLanguageManager()
                                                            .get("player-join.resource-pack.line-2")
                                            ).color(
                                                    NamedTextColor.AQUA
                                            )
                                    )
                                    .build();

                    player.setResourcePack(
                            RESOURCE_PACK_URL,
                            RESOURCE_PACK_SHA1,
                            false,
                            prompt
                    );

                },
                20L
        );
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onResourcePackStatus(
            PlayerResourcePackStatusEvent event
    ) {

        Player player = event.getPlayer();

        switch (event.getStatus()) {

            case SUCCESSFULLY_LOADED -> {

                Bukkit.getConsoleSender().sendMessage(
                        ConsoleColor.SAGE
                                + "[BastiGHG-Challenges] "
                                + player.getName()
                                + " successfully loaded the resource pack."
                                + ConsoleColor.RESET
                );

                plugin.getLobbyDisplayManager().refreshLogo();
            }

            case DECLINED -> Bukkit.getConsoleSender().sendMessage(
                    ConsoleColor.LIGHT_RED
                            + "[BastiGHG-Challenges] "
                            + player.getName()
                            + " declined the resource pack."
                            + ConsoleColor.RESET
            );

            case FAILED_DOWNLOAD, FAILED_RELOAD, INVALID_URL,
                 DISCARDED -> Bukkit.getConsoleSender().sendMessage(
                    ConsoleColor.LIGHT_RED
                            + "[BastiGHG-Challenges] "
                            + player.getName()
                            + " failed to load the resource pack ("
                            + event.getStatus()
                            + ")."
                            + ConsoleColor.RESET
            );

            default -> {
            }
        }
    }

}