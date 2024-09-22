package canaryprism.dbc.swing.channel.memberlist;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Ellipse2D;

import javax.swing.JComponent;

import org.javacord.api.entity.user.User;

import canaryprism.dbc.MediaCache;

public class MemberListMemberView extends JComponent {
    private final User user;


    private Image image;

    public MemberListMemberView(User user) {
        this.user = user;

        Thread.ofVirtual().start(() -> {
            // image = Main.getImage(message.getUserAuthor().get());

            image = MediaCache.getImage("member_list", user, (e) -> e.getAvatar(64).getUrl());
            repaint();
        });
    }

    static final int pfp = 35;

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(200, pfp);
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
            g2.drawString(user.getName(), x, y);

            x += g2.getFontMetrics().stringWidth(user.getName()) + 10;

            y += 20;
        }

    }
}
