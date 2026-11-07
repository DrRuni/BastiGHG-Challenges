
package runi.myddns.challenges.core.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import runi.myddns.challenges.ChallengeMain;
import runi.myddns.challenges.core.game.ChallengeGame;
import runi.myddns.challenges.games.chunkrandomizer.ChunkRandomizerGame;
import runi.myddns.challenges.games.mobarmybattle.MobArmyBattleGame;

import java.util.List;

public class ChallengeCommand implements CommandExecutor, TabCompleter {

    private final ChallengeMain plugin;

    public ChallengeCommand(ChallengeMain plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cDieser Befehl ist nur für Spieler.");
            return true;
        }

        if (args.length != 1 || !args[0].equalsIgnoreCase("start")) {
            player.sendMessage("§eVerwendung: /" + label + " start");
            return true;
        }

        ChallengeGame game = plugin.getGameManager().getSelectedGame();

        if (game == null || !game.isLoaded()) {
            player.sendMessage("§cKeine Challenge geladen.");
            return true;
        }

        if (!plugin.getGameWorldManager()
                .isGameWorld(player.getWorld(), game.getId())) {

            player.sendMessage("§cDu musst dich in der jeweiligen Gamewelt befinden.");
            return true;
        }

        switch (game.getId().toLowerCase(java.util.Locale.ROOT)) {

            case "levelborder" ->
                    player.performCommand("levelborder start");

            case "levelblock" ->
                    player.performCommand("levelblock start");

            case "mobarmybattle" -> {
                MobArmyBattleGame mobArmy = (MobArmyBattleGame) game;

                mobArmy.getButtonManager().handleStartButton(player);
            }

            case "chunkrandomizer" -> {
                ChunkRandomizerGame randomizer = (ChunkRandomizerGame) game;
                randomizer.startChallenge(player);
            }

            default ->
                    player.sendMessage("§cUnbekannte Challenge.");
        }

        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String[] args
    ) {

        if (args.length == 1
                && "start".startsWith(args[0].toLowerCase(
                java.util.Locale.ROOT))) {
            return List.of("start");
        }

        return List.of();
    }
}
