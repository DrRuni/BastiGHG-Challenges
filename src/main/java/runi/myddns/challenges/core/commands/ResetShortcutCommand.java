
package runi.myddns.challenges.core.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import runi.myddns.challenges.ChallengeMain;
import runi.myddns.challenges.core.game.ChallengeGame;

import java.util.List;

public class ResetShortcutCommand implements CommandExecutor, TabCompleter {

    private final ChallengeMain plugin;

    public ResetShortcutCommand(ChallengeMain plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            String @NotNull [] args
    ) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getLanguageManager()
                    .getComponent("commands.language.player-only"));
            return true;
        }

        ChallengeGame game = getPlayerGame(player);

        if (game == null) {
            player.sendMessage(plugin.getLanguageManager()
                    .getComponent("reset-shortcut.game-world-only"));
            return true;
        }

        var language = plugin.getLanguageManager();
        String gameId = game.getId();

        // /reset -> Befehlsübersicht
        if (args.length == 0) {
            player.sendMessage(Component.empty());
            player.sendMessage(language.getComponent(
                    "reset-shortcut.title", "game", game.getDisplayName()));

            player.sendMessage(language.getComponent("reset-shortcut.gamedata"));
            player.sendMessage(language.getComponent("reset-shortcut.world"));

            // Nur MobArmyBattle hat diese beiden Reset-Arten
            if (gameId.equalsIgnoreCase("mobarmybattle")) {
                player.sendMessage(language.getComponent("reset-shortcut.arena"));
                player.sendMessage(language.getComponent("reset-shortcut.teamwelt"));
            }

            player.sendMessage(Component.empty());
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(language.getComponent("reset-shortcut.usage"));
            return true;
        }

        String type = args[0].toLowerCase(java.util.Locale.ROOT);
        String targetCommand;

        switch (type) {
            case "gamedata" -> targetCommand =
                    gameId.equalsIgnoreCase("mobarmybattle")
                            ? "mobarmy gamereset"
                            : gameId + " reset gamedata";

            case "world" -> targetCommand =
                    gameId.equalsIgnoreCase("mobarmybattle")
                            ? "mobarmy reset world"
                            : gameId + " reset world";

            case "arena" -> {
                if (!gameId.equalsIgnoreCase("mobarmybattle")) {
                    player.sendMessage(language.getComponent("reset-shortcut.unknown"));
                    return true;
                }
                targetCommand = "mobarmy reset arena";
            }

            case "teamwelt", "teamworld" -> {
                if (!gameId.equalsIgnoreCase("mobarmybattle")) {
                    player.sendMessage(language.getComponent("reset-shortcut.unknown"));
                    return true;
                }
                targetCommand = "mobarmy reset teamwelt";
            }

            default -> {
                player.sendMessage(language.getComponent("reset-shortcut.unknown"));
                return true;
            }
        }

        player.performCommand(targetCommand);
        return true;
    }


    private ChallengeGame getPlayerGame(Player player) {

        for (String gameId : List.of(
                "mobarmybattle",
                "levelborder",
                "levelblock"
        )) {
            ChallengeGame game = plugin.getGameManager().getGame(gameId);

            if (game != null
                    && game.isLoaded()
                    && plugin.getGameWorldManager()
                    .isGamePlayer(player, gameId)) {
                return game;
            }
        }

        return null;
    }

    @Override
    public @NotNull List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            String @NotNull [] args
    ) {
        if (!(sender instanceof Player player)) {
            return List.of();
        }

        ChallengeGame game = getPlayerGame(player);

        if (game == null || args.length != 1) {
            return List.of();
        }

        List<String> options = game.getId().equalsIgnoreCase("mobarmybattle")
                ? List.of("gamedata", "world", "arena", "teamwelt")
                : List.of("gamedata", "world");

        String input = args[0].toLowerCase(java.util.Locale.ROOT);

        return options.stream()
                .filter(option -> option.startsWith(input))
                .toList();
    }
}

