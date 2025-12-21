package View;

import javax.swing.*;
import javax.swing.plaf.LayerUI;
import java.awt.*;

class FadeInLayerUI extends LayerUI<JComponent> {

	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private float alpha = 0f;
    private Timer timer;

    public void startFade(JLayer<?> layer) {
        timer = new Timer(16, e -> {
            alpha += 0.04f;
            if (alpha >= 1f) {
                alpha = 1f;
                timer.stop();
            }
            layer.repaint();
        });
        timer.start();
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, alpha));
        super.paint(g2, c);
        g2.dispose();
    }
}

