package canaryprism.dbc.swing.text.attachment;

import java.awt.Image;

import javax.swing.JPopupMenu;

import canaryprism.dbc.swing.text.TextView;

public class ImageAttachmentView extends AttachmentView {
    private final Image image;
    private final String filename;

    public ImageAttachmentView(String filename, Image image) {
        this.image = image;
        this.filename = filename;
    }

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);

        var g2d = (java.awt.Graphics2D) g;

        g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g2d.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), this);
    }

    @Override
    protected void initContextMenu(JPopupMenu context_menu) {
        var copy_item = context_menu.add("Copy Image");
        copy_item.addActionListener((e) -> {
            TextView.copyToClipboard(image);
        });

        var save_item = context_menu.add("Save Image");
        save_item.addActionListener((e) -> {
            TextView.savePrompt(image, filename);
        });

    }
}
