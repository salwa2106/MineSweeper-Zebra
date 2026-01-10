package View;

import Model.SysData;

import javax.swing.*;
import java.awt.*;

public abstract class FullScreenFrostedFrame extends JFrame {

    protected static final Color TEXT   = new Color(225, 245, 240);
    protected static final Color BORDER = new Color(160, 255, 255, 130);

    protected FullScreenFrostedFrame(String title) {
        super(title);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        setLocationRelativeTo(null);

        // ESC closes
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    protected JComponent wrapBackground(JComponent center) {
        JPanel bg = new JPanel(new GridBagLayout());
        bg.setBackground(new Color(10, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE; // נשאר בגודל preferredSize של ה-card

        bg.add(center, gbc);
        return bg;
    }

    protected JPanel frostedCard(int maxW, int maxH, Insets padding) {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(20, 35, 35, 190));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 36, 36);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(
                padding.top, padding.left, padding.bottom, padding.right
        ));

        // ✅ THIS is what GridBag respects:
        p.setPreferredSize(new Dimension(maxW, maxH));

        // optional:
        p.setMaximumSize(new Dimension(maxW, maxH));

        return p;
    }


    protected Font uiFont(int style, int size) {
        boolean he = (SysData.getI18n() != null && SysData.getI18n().isHebrew());
        return new Font(he ? "SansSerif" : "Georgia", style, size);
    }

    protected JLabel titleLabel(String text) {
        JLabel title = new JLabel(text, SwingConstants.CENTER);
        title.setFont(uiFont(Font.BOLD, 28));
        title.setForeground(new Color(190, 255, 220));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        return title;
    }

    protected static String safeT(String key, String fallback) {
        try {
            String v = SysData.getI18n().t(key);
            if (v == null) return fallback;
            v = v.trim();
            if (v.isBlank()) return fallback;
            if (v.startsWith("!") && v.endsWith("!")) return fallback; // missing key pattern
            if (v.equalsIgnoreCase(key)) return fallback;
            return v;
        } catch (Exception e) {
            return fallback;
        }
    }

    /** Call at the end of your concrete frame constructor */
    /** Call at the end of your concrete frame constructor */
    protected void finalizeUI() {
        try {
            SysData.applyGlobalFont(this);
        } catch (Exception ignored) {}

        // IMPORTANT: DO NOT pack() in full-screen frames
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setVisible(true);

        // Make sure layout recalculates after showing
        revalidate();
        repaint();
    }

}
