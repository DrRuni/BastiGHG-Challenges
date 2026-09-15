package runi.myddns.challenges.games.LevelBorder.Listeners;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.*;
import runi.myddns.challenges.core.utils.ColorUtil;
import runi.myddns.challenges.games.LevelBorder.LevelBorderGame;
import runi.myddns.challenges.games.LevelBorder.Manager.BorderDataManager;
import runi.myddns.challenges.games.LevelBorder.Manager.LevelBorderManager;
import runi.myddns.challenges.games.LevelBorder.Manager.ScoreboardManager;
import runi.myddns.challenges.games.LevelBorder.Manager.TimerManager;

import java.time.Duration;

public class PlayerListener implements Listener {

    private final LevelBorderGame game;
    private final LevelBorderManager borderManager;
    private final ScoreboardManager scoreboardManager;
    private final TimerManager timerManager;

    public PlayerListener(LevelBorderGame game, LevelBorderManager borderManager, ScoreboardManager scoreboardManager, TimerManager timerManager) {
        this.game = game;
        this.borderManager = borderManager;
        this.scoreboardManager = scoreboardManager;
        this.timerManager = timerManager;
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        if (!game.isLevelBorderPlayer(player)) return;

        scoreboardManager.addPlayer(player);

        player.sendMessage("");
        player.sendMessage(ColorUtil.borderColor("Run´s LevelBorder"));
        player.sendMessage("");
        player.sendMessage(ChatColor.GRAY + "Starte mit " + ChatColor.AQUA + "/levelborder " + ChatColor.GOLD + "start");
        player.sendMessage("");

        Bukkit.getScheduler().runTaskLater(game.getPlugin(), () -> {
            if (!player.isOnline()) return;
            if (!game.isLevelBorderPlayer(player)) return;

            Component titleImage = Component.text("\uE032");

            player.showTitle(Title.title(
                    titleImage,
                    Component.empty(),
                    Title.Times.times(
                            Duration.ofMillis(300),
                            Duration.ofSeconds(2),
                            Duration.ofMillis(500)
                    )
            ));
        }, 40L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (!game.isLevelBorderPlayer(player)) return;

        borderManager.getData().savePlayerLevel(player);
        scoreboardManager.removePlayer(player);
    }

    @EventHandler
    public void onLevelChange(PlayerLevelChangeEvent event) {
        Player player = event.getPlayer();

        if (!game.isLevelBorderPlayer(player)) return;

        BorderDataManager data = borderManager.getData();
        data.savePlayerLevel(player);

        if (!data.isActive() || data.getCenter() == null) return;

        int totalNow = data.getTotalLevelSum();
        int maxTotal = data.getMaxTotalLevel();

        if (totalNow > maxTotal) {
            int diff = totalNow - maxTotal;
            data.setMaxTotalLevel(totalNow);
            borderManager.growByLevel(player, diff);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (!game.isLevelBorderPlayer(player)) return;

        BorderDataManager data = borderManager.getData();
        if (!data.isActive() || data.getCenter() == null) return;

        if (!event.isBedSpawn()) {
            Location center = data.getCenter();
            World world = center.getWorld();

            if (world == null) return;

            int y = world.getHighestBlockYAt(center) + 1;
            event.setRespawnLocation(new Location(world, center.getX(), y, center.getZ()));
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!game.getPlugin().getConfig().getBoolean("mob-spawning.debug.vanilla-spawns", false)) return;
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) return;

        Location loc = event.getLocation();

        Bukkit.getConsoleSender().sendMessage(
                ChatColor.DARK_RED + "[VanillaSpawn] "
                        + event.getEntityType()
                        + " bei X:" + loc.getBlockX()
                        + " Y:" + loc.getBlockY()
                        + " Z:" + loc.getBlockZ()
                        + " Welt:" + loc.getWorld().getName()
        );
    }
}