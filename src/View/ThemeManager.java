package View;

import Model.AppTheme;

import javax.swing.*;
import java.awt.*;

public class ThemeManager {
    public static void apply(AppTheme theme) {
        if (theme == AppTheme.DARK) {
            UIManager.put("Panel.background", new Color(30,30,30));
            UIManager.put("Label.foreground", Color.WHITE);
            UIManager.put("Button.background", new Color(60,60,60));
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Table.background", new Color(40,40,40));
            UIManager.put("Table.foreground", Color.WHITE);
            UIManager.put("Table.selectionBackground", new Color(70,70,70));
        } else {
            // reset defaults
            UIManager.put("Panel.background", null);
            UIManager.put("Label.foreground", null);
            UIManager.put("Button.background", null);
            UIManager.put("Button.foreground", null);
            UIManager.put("Table.background", null);
            UIManager.put("Table.foreground", null);
            UIManager.put("Table.selectionBackground", null);
        }
    }

    public static void refreshAllWindows() {
        for (Window w : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(w);
            w.repaint();
        }
    }
}
