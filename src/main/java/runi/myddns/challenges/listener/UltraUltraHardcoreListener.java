package runi.myddns.challenges.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import runi.myddns.challenges.ChallengeMain;
import runi.myddns.challenges.core.game.ChallengeGame;
import runi.myddns.challenges.core.world.GameWorldSettingsManager;

public class UltraUltraHardcoreListener implements Listener {

    private final ChallengeMain plugin;

    public UltraUltraHardcoreListener(ChallengeMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRegainHealth(EntityRegainHealthEvent event) {

        if (!(event.getEntity() instanceof Player player)) return;

        ChallengeGame game = plugin.getGameManager().getSelectedGame();

        if (game == null) return;
        if (!game.isLoaded()) return;

        GameWorldSettingsManager settings = game.getWorldSettingsManager();

        if (!settings.isUltraUltraHardcore()) return;

        if (!settings.getWorldNames().contains(player.getWorld().getName())) {
            return;
        }

        event.setCancelled(true);
    }
}