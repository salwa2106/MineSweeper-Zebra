package View;

import javax.swing.border.AbstractBorder;
import java.awt.*;

/**
 * Simple soft drop shadow border for UI cards.
 */
public class SoftDropShadowBorder extends AbstractBorder {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Color shadowColor;
    private final int shadowSize;
    private final float shadowOpacity;
    private final int arc;

    public SoftDropShadowBorder(Color shadowColor, int shadowSize, float shadowOpacity, int arc) {
        this.shadowColor = shadowColor;
        this.shadowSize = shadowSize;
        this.shadowOpacity = shadowOpacity;
        this.arc = arc;
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(shadowSize, shadowSize, shadowSize, shadowSize);
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setComposite(AlphaComposite.SrcOver.derive(shadowOpacity));
        g2.setColor(shadowColor);

        g2.fillRoundRect(
                x + shadowSize, y + shadowSize,
                width - shadowSize * 2, height - shadowSize * 2,
                arc, arc
        );

        g2.dispose();
    }
}
