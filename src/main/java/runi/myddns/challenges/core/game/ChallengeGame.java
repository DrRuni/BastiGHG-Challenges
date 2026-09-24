package runi.myddns.challenges.core.game;

import org.bukkit.entity.Player;
import runi.myddns.challenges.core.world.GameWorldSettingsManager;

import java.util.Collection;

public interface ChallengeGame {

    GameWorldSettingsManager getWorldSettingsManager();

    boolean hasPlayers();
    boolean hasOtherPlayers(Player ignoredPlayer);
    boolean isLoading();
    boolean canUnload();

    String getId();
    String getDisplayName();

    void load();
    boolean isLoaded();
    void unload();
    void shutdown();

    void startPlayers(Collection<? extends Player> players);
    void joinActiveGame(Player player);
    void leavePlayer(Player player);
    void openSettings(Player player);
}