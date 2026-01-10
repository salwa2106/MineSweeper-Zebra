package View;

import javax.swing.*;
import java.awt.*;

public class PillButton extends JButton {
    private final Color base;

    public PillButton(String text, Color base) {
        super(text);
        this.base = base;

        setFont(new Font("Georgia", Font.BOLD, 16));
        setForeground(Color.WHITE);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(190, 52));
        setBorder(new javax.swing.border.EmptyBorder(10, 18, 10, 18));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color bg = base;
        if (getModel().isPressed()) bg = bg.darker();
        else if (getModel().isRollover()) bg = bg.brighter();

        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

        g2.setColor(new Color(255, 255, 255, 70));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 18, 18);

        g2.dispose();
        super.paintComponent(g);
    }
}
