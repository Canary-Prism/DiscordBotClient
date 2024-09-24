package canaryprism.dbc.swing.message;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Ellipse2D;
import java.util.HashMap;

import javax.swing.JComponent;

import org.apache.commons.text.StringEscapeUtils;
import org.javacord.api.entity.message.Message;
import canaryprism.dbc.MediaCache;
import canaryprism.dbc.markdown.DiscordMarkdown;
import canaryprism.dbc.swing.text.TextView;

public class ReferenceMessageView extends JComponent {

    private final Message message;

    private TextView text_pane = new TextView("");

    private boolean edited;

    private Image image;

    private boolean pings;

    public ReferenceMessageView(Message message) {
        this(message, true);
    }
    public ReferenceMessageView(Message message, boolean pings) {
        this.message = message;
        this.pings = pings;

        this.edited = message.getLastEditTimestamp().isPresent();

        Thread.ofVirtual().start(() -> {
            // image = Main.getImage(message.getUserAuthor().get());
            image = MediaCache.getImage("author_pfp", message.getAuthor(), (e) -> e.getAvatar(64).getUrl());
            repaint();
        });

        message.addMessageEditListener((e) -> {
            edited = true;

            reloadText();
            repaint();
        });
        reloadText();

        this.add(text_pane);
    }

    private void reloadText() {
        var sb = new StringBuilder();
        var map = new HashMap<>();
        sb.append(String.format(
            "%s%s", 
            DiscordMarkdown.parseEmojis(DiscordMarkdown.toXHTML(StringEscapeUtils.escapeXml11((message.getContent()))), false), 
            edited ? " <small>(edited)</small>" : ""
        ));

        for (var emoji : message.getCustomEmojis()) {

            map.put(emoji.getIdAsString(), emoji);
        }
        text_pane.setText(sb.toString(), map);

        revalidate();
    }

    static final int pfp = 15;

    @Override
    public void doLayout() {
        super.doLayout();
        var x = pfp + 5;
        var y = this.getHeight() / 2 - this.getFontMetrics(this.getFont()).getHeight() / 2 - 7;

        text_pane.setBounds(x, y, this.getWidth() - x, this.getHeight() - y);
        text_pane.doLayout();
        text_pane.repaint();
    }

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

    }
}
