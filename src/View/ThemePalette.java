package View;

import Model.ThemeType;
import java.awt.*;

public class ThemePalette {
    public final Color cardBg;     // panel dark glass
    public final Color stroke;     // border
    public final Color text;       // labels
    public final Color mutedText;  // secondary labels
    public final Color primary;    // main buttons
    public final Color secondary;  // back/cancel buttons
    public final Color danger;     // delete buttons

    private ThemePalette(Color cardBg, Color stroke, Color text, Color mutedText,
                         Color primary, Color secondary, Color danger) {
        this.cardBg = cardBg;
        this.stroke = stroke;
        this.text = text;
        this.mutedText = mutedText;
        this.primary = primary;
        this.secondary = secondary;
        this.danger = danger;
    }

    public static ThemePalette of(ThemeType t) {
        // safe defaults (forest-like)
        if (t == null) t = ThemeType.FOREST;

        return switch (t) {
            case FOREST -> new ThemePalette(
                    new Color(15, 25, 20, 210),
                    new Color(170, 255, 255, 120),
                    new Color(235, 255, 245),
                    new Color(185, 205, 195),
                    new Color(40, 160, 90),   // primary
                    new Color(85, 110, 70),   // secondary
                    new Color(180, 60, 55)    // danger
            );

            case ICE -> new ThemePalette(
                    new Color(12, 18, 28, 210),
                    new Color(160, 220, 255, 140),
                    new Color(235, 250, 255),
                    new Color(190, 210, 220),
                    new Color(70, 150, 220),
                    new Color(70, 110, 140),
                    new Color(200, 70, 70)
            );

            case LAVA -> new ThemePalette(
                    new Color(30, 12, 10, 210),
                    new Color(255, 160, 120, 140),
                    new Color(255, 240, 235),
                    new Color(220, 200, 190),
                    new Color(220, 90, 35),
                    new Color(140, 80, 60),
                    new Color(230, 60, 55)
            );

            case BEACH -> new ThemePalette(
                    new Color(20, 20, 16, 210),
                    new Color(255, 245, 200, 120),
                    new Color(255, 255, 245),
                    new Color(220, 220, 200),
                    new Color(60, 170, 160),
                    new Color(140, 150, 90),
                    new Color(200, 70, 70)
            );

            case SPACE -> new ThemePalette(
                    new Color(10, 10, 18, 220),
                    new Color(180, 170, 255, 140),
                    new Color(245, 245, 255),
                    new Color(200, 200, 220),
                    new Color(130, 100, 255),
                    new Color(90, 90, 140),
                    new Color(220, 70, 90)
            );
        };
    }
}
