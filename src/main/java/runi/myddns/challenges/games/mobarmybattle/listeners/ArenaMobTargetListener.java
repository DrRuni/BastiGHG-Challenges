
package runi.myddns.challenges.games.mobarmybattle.listeners;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.persistence.PersistentDataType;
import runi.myddns.challenges.games.mobarmybattle.MobArmyBattleGame;

import java.util.Comparator;

@SuppressWarnings("unused")
public class ArenaMobTargetListener implements Listener {

    private static final String ARENA_WORLD = "world_mobarmy_arena";

    private final MobArmyBattleGame game;
    private final NamespacedKey arenaMobKey;
    private final NamespacedKey enemyTeamKey;

    public ArenaMobTargetListener(MobArmyBattleGame game) {
        this.game = game;
        this.arenaMobKey = new NamespacedKey(game.getPlugin(), "arenaMob");
        this.enemyTeamKey = new NamespacedKey(game.getPlugin(), "enemyTeam");
    }

    private boolean isArenaMob(Entity entity) {
        if (!entity.getWorld().getName().equals(ARENA_WORLD)) {
            return false;
        }

        Byte value = entity.getPersistentDataContainer().get(
                arenaMobKey,
                PersistentDataType.BYTE
        );

        return value != null && value == (byte) 1;
    }

    @EventHandler
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {

        if (!(event.getEntity() instanceof Creature creature)) {
            return;
        }

        if (!isArenaMob(creature)) {
            return;
        }

        String enemyTeam = creature.getPersistentDataContainer().get(
                enemyTeamKey,
                PersistentDataType.STRING
        );

        if (enemyTeam == null) {
            event.setCancelled(true);
            return;
        }

        Player nearest = Bukkit.getOnlinePlayers()
                .stream()
                .filter(player ->
                        player.getWorld().equals(creature.getWorld())
                )
                .filter(player ->
                        !player.isDead()
                                && player.getGameMode() != GameMode.SPECTATOR
                )
                .filter(player -> {
                    String team = game.getTeamManager()
                            .getPlayerTeam(player);

                    return team != null
                            && team.equalsIgnoreCase(enemyTeam);
                })
                .min(Comparator.comparingDouble(
                        player -> player.getLocation()
                                .distanceSquared(creature.getLocation())
                ))
                .orElse(null);

        if (nearest == null) {
            event.setCancelled(true);
            return;
        }

        event.setTarget(nearest);
    }

    @EventHandler
    public void onMobCombust(EntityCombustEvent event) {

        if (event.getEntity() instanceof LivingEntity living
                && isArenaMob(living)) {

            event.setCancelled(true);
        }
    }
}
