package runi.myddns.challenges.core.game;

import org.bukkit.entity.Player;

import java.util.Collection;

public interface ChallengeGame {

    boolean hasPlayers();
    boolean hasOtherPlayers(Player ignoredPlayer);
    boolean canUnload();

    String getId();
    String getDisplayName();

    void load();
    void unload();
    boolean isLoaded();

    void startPlayers(Collection<? extends Player> players);

    void joinActiveGame(Player player);

    void leavePlayer(Player player);
}