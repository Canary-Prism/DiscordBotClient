package canaryprism.dbc;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;

import canaryprism.dbc.swing.DiscordView;
import canaryprism.dbc.swing.message.MessageView;
import dev.dirs.ProjectDirectories;

public class Main {
    static {
        ImageIO.scanForPlugins();
    }

    // static void printNode(Node node) {
    //     System.out.println(node.getNodeName());
    //     var children = node.getChildNodes();
    //     for (int i = 0; i < children.getLength(); i++) {
    //         printNode(children.item(i));
    //     }
    //     System.out.println("end " + node.getNodeName());
    // }

    private static <T> T getArg(String[] args, String key, Function<String, T> parser, Supplier<T> default_value) {
        for (int i = args.length - 2; i >= 0; i--) {
            if (args[i].equals(key)) {
                return parser.apply(args[i + 1]);
            }
        }
        return default_value.get();
    }

    public static final ProjectDirectories dirs = ProjectDirectories.from("", "canaryprism", "DiscordBotClient");

    public static void main(String[] args) {

        {
            System.setProperty("apple.awt.application.name", "DiscordBotClient");
            var laf = getArg(args, "--laf", (e) -> {
                if (e.equals("default")) {
                    return UIManager.getSystemLookAndFeelClassName();
                }
                return e;
            }, () -> {
                System.setProperty("apple.awt.application.appearance", "system");
                return FlatMacDarkLaf.class.getName();
            });
        
            try {
                UIManager.setLookAndFeel(laf);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        var frame = new JFrame();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        var main_panel = new JPanel();

        main_panel.setLayout(new BorderLayout());
        var config_path = Path.of(dirs.configDir);
        if (!Files.isDirectory(config_path)) {
            try {
                Files.createDirectories(config_path);
            } catch (IOException e) {
                System.err.print("Failed to create config directory: ");
                e.printStackTrace();

                JOptionPane.showMessageDialog(null, "Failed to create config directory", "DiscordBotClient",
                    JOptionPane.ERROR_MESSAGE);
            }
        }

        var loading_panel = new JPanel(); 
        {
            var label = new JPanel();
            label.add(new JLabel("""
                <html>
                    <h1>Discord Bot Client</h1>
                    <p>Loading...</p>
                </html>
                """));
            loading_panel.add(label);
        }

        frame.setContentPane(loading_panel);
        frame.pack();
        frame.setVisible(true);


        var api = login(config_path.resolve("config.json"));

        frame.setVisible(false);

        var lost_connection_panel = new JPanel();
        {
            var label = new JPanel();
            label.add(new JLabel("""
                <html>
                    <h1>Discord Bot Client</h1>
                    <p>Lost connection to Discord</p>
                    <p>Attempting to reconnect...</p>
                </html>
                """));
            lost_connection_panel.add(label);
        }

        var media_cache_path = Path.of(dirs.cacheDir, "media");
        if (!Files.exists(media_cache_path)) {
            try {
                Files.createDirectories(media_cache_path);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Failed to create media cache directory", "DiscordBotClient",
                    JOptionPane.ERROR_MESSAGE);
                
                System.exit(1); // ehehe
            }
        }
        MediaCache.loadStorageCache(media_cache_path);


        main_panel.add(
            new DiscordView(
                api
            ),
            BorderLayout.CENTER
        );

        // frame.pack();
        main_panel.setBorder(new EmptyBorder(5, 5, 5, 5));
        frame.setContentPane(main_panel);
        frame.setSize(900, 700);
        frame.revalidate();
        // SwingUtilities.invokeLater(() -> {
            frame.setVisible(true);
        // });

        api.addLostConnectionListener((e) -> {
            SwingUtilities.invokeLater(() -> {
                frame_size = frame.getSize();
                frame.setContentPane(lost_connection_panel);
                frame.pack();
                frame.revalidate();
                frame.repaint();
            });
        });

        api.addResumeListener((e) -> {
            SwingUtilities.invokeLater(() -> {
                frame.setContentPane(main_panel);
                frame.setSize(frame_size);
                frame.revalidate();
                frame.repaint();
            });
        });

        api.addReconnectListener((e) -> {
            SwingUtilities.invokeLater(() -> {
                var view = (DiscordView) main_panel.getComponent(0);

                main_panel.removeAll();
                
                var selected_server = view.getSelectedServer();
                var selected_channel = view.getSelectedServerView().getSelectedChannel();
                
                var new_view = new DiscordView(api);
                main_panel.add(new_view, BorderLayout.CENTER);

                new_view.showServer(selected_server);
                new_view.getSelectedServerView().showChannel(selected_channel);


                frame.setContentPane(main_panel);
                frame.setSize(frame_size);
                frame.revalidate();
                frame.repaint();
            });
        });

        System.out.println("done");
    }
    
    private static Dimension frame_size;

    private static DiscordApi login(Path path) {
        String token = "";
        try {
            var json = new JSONObject(Files.readString(path));
            token = json.getString("token");
        } catch (JSONException | IOException e) {}

        while (true) {
            try {
                var api = new DiscordApiBuilder().setToken(token).setAllIntents().login().join();

                try {
                    var json = new JSONObject();
                    json.put("token", token);
                    Files.writeString(path, json.toString());
                } catch (IOException e) {
                    System.err.print("Failed to save token to file: ");
                    e.printStackTrace();

                    JOptionPane.showMessageDialog(null, "Failed to save token to file", "DiscordBotClient",
                        JOptionPane.ERROR_MESSAGE);
                }

                return api;
            } catch (Exception e) {
                e.printStackTrace();
            }

            token = JOptionPane.showInputDialog(null, "Enter your discord token", "DiscordBotClient",
                JOptionPane.QUESTION_MESSAGE);

        }
    }
}
