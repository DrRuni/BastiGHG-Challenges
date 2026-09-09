package runi.myddns.challenges.core.server;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.CachedServerIcon;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.logging.Level;

public class ServerIconManager implements Listener {

    private final JavaPlugin plugin;
    private CachedServerIcon serverIcon;

    public ServerIconManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadIcon();
    }

    private void loadIcon() {

        try (InputStream inputStream =
                     plugin.getResource("server-icon.png")) {

            if (inputStream == null) {
                plugin.getLogger().warning(
                        "server-icon.png wurde nicht in resources gefunden."
                );
                return;
            }

            BufferedImage original =
                    ImageIO.read(inputStream);

            if (original == null) {
                plugin.getLogger().warning(
                        "server-icon.png konnte nicht gelesen werden."
                );
                return;
            }

            BufferedImage scaled =
                    new BufferedImage(
                            64,
                            64,
                            BufferedImage.TYPE_INT_ARGB
                    );

            Graphics2D graphics =
                    scaled.createGraphics();

            graphics.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC
            );

            graphics.setRenderingHint(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY
            );

            graphics.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            graphics.drawImage(
                    original,
                    0,
                    0,
                    64,
                    64,
                    null
            );

            graphics.dispose();

            serverIcon =
                    plugin.getServer().loadServerIcon(
                            scaled
                    );

            plugin.getLogger().info(
                    "Server-Icon erfolgreich geladen."
            );

        } catch (Exception exception) {

            plugin.getLogger().log(
                    Level.SEVERE,
                    "Server-Icon konnte nicht geladen werden.",
                    exception
            );
        }
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {

        if (serverIcon != null) {
            event.setServerIcon(serverIcon);
        }

        event.motd(createMotd());
    }

    private Component createMotd() {
        return MiniMessage.miniMessage().deserialize(
                "<bold><gradient:#20D5C2:#B8FF32>     BastiGHG's Challenges</gradient></bold> " +
                        "<dark_gray>•</dark_gray> " +
                        "<gray>Fan Project</gray>" +
                        "\n" +
                        "<dark_aqua><obfuscated>XX</obfuscated></dark_aqua> " +
                        "<bold><gradient:#00BFAF:#B7FF38>CHALLENGE NETWORK</gradient></bold> " +
                        "<dark_gray>┃</dark_gray> " +
                        "<aqua>Paper</aqua> " +
                        "<bold><white>26.2</white></bold> " +
                        "<green><obfuscated>XX</obfuscated></green>"
        );
    }
}
