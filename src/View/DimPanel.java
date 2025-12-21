package View;

import javax.swing.*;
import java.awt.*;

public class DimPanel extends JPanel {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private float alpha = 1f;

    public DimPanel() {
        setOpaque(false);
    }

    public void setDim(float value) {
        alpha = value;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (alpha >= 0.99f) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(new Color(0, 0, 0, (int) (140 * (1f - alpha))));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }
}
