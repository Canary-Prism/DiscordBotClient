package canaryprism.dbc.swing.server.serverlist;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.geom.Ellipse2D;
import java.util.stream.Stream;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;

import canaryprism.dbc.MediaCache;
import canaryprism.dbc.swing.text.TextView;

public class ServerListView extends JComponent {

    private final DiscordApi api;

    private final JPanel server_list;

    public ServerListView(DiscordApi api) {
        this.api = api;
        this.setLayout(new BorderLayout());
        server_list = new JPanel();
        server_list.setLayout(new BoxLayout(server_list, BoxLayout.Y_AXIS));


        reloadList();

        var scroll_pane = new JScrollPane(server_list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // scroll_pane.setSize(500, 500);
        scroll_pane.getVerticalScrollBar().setUnitIncrement(16);

        this.add(scroll_pane, BorderLayout.CENTER);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                scroll_pane.setSize(ServerListView.this.getSize());
                server_list.revalidate();
                server_list.repaint();
            }
        });

        api.addServerBecomesAvailableListener((e) -> {
            reloadList();
        });

        api.addServerBecomesUnavailableListener((e) -> {
            reloadList();
        });
    }

    public void reloadList() {
        server_list.removeAll();

        
        var servers = api.getServers();

        for (var server : servers) {
            var view = createServerItemView(server);
            server_list.add(view);
        }
    }

    protected JComponent createServerItemView(Server server) {
        var view = new JComponent() {

            private volatile Image icon;

            static final int pfp = 45;

            {
                Thread.ofVirtual().start(() -> {
                    mewo: try {
                        if (server.getIcon().isEmpty()) {
                            break mewo;
                        }
                        icon = MediaCache.getImage("server_icon", server, (e) -> e.getIcon().get().getUrl());
                        repaint();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }


            @Override
            public Dimension getPreferredSize() {
                return new Dimension(pfp, pfp);
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
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        // view.setLayout(new FlowLayout());
        panel.setBorder(new EmptyBorder(2, 2, 2, 2));

        view.setToolTipText(server.getName());
        panel.add(view);

        return panel;
    }

    // protected JComponent createRoleLabel(String text) {
    //     var role_label = new JLabel(String.format(
    //         """
    //         <html>
    //             <body style="font-size: 10px; font-weight: bold;">
    //                 %s
    //             </body>
    //         </html>
    //         """, 
    //         text
    //     ));
    //     var panel = new JPanel(new BorderLayout());

    //     panel.setBorder(new EmptyBorder(2, 2, 2, 2));

    //     panel.add(role_label, BorderLayout.CENTER);

    //     return panel;
    // }

    // @Override
    // public Dimension getPreferredSize() {
    //     var size = super.getPreferredSize();
    //     size.width = 35;
    //     return size;
    // }

    @Override
    public void doLayout() {
        // for (var e : message_list.getComponents()) {
        //     var view = (MessageView) e;
        //     view.setMaximumSize(this.getSize());
        // }

        // reloadList();
        server_list.doLayout();
    }
}
