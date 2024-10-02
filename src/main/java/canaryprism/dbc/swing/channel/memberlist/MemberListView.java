package canaryprism.dbc.swing.channel.memberlist;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.user.UserStatus;

import canaryprism.dbc.Main;

public class MemberListView extends JComponent {

    protected Channel channel;

    protected JPanel member_list;

    public MemberListView(Channel channel) {
        this.channel = channel;
        this.setLayout(new BorderLayout());
        member_list = new JPanel();
        member_list.setLayout(new BoxLayout(member_list, BoxLayout.Y_AXIS));


        reloadList();

        var wrapping_panel = new JPanel(new BorderLayout());
        wrapping_panel.add(member_list, BorderLayout.NORTH);

        var scroll_pane = new JScrollPane(wrapping_panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // scroll_pane.setSize(500, 500);
        scroll_pane.getVerticalScrollBar().setUnitIncrement(16);

        add(scroll_pane, BorderLayout.CENTER);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                scroll_pane.setSize(MemberListView.this.getSize());
                member_list.revalidate();
                member_list.repaint();
            }
        });

        if (channel instanceof ServerChannel server_channel) {
            server_channel.getServer().addRoleChangeHoistListener((e) -> {
                revalidate();
                repaint();
            });
            server_channel.getServer().addRoleChangePositionListener((e) -> {
                revalidate();
                repaint();
            });
            server_channel.getServer().addRoleChangeColorListener((e) -> {
                revalidate();
                repaint();
            });
            server_channel.getServer().addServerMemberJoinListener((e) -> {
                revalidate();
                repaint();
            });
            server_channel.getServer().addServerMemberLeaveListener((e) -> {
                revalidate();
                repaint();
            });
            server_channel.getServer().addUserChangeStatusListener((e) -> {
                revalidate();
                repaint();
            });
            server_channel.getServer().addUserRoleAddListener((e) -> {
                revalidate();
                repaint();
            });
            server_channel.getServer().addUserRoleRemoveListener((e) -> {
                revalidate();
                repaint();
            });
        }

        if (Main.debug)
            this.setBorder(new LineBorder(Main.hashColor(MemberListView.class), 1));
    }

    private Message last_new;

    public void reloadList() {
        member_list.removeAll();

        switch (channel) {
            case PrivateChannel private_channel -> {
                private_channel.getRecipient().get();
                
            }
            case ServerChannel server_channel -> {
                var server = server_channel.getServer();

                var members = server.getMembers().stream().filter((e) -> channel.canSee(e)).toList();

                var roled_members = new HashMap<Role, ArrayList<User>>();

                var online_members = new ArrayList<User>();
                var offline_members = new ArrayList<User>();

                for (var e : members) {
                    if (e.getStatus() != UserStatus.OFFLINE) {
                        e.getRoles(server).stream()
                            .filter(Role::isDisplayedSeparately)
                            .sorted((a, b) -> b.getPosition() - a.getPosition())
                            .findFirst()
                            .ifPresentOrElse((role) -> {
                                var list = roled_members.computeIfAbsent(role, (k) -> new ArrayList<>());
                                list.add(e);
                            }, () -> {
                                online_members.add(e);
                            });
                    } else {
                        offline_members.add(e);
                    }
                }

                var sorted_roles = roled_members.keySet()
                    .stream()
                    .sorted((a, b) -> b.getPosition() - a.getPosition())
                    .toList();
                    // .sorted((a, b) -> a.user.getDisplayName(server).compareTo(b.user.getDisplayName(server)))
                    // .sorted((a, b) -> b.role.getPosition() - a.role.getPosition())
                    // .toList();


                for (var role : sorted_roles) {
                    var role_members = roled_members.get(role);
                    member_list.add(createRoleLabel(role.getName() + " - " + role_members.size()));

                    for (var member : role_members) {
                        member_list.add(createMemberView(member, server));
                    }
                }

                if (!online_members.isEmpty()) {
                    member_list.add(createRoleLabel("Online - " + online_members.size()));
    
                    online_members.stream()
                        .sorted((a, b) -> a.getDisplayName(server).compareTo(b.getDisplayName(server)))
                        .forEach((e) -> {
                            member_list.add(createMemberView(e, server));
                        });
                }

                if (!offline_members.isEmpty()) {
                    member_list.add(createRoleLabel("Offline - " + offline_members.size()));
    
                    offline_members.stream()
                        .sorted((a, b) -> a.getDisplayName(server).compareTo(b.getDisplayName(server)))
                        .forEach((e) -> {
                            member_list.add(createMemberView(e, server));
                        });
                }
            }
            case null, default -> {}
        }
    }

    protected JComponent createMemberView(User user, Server server) {
        var view = new MemberListServerMemberView(user, server);

        var panel = new JPanel(new BorderLayout());
        // panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        if (Main.debug)
            view.setBorder(new LineBorder(Main.hashColor(view.getClass()), 1));

        panel.setBorder(new EmptyBorder(3, 5, 3, 5));

        panel.add(view, BorderLayout.CENTER);

        return panel;
    }

    protected JComponent createRoleLabel(String text) {
        var role_label = new JLabel(String.format(
            """
            <html>
                <body style="font-size: 10px; font-weight: bold;">
                    %s
                </body>
            </html>
            """, 
            text
        ));
        var panel = new JPanel(new BorderLayout());

        panel.setBorder(new EmptyBorder(2, 2, 2, 2));

        panel.add(role_label, BorderLayout.CENTER);

        return panel;
    }

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
        member_list.doLayout();
    }
}
