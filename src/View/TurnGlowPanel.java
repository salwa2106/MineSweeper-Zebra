package View;

import javax.swing.*;
import java.awt.*;

public class TurnGlowPanel extends JPanel {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private float phase = 0f;
    private final Timer timer;
    private boolean active = false;

    public TurnGlowPanel() {
        setOpaque(false);

        timer = new Timer(40, e -> {
            if (active) {
                phase += 0.07f;
                repaint();
            }
        });
        timer.start();
    }

    public void setActive(boolean isActive) {
        active = isActive;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!active) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        float glow = 0.4f + (float) Math.sin(phase) * 0.4f;
        int alpha = (int) (160 * glow);

        Color icy = new Color(150, 255, 255, alpha);

        g2.setStroke(new BasicStroke(6f));
        g2.setColor(icy);
        g2.drawRoundRect(3, 3, getWidth() - 6, getHeight() - 6, 28, 28);

        g2.dispose();
    }
}
