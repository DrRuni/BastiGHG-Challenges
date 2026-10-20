
package runi.myddns.challenges.core.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class ColorUtil {

    public static String borderColor(String text) {

        int startColor = DisplayColor.LIGHT_BLUE;
        int endColor = 0x1E3A5A;

        StringBuilder sb = new StringBuilder();
        int len = Math.max(1, text.length() - 1);

        for (int i = 0; i < text.length(); i++) {

            float t = i / (float) len;

            int r = interpolate(startColor >> 16 & 0xFF, endColor >> 16 & 0xFF, t);
            int g = interpolate(startColor >> 8 & 0xFF, endColor >> 8 & 0xFF, t);
            int b = interpolate(startColor & 0xFF, endColor & 0xFF, t);

            sb.append(hexColor(r, g, b))
                    .append(text.charAt(i));
        }

        return sb.toString();
    }

    public static Component gradientText(
            String text,
            int startColor,
            int endColor
    ) {

        Component result = Component.empty();
        int length = Math.max(1, text.length() - 1);

        for (int i = 0; i < text.length(); i++) {

            double factor = (double) i / length;

            int red = (int) Math.round(
                    ((startColor >> 16) & 0xFF)
                            + (((endColor >> 16) & 0xFF) - ((startColor >> 16) & 0xFF)) * factor
            );

            int green = (int) Math.round(
                    ((startColor >> 8) & 0xFF)
                            + (((endColor >> 8) & 0xFF) - ((startColor >> 8) & 0xFF)) * factor
            );

            int blue = (int) Math.round(
                    (startColor & 0xFF)
                            + ((endColor & 0xFF) - (startColor & 0xFF)) * factor
            );

            result = result.append(
                    Component.text(
                            String.valueOf(text.charAt(i)),
                            TextColor.color(red, green, blue)
                    ).decorate(TextDecoration.BOLD)
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

        StringBuilder sb = new StringBuilder();
        int length = Math.max(1, text.length() - 1);

        for (int i = 0; i < text.length(); i++) {

            float offset = i / (float) length;
            float wave = offset * 1.5f + tick * 0.025f;
            float t = (float) (Math.sin(wave * Math.PI * 2) * 0.5f + 0.5f);

            int r = interpolate((startColor >> 16) & 0xFF, (endColor >> 16) & 0xFF, t);
            int g = interpolate((startColor >> 8) & 0xFF, (endColor >> 8) & 0xFF, t);
            int b = interpolate(startColor & 0xFF, endColor & 0xFF, t);

            sb.append(hexColor(r, g, b))
                    .append("§l")
                    .append(text.charAt(i));
        }

        return sb.append("§r").toString();
    }

    private static int interpolate(int start, int end, float factor) {
        return (int) (start + (end - start) * factor);
    }

    private static String hexColor(int r, int g, int b) {

        String hex = String.format("%02x%02x%02x", r, g, b);
        StringBuilder result = new StringBuilder("§x");

        for (char c : hex.toCharArray()) {
            result.append('§').append(c);
        }

        return result.toString();
    }
}
