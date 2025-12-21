package View;

import javax.swing.*;
import java.awt.*;

public class GradientPaintPanel extends JPanel {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Color top;
    private final Color bottom;

    public GradientPaintPanel(Color top, Color bottom) {
        this.top = top;
        this.bottom = bottom;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int h = getHeight();
        Graphics2D g2 = (Graphics2D) g;
        g2.setPaint(new GradientPaint(
                0, 0, top,
                0, h, bottom
        ));
        g2.fillRect(0, 0, getWidth(), h);
    }
}
