package canaryprism.dbc.swing.message;

import javax.swing.JComponent;
import javax.swing.JLabel;
import org.apache.commons.text.StringEscapeUtils;
import org.javacord.api.entity.message.Message;

import canaryprism.dbc.MediaCache;
import canaryprism.dbc.markdown.DiscordMarkdown;
import canaryprism.dbc.swing.text.TextView;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.Objects;

public class MessageView extends JComponent {

    private Message message;
    private Image image;

    private boolean edited;

    private final JLabel creation_label = new JLabel();

    private final ZonedDateTime creation;

    private volatile boolean shows_author = true;
    private final Duration author_collapse_timeout = Duration.ofMinutes(3);

    private final boolean is_reply;
    private ReferenceMessageView reply_view;

    private TextView text_pane = new TextView("");

    public MessageView(Message message) {
        this(message, null);
    }

    public Message getMessage() {
        return message;
    }

    public MessageView(Message message, Message previous) {
        this.setOpaque(true);

        this.message = Objects.requireNonNull(message);

        this.edited = message.getLastEditTimestamp().isPresent();

        this.is_reply = message.getReferencedMessage().isPresent();
        if (is_reply) {
            reply_view = new ReferenceMessageView(message.getReferencedMessage().get());
            reply_view.setLocation(pfp + 10 + 5, 0);
            this.add(reply_view);
        }
        
        // text_label.setVerticalAlignment(SwingConstants.TOP);

        this.creation = ZonedDateTime.ofInstant(message.getCreationTimestamp(), ZoneId.systemDefault());

        creation_label.setVerticalAlignment(JLabel.TOP);

        this.add(creation_label);
        updateCreationLabel();

        if (!is_reply) {
            if (previous != null) {
                if (message.getAuthor().equals(previous.getAuthor())
                    && previous.getCreationTimestamp().plus(author_collapse_timeout).isAfter(message.getCreationTimestamp())) {
                    this.collapse();
                }
            } else {
                message.getChannel().getMessagesBefore(1, message).thenAccept((e) -> {
                    var older = e.getLast();
                    if (message.getAuthor().equals(older.getAuthor())
                        && older.getCreationTimestamp().plus(author_collapse_timeout).isAfter(message.getCreationTimestamp()))
                        this.collapse();
        
                });
            }
        }

        // text_pane.setBorder(new LineBorder(Color.red, 1));

        this.add(text_pane);

        Thread.ofVirtual().start(() -> {
            // image = Main.getImage(message.getUserAuthor().get());
            image = MediaCache.getImage("author_pfp", message.getAuthor(), (e) -> e.getAvatar(64).getUrl());
            repaint();
        });

        message.addMessageEditListener((_) -> {
            edited = true;

            reloadText();
            repaint();
        });
        reloadText();

        this.addMouseListener(new MouseAdapter() {
            boolean hovers;
            private void update() {

                is_hover = hovers;

                MessageView.this.repaint();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                hovers = true;
                update();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovers = false;
                update();
            }
        });
    }

    private void reloadText() {
        var sb = new StringBuilder();
        var map = new HashMap<>();
        sb.append(String.format(
            "%s%s", 
            DiscordMarkdown.toXHTML(StringEscapeUtils.escapeXml11((message.getContent()))), 
            edited ? " <small>(edited)</small>" : ""
        ));
        int i = 0;
        for (var attachment : message.getAttachments()) {
            sb.append(String.format(
                "<atch key=\"%s\" />", 
                "a" + i
            ));

            map.put("a" + i, attachment);

            i++;
        }
        for (var embed : message.getEmbeds()) {
            sb.append(String.format(
                "<embed key=\"%s\" />", 
                "a" + i
            ));

            map.put("a" + i, embed);

            i++;
        }
        for (var emoji : message.getCustomEmojis()) {

            map.put(emoji.getIdAsString(), emoji);
        }
        text_pane.setText(sb.toString(), map);

        revalidate();
    }
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private static final DateTimeFormatter collapsed_formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM);
    private static final DateTimeFormatter full_formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.FULL);

    private void updateCreationLabel() {
        if (shows_author) {
            creation_label.setText(creation.format(formatter));
        } else {
            creation_label.setText(creation.format(collapsed_formatter));
        }
    }

    private void collapse() {
        shows_author = false;
        updateCreationLabel();
        this.revalidate();
    }

    private boolean is_hover;

    private boolean is_highlight;

    public void setHighlight(boolean is_highlight) {
        this.is_highlight = is_highlight;
        this.repaint();
    }

    @Override
    public void doLayout() {
        var size = getSize();
        var width = size.width - 5;
        var height = size.height - 3;

        // reloadText();
        
        var x = pfp + 10 + 5;
        var y = 8 + 3;

        creation_label.setFont(getFont().deriveFont(10f));
        if (!shows_author) {
            // this is stupid but it works
            var y1 = y - creation_label.getFontMetrics(creation_label.getFont()).getAscent() / 2;
            creation_label.setBounds(1, (int)y1, width - (int) x, height - (int) y1);
        }

        if (shows_author) {
            y += 20;
        }

        if (is_reply) {
            reply_view.setSize(width, reply_view.getPreferredSize().height);
            y += reply_view.getPreferredSize().height;
        }
    
        y -= text_pane.getFontMetrics(text_pane.getFont()).getAscent();
        text_pane.setBounds(
            (int)x, 
            (int)y, 
            width - (int)x, 
            height - (int)y
        );
        

        Dimension preferred_size = super.getPreferredSize();

        // preferred_size.width += x;
        // preferred_size.height += y;

        // preferredSize.width = MAX_WIDTH;
        // text_pane.setSize(getWidth() - pfp - 4, Integer.MAX_VALUE); // Temporarily
        // set height to max value to recalculate
        // // preferred height
        text_pane.doLayout();

        var mewo = text_pane.getPreferredSize().height + y;
        if (shows_author) {
            preferred_size.height = Math.max(mewo, pfp);
        } else {
            preferred_size.height = mewo;
        }

        // preferred_size.width += 5;
        preferred_size.height += 3;
        
        this.setPreferredSize(preferred_size);
        // System.out.println("Setting size to " + preferred_size);

        // if (this.getHeight() < preferred_size.height) {
        //     getParent().revalidate();
        // }

        // this.revalidate();
        // this.repaint();
        // this.setSize(preferred_size);

        // getParent().revalidate();
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

        creation_label.setVisible(is_hover || shows_author);


        // g.scale(scale, scale);
        if (is_highlight) {
            // System.out.println("drawing highlight");
            var g2 = (Graphics2D) g.create();
            g2.setColor(new Color(0x225500ff, true));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(0x5500ff));
            g2.fillRect(0, 0, 2, getHeight());
        } else if (message.getMentionedUsers().contains(message.getApi().getYourself())) {
            var g2 = (Graphics2D) g.create();
            g2.setColor(new Color(0x22dbdb7f, true));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(0xdbdb7f));
            g2.fillRect(0, 0, 2, getHeight());
            
        }

        var x = 5;
        var y = 5;

        if (is_reply) {
            y += reply_view.getPreferredSize().height;

            // uhh time to draw some line but with rounded corners, hopefully
            var g2 = (Graphics2D) g.create();
            g2.setColor(new Color(0xaaaaaa));

            g2.setStroke(new BasicStroke(2));

            // this is for the pfp side
            var x1 = pfp / 2 + 5;
            var y1 = y;

            // this is for the reply side
            var x2 = reply_view.getX() - 2;
            var y2 = reply_view.getY() + reply_view.getPreferredSize().height / 2;

            // this is for the turn
            var x3 = x1;
            var y3 = y2;
            final var radius = 3;

            g2.drawArc(x3, y3, radius * 2, radius * 2, 90, 90);

            g2.drawLine(x1, y1, x1, y3 + radius);

            g2.drawLine(x2, y2, x3 + radius, y2);

        }

        if (shows_author) {
            var g2 = (Graphics2D) g.create();

            g2.clip(new Ellipse2D.Double(x, y, pfp, pfp));
            g2.drawImage(image, x, y, pfp, pfp, this);
        }

        x += pfp + 10;
        y += 14;

        if (shows_author) {
            var g2 = (Graphics2D) g.create();
            g2.setColor(message.getAuthor().getRoleColor().orElse(this.getForeground()));
            g2.drawString(message.getAuthor().getDisplayName(), x, y);

            x += g2.getFontMetrics().stringWidth(message.getAuthor().getDisplayName()) + 10;

            creation_label.setBounds(x, y - creation_label.getFontMetrics(creation_label.getFont()).getAscent(), creation_label.getPreferredSize().width, creation_label.getPreferredSize().height);

            y += 20;
        }

    }
}
