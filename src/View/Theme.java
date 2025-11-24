package View;

import javax.swing.*;
import java.awt.*;

/**
 * Central place for colors, fonts, paddings, etc.
 * Your MineSweeperPrototype already uses these names.
 */
public class Theme {

    // Layout constants
    public static final int PAD       = 24;
    public static final int GAP       = 8;
    public static final int RADIUS    = 24;
    public static final int RADIUS_SM = 14;

    // Colors
    public static final Color CARD        = new Color(18, 28, 18, 230);
    public static final Color CARD_STROKE = new Color(0, 0, 0, 90);
    public static final Color SECONDARY   = new Color(58, 83, 45);
    public static final Color DANGER      = new Color(192, 57, 43);
    public static final Color MUTED       = new Color(180, 200, 180);

    private static final String BASE_FONT = "SansSerif";

    public static Font regular() {
        return new Font(BASE_FONT, Font.PLAIN, 14);
    }

    public static Font bold() {
        return new Font(BASE_FONT, Font.BOLD, 14);
    }

    public static Font bold(int size) {
        return new Font(BASE_FONT, Font.BOLD, size);
    }

    // Optional: apply default font to common Swing components
    static {
        Font f = regular();
        UIManager.put("Label.font", f);
        UIManager.put("Button.font", f);
        UIManager.put("TextField.font", f);
        UIManager.put("ComboBox.font", f);
        UIManager.put("Table.font", f);
        UIManager.put("TableHeader.font", bold(14));
    }

    private Theme() { }
}
