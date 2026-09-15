package runi.myddns.challenges.games.LevelBorder.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import runi.myddns.challenges.games.LevelBorder.LevelBorderGame;
import runi.myddns.challenges.games.LevelBorder.Manager.BorderDataManager;
import runi.myddns.challenges.games.LevelBorder.Manager.LevelBorderManager;
import runi.myddns.challenges.games.LevelBorder.Manager.PortalManager;
import runi.myddns.challenges.games.LevelBorder.Manager.ScoreboardManager;
import runi.myddns.challenges.games.LevelBorder.Manager.TimerManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LevelBorderCommand implements CommandExecutor, TabCompleter {

    private final LevelBorderGame game;
    private final LevelBorderManager borderManager;
    private final ScoreboardManager scoreboardManager;
    private final TimerManager timerManager;
    private final PortalManager portalManager;

    public LevelBorderCommand(
            LevelBorderGame game,
            LevelBorderManager borderManager,
            ScoreboardManager scoreboardManager,
            TimerManager timerManager,
            PortalManager portalManager
    ) {
        this.game = game;
        this.borderManager = borderManager;
        this.scoreboardManager = scoreboardManager;
        this.timerManager = timerManager;
        this.portalManager = portalManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler können diesen Befehl nutzen.");
            return true;
        }

        int playerLevelRank = getPlayerStufe(player);
        boolean isAdmin = playerLevelRank >= 4;

        if (args.length > 0 && args[0].equalsIgnoreCase("score")) {
            ScoreboardCommand scoreCmd = new ScoreboardCommand(scoreboardManager);
            String[] shifted = Arrays.copyOfRange(args, 1, args.length);
            return scoreCmd.onCommand(sender, cmd, label, shifted);
        }

        BorderDataManager data = borderManager.getData();

        if (args.length == 0) {
            sendStatus(player, data);
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "info" -> sendDetailedInfo(player, data);

            case "center" -> {
                if (!isAdmin) {
                    player.sendMessage(ChatColor.RED + "❌ Nur der Admin darf die Border-Mitte setzen!");
                    return true;
                }
                Location loc = player.getLocation();
                double x = Math.floor(loc.getX()) + 0.5;
                double z = Math.floor(loc.getZ()) + 0.5;
                Location centered = new Location(loc.getWorld(), x, loc.getY(), z);

                player.playSound(
                        player.getLocation(),
                        Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                        0.6f,
                        1.2f
                );

                borderManager.setCenter(centered);
                player.sendMessage(ChatColor.GREEN + "📍 Border-Mitte exakt auf Blockgrenze gesetzt!");
            }

            case "start" -> {
                if (!isAdmin) {
                    player.sendMessage(ChatColor.RED + "❌ Nur der Admin darf den LevelBorder starten!");
                    return true;
                }

                if (borderManager.getData().getCenter() == null) {
                    borderManager.setCenter(player.getLocation());
                }

                borderManager.setActive(true);
                scoreboardManager.show();

                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!game.isLevelBorderPlayer(p)) continue;
                    Location loc = p.getLocation();

                    p.playSound(
                            loc,
                            Sound.BLOCK_BEACON_ACTIVATE,
                            0.9f,
                            0.8f
                    );

                    Bukkit.getScheduler().runTaskLater(
                            game.getPlugin(),
                            () -> p.playSound(
                                    loc,
                                    Sound.ENTITY_WARDEN_HEARTBEAT,
                                    1.2f,
                                    0.7f
                            ),
                            10L
                    );

                    Bukkit.getScheduler().runTaskLater(
                            game.getPlugin(),
                            () -> p.playSound(
                                    loc,
                                    Sound.BLOCK_SCULK_SHRIEKER_SHRIEK,
                                    0.4f,
                                    0.6f
                            ),
                            18L
                    );
                }

                player.sendMessage(ChatColor.GREEN + "✅ Border aktiviert!");

            }

            case "stop" -> {
                if (!isAdmin) {
                    player.sendMessage(ChatColor.RED + "❌ Nur der Admin darf den LevelBorder stoppen!");
                    return true;
                }

                borderManager.setActive(false);

                player.playSound(
                        player.getLocation(),
                        Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                        0.6f,
                        1.2f
                );

                player.sendMessage(ChatColor.RED + "🛑 Border deaktiviert!");
            }

            case "set" -> {
                if (!isAdmin) {
                    player.sendMessage(ChatColor.RED + "❌ Nur der Admin darf die Bordergröße ändern!");
                    return true;
                }

                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "⚠ Nutzung: /levelborder set <größe>");
                    return true;
                }

                try {
                    double size = Double.parseDouble(args[1]);
                    borderManager.setSize(size);
                    player.sendMessage(ChatColor.YELLOW + "📏 Bordergröße gesetzt auf " + size + " Blöcke.");
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Bitte eine gültige Zahl eingeben!");
                }

                player.playSound(
                        player.getLocation(),
                        Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                        0.6f,
                        1.2f
                );

            }

            case "reset" -> {
                if (!isAdmin) {
                    player.sendMessage(ChatColor.RED + "❌ Nur der Admin darf den LevelBorder zurücksetzen!");
                    return true;
                }

                scoreboardManager.reset();
                borderManager.resetBorder(player);
                timerManager.reset();
                portalManager.clearPortalWorldData();

                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!game.isLevelBorderPlayer(p)) continue;

                    p.playSound(
                            p.getLocation(),
                            Sound.BLOCK_BEACON_DEACTIVATE,
                            0.6f,
                            0.7f
                    );
                }

                player.sendMessage(ChatColor.GREEN + "♻ LevelBorder + Portale wurden zurückgesetzt.");

            }

            default -> player.sendMessage(ChatColor.RED + "Unbekannter Unterbefehl. Nutze /levelborder für Hilfe.");
        }

        return true;
    }

    private int getPlayerStufe(Player p) {
        return p.isOp() ? 4 : 2;
    }

    private void sendStatus(Player player, BorderDataManager data) {
        player.sendMessage("\n" + ChatColor.GOLD + "============= LevelBorder Befehle ============");
        player.sendMessage(ChatColor.GRAY + " info" + ChatColor.DARK_GRAY + " → Status anzeigen");
        player.sendMessage(ChatColor.GRAY + " score" + ChatColor.DARK_GRAY + " → Scoreboard anzeigen");
        player.sendMessage(ChatColor.GRAY + " start / stop / set / reset / center" +
                ChatColor.DARK_GRAY + " → Admin");
        player.sendMessage(ChatColor.GOLD + "============================================");
    }

    private void sendDetailedInfo(Player player, BorderDataManager data) {
        int total = Bukkit.getOnlinePlayers().stream()
                .filter(game::isLevelBorderPlayer)
                .mapToInt(Player::getLevel)
                .sum();

        player.sendMessage(ChatColor.AQUA + "============= 📊 LevelBorder Info =============");
        player.sendMessage(ChatColor.GRAY + "Aktuelle Gesamt-Level: " + ChatColor.YELLOW + total);
        player.sendMessage(ChatColor.GRAY + "Bisheriger Rekord: " + ChatColor.GOLD + data.getMaxTotalLevel());
        player.sendMessage(ChatColor.GRAY + "Border-Größe: " + ChatColor.GREEN + data.getSize());
        player.sendMessage(ChatColor.GRAY + "Aktiv: " +
                (data.isActive() ? ChatColor.GREEN + "Ja" : ChatColor.RED + "Nein"));
        player.sendMessage(ChatColor.AQUA + "============================================");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {

        List<String> list = new ArrayList<>();

        if (args.length == 1) {
            list.addAll(Arrays.asList("info", "score", "center", "start", "stop", "set", "reset"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            list.addAll(Arrays.asList("10", "25", "50", "100"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("score")) {
            list.addAll(Arrays.asList("hide", "reload", "reset"));
        }

        return list;
    }
}