package View;

import javax.swing.*;
import java.awt.*;

/**
 * Simple panel that paints a scaled background image and lets you
 * add child components on top (your card layout root).
 */
public class BackgroundPanel extends JPanel {
	

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Image background;

    public BackgroundPanel(String imagePath) {
        setLayout(new BorderLayout());
        try {
            background = new ImageIcon(imagePath).getImage();
        } catch (Exception e) {
            System.err.println("BackgroundPanel: could not load image " + imagePath);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (background != null) {
            g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
