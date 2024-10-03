package canaryprism.dbc.swing.text;

import java.awt.Image;

import javax.swing.JComponent;
import javax.swing.border.LineBorder;

import canaryprism.dbc.Main;

/**
 * A component that displays an image. this is like really simple and i only made this because i needed it for the
 * {@link TextView} class. and i didn't want to use a JLabel because i wanted to have more control over the rendering 
 * also i wanted anti-aliasing and i didn't want to have to set it up for every JLabel i made. so i made this.
 */
public class ImageView extends JComponent {

    private final Image image;

    public ImageView(Image image) {
        this.image = image;

        if (Main.debug) {
            this.setBorder(new LineBorder(Main.hashColor(ImageView.class)));
        }
    }

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);

        var g2d = (java.awt.Graphics2D) g;

        g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g2d.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), this);
    }
}
