package runi.myddns.challenges.games.MobArmyBattle.Managers.Event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import runi.myddns.challenges.games.MobArmyBattle.MobArmyBattleGame;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TeamManager implements Listener {

    private final MobArmyBattleGame game;

    private final Map<UUID, String> playerTeams = new HashMap<>();
    private final Map<UUID, String> playerNames = new HashMap<>();

    public TeamManager(MobArmyBattleGame game) {
        this.game = game;
    }

    public void assignTeam(Player player, String team) {

        UUID uuid = player.getUniqueId();

        String oldTeam = getPlayerTeam(player);

        playerTeams.put(uuid, team);
        playerNames.put(uuid, player.getName());

        saveTeamToFile(player, team);

        game.getTeamScoreboardManager()
                .addPlayerToTeam(player, team);

        playTeamJoinSound(player, team);
        showTeamTitle(player, team);

        game.getBundleManager()
                .giveTeamBundle(player);
    }

    public void loadTeams() {

        File teamsFile =
                new File(game.getDataFolder(), "teams.yml");

        if (!teamsFile.exists()) {
            return;
        }

        YamlConfiguration teamsConfig =
                YamlConfiguration.loadConfiguration(teamsFile);

        playerTeams.clear();
        playerNames.clear();

        ConfigurationSection teamsSection =
                teamsConfig.getConfigurationSection("teams");

        if (teamsSection == null) {
            return;
        }

        for (String uuidString :
                teamsSection.getKeys(false)) {

            try {

                UUID uuid =
                        UUID.fromString(uuidString);

                String team =
                        teamsConfig.getString(
                                "teams." + uuidString + ".team"
                        );

                String name =
                        teamsConfig.getString(
                                "teams." + uuidString + ".name",
                                "Unbekannt"
                        );

                if (team == null
                        || team.equalsIgnoreCase("Kein Team")) {
                    continue;
                }

                playerTeams.put(uuid, team);
                playerNames.put(uuid, name);

            } catch (IllegalArgumentException ignored) {

                /*
                 * Alter Eintrag mit Spielername.
                 * Wird ignoriert.
                 *
                 * Für den ersten Test am besten
                 * die alte teams.yml löschen.
                 */
            }
        }
    }

    public String getPlayerTeam(Player player) {

        return playerTeams.getOrDefault(
                player.getUniqueId(),
                "Kein Team"
        );
    }

    public String getPlayerTeam(
            OfflinePlayer offlinePlayer
    ) {

        return playerTeams.getOrDefault(
                offlinePlayer.getUniqueId(),
                "Kein Team"
        );
    }

    public boolean isInTeam(Player player) {

        return playerTeams.containsKey(
                player.getUniqueId()
        );
    }

    /*
     * Wichtig für unsere spätere Abstimmung:
     *
     * true = Spieler steht bereits in teams.yml
     * und gehört damit schon zum Match.
     */
    public boolean isRegisteredParticipant(Player player) {

        return playerTeams.containsKey(
                player.getUniqueId()
        );
    }

    public void resetTeams() {

        playerTeams.clear();
        playerNames.clear();

        game.getTeamScoreboardManager()
                .rebuildBoard();

        deleteAllTeamsFromFile();
    }

    public void waitForAllTeamsAndStart() {

        new BukkitRunnable() {

            @Override
            public void run() {

                boolean allHaveTeam =
                        Bukkit.getOnlinePlayers()
                                .stream()
                                .noneMatch(player ->
                                        getPlayerTeam(player)
                                                .equalsIgnoreCase("Kein Team")
                                );

                if (allHaveTeam) {

                    cancel();

                    game.getEventManager()
                            .startEvent();
                }
            }

        }.runTaskTimer(
                game.getPlugin(),
                40L,
                40L
        );
    }

    public void removePlayerFromTeam(Player player) {

        UUID uuid = player.getUniqueId();

        String oldTeam =
                getPlayerTeam(player);

        game.getBundleManager()
                .removeTeamBundle(player);

        playerTeams.remove(uuid);
        playerNames.remove(uuid);

        removeTeamFromFile(uuid);

        game.getTeamScoreboardManager()
                .removePlayer(player);

        if (oldTeam != null
                && !oldTeam.equalsIgnoreCase("Kein Team")) {

            playTeamLeaveSound(
                    player,
                    oldTeam
            );
        }
    }

    private void saveTeamToFile(
            Player player,
            String team
    ) {

        File teamsFile =
                new File(game.getDataFolder(), "teams.yml");

        YamlConfiguration teamsConfig =
                YamlConfiguration.loadConfiguration(teamsFile);

        String path =
                "teams." + player.getUniqueId();

        teamsConfig.set(
                path + ".name",
                player.getName()
        );

        teamsConfig.set(
                path + ".team",
                team
        );

        try {
            teamsConfig.save(teamsFile);
        } catch (IOException ignored) {
        }
    }

    private void removeTeamFromFile(UUID uuid) {

        File teamsFile =
                new File(game.getDataFolder(), "teams.yml");

        if (!teamsFile.exists()) {
            return;
        }

        YamlConfiguration teamsConfig =
                YamlConfiguration.loadConfiguration(teamsFile);

        teamsConfig.set(
                "teams." + uuid,
                null
        );

        try {
            teamsConfig.save(teamsFile);
        } catch (IOException ignored) {
        }
    }

    private void deleteAllTeamsFromFile() {

        File teamsFile =
                new File(game.getDataFolder(), "teams.yml");

        if (!teamsFile.exists()) {
            return;
        }

        YamlConfiguration teamsConfig =
                YamlConfiguration.loadConfiguration(teamsFile);

        teamsConfig.set(
                "teams",
                null
        );

        try {
            teamsConfig.save(teamsFile);
        } catch (IOException ignored) {
        }
    }

    private void showTeamTitle(
            Player player,
            String team
    ) {

        boolean blue =
                team.equalsIgnoreCase("Blau");

        Component title = blue
                ? lang("team-manager.joined.title-blue")
                : lang("team-manager.joined.title-red");

        Component subtitle = blue
                ? lang("team-manager.joined.subtitle-blue")
                : lang("team-manager.joined.subtitle-red");

        player.showTitle(
                Title.title(
                        title,
                        subtitle,
                        Title.Times.times(
                                Duration.ofMillis(500),
                                Duration.ofMillis(3500),
                                Duration.ofSeconds(1)
                        )
                )
        );
    }

    private void playTeamJoinSound(
            Player changedPlayer,
            String team
    ) {

        for (Player onlinePlayer :
                Bukkit.getOnlinePlayers()) {

            String onlineTeam =
                    getPlayerTeam(onlinePlayer);

            if (onlinePlayer.equals(changedPlayer)
                    || team.equalsIgnoreCase(onlineTeam)) {

                onlinePlayer.playSound(
                        onlinePlayer.getLocation(),
                        Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                        1f,
                        1.4f
                );
            }
        }
    }

    private void playTeamLeaveSound(
            Player changedPlayer,
            String oldTeam
    ) {

        for (Player onlinePlayer :
                Bukkit.getOnlinePlayers()) {

            String onlineTeam =
                    getPlayerTeam(onlinePlayer);

            if (onlinePlayer.equals(changedPlayer)
                    || oldTeam.equalsIgnoreCase(onlineTeam)) {

                onlinePlayer.playSound(
                        onlinePlayer.getLocation(),
                        Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO,
                        1.0f,
                        0.7f
                );
            }
        }
    }

    public Set<String> getAllTeamPlayers() {

        return new HashSet<>(
                playerNames.values()
        );
    }

    private Component lang(String path) {

        return game.getLanguageManager()
                .getComponent(path);
    }
}