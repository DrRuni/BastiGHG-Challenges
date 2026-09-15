package runi.myddns.challenges.games.MobArmyBattle.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import runi.myddns.challenges.games.MobArmyBattle.MobArmyBattleGame;

public class UltraHardcoreListener implements Listener {

    private final MobArmyBattleGame game;

    public UltraHardcoreListener(MobArmyBattleGame game) {
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
