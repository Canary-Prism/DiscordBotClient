package canaryprism.dbc.swing.channel.channellist;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ServerChannel;

import canaryprism.dbc.Main;
import canaryprism.dbc.save.channels.ChannelSaveSystem;
import canaryprism.dbc.save.channels.ChannelSaveSystem.Property;

public class ChannelCategoryView extends JComponent {

    private final ChannelCategory category;

    private final JPanel channels_panel;

    private boolean expanded;

    public ChannelCategoryView(ChannelCategory category) {
        this.category = category;

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        var header = new JLabel(category.getName());

        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                expanded = !expanded;
                // System.out.println("Expanded: " + expanded);
                updateChannelList();
                // SwingUtilities.invokeLater(ChannelCategoryView.this::updateChannelList);
            }
        });
        header.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        header.setHorizontalAlignment(SwingConstants.LEFT);

        // header.setBorder(new LineBorder(Color.blue, 1));
        this.add(header);
        
        this.channels_panel = new JPanel();
        // channels_panel.setBorder(new LineBorder(Color.blue, 1));
        channels_panel.setBorder(new EmptyBorder(0, 5, 0, 0));
        channels_panel.setLayout(new BoxLayout(channels_panel, BoxLayout.Y_AXIS));
        channels_panel.setAlignmentX(LEFT_ALIGNMENT);

        this.add(channels_panel);

        this.expanded = ChannelSaveSystem.get(category, Property.expanded);

        updateChannelList();

        if (Main.debug) {
            this.setBorder(new LineBorder(Main.hashColor(ChannelCategoryView.class)));
        }
    }

    private void updateChannelList() {
        channels_panel.removeAll();

        var channels = category.getChannels();
        
        ChannelSaveSystem.set(category, Property.expanded, this.expanded);
        
        if (!expanded) {
            // TODO: the thing where it shows the channel even when collapsed if it's got unread messages
            channels = List.of();
        }

        for (var channel : channels) {
            channels_panel.add(createChannelView(channel));
        }

        channels_panel.revalidate();
    }

    protected JComponent createChannelView(ServerChannel channel) {
        var view = new JLabel(channel.getName());

        var panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        view.setAlignmentX(LEFT_ALIGNMENT);
        // view.setBorder(new LineBorder(Color.blue, 1));
        
        if (Main.debug) 
            view.setBorder(new LineBorder(Main.hashColor(view.getClass())));

        
        // panel.setBorder(new LineBorder(Color.red, 1));
        panel.setBorder(new EmptyBorder(2, 2, 2, 2));
        panel.add(view);
        panel.setAlignmentX(LEFT_ALIGNMENT);
        
        return panel;
    }
}
