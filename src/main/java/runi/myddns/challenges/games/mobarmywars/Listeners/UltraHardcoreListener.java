package runi.myddns.challenges.games.mobarmywars.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import runi.myddns.challenges.games.mobarmywars.MobArmyWarsGame;

public class UltraHardcoreListener implements Listener {

    private final MobArmyWarsGame game;

    public UltraHardcoreListener(MobArmyWarsGame game) {
        this.game = game;
    }

    @EventHandler
    public void onRegainHealth(EntityRegainHealthEvent event) {

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (!game.getWorldSettings()
                .getDifficulty()
                .equalsIgnoreCase("ultra-ultra-hardcore")) {
            return;
        }

        if (event.getRegainReason()
                == EntityRegainHealthEvent.RegainReason.SATIATED) {

            event.setCancelled(true);
        }
    }
}
