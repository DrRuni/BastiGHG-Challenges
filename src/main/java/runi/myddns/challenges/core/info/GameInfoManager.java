package runi.myddns.challenges.core.info;

import runi.myddns.challenges.ChallengeMain;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GameInfoManager {

    private final ChallengeMain plugin;

    public GameInfoManager(ChallengeMain plugin) {
        this.plugin = plugin;
    }

    public List<String> getDescription(String gameId) {

        List<String> lines = readReadme(gameId);

        List<String> result = new ArrayList<>();

        boolean started = false;

        for (String line : lines) {

            if (line.startsWith("# ")) {
                started = true;
                continue;
            }

            if (!started) {
                continue;
            }

            if (line.startsWith("## ")) {
                break;
            }

            if (line.startsWith(">")) {
                continue;
            }

            if (!line.isBlank()) {
                result.add(cleanLine(line));
            }
        }

        return result;
    }

    public List<String> getFeatures(String gameId) {
        return getSection(
                gameId,
                "Features"
        );
    }

    public List<String> getCommands(String gameId) {
        return getSection(
                gameId,
                "Commands"
        );
    }

    public List<String> getChangelog(String gameId) {
        return getSection(
                gameId,
                "Changelog"
        );
    }

    public String getVersion(String gameId) {

        List<String> lines =
                getSection(
                        gameId,
                        "Current Version"
                );

        if (lines.isEmpty()) {
            return "Unknown";
        }

        return cleanLine(
                lines.getFirst()
        );
    }

    private List<String> getSection(
            String gameId,
            String section
    ) {

        List<String> lines =
                readReadme(gameId);

        List<String> result =
                new ArrayList<>();

        boolean inside =
                false;

        for (String line : lines) {

            if (line.equalsIgnoreCase(
                    "## " + section
            )) {
                inside = true;
                continue;
            }

            if (inside
                    && line.startsWith("## ")) {
                break;
            }

            if (!inside) {
                continue;
            }

            if (line.isBlank()) {
                continue;
            }

            if (line.equals("```text")
                    || line.equals("```")) {
                continue;
            }

            result.add(
                    cleanLine(line)
            );
        }

        return result;
    }

    private List<String> readReadme(
            String gameId
    ) {

        String path =
                "games/"
                        + gameId.toLowerCase()
                        + "/README.md";

        InputStream inputStream =
                plugin.getResource(path);

        if (inputStream == null) {

            plugin.getLogger().warning(
                    "README nicht gefunden: "
                            + path
            );

            return List.of();
        }

        List<String> lines =
                new ArrayList<>();

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        inputStream,
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            String line;

            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }

        } catch (Exception exception) {

            plugin.getLogger().warning(
                    "README konnte nicht gelesen werden: "
                            + path
            );
        }

        return lines;
    }

    private String cleanLine(
            String line
    ) {

        return line
                .replace("**", "")
                .replace("`", "")
                .replaceFirst("^###\\s+", "")
                .replaceFirst("^####\\s+", "")
                .replaceFirst("^-\\s+", "")
                .trim();
    }
}
