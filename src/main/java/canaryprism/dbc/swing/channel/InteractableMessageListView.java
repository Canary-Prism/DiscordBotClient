package canaryprism.dbc.swing.channel;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;

import canaryprism.dbc.swing.message.MessageInputView;
import canaryprism.dbc.swing.message.MessageView;
import canaryprism.dbc.swing.message.PendingMessageView;

public class InteractableMessageListView extends JComponent {

    private TextChannel channel;
    private MessageListView message_list_view;

    private MessageInputView input_view;

    private MessageView replying_to;

    public InteractableMessageListView(TextChannel channel) {
        this.channel = channel;
        this.setLayout(new BorderLayout());

        message_list_view = new MessageListView(channel) {
            @Override
            protected JComponent createMessageView(Message message, Message previous) {
                var view = new MessageView(message, previous);
                // view.setBorder(new LineBorder(Color.red, 1));
                

                view.addMouseListener(new MouseAdapter() {

                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (e.isPopupTrigger()) {
                            select = true;
                            updateBackground();
                            showContextmenu(view, e).thenRun(() -> {
                                select = false;
                                updateBackground();
                            });
                        }
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if (e.isPopupTrigger()) {
                            select = true;
                            updateBackground();
                            showContextmenu(view, e).thenRun(() -> {
                                select = false;
                                updateBackground();
                            });
                        }
                    }

                    private boolean select;
                    private boolean hover;

                    private Color normal_background;

                    private void updateBackground() {
                        if (hover || select) {
                            normal_background = InteractableMessageListView.this.getBackground();
                            view.setBackground(normal_background.brighter());
                        } else {
                            view.setBackground(normal_background);
                        }
                        view.repaint();
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hover = true;
                        updateBackground();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hover = false;
                        updateBackground();
                    }
                });

                return view;
            }
        };

        this.add(message_list_view, BorderLayout.CENTER);

        this.input_view = new MessageInputView(channel) {
            @Override
            public void setReplyingTo(Message reference_message) {
                super.setReplyingTo(reference_message);

                if (replying_to != null && reference_message != replying_to.getMessage()) {
                    replying_to.setHighlight(false);
                }
            }

            @Override
            public CompletableFuture<Message> sendMessage() {
                var pending_message_view = new PendingMessageView(
                    input.getText(), 
                    channel.asServerChannel().map(ServerChannel::getServer),
                    channel.getApi().getYourself()
                );

                message_list_view.addMessage(pending_message_view);

                return super.sendMessage().thenApply((e) -> {
                    message_list_view.removeMessage(pending_message_view);
                    return e;
                });
            }
        };

        this.add(input_view, BorderLayout.SOUTH);

        this.setDropTarget(new DropTarget() {
            @SuppressWarnings("unchecked")
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> dropped_files = (List<File>) evt.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);
                    Thread.ofPlatform().start(() -> {
                        var list = dropped_files;
                        if (list.isEmpty()) {
                            return;
                        }
                        for (var e : list) {
                            input_view.addAttachment(e);
                        }
                    });
                    evt.dropComplete(true);
                } catch (Exception ex) {
                }
            }
        });
    }

    // private JPopupMenu context_menu;

    // protected void createContextMenu() {

    // }

    protected CompletableFuture<Void> showContextmenu(MessageView view, MouseEvent event) {
        var context_menu = new JPopupMenu();

        var message = view.getMessage();

        
        var reply_item = context_menu.add("Reply");
        reply_item.addActionListener((e) -> {
            if (replying_to != null) {
                replying_to.setHighlight(false);
            }
            replying_to = view;
            view.setHighlight(true);
            input_view.setReplyingTo(message);
        });
        
        if (message.canYouDelete()) {
            var delete_item = context_menu.add("Delete");
            delete_item.addActionListener((e) -> {
                message.delete();
            });
        }

        if (message.getAuthor().getId() == channel.getApi().getYourself().getId()) {
            var edit_item = context_menu.add("Edit");
            edit_item.addActionListener((e) -> {
                view.setEditing(true);
            });
        }
        
        var future = new CompletableFuture<Void>();

        context_menu.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                future.complete(null);
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {}
            
        });

        context_menu.show(view, event.getX(), event.getY());

        return future;
    }

    // protected CompletableFuture<Message> sendMessage(String text) {
    //     var pending_message_view = new PendingMessageView(text);

    //     message_list_view.addMessage(pending_message_view);

    //     return channel.sendMessage(text).thenApply((e) -> {
    //         message_list_view.removeMessage(pending_message_view);
    //         return e;
    //     });
    // }
}
