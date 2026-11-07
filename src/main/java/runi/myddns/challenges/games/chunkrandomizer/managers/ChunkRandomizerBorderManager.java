
package runi.myddns.challenges.games.chunkrandomizer.managers;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;

public class ChunkRandomizerBorderManager {

    public record BorderState(
            double centerX,
            double centerZ,
            double size,
            int warningDistance,
            double damageBuffer,
            double damageAmount
    ) {}

    private World borderWorld;
    private Location originalCenter;
    private double originalSize;
    private int originalWarningDistance;
    private double originalDamageBuffer;
    private double originalDamageAmount;

    private boolean active = false;

    public void createBorder(Player player) {
        if (player == null) return;

        createBorder(player.getLocation());
    }

    public void createBorder(Location location) {
        if (location == null || location.getWorld() == null || active) {
            return;
        }

        World world = location.getWorld();

        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;

        double centerX = chunkX * 16.0 + 8.0;
        double centerZ = chunkZ * 16.0 + 8.0;

        WorldBorder border = world.getWorldBorder();

        borderWorld = world;
        originalCenter = border.getCenter().clone();
        originalSize = border.getSize();
        originalWarningDistance = border.getWarningDistance();
        originalDamageBuffer = border.getDamageBuffer();
        originalDamageAmount = border.getDamageAmount();

        border.setCenter(centerX, centerZ);
        border.setSize(16.0);
        border.setWarningDistance(0);

        active = true;
    }

    public boolean moveBorder(Location location) {

        if (!active || borderWorld == null || location == null) {
            return false;
        }

        if (!borderWorld.equals(location.getWorld())) {
            return false;
        }

        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;

        double centerX = chunkX * 16.0 + 8.0;
        double centerZ = chunkZ * 16.0 + 8.0;

        WorldBorder border = borderWorld.getWorldBorder();
        Location currentCenter = border.getCenter();

        if (currentCenter.getX() == centerX
                && currentCenter.getZ() == centerZ) {
            return false;
        }

        border.setCenter(centerX, centerZ);
        border.setSize(16.0);

        return true;
    }

    public boolean restoreBorder(
            World world,
            double centerX,
            double centerZ,
            double size
    ) {

        if (world == null || active) {
            return false;
        }

        if (!Double.isFinite(centerX)
                || !Double.isFinite(centerZ)
                || !Double.isFinite(size)
                || size <= 0) {
            return false;
        }

        WorldBorder border = world.getWorldBorder();

        borderWorld = world;
        originalCenter = border.getCenter().clone();
        originalSize = border.getSize();
        originalWarningDistance = border.getWarningDistance();
        originalDamageBuffer = border.getDamageBuffer();
        originalDamageAmount = border.getDamageAmount();

        border.setCenter(centerX, centerZ);
        border.setSize(size);
        border.setWarningDistance(0);

        active = true;

        return true;
    }


    public void removeBorder() {

        if (!active || borderWorld == null) return;

        WorldBorder border = borderWorld.getWorldBorder();

        if (originalCenter != null) {
            border.setCenter(originalCenter);
        }

        border.setSize(originalSize);
        border.setWarningDistance(originalWarningDistance);
        border.setDamageBuffer(originalDamageBuffer);
        border.setDamageAmount(originalDamageAmount);

        borderWorld = null;
        originalCenter = null;
        active = false;
    }

    public boolean isActive() {
        return active;
    }

    public void setOriginalBorderState(BorderState state) {

        if (state == null || borderWorld == null || !active) {
            return;
        }

        originalCenter = new Location(
                borderWorld,
                state.centerX(),
                0,
                state.centerZ()
        );

        originalSize = state.size();
        originalWarningDistance = state.warningDistance();
        originalDamageBuffer = state.damageBuffer();
        originalDamageAmount = state.damageAmount();
    }

    public BorderState getOriginalBorderState() {

        if (originalCenter == null) return null;

        return new BorderState(
                originalCenter.getX(),
                originalCenter.getZ(),
                originalSize,
                originalWarningDistance,
                originalDamageBuffer,
                originalDamageAmount
        );
    }
}

