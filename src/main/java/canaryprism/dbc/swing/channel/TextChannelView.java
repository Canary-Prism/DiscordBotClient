package canaryprism.dbc.swing.channel;

import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.javacord.api.entity.channel.ServerTextChannel;
import canaryprism.dbc.swing.channel.memberlist.MemberListView;

public class TextChannelView extends JComponent {
    public TextChannelView(ServerTextChannel channel) {
        this.setLayout(new BorderLayout());

        var content_panel = new JPanel(new BorderLayout());

        var message_list_view = new InteractableMessageListView(channel);

        content_panel.add(message_list_view, BorderLayout.CENTER);

        var member_list_view = new MemberListView(channel);

        content_panel.add(member_list_view, BorderLayout.EAST);

        this.add(content_panel, BorderLayout.CENTER);

        var channel_info = new JLabel(channel.getName() + " | " + channel.getTopic());

        this.add(channel_info, BorderLayout.NORTH);
    }
}
