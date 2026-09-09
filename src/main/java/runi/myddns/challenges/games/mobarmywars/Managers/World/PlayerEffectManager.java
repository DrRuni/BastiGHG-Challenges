package runi.myddns.challenges.games.mobarmywars.Managers.World;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import runi.myddns.challenges.games.mobarmywars.MobArmyWarsGame;


public class PlayerEffectManager {

    private final MobArmyWarsGame game;

    public PlayerEffectManager(MobArmyWarsGame game) {
        this.game = game;
    }

    public void applyNightVision(Player player) {
        if (player == null) return;

        if (game.getWorldSettings().isNightVisionEnabled()) {
            player.addPotionEffect(
                    new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false)
            );
        } else {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }
    }

    public void applyNightVisionToAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            applyNightVision(player);
        }
    }
}