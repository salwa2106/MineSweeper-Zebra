package View;

import javax.swing.*;
import javax.swing.plaf.LayerUI;
import java.awt.*;

public class SlideFadeLayerUI extends LayerUI<JComponent> {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private float alpha = 0f;   // 0 → 1
    private float offset = 40f; // slide amount in px
    private Timer timer;

    public void start(JLayer<?> layer) {
        timer = new Timer(16, e -> {
            alpha += 0.04f;
            offset -= 2.5f;

            
            if (alpha >= 1f) alpha = 1f;
            if (offset <= 0f) offset = 0f;

            if (alpha == 1f && offset == 0f) timer.stop();
            layer.repaint();
        });
        timer.start();
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        g2.translate(0, offset);

        super.paint(g2, c);

        g2.dispose();
    }
}
