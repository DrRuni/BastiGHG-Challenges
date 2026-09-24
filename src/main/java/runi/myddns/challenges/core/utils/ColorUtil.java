package runi.myddns.challenges.core.utils;

import net.md_5.bungee.api.ChatColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class ColorUtil {

    public static String borderColor(String text) {

        int startR = 0x7F, startG = 0xD8, startB = 0xFF;
        int endR = 0x1E, endG = 0x3A, endB = 0x5A;

        StringBuilder sb = new StringBuilder();
        int len = Math.max(1, text.length() - 1);

        for (int i = 0; i < text.length(); i++) {
            float t = i / (float) len;

            int r = (int) (startR + (endR - startR) * t);
            int g = (int) (startG + (endG - startG) * t);
            int b = (int) (startB + (endB - startB) * t);

            sb.append(ChatColor.of(
                    String.format("#%02x%02x%02x", r, g, b)
            )).append(text.charAt(i));
        }
        return sb.toString();
    }

    public static Component gradientText(
            String text,
            int startColor,
            int endColor
    ) {

        Component result = Component.empty();

        int length = Math.max(
                1,
                text.length() - 1
        );

        int startR = (startColor >> 16) & 0xFF;
        int startG = (startColor >> 8) & 0xFF;
        int startB = startColor & 0xFF;

        int endR = (endColor >> 16) & 0xFF;
        int endG = (endColor >> 8) & 0xFF;
        int endB = endColor & 0xFF;

        for (int i = 0; i < text.length(); i++) {

            double factor =
                    (double) i / length;

            int red = (int) Math.round(
                    startR + (endR - startR) * factor
            );

            int green = (int) Math.round(
                    startG + (endG - startG) * factor
            );

            int blue = (int) Math.round(
                    startB + (endB - startB) * factor
            );

            result = result.append(
                    Component.text(
                            String.valueOf(text.charAt(i)),
                            TextColor.color(
                                    red,
                                    green,
                                    blue
                            )
                    ).decorate(
                            TextDecoration.BOLD
                    )
            );
        }

        return result;
    }

    public static String animatedBlueGradient(String text, float tick) {
        return animatedGradient(
                text,
                tick,
                DisplayColor.LIGHT_BLUE,
                DisplayColor.MIDNIGHT_BLUE
        );
    }

    public static String animatedGreenGradient(String text, float tick) {
        return animatedGradient(
                text,
                tick,
                DisplayColor.LIME,
                DisplayColor.DARK_GREEN
        );
    }

    private static String animatedGradient(
            String text,
            float tick,
            int startColor,
            int endColor
    ) {

        int startR = (startColor >> 16) & 0xFF;
        int startG = (startColor >> 8) & 0xFF;
        int startB = startColor & 0xFF;

        int endR = (endColor >> 16) & 0xFF;
        int endG = (endColor >> 8) & 0xFF;
        int endB = endColor & 0xFF;

        StringBuilder sb = new StringBuilder();

        int length = Math.max(
                1,
                text.length() - 1
        );

        for (int i = 0; i < text.length(); i++) {

            float offset =
                    i / (float) length;

            float wave =
                    offset * 1.5f
                            + tick * 0.025f;

            float t =
                    (float) (
                            Math.sin(
                                    wave * Math.PI * 2
                            ) * 0.5f + 0.5f
                    );

            int r =
                    (int) (
                            startR
                                    + (endR - startR) * t
                    );

            int g =
                    (int) (
                            startG
                                    + (endG - startG) * t
                    );

            int b =
                    (int) (
                            startB
                                    + (endB - startB) * t
                    );

            sb.append(
                    ChatColor.of(
                            String.format(
                                    "#%02x%02x%02x",
                                    r,
                                    g,
                                    b
                            )
                    )
            );

            sb.append(ChatColor.BOLD);
            sb.append(text.charAt(i));
        }

        return sb.append(
                ChatColor.RESET
        ).toString();
    }
}