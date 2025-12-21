package View;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TileButton extends JButton {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Image overlay;                 // overlay icon image
    private boolean revealedVisual = false;
    private float revealAlpha = 0f;         // fade animation %
    private boolean fading = false;
    private List<Point> snowOnTile = new ArrayList<>();

    public TileButton(int size) {
        setPreferredSize(new Dimension(size, size));
        setMinimumSize(new Dimension(size, size));
        setMaximumSize(new Dimension(size, size));

        setMargin(new Insets(0, 0, 0, 0));
        setContentAreaFilled(false);
        setOpaque(false);
        setBorder(null);
        setFocusPainted(false);

        setFont(new Font("Georgia", Font.BOLD, Math.max(12, size / 3)));
        setForeground(Color.WHITE);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public void setOverlayIcon(ImageIcon icon) {
        overlay = (icon == null) ? null : icon.getImage();
        repaint();
    }

    public void setRevealedVisual(boolean r) {
        if (r && !revealedVisual) {
            revealedVisual = true;

            // start fade animation
            fading = true;
            revealAlpha = 0f;

            // add 3–5 snowflakes on tile
            snowOnTile.clear();
            int count = 3 + (int) (Math.random() * 3);
            for (int i = 0; i < count; i++) {
                int x = (int) (Math.random() * getWidth());
                int y = (int) (Math.random() * getHeight());
                snowOnTile.add(new Point(x, y));
            }

            // animation timer
            Timer t = new Timer(20, e -> {
                revealAlpha += 0.08f;
                if (revealAlpha >= 1f) {
                    revealAlpha = 1f;
                    fading = false;
                    ((Timer) e.getSource()).stop();
                }
                repaint();
            });
            t.start();
        } else {
            revealedVisual = r;
        }
    }

    public boolean isRevealedVisual() {
        return revealedVisual;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // ---------------------------------
        // 1) HIDDEN TILE
        // ---------------------------------
        if (!revealedVisual) {
            super.paintComponent(g2);

            if (overlay != null) {
                int iconSize = 16;
                int x = (getWidth() - iconSize) / 2;
                int y = (getHeight() - iconSize) / 2;
                g2.drawImage(overlay, x, y, iconSize, iconSize, this);
            }

            g2.dispose();
            return;
        }

        // ---------------------------------
        // 2) REVEALED TILE (fade animation)
        // ---------------------------------
        float alpha = fading ? revealAlpha : 1f;
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        super.paintComponent(g2);

        if (overlay != null) {
            int iconSize = 16;
            int x = (getWidth() - iconSize) / 2;
            int y = (getHeight() - iconSize) / 2;
            g2.drawImage(overlay, x, y, iconSize, iconSize, this);
        }

        // ---------------------------------
        // 3) GLOW OUTLINE
        // ---------------------------------
        g2.setComposite(AlphaComposite.SrcOver);
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(0, 255, 255, 120));
        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 6, 6);

        // ---------------------------------
        // 4) SNOW ACCUMULATION
        // ---------------------------------
        g2.setColor(new Color(255, 255, 255, 230));
        for (Point p : snowOnTile) {
            g2.fillOval(p.x, p.y, 3, 3);
        }

        g2.dispose();
    }
}
