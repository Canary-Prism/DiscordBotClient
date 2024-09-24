package canaryprism.dbc.swing.message;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;

public class MessageInputView extends JComponent {

    private final TextChannel channel;

    private Message replying_to;

    private final JPanel reference_message_panel;

    private final JPanel attachments_panel;

    protected final JTextArea input;

    // private final List<File> attachments = new ArrayList<>();

    private final List<AttachmentView> attachments = new ArrayList<>();

    public MessageInputView(TextChannel channel) {
        this.channel = channel;

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.reference_message_panel = new JPanel();
        reference_message_panel.setLayout(new BorderLayout());

        this.add(reference_message_panel);

        this.attachments_panel = new JPanel();
        attachments_panel.setLayout(new BoxLayout(attachments_panel, BoxLayout.X_AXIS));

        this.add(attachments_panel);

        this.input = new JTextArea();
        input.setLineWrap(true);
        input.setWrapStyleWord(true);

        input.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (!e.isShiftDown()) {
                        var text = input.getText();
                        if (text.isBlank() && attachments.isEmpty()) {
                            return;
                        }
                        sendMessage();
    
                        e.consume();
                    } else {
                        // uhh somehow add a newline because JTextArea doesn't do that by default for some reason hehe
                        input.replaceRange("\n", input.getSelectionStart(), input.getSelectionEnd());
                    }
                }
            }
        });

        this.add(input);
    }

    @Override
    public void doLayout() {
        super.doLayout();
    }


    public void addAttachment(File file) {
        var attachment = new AttachmentView(file);

        attachment.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextmenu(attachment, e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextmenu(attachment, e);
                }
            }
            
            private void showContextmenu(AttachmentView view, MouseEvent event) {
                var context_menu = new JPopupMenu();

                var reply_item = context_menu.add("Remove Attachment");
                reply_item.addActionListener((e) -> {
                    attachments.remove(view);
                    attachments_panel.remove(view);
                    if (attachments.isEmpty()) {
                        attachments_panel.removeAll();
                    }
                    attachments_panel.revalidate();
                    attachments_panel.repaint();
                });

                context_menu.show(view, event.getX(), event.getY());
            }
        });

        if (attachments.isEmpty()) {
            attachments_panel.add(new JLabel("Attachments: "));
        }

        attachment.setBorder(new LineBorder(getForeground(), 1));
        attachments.add(attachment);
        attachments_panel.add(attachment);

        this.revalidate();
    }

    public CompletableFuture<Message> sendMessage() {

        var builder = new MessageBuilder();

        builder.setContent(input.getText());

        attachments.forEach((e) -> {
            builder.addAttachment(e.file);
        });

        if (replying_to != null) {
            builder.replyTo(replying_to);
        }

        input.setText("");
        
        setReplyingTo(null);

        attachments.clear();
        attachments_panel.removeAll();

        return builder.send(channel);
    }

    public void setReplyingTo(Message reference_message) {
        this.replying_to = reference_message;
        if (reference_message != null) {
            reference_message_panel.removeAll();

            var panel = new JPanel();

            panel.add(new JLabel("Replying to: "));

            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));


            var view = new ReferenceMessageView(reference_message);
            view.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        showContextmenu(view, e);
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        showContextmenu(view, e);
                    }
                }

                private void showContextmenu(ReferenceMessageView view, MouseEvent event) {
                    var context_menu = new JPopupMenu();

                    var reply_item = context_menu.add("Remove Reply");
                    reply_item.addActionListener((e) -> {
                        setReplyingTo(null);
                    });

                    context_menu.show(view, event.getX(), event.getY());
                }
            });

            panel.add(view);

            var cancel_button = new JLabel("Cancel");

            cancel_button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            cancel_button.setBorder(new LineBorder(getForeground(), 1));

            cancel_button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    setReplyingTo(null);
                }
            });

            reference_message_panel.add(panel, BorderLayout.CENTER);

            reference_message_panel.add(cancel_button, BorderLayout.EAST);


            reference_message_panel.revalidate();
        } else {
            reference_message_panel.removeAll();
            reference_message_panel.revalidate();
        }
    }

    class AttachmentView extends JComponent {

        private final File file;

        public AttachmentView(File file) {
            this.file = file;

            this.setLayout(new FlowLayout());

            this.setBorder(new LineBorder(Color.red, 1));

            this.add(new JLabel(file.getName()));
        }
    }
}
