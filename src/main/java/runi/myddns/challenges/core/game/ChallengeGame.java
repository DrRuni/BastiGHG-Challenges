package runi.myddns.challenges.core.game;

import org.bukkit.entity.Player;

import java.util.Collection;

public interface ChallengeGame {

    boolean hasPlayers();
    boolean canUnload();

    String getId();

    String getDisplayName();

    void load();

    void unload();

    boolean isLoaded();

    void startPlayers(
            Collection<? extends Player> players
    );
}