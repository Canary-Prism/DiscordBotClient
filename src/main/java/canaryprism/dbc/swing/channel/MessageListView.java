package canaryprism.dbc.swing.channel;

import java.awt.BorderLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.util.NoSuchElementException;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageSet;

import canaryprism.dbc.swing.message.MessageView;

public class MessageListView extends JComponent {

    protected TextChannel channel;

    protected JPanel message_list;

    public MessageListView(TextChannel channel) {
        this.channel = channel;
        this.setLayout(new BorderLayout());
        message_list = new JPanel();
        // message_list.setBorder(new LineBorder(Color.cyan, 1));
        message_list.setLayout(new BoxLayout(message_list, BoxLayout.Y_AXIS));
        // message_list = new JTable(channel.getMessages(100).join().stream().map((e) -> new Object[] {e}).toArray(Object[][]::new), new Object[] {"Message"}) {
        //     @Override
        //     public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        //         Component component = super.prepareRenderer(renderer, row, column);

        //         // Adjust row height based on component preferred size
        //         int rendererHeight = component.getPreferredSize().height;
        //         if (getRowHeight(row) != rendererHeight) {
        //             setRowHeight(row, rendererHeight);
        //         }

        //         return component;
        //     }
        // };
        // message_list.setLayout(new BoxLayout(message_list, BoxLayout.Y_AXIS));
        // channel.getMessages(100).join().stream().forEach((e) -> {
        //     var view = new MessageView(e);
        //     var panel = new JPanel(new BorderLayout());
        //     // view.setSize(new Dimension(500, 0));
        //     // view.setSize(500, 0);

        //     // view.setMaximumSize(TextChannelView.this.getSize());
        //     panel.setBorder(new EmptyBorder(2, 2, 2, 2));
        //     // view.addComponentListener(new java.awt.event.ComponentAdapter() {
        //     // @Override
        //     // public void componentResized(java.awt.event.ComponentEvent e) {
        //     // message_list.revalidate();
        //     // message_list.validate();
        //     // message_list.repaint();
        //     // }
        //     // });
        //     panel.add(view, BorderLayout.CENTER);

        //     message_list.add(panel);
        // });
        

        // message_list.setCellRenderer(new ListCellRenderer<>() {
        //     @Override
        //     public Component getListCellRendererComponent(JList<? extends Message> list, Message value, int index,
        //             boolean isSelected, boolean cellHasFocus) {
        //         var view = new MessageView(value);
        //         var panel = new JPanel(new BorderLayout());
        //         // view.setSize(new Dimension(500, 0));
        //         // view.setSize(500, 0);

        //         // view.setMaximumSize(TextChannelView.this.getSize());
        //         panel.setBorder(new EmptyBorder(2, 2, 2, 2));
        //         view.addComponentListener(new java.awt.event.ComponentAdapter() {
        //             @Override
        //             public void componentResized(java.awt.event.ComponentEvent e) {
        //                 message_list.revalidate();
        //                 message_list.validate();
        //                 message_list.repaint();
        //             }
        //         });
        //         panel.add(view, BorderLayout.CENTER);
        //         return panel;
        //     }
        // });
        // message_list.setFixedCellHeight(-1);
        // message_list.getColumn("Message").setCellRenderer(new TableCellRenderer() {
        //     @Override
        //     public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
        //             int row, int column) {
        //         var view = new MessageView((Message) value);
        //         var panel = new JPanel(new BorderLayout());
        //         // view.setSize(new Dimension(500, 0));
        //         // view.setSize(500, 0);

        //         // view.setMaximumSize(TextChannelView.this.getSize());
        //         panel.setBorder(new EmptyBorder(2, 2, 2, 2));
        //         view.addComponentListener(new java.awt.event.ComponentAdapter() {
        //             @Override
        //             public void componentResized(java.awt.event.ComponentEvent e) {
        //                 message_list.revalidate();
        //                 message_list.validate();
        //                 message_list.repaint();
        //             }
        //         });
        //         panel.add(view, BorderLayout.CENTER);
        //         return panel;
        //     }
        // });


        loadOlderMessages();

        // (new ListCellRenderer<>() {
        //     @Override
        //     public Component getListCellRendererComponent(JList<? extends Message> list, Message value, int index,
        //             boolean isSelected, boolean cellHasFocus) {
        //         var view = new MessageView(value);
        //         var panel = new JPanel(new BorderLayout());
        //         // view.setSize(new Dimension(500, 0));
        //         // view.setSize(500, 0);

        //         // view.setMaximumSize(TextChannelView.this.getSize());
        //         panel.setBorder(new EmptyBorder(2, 2, 2, 2));
        //         view.addComponentListener(new java.awt.event.ComponentAdapter() {
        //             @Override
        //             public void componentResized(java.awt.event.ComponentEvent e) {
        //                 message_list.revalidate();
        //                 message_list.validate();
        //                 message_list.repaint();
        //             }
        //         });
        //         panel.add(view, BorderLayout.CENTER);
        //         return panel;
        //     }
        // });

        // message_list.setSize(500, 500);

        var scroll_pane = new JScrollPane(message_list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // scroll_pane.setSize(500, 500);
        scroll_pane.getVerticalScrollBar().setUnitIncrement(16);

        // Ensure the layout is updated when the preferred size changes
        // message_list.addComponentListener(new java.awt.event.ComponentAdapter() {
        //     @Override
        //     public void componentResized(java.awt.event.ComponentEvent e) {
        //         message_list.revalidate();
        //         message_list.repaint();
        //     }
        // });

                // Infinite scrolling upwards
        scroll_pane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            private volatile boolean loading = false; // To avoid multiple loads at the same time

            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                JScrollBar scroll_bar = (JScrollBar) e.getSource();
                int value = scroll_bar.getValue();

                // Check if the user has scrolled near the top
                if (!loading && value <= 250 && !earliest_reached) {
                    loading = true;

                    // Save the current view position
                    int originalHeight = message_list.getHeight();

                    // Simulate loading new content at the top
                    Thread.ofVirtual().start(() -> {
                        var old_scroll = scroll_bar.getValue();

                        loadOlderMessages();

                        // Update the panel and adjust the scroll position to maintain the view
                        message_list.revalidate();
                        message_list.repaint();

                        SwingUtilities.invokeLater(() -> {
                            // Adjust scroll position to prevent "jumping"
                            int newHeight = message_list.getHeight();
                            scroll_bar.setValue(old_scroll + (newHeight - originalHeight));
    
                            loading = false;
                        });

                    });
                }
            }
        });

        channel.addMessageCreateListener((e) -> {

            var view = createMessageView(e.getMessage(), last_new);

            message_list.add(view);
            scroll_pane.revalidate();

        });

        message_list.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                var scroll_bar = scroll_pane.getVerticalScrollBar();

                // Scroll to the bottom if the user is already at the bottom

                var scroll = scroll_bar.getValue() + scroll_bar.getVisibleAmount() + message_list.getComponent(message_list.getComponentCount() - 1).getHeight() + 30;
                // System.out.println("Scroll: " + scroll);
                var maximum_scroll = scroll_bar.getMaximum();
                // System.out.println("Max Scroll: " + maximum_scroll);

                if (scroll >= maximum_scroll) {
                    SwingUtilities.invokeLater(() -> {
                        // why is it not scrolling to the bottom?
                        // i hate this

                        scroll_bar.setValue(message_list.getHeight());
                        // System.out.println("Scrolling to: " + message_list.getHeight());
                        scroll_bar.revalidate();
                        scroll_pane.repaint();
                        scroll_pane.revalidate();
                        message_list.revalidate();
                        message_list.repaint();
                    });
                }
            }
        });

        add(scroll_pane, BorderLayout.CENTER);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                scroll_pane.setSize(MessageListView.this.getSize());
                message_list.setSize(MessageListView.this.getWidth(), message_list.getHeight());
                message_list.revalidate();
                message_list.repaint();
            }
        });
    }

    private boolean earliest_reached = false;
    private Message earliest_loaded;

    private Message last_new;

    public void loadOlderMessages() {
        if (earliest_reached) {
            return;
        }
        MessageSet messages;
        if (earliest_loaded == null) {
            messages = channel.getMessages(100).join();
        } else {
            messages = channel.getMessagesBefore(100, earliest_loaded).join();
        }
        try {
            earliest_loaded = messages.getOldestMessage().get();
        } catch (NoSuchElementException e) {
            earliest_reached = true;
        }

        Message last = null;
        for (var e : messages.reversed()) {
            if (last == null) {
                last = e;
                continue;
            }

            var view = createMessageView(last, e);
            last = e;

            message_list.add(view, 0);
        }

        if (last != null) {
            var view = createMessageView(last, null);

            message_list.add(view, 0);
        }

        // // slightly unload the components at the bottom
        // for (int i = 0; i < message_list.getComponentCount() - 100; i++) {
        //     message_list.remove(100);
        // }
    }

    public void addMessage(JComponent view) {
        message_list.add(view);
    }
    public void addMessage(JComponent view, int index) {
        message_list.add(view, index);
    }

    public void removeMessage(JComponent view) {
        message_list.remove(view);
    }

    protected JComponent createMessageView(Message message, Message previous) {
        var view = new MessageView(message, previous);
        // var panel = new JPanel(new BorderLayout());

        // panel.setBorder(new EmptyBorder(3, 5, 3, 5));

        // panel.add(view, BorderLayout.CENTER);

        return view;
    }

    @Override
    public void doLayout() {
        // for (var e : message_list.getComponents()) {
        //     var view = (MessageView) e;
        //     view.setMaximumSize(this.getSize());
        // }
        message_list.doLayout();
    }
}
