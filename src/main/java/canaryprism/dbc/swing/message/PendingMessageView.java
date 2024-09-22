package canaryprism.dbc.swing.message;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Ellipse2D;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import javax.swing.JComponent;

import org.apache.commons.text.StringEscapeUtils;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import canaryprism.dbc.MediaCache;
import canaryprism.dbc.markdown.DiscordMarkdown;
import canaryprism.dbc.swing.text.TextView;

public class PendingMessageView extends JComponent {

    private String message_text;

    private Image image;

    private boolean edited;

    private final User author;
    private final Optional<Server> server;

    private volatile boolean shows_author = true;
    private static final Duration author_collapse_timeout = Duration.ofMinutes(3);

    private TextView text_pane = new TextView("");

    public PendingMessageView(String message, Optional<Server> server, User author) {
        this(message, author, server, Instant.now(), null);
    }

    public PendingMessageView(String message, User author, Optional<Server> server, Instant creation, Message previous) {

        
        // text_label.setVerticalAlignment(SwingConstants.TOP);

        this.message_text = message;
        this.author = author;
        this.server = server;



        if (previous != null) {
            if (author.getId() == previous.getAuthor().getId()
                && previous.getCreationTimestamp().plus(author_collapse_timeout).isAfter(creation)) {
                shows_author = false;
            }
        }

        text_pane.setForeground(Color.lightGray);;

        add(text_pane);

        Thread.ofVirtual().start(() -> {
            image = MediaCache.getImage("author_pfp", author, (e) -> e.getAvatar(64).getUrl());
            repaint();
        });

        reloadText();
    }

    private void reloadText() {
        var sb = new StringBuilder();
        sb.append(String.format(
            "%s%s", 
            DiscordMarkdown.toXHTML(StringEscapeUtils.escapeXml11((message_text))), 
            edited ? " <small>(edited)</small>" : ""
        ));
        // for (var attachment : message.getAttachments()) {
        //     sb.append(String.format(
        //         "<image escaped_url=\"%s\" escaped_filename=\"%s\" />", 
        //         StringEscapeUtils.escapeXml11(attachment.getUrl().toExternalForm()),
        //         StringEscapeUtils.escapeXml11(attachment.getFileName())
        //     ));
        // }
        text_pane.setText(sb.toString());

        revalidate();
    }


    @Override
    public void doLayout() {
        var size = getSize();
        var width = size.width - 5;

        var height = size.height - 3;

        reloadText();

        var x = pfp + 10 + 5;
        var y = 8 - text_pane.getFontMetrics(text_pane.getFont()).getAscent() + 5;
        if (shows_author) {
            y += 20;
        }
        text_pane.setBounds((int)x, (int)y, width - (int)x, height - (int)y);

        Dimension preferredSize = super.getPreferredSize();

        // preferredSize.width = MAX_WIDTH;
        // text_pane.setSize(getWidth() - pfp - 4, Integer.MAX_VALUE); // Temporarily
        // set height to max value to recalculate
        // // preferred height
        text_pane.revalidate();
        var mewo = text_pane.getPreferredSize().height + y;
        if (shows_author) {
            preferredSize.height = Math.max(mewo, pfp);
        } else {
            preferredSize.height = mewo;
        }

        height += 3;
        
        this.setPreferredSize(preferredSize);
        this.setSize(preferredSize);
        getParent().doLayout();
    }

    static final int pfp = 35;

    static final int MAX_WIDTH = 300;

    // @Override
    // public Dimension getPreferredSize() {
    //     // return new Dimension(size.width + pfp + 10, size.height + 10);

    //     Dimension preferredSize = super.getPreferredSize();
    //     if (true) {
    //         // preferredSize.width = MAX_WIDTH;
    //         // text_pane.setSize(getWidth() - pfp - 4, Integer.MAX_VALUE); // Temporarily set height to max value to recalculate
    //         //                                                 // preferred height
    //         // text_pane.revalidate();
    //         var mewo = text_pane.getPreferredSize().height;
    //         preferredSize.height = Math.max(mewo, pfp);
    //     }
    //     return preferredSize;
    // }

    @Override
    protected void paintComponent(java.awt.Graphics g1) {
        super.paintComponent(g1);

        var g = (Graphics2D) g1;


        // g.scale(scale, scale);

        var x = 5;
        var y = 5;

        if (shows_author) {
            var g2 = (Graphics2D) g.create();

            g2.clip(new Ellipse2D.Double(x, y, pfp, pfp));
            g2.drawImage(image, x, y, pfp, pfp, this);
        }

        x += pfp + 10;
        y += 14;

        if (shows_author) {
            var g2 = (Graphics2D) g.create();
            if (server.isPresent()) {
                g2.setColor(author.getRoleColor(server.get()).orElse(getForeground()));
                g2.drawString(author.getDisplayName(server.get()), x, y);
            } else {
                g2.drawString(author.getName(), x, y);
            }

            y += 20;
        }

    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        var g2 = (Graphics2D) g.create();

        g2.setColor(new Color((this.getBackground().getRGB() & 0x00FFFFFF) | 0x99000000));

        g2.drawRect(0, 0, this.getWidth(), this.getHeight());
    }
}
