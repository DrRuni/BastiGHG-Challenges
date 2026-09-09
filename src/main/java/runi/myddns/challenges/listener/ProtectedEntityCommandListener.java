package runi.myddns.challenges.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

public class ProtectedEntityCommandListener
        implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(
            PlayerCommandPreprocessEvent event
    ) {

        String command =
                event.getMessage()
                        .trim();

        if (command.equalsIgnoreCase(
                "/kill @e"
        )) {

            event.setMessage(
                    "/kill @e[tag=!challenge_protected]"
            );

            return;
        }

        if (command.equalsIgnoreCase(
                "/minecraft:kill @e"
        )) {

            event.setMessage(
                    "/minecraft:kill @e[tag=!challenge_protected]"
            );
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerCommand(
            ServerCommandEvent event
    ) {

        String command =
                event.getCommand()
                        .trim();

        if (command.equalsIgnoreCase(
                "kill @e"
        )) {

            event.setCommand(
                    "kill @e[tag=!challenge_protected]"
            );

            return;
        }

        if (command.equalsIgnoreCase(
                "minecraft:kill @e"
        )) {

            event.setCommand(
                    "minecraft:kill @e[tag=!challenge_protected]"
            );
        }
    }
}
