package canaryprism.dbc.swing;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.nio.channels.Channel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;

import canaryprism.dbc.MediaCache;
import canaryprism.dbc.swing.server.ServerView;
import canaryprism.dbc.swing.server.serverlist.ServerListView;
import canaryprism.dbc.swing.text.TextView;

public class DiscordView extends JComponent {

    private final DiscordApi api;

    private final CardLayout server_card = new CardLayout();
    private final JPanel server_view_panel = new JPanel(server_card);
    private final Map<String, ServerView> loaded_servers = new HashMap<>();

    private Server selected_server;

    public DiscordView(DiscordApi api) {
        this.api = api;

        this.setLayout(new BorderLayout());

        var channel_list = new ServerListView(api) {
            @Override
            protected JComponent createServerItemView(Server server) {
                var view = new JComponent() {

                    private Image icon;

                    static final int pfp = 45;

                    {
                        // Thread.ofVirtual().start(() -> {
                            mewo: try {
                                if (server.getIcon().isEmpty()) {
                                    break mewo;
                                }
                                icon = MediaCache.getImage("server_icon", server, (e) -> e.getIcon().get().getUrl());
                                repaint();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        // });
                    }


                    @Override
                    public Dimension getPreferredSize() {
                        return new Dimension(pfp, pfp);
                    }

                    @Override
                    public Dimension getMaximumSize() {
                        return getPreferredSize();
                    }

                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        {
                            var g2 = (Graphics2D) g.create();
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                            g2.clip(new Ellipse2D.Double(0, 0, pfp, pfp));

                            if (icon != null) {
                                g2.drawImage(icon, 0, 0, pfp, pfp, this);
                            } else {
                                g2.setColor(java.awt.Color.GRAY);
                                g2.fill(new Ellipse2D.Double(0, 0, pfp, pfp));

                                g2.setColor(getForeground());

                                // draw the initials of the server name, this'll be a bit tricky
                                var name = server.getName();
                                var sb = new StringBuilder();
                                Stream.of(TextView.whitespace.split(name)) // just steal the pattern from TextView, it's fine
                                    .map((e) -> e.charAt(0))
                                    .forEach(sb::append);

                                var initials = sb.toString();

                                var string_width = g2.getFontMetrics().stringWidth(initials) + 5;

                                var scale = pfp / (float) string_width;

                                g2.scale(scale, scale);

                                g2.drawString(initials, 2.5f, (string_width) / 2f + g2.getFontMetrics().getHeight() / 2f - g2.getFontMetrics().getDescent());
                            }
                        }
                    }
                };

                var panel = new JPanel();

                // panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

                // view.setLayout(new FlowLayout());
                panel.setBorder(new EmptyBorder(2, 2, 2, 2));

                view.setToolTipText(server.getName());

                // view.setBorder(new LineBorder(Color.red, 1));

                view.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                view.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        showServer(server);
                    }
                });
                
                panel.add(view);

                return panel;
            }
        };

        this.add(channel_list, BorderLayout.WEST);

        this.add(server_view_panel, BorderLayout.CENTER);
    }

    public void showServer(Server server) {
        var id = server.getIdAsString();

        if (!loaded_servers.containsKey(id)) {
            var view = createServerView(server);
            server_view_panel.add(view, id);
            loaded_servers.put(id, view);
        }
        server_card.show(server_view_panel, id);
        selected_server = server;

        this.revalidate();
        this.repaint();
    }

    public Server getSelectedServer() {
        return selected_server;
    }

    public ServerView getSelectedServerView() {
        return (ServerView) loaded_servers.get(selected_server.getIdAsString());
    }

    protected ServerView createServerView(Server s) {
        return new ServerView(s);
    }
}
