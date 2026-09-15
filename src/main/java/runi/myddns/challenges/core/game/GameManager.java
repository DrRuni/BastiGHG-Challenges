package runi.myddns.challenges.core.game;

import java.util.ArrayList;
import java.util.List;

public class GameManager {

    private final List<ChallengeGame> games =
            new ArrayList<>();

    private int selectedGameIndex = 0;

    public void registerGame(
            ChallengeGame game
    ) {
        games.add(game);
    }

    public ChallengeGame getSelectedGame() {

        if (games.isEmpty()) {
            return null;
        }

        return games.get(
                selectedGameIndex
        );
    }

    public void selectGame(String gameId) {
        for (int i = 0; i < games.size(); i++) {
            if (games.get(i).getId().equalsIgnoreCase(gameId)) {
                selectedGameIndex = i;
                return;
            }
        }
    }

    public void selectNextGame() {

        if (games.isEmpty()) {
            return;
        }

        selectedGameIndex++;

        if (selectedGameIndex >= games.size()) {
            selectedGameIndex = 0;
        }
    }

    public int getSelectedGameNumber() {
        return selectedGameIndex + 1;
    }

    public int getSelectedGameIndex() {
        return selectedGameIndex;
    }

    public void setSelectedGameIndex(
            int index
    ) {

        if (index < 0
                || index >= games.size()) {
            return;
        }

        selectedGameIndex = index;
    }

    public void loadSelectedGame() {

        ChallengeGame game =
                getSelectedGame();

        if (game == null) {
            return;
        }

        game.load();
    }
}
