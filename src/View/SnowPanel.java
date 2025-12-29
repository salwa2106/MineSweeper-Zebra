package View;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;
import javax.swing.Timer;

class SnowPanel extends JPanel {
	
    private static class Snowflake {
        float x, y, speed, drift;
        float size;
        float opacity;
        boolean sparkle;
    }

    private final java.util.List<Snowflake> flakes = new java.util.ArrayList<>();
    private final Timer timer;

    SnowPanel() {
        setOpaque(false);

        // create snowflakes
        for (int i = 0; i < 150; i++) {
            flakes.add(makeFlake());
        }

        timer = new Timer(33, e -> {
            updateFlakes();
            repaint();
        });
        timer.start();
    }

    private Snowflake makeFlake() {
        Snowflake f = new Snowflake();
        f.x = (float)(Math.random() * 2000);
        f.y = (float)(Math.random() * 1200);
        f.speed = 1.5f + (float)Math.random() * 2f;
        f.drift = -0.5f + (float)Math.random();
        f.size = 2f + (float)Math.random() * 3f;
        f.opacity = 0.4f + (float)Math.random() * 0.6f;
        f.sparkle = Math.random() < 0.05;
        return f;
    }

    private void updateFlakes() {
        for (Snowflake f : flakes) {
            f.y += f.speed;
            f.x += f.drift;

            if (f.sparkle)
                f.opacity = 0.6f + (float)Math.random()*0.4f;

            if (f.y > getHeight()) {
                // recycle at top
                f.x = (float)(Math.random() * getWidth());
                f.y = -10;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Snowflake f : flakes) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, f.opacity));
            g2.setColor(Color.white);
            g2.fillOval((int)f.x, (int)f.y, (int)f.size, (int)f.size);

            // sparkle
            if (f.sparkle) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, f.opacity * 0.7f));
                g2.drawOval((int)f.x-1, (int)f.y-1, (int)f.size+2, (int)f.size+2);
            }
        }
    }
}