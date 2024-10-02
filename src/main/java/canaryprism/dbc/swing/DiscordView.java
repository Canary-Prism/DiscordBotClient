package canaryprism.dbc.swing;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;

import canaryprism.dbc.swing.server.ServerView;
import canaryprism.dbc.swing.server.serverlist.ServerListView;

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
                var view = super.createServerItemView(server);

                // view.setBorder(new LineBorder(Color.red, 1));

                view.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                view.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        showServer(server);
                    }
                });

                return view;
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
