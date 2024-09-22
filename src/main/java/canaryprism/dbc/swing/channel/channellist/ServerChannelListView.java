package canaryprism.dbc.swing.channel.channellist;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.util.ArrayList;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.RegularServerChannel;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.server.Server;

public class ServerChannelListView extends JComponent {

    protected Server server;

    protected JPanel channel_list;

    public ServerChannelListView(Server server) {
        this.server = server;
        this.setLayout(new BorderLayout());
        channel_list = new JPanel();
        channel_list.setLayout(new BoxLayout(channel_list, BoxLayout.Y_AXIS));


        reloadList();

        var scroll_pane = new JScrollPane(channel_list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // scroll_pane.setSize(500, 500);
        scroll_pane.getVerticalScrollBar().setUnitIncrement(16);

        this.add(scroll_pane, BorderLayout.CENTER);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                scroll_pane.setSize(ServerChannelListView.this.getSize());
                channel_list.revalidate();
                channel_list.repaint();
            }
        });
    }

    public void reloadList() {
        channel_list.removeAll();

        
        var categories = server.getChannelCategories();
        var ungrouped_channels = new ArrayList<ServerChannel>();
        ungrouped_channels.addAll(server.getChannels().stream().filter((e) -> e instanceof RegularServerChannel && !(e instanceof ChannelCategory)).toList());

        for (var category : categories) {
            for (var channel : category.getChannels()) {
                ungrouped_channels.remove(channel);
            }
        }

        for (var channel : ungrouped_channels) {
            channel_list.add(createChannelView(channel));
        }

        for (var category : categories) {
            channel_list.add(createChannelCategoryView(category));
        }

    }

    protected JComponent createChannelView(ServerChannel channel) {
        var view = new JLabel(channel.getName());

        var panel = new JPanel(new BorderLayout());

        panel.setBorder(new EmptyBorder(2, 2, 2, 2));
        panel.add(view, BorderLayout.CENTER);

        return panel;
    }

    protected JComponent createChannelCategoryView(ChannelCategory category) {
        var view = new ChannelCategoryView(category);


        // view.setBorder(new LineBorder(Color.red, 1));

        var panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setBorder(new EmptyBorder(0, 0, 5, 0));

        panel.add(view);


        // System.out.println("Preferred: " + view.getPreferredSize());

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

    @Override
    public Dimension getPreferredSize() {
        var size = super.getPreferredSize();
        size.width = 200;
        return size;
    }

    @Override
    public void doLayout() {
        // for (var e : message_list.getComponents()) {
        //     var view = (MessageView) e;
        //     view.setMaximumSize(this.getSize());
        // }

        reloadList();
        channel_list.doLayout();
    }
}
