package View;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// 🎄 Animated Christmas lights overlay
public class LightsOverlay extends JPanel {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static class Light {
        int x, y, radius;
        Color base;
        float glowPhase;
    }

    private final List<Light> bulbs = new ArrayList<>();
    private final Timer timer;

    public LightsOverlay() {
        setOpaque(false);

        // generate bulbs across top
        for (int i = 0; i < 18; i++) {
            Light L = new Light();
            L.x = 80 + i * 85;
            L.y = 20;
            L.radius = 8;
            L.base = pickColor();
            L.glowPhase = (float) (Math.random() * Math.PI * 2);
            bulbs.add(L);
        }

        timer = new Timer(50, e -> {
            for (Light L : bulbs) {
                L.glowPhase += 0.1f;
            }
            repaint();
        });
        timer.start();
    }

    private Color pickColor() {
        Color[] c = {
                new Color(255, 75, 75),
                new Color(255, 180, 40),
                new Color(120, 200, 255),
                new Color(140, 255, 140)
        };
        return c[(int) (Math.random() * c.length)];
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Light L : bulbs) {

            float glow = 0.4f + (float) Math.sin(L.glowPhase) * 0.4f;
            Color glowColor = new Color(
                    (int) (L.base.getRed() * glow),
                    (int) (L.base.getGreen() * glow),
                    (int) (L.base.getBlue() * glow),
                    150
            );

            // Glow halo
            g2.setColor(glowColor);
            g2.fillOval(L.x - L.radius * 2, L.y - L.radius * 2, L.radius * 4, L.radius * 4);

            // Solid bulb
            g2.setColor(L.base);
            g2.fillOval(L.x - L.radius, L.y - L.radius, L.radius * 2, L.radius * 2);
        }
    }
}
