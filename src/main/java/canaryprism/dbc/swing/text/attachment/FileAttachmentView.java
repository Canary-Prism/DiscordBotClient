package canaryprism.dbc.swing.text.attachment;

import java.awt.Color;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.border.LineBorder;

import org.apache.commons.text.StringEscapeUtils;

import canaryprism.dbc.swing.text.TextView;

public class FileAttachmentView extends AttachmentView {

    private static final JFileChooser fc = new JFileChooser();
    static {
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setMultiSelectionEnabled(false);
    }

    private final String filename;
    private final URL url;

    private final TextView text_view;

    public FileAttachmentView(String filename, URL url) {
        this.filename = filename;
        this.url = url;

        this.setBorder(new LineBorder(this.getForeground()));

        this.text_view = new TextView(String.format(
            "<link url=\"%s\">%s</link>", 
            StringEscapeUtils.escapeXml11(url.toString()),
            StringEscapeUtils.escapeXml11(filename)
        ));

        this.add(text_view);
    }

    @Override
    public void doLayout() {
        var x = 5;
        var y = 5;
        var wp = 5;
        var hp = 5;
        this.text_view.setBounds(x, y, this.getWidth() - wp - x, this.getHeight() - hp - y);
        text_view.doLayout();

        var size = text_view.getPreferredSize();

        size.width += x + wp;
        size.height += y + hp;

        this.setPreferredSize(size);
    }

    @Override
    protected void initContextMenu(JPopupMenu context_menu) {
        var copy_item = context_menu.add("Copy Link");
        copy_item.addActionListener((e) -> {
            var transferrable = new StringSelection(url.toString());
            this.getToolkit().getSystemClipboard().setContents(transferrable, transferrable);
        });

        var save_item = context_menu.add("Save File");
        save_item.setForeground(Color.yellow);

        save_item.addActionListener((e) -> {
            var extension = filename.substring(switch (filename.lastIndexOf('.')) {
                case -1 -> filename.length();
                default -> filename.lastIndexOf('.');
            });

            synchronized (fc) {
                fc.setFileFilter(new javax.swing.filechooser.FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.isDirectory() || f.getName().endsWith(extension);
                    }
    
                    @Override
                    public String getDescription() {
                        return "Image Files";
                    }
                });
                if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    Thread.ofVirtual().start(() -> {
                        try {
                            var path = fc.getSelectedFile().toPath();
                            if (!path.toString().endsWith(extension)) {
                                path = path.resolveSibling(path.getFileName() + extension);
                            }
    
                            try (var in = url.openStream()) {
                                Files.copy(in, path);
                            }
                        } catch (IOException n) {
                            JOptionPane.showMessageDialog(null, "Failed to save file " + filename + " : " + n.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            n.printStackTrace();
                        }
                    });
                }
            }
        });
    }
}
