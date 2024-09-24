package canaryprism.dbc.swing.server;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.RegularServerChannel;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextableRegularServerChannel;
import org.javacord.api.entity.server.Server;

import canaryprism.dbc.swing.channel.ServerTextableChannelView;
import canaryprism.dbc.swing.channel.channellist.ChannelCategoryView;
import canaryprism.dbc.swing.channel.channellist.ServerChannelListView;

public class ServerView extends JComponent {
    private final Server server;

    private final CardLayout channel_card = new CardLayout();
    private final JPanel channel_view_panel = new JPanel(channel_card);
    private final Set<String> loaded_channels = new HashSet<>();

    private Channel selected_channel;

    public ServerView(Server server) {
        this.server = server;

        this.setLayout(new BorderLayout());

        var channel_list = new ServerChannelListView(server) {
            @Override
            protected JComponent createChannelView(ServerChannel channel) {
                var view = new JLabel(channel.getName());

                var panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

                view.setAlignmentX(LEFT_ALIGNMENT);

                view.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                view.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        showChannel(channel);
                    }
                });

                panel.setBorder(new EmptyBorder(2, 2, 2, 2));
                panel.add(view);
                panel.setAlignmentX(LEFT_ALIGNMENT);

                return panel;
            }

            @Override
            protected JComponent createChannelCategoryView(ChannelCategory category) {
                var view = new ChannelCategoryView(category) {
                    @Override
                    protected JComponent createChannelView(ServerChannel channel) {
                        var view = new JLabel(channel.getName());

                        var panel = new JPanel();
                        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

                        view.setAlignmentX(LEFT_ALIGNMENT);

                        view.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                        view.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                showChannel(channel);
                            }
                        });

                        panel.setBorder(new EmptyBorder(2, 2, 2, 2));
                        panel.add(view);
                        panel.setAlignmentX(LEFT_ALIGNMENT);

                        return panel;
                    }
                };


                // view.setBorder(new LineBorder(Color.red, 1));

                var panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
                panel.setAlignmentX(LEFT_ALIGNMENT);
                panel.setBorder(new EmptyBorder(0, 0, 5, 0));

                panel.add(view);


                // System.out.println("Preferred: " + view.getPreferredSize());

                return panel;
            }
        };

        this.add(channel_list, BorderLayout.WEST);

        this.add(channel_view_panel, BorderLayout.CENTER);
    }

    public void showChannel(Channel channel) {
        var id = channel.getIdAsString();
        
        if (!loaded_channels.contains(id)) {
            channel_view_panel.add(createChannelView(channel), id);
            loaded_channels.add(id);
        }
        channel_card.show(channel_view_panel, id);
        selected_channel = channel;

        this.revalidate();
        this.repaint();
    }

    protected JComponent createChannelView(Channel c) {
        return switch (c) {
            case ServerTextChannel channel when channel.canYouSee() -> {
                var view = new ServerTextableChannelView(channel);
                var panel = new JPanel(new BorderLayout());
                panel.add(view);
                yield panel;
            }
            case ServerVoiceChannel channel when channel.canYouSee() -> {
                var view = new ServerTextableChannelView(channel);
                var panel = new JPanel(new BorderLayout());
                panel.add(view);
                yield panel;
            }
            case Channel channel when !channel.canYouSee() -> {
                yield new JLabel("You do not have permission to view this channel");
            }
            case null, default -> new JLabel("Unsupported Channel");
        };
    }
}
