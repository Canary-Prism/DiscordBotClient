package canaryprism.dbc.swing.text;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ComponentAdapter;
import java.awt.event.MouseAdapter;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.text.StringEscapeUtils;
import org.javacord.api.entity.Attachment;
import org.javacord.api.entity.emoji.CustomEmoji;
import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import canaryprism.dbc.MediaCache;

public class TextView extends JComponent {

    private String text;

    private Map<? super String, ? extends Object> external_data = new HashMap<>();

    public TextView() {
        this("");
    }

    public TextView(String text) {
        this.setLayout(null);
        setText(text);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                doLayout();
            }
        });

        // this.setBorder(new LineBorder(Color.blue, 1));
    }

    public void setText(String text) {
        setText(text, Map.of());
    }

    public void setText(String text, Map<? super String, ? extends Object> data) {
        this.text = text;
        this.external_data = data;

        media_cache.clear();

        try {
            this.xml = parseXML(String.format("<root>%s</root>", text));

            last_width = 0;
            this.revalidate();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            System.err.println("Failed to parse XML for: " + text);
            e.printStackTrace();
        }
    }

    private Document xml;

    private static long timeout = 0;

    // private volatile CompletableFuture<Void> future;

    private volatile int last_width;

    @Override
    public void doLayout() {
        this.doLayout(false);
    }

    public void doLayout(boolean force) {
        // System.out.println("dolayout " + System.currentTimeMillis());
        super.doLayout();


        if (last_width == this.getWidth() && !force) {
            // this.setPreferredSize(new Dimension(getWidth(), (int) (y + yinc)));
            // this.revalidate();
            // this.getParent().revalidate(); // i do'nt know anymore i' m just trying to make it work
            // this.repaint();
            // this.getParent().doLayout();
            // System.out.println("please update");
            return;
        }
        last_width = this.getWidth();

        // if (System.currentTimeMillis() < timeout) {
        //     return;
        // }
        // timeout = System.currentTimeMillis() + 10;

        // if (future != null) {
        //     if (future.isDone()) {
        //         future = null;
        //     }
        //     return;
        // }

        // future = new CompletableFuture<>();
        // future.thenRun(this::removeAll);

        this.removeAll();
        
        try {
            var root = xml;

            if (root == null) {
                return;
            }

            x = 0; y = 0;
            // label_cache_index = 0;
            strikethrough = underline = false;

            // lines.clear();

            // Thread.ofVirtual().start(() -> {
                // System.out.println("dolayout " + System.currentTimeMillis());
                parse(root);

                var height_y = y;
                if (x != 0) {
                    height_y += yinc;
                }

                this.setPreferredSize(new Dimension(getWidth(), (int)(height_y)));

                // this.invalidate();
                // this.revalidate();
                // this.getParent().revalidate();
                
                // setPreferredSize(new Dimension(getWidth(), (int)(y + yinc)));
                // setMinimumSize(new Dimension(getWidth(), (int)(y + yinc)));
                // getParent().revalidate();
                // this.repaint();

                // SwingUtilities.invokeLater(() -> {
                //     future.complete(null);
                //     this.repaint();
                // });
            // });
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    private volatile double height_y;

    // @Override
    // public Dimension getPreferredSize() {
    //     // if (height_y == 0) // don't look at me, i don't know why this is necessary
    //     //     doLayout();
    //     return new Dimension(getWidth(), (int) height_y);
    // }

    
    public static final Pattern whitespace = Pattern.compile("\\s+");
    public static final Pattern linebreak = Pattern.compile("\\n");

    private double x, y;
    private double yinc;
    private boolean strikethrough, underline, small, code;

    private final Map<String, URL> media_cache = new HashMap<>();

    private URL getMediaURL(String url) {
        return media_cache.computeIfAbsent(url, (e) -> {
            try {
                return new URI(e).toURL();
            } catch (MalformedURLException | URISyntaxException ex) {
                System.err.println("Failed to parse URL: " + e);
                ex.printStackTrace();
                return null;
            }
        });
    }

    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
        this.doLayout(true);
        last_width = 0;
        this.revalidate();
        return super.imageUpdate(img, infoflags, x, y, w, h);
    }

    // private JLabel cacheJLabel() {
    //     if (label_cache_index < label_cache.size()) {
    //         return label_cache.get(label_cache_index++);
    //     }
    //     var label = new JLabel();
    //     label_cache.add(label);
    //     label_cache_index++;
    //     return label;
    // }

    // private int label_cache_index = 0;

    // private final ArrayList<JLabel> label_cache = new ArrayList<>();
    
    private void parse(Node node) {
        var children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            var child = children.item(i);
            switch (child.getNodeType()) {
                case Node.TEXT_NODE -> {
                    var font = this.getFont();

                    var text = StringEscapeUtils.unescapeXml(child.getTextContent());

                    var split = linebreak.split(text);

                    if (split.length == 0) {
                        continue;
                    }

                    var metrics = this.getFontMetrics(font);

                    for (int j = 0; j < split.length - 1; j++) {
                        placeText(split[j]);
                        x = 0;
                        yinc = metrics.getMaxDescent();
                        y += metrics.getHeight();
                    }

                    var last = split[split.length - 1];
                    placeText(last);
                    yinc = metrics.getMaxDescent();
                }
                case Node.ELEMENT_NODE -> {
                    switch (child.getNodeName()) {
                        case "br" -> {
                            x = 0;
                            var metrics = this.getFontMetrics(this.getFont());
                            yinc = metrics.getHeight() - metrics.getDescent() + metrics.getMaxDescent();
                            y += metrics.getHeight();
                        }
                        case "b" -> {
                            var font = this.getFont();
                            this.setFont(font.deriveFont(this.getFont().getStyle() | java.awt.Font.BOLD));
                            parse(child);
                            this.setFont(font);
                        }
                        case "i" -> {
                            var font = this.getFont();
                            this.setFont(font.deriveFont(this.getFont().getStyle() | java.awt.Font.ITALIC));
                            parse(child);
                            this.setFont(font);
                        }
                        case "u" -> {
                            underline = true;
                            parse(child);
                            underline = false;
                        }
                        case "s" -> {
                            strikethrough = true;
                            parse(child);
                            strikethrough = false;
                        }
                        case "small" -> {
                            small = true;
                            parse(child);
                            small = false;
                        }
                        case "pre" -> {
                            var font = this.getFont();
                            this.setFont(new Font("Menlo", font.getStyle(), font.getSize()));
                            code = true;
                            parse(child);
                            code = false;
                            this.setFont(font);
                        }

                        case "h1" -> {
                            var font = this.getFont();
                            this.setFont(font.deriveFont(24f));
                            parse(child);
                            this.setFont(font);
                        }
                        case "h2" -> {
                            var font = this.getFont();
                            this.setFont(font.deriveFont(20f));
                            parse(child);
                            this.setFont(font);
                        }
                        case "h3" -> {
                            var font = this.getFont();
                            this.setFont(font.deriveFont(16f));
                            parse(child);
                            this.setFont(font);
                        }

                        case "quote" -> {
                            // i'll do this later
                            parse(child);
                        }

                        case "atch" -> {
                            // if (true) continue;
                            try {

                                var key = child.getAttributes().getNamedItem("key").getTextContent();
                                if (!external_data.containsKey(key)
                                    || !(external_data.get(key) instanceof Attachment attachment)) {
                                    continue;
                                }

                                if (x > 0) {
                                    x = 0;
                                    y += yinc;
                                }
                                var url = attachment.getUrl();

                                var filname = attachment.getFileName();

                                if (!MediaCache.has("attachment", attachment, Attachment::getUrl)) {
                                    Thread.ofVirtual().start(() -> {
                                        MediaCache.getImage("attachment", attachment, Attachment::getUrl);
                                        
                                        SwingUtilities.invokeLater(() -> {
                                            last_width = 0;
                                            this.revalidate();
                                        });
                                    });
                                    continue;
                                }
                                var image = MediaCache.getImage("attachment", attachment, Attachment::getUrl);
                                var img = new JLabel();

                                if (image == null) {
                                    System.err.println("Failed to load image: " + url);
                                    continue;
                                }

                                var width = image.getWidth(this);
                                var height = image.getHeight(this);
                                // System.out.println("height: " + height);
                                var scale = Math.min((double)width, this.getWidth()) / width;

                                var context_menu = new JPopupMenu();

                                var popup_label = context_menu.add("Copy Image");
                                popup_label.addActionListener((e) -> {
                                    copyToClipboard(image);
                                });

                                var save_item = context_menu.add("Save Image");
                                save_item.addActionListener((e) -> {
                                    savePrompt(image, filname);
                                });

                                img.setIcon(new ImageIcon(image.getScaledInstance((int) (width * scale),
                                        (int) (height * scale) , Image.SCALE_SMOOTH)));
                                img.setBounds((int) x, (int) y, (int)(width * scale), (int)(height * scale));
                                img.addMouseListener(new MouseAdapter() {
                                    @Override
                                    public void mouseClicked(java.awt.event.MouseEvent e) {
                                        if (context_menu.isShowing())
                                            return;
                                        JOptionPane.showMessageDialog(null, new ImageIcon(image), filname, JOptionPane.PLAIN_MESSAGE);
                                    }

                                    public void mousePressed(java.awt.event.MouseEvent e) {
                                        if (e.isPopupTrigger()) {
                                            context_menu.show(img, e.getX(), e.getY());
                                            e.consume();
                                        }
                                    }

                                    public void mouseReleased(java.awt.event.MouseEvent e) {
                                        if (e.isPopupTrigger()) {
                                            context_menu.show(img, e.getX(), e.getY());
                                            e.consume();
                                        }
                                    }

                                    @Override
                                    public void mouseEntered(java.awt.event.MouseEvent e) {
                                        TextView.this.getParent().dispatchEvent(SwingUtilities.convertMouseEvent(img, e, img.getParent()));
                                    }

                                    @Override
                                    public void mouseExited(java.awt.event.MouseEvent e) {
                                        TextView.this.getParent().dispatchEvent(SwingUtilities.convertMouseEvent(img, e, img.getParent()));
                                    }
                                });
                                img.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                                // future.thenRun(() -> {
                                    this.add(img);
                                // });
                                
                                y += height * scale;
                            } catch (DOMException e) {
                                System.err.print("Failed to load image: ");
                                e.printStackTrace();
                            }

                        }
                        case "emoji" -> {
                            try {
                                var key = child.getAttributes().getNamedItem("key").getTextContent();
                                if (!external_data.containsKey(key)
                                    || !(external_data.get(key) instanceof CustomEmoji emoji)) {
                                    continue;
                                }

                                
                                if (!MediaCache.has("attachment", emoji, (e) -> e.getImage().getUrl())) {
                                    Thread.ofVirtual().start(() -> {
                                        MediaCache.getImage("attachment", emoji, (e) -> e.getImage().getUrl());

                                        SwingUtilities.invokeLater(() -> {
                                            last_width = 0;
                                            this.revalidate();
                                        });
                                    });
                                    continue;
                                }
                                var image = MediaCache.getImage("attachment", emoji, (e) -> e.getImage().getUrl());
                                var label = new JLabel();

                                if (image == null) {
                                    System.err.println("Failed to load emoji: " + emoji);
                                    continue;
                                }

                                int size;
                                try {
                                    size = Integer.parseInt(child.getAttributes().getNamedItem("size").getTextContent());
                                } catch (NumberFormatException | DOMException | NullPointerException e) {
                                    size = this.getFontMetrics(this.getFont()).getHeight();
                                }

                                var scale = (double)size / image.getHeight(this);

                                label.setIcon(new ImageIcon(image.getScaledInstance((int) (image.getWidth(this) * scale),
                                        (int) (image.getHeight(this) * scale), Image.SCALE_SMOOTH)));

                                var popup = new JPopupMenu();
                                var popup_label = new JLabel();

                                popup_label.setIcon(new ImageIcon(image));

                                if (emoji instanceof KnownCustomEmoji known) {
                                    popup_label.setText(String.format("""
                                            <html>
                                                <b>:%s:</b>
                                                <br>
                                                this emoji is from the server: %s
                                            """, emoji.getName(), known.getServer().getName()));
                                } else {
                                    popup_label.setText(String.format("""
                                            <html>
                                                <b>:%s:</b>
                                                <br>
                                                this emoji is not from one of the servers you are in
                                            </html>
                                            """, emoji.getName()));
                                }

                                popup.add(popup_label);

                                var copy_item = popup.add("Copy Emoji");
                                copy_item.addActionListener((e) -> {
                                    copyToClipboard(image);
                                });

                                var save_item = popup.add("Save Emoji");
                                save_item.addActionListener((e) -> {
                                    savePrompt(image, emoji.getName() + ".webp"); // webp is the only format discord uses for emojis, probably
                                });

                                label.addMouseListener(new MouseAdapter() {
                                    @Override
                                    public void mouseClicked(java.awt.event.MouseEvent e) {
                                        popup.show(label, e.getX(), e.getY());
                                    }

                                    @Override
                                    public void mouseEntered(java.awt.event.MouseEvent e) {
                                        TextView.this.getParent().dispatchEvent(
                                                SwingUtilities.convertMouseEvent(label, e, label.getParent()));
                                    }

                                    @Override
                                    public void mouseExited(java.awt.event.MouseEvent e) {
                                        TextView.this.getParent().dispatchEvent(
                                                SwingUtilities.convertMouseEvent(label, e, label.getParent()));
                                    }
                                });
                                label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                                yinc = image.getHeight(this) * scale;
                                if (x + image.getWidth(this) * scale > this.getWidth()) {
                                    x = 0;
                                    y += image.getHeight(this) * scale;
                                }

                                label.setBounds((int) x, (int) y, (int)(image.getWidth(this) * scale), (int)(image.getHeight(this) * scale));

                                this.add(label);

                                x += image.getWidth(this) * scale + 5;


                            } catch (DOMException e) {
                                System.err.print("Failed to load emoji: ");
                                e.printStackTrace();
                            }
                        }
                        case "root" -> parse(child);
                    }
                }
            }
        }
    }

    private void placeText(String text) {
        var font = this.getFont();
        var metrics = this.getFontMetrics(font);

        var width = metrics.stringWidth(text);

        if (x + width > this.getWidth()) {

            // best effort to split the text
            // if the text is too long to fit in the width

            var split = whitespace.splitWithDelimiters(" " + text, 0);
            split[1] = split[1].substring(1);

            {

                var sb = new StringBuilder();

                for (int j = 1; j + 1 < split.length; j += 2) {
                    var split_space = split[j];
                    var split_text = split[j + 1];

                    var insert = sb.length();

                    sb.append(split_space).append(split_text);

                    if (x + metrics.stringWidth(sb.toString()) > this.getWidth()) {
                        sb.delete(insert, sb.length());
                        if ((x + metrics.stringWidth(sb.toString() + split_space)) < this.getWidth()
                                && metrics.stringWidth(split_text) > this.getWidth()) {
                            // if the word is too long to fit even in the next line
                            // then we just split the word

                            sb.append(split_space);

                            int k = 0;

                            if (x == 0) { // if it's already a new line, we *must* add at least one character
                                sb.append(split_text.charAt(k++));
                            }

                            while ((x + metrics.stringWidth(sb.toString())) < this.getWidth()) {
                                sb.append(split_text.charAt(k++));
                            }

                            // setting the space to nothing is done a bit later
                            split[j + 1] = split_text.substring(k);
                        }

                        var str = sb.toString();
                        
                        if (!sb.isEmpty()) {
                            createJLabel(str);
                            sb.setLength(0);
                        } else if (metrics.stringWidth(split[j + 1]) > this.getWidth()) {
                            continue;
                        }
                        

                        var line_metrics = metrics.getLineMetrics(str, getGraphics());

                        x = 0;

                        y += line_metrics.getHeight();

                        split[j] = "";

                        j -= 2;
                    }
                }

                if (sb.length() > 0) {

                    var str = sb.toString();

                    createJLabel(str);
                    sb.setLength(0);

                    x += metrics.stringWidth(str);
                }
            }
        } else {
            if (text.isEmpty())
                return;
            createJLabel(text);

            x += metrics.stringWidth(text);
        }
    }

    private static final Pattern space = Pattern.compile(" ");

    private void createJLabel(String text) {
        // SwingUtilities.invokeLater(() -> {
        // Thread.ofVirtual().start(() -> {
            var sb = new StringBuilder();
    
            var metrics = this.getFontMetrics(this.getFont());
    
            sb.append(space.matcher(StringEscapeUtils.escapeHtml3(text)).replaceAll("&nbsp;"));

            if (strikethrough) {
                sb.insert(0, "<s>").append("</s>");
            }
            if (underline) {
                sb.insert(0, "<u>").append("</u>");
            }
            if (small) {
                sb.insert(0, "<span style='font-size: 7px;'>").append("</span>");
            }
    
            sb.insert(0, "<html>").append("</html>");
    
            var line_metrics = metrics.getLineMetrics(text, getGraphics());
    
            var label = new JLabel();
            label.setText(sb.toString());
    
            label.setFont(this.getFont());
            // label.setBorder(new LineBorder(Color.red, 1));

            if (y == 0) {
                y += metrics.getMaxAscent() + line_metrics.getBaselineOffsets()[line_metrics.getBaselineIndex()];
            }

            // if (lines != null)
            //     lines.add((int) y);
    
            label.setBounds((int) x, (int) Math.round(y - metrics.getMaxAscent() + line_metrics.getBaselineOffsets()[line_metrics.getBaselineIndex()]),
                    (int) metrics.stringWidth(text), (int) metrics.getMaxAscent() + metrics.getMaxDescent() + metrics.getLeading());
            sb.setLength(0);
    
            // System.out.println("add label: " + label.getText());
            if (code) {
                label.setBackground(this.getBackground().darker());
                label.setOpaque(true);
            }

            // label.setBorder(new LineBorder(Color.red, 1));

            // future.thenRun(() -> {
                this.add(label);
            // });

            // var stacktrace = Thread.currentThread().getStackTrace();
            // for (var e : stacktrace) {
            //     System.out.println(e);
            // }
            // System.out.println("added label");
        // });
    }

    public static void copyToClipboard(Image image) {
        var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new Transferable() {
            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[] { DataFlavor.imageFlavor };
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return flavor.equals(DataFlavor.imageFlavor);
            }

            @Override
            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                if (flavor.equals(DataFlavor.imageFlavor)) {
                    return image;
                }
                throw new UnsupportedFlavorException(flavor);
            }
        }, null);
    }

    public static void savePrompt(Image image, String filename) {
        var fc = new JFileChooser();
        fc.setDialogTitle("Save Image");
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        var extension = filename.substring(filename.lastIndexOf('.'));

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
            try {
                var file = fc.getSelectedFile();
                if (!file.getName().endsWith(extension)) {
                    file = new File(file.getAbsolutePath() + extension);
                }
                ImageIO.write((RenderedImage) image, extension.substring(1), file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    // private final ArrayList<Integer> lines;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // if (lines == null)
        //     return;
        
        // g.setColor(Color.cyan);

        // for (var y : lines) {
        //     g.drawLine(0, y, getWidth(), y);
        // }
    }

    public static Document parseXML(String xmlContent) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        ByteArrayInputStream input = new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8));
        return builder.parse(input);
    }
}
