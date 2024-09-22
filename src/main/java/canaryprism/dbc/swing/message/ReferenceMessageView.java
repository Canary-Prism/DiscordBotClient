package canaryprism.dbc.swing.message;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Ellipse2D;
import javax.swing.JComponent;
import org.javacord.api.entity.message.Message;
import canaryprism.dbc.MediaCache;

public class ReferenceMessageView extends JComponent {

    private final Message message;


    private Image image;

    private boolean pings;

    public ReferenceMessageView(Message message) {
        this(message, true);
    }
    public ReferenceMessageView(Message message, boolean pings) {
        this.message = message;
        this.pings = pings;

        Thread.ofVirtual().start(() -> {
            // image = Main.getImage(message.getUserAuthor().get());
            image = MediaCache.getImage("author_pfp", message.getAuthor(), (e) -> e.getAvatar(64).getUrl());
            repaint();
        });
    }

    static final int pfp = 15;

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(super.getPreferredSize().width, pfp);
    }

    @Override
    protected void paintComponent(java.awt.Graphics g1) {
        super.paintComponent(g1);

        var g = (Graphics2D) g1;

        // g.scale(scale, scale);

        {
            var g2 = (Graphics2D) g.create();

            g2.clip(new Ellipse2D.Double(0, 0, pfp, pfp));
            g2.drawImage(image, 0, 0, pfp, pfp, this);
        }

        var x = pfp + 10;
        var y = this.getHeight() / 2 + g.getFontMetrics().getHeight() / 2 - g.getFontMetrics().getDescent();

        {
            var g2 = (Graphics2D) g.create();
            g2.drawString(message.getContent(), x, y);

            x += g2.getFontMetrics().stringWidth(message.getContent()) + 10;

            y += 20;
        }

    }
}
