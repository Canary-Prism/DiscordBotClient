package canaryprism.dbc;

import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import javax.imageio.ImageIO;

public class MediaCache {


    private record Media(URL url, byte[] data) {}

    private static final Map<URL, Path> storage = new HashMap<>();

    private static final Map<URL, byte[]> to_store = new HashMap<>();
    
    public static void loadStorageCache(Path dir) {
        int i = 0;
        while (Files.exists(dir.resolve("k" + i))) {
            var key = dir.resolve("k" + i);
            var value = dir.resolve("v" + i);
            try {
                storage.put(new URI(Files.readString(key)).toURL(), value);
            } catch (IOException | URISyntaxException e) {
                System.err.print("Failed to load cache entry " + i + " from " + dir + ": ");
                e.printStackTrace();
            }
            i++;
        }

        System.out.println("Loaded " + i + " cache entries from " + dir);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            saveFinal(dir);
        }));

        System.out.println("Registered cache save hook for " + dir);
    }

    public static void saveFinal(Path dir) {
        // i guess we just write the cache to disk

        // delete all the files in the directory
        try (var stream = Files.list(dir)) {
            stream.forEach((e) -> {
                try {
                    Files.delete(e);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        // if it fails i.. guess we just ignore it

        int i = 0;

        var futures = new ArrayList<CompletableFuture<Void>>();

        for (var entry : to_store.entrySet()) {
            var key = dir.resolve("k" + i);
            var value = dir.resolve("v" + i);
            final int j = i; // for the lambda autocapture
            var future = CompletableFuture.runAsync(() -> {
                try {
                    Files.writeString(key, entry.getKey().toExternalForm());
                    Files.write(value, entry.getValue());
                } catch (IOException e) {
                    System.err.print("Failed to save cache entry " + j + " to " + dir + ": ");
                    e.printStackTrace();
                }
            });

            futures.add(future);
            i++;
        }

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

        System.out.println("Saved " + i + " cache entries to " + dir);
    }

    private static final Map<String, Map<Object, Media>> cache = new HashMap<>();

    public static synchronized <T> Image getImage(String scope, T key, Function<? super T, ? extends URL> url_provider) {
        try {
            return ImageIO.read(new ByteArrayInputStream(get(scope, key, url_provider)));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    static int indent = 0;

    static String toString(Object o) {
        var str = switch (o) {
            case Map<?, ?> map -> {
                var builder = new StringBuilder();
                builder.append("{\n");
                indent++;
                for (var entry : map.entrySet()) {
                    for (int i = 0; i < indent; i++) {
                        builder.append("    ");
                    }
                    builder
                        .append(toString(entry.getKey()))
                        .append(": ")
                        .append(toString(entry.getValue()))
                        .append(", \n");
                }
                indent--;
                for (int i = 0; i < indent; i++) {
                    builder.append("    ");
                }
                builder.append("}");
                yield builder.toString();
            }
            case null, default -> String.valueOf(o);
        };
        return str;
    }

    public static synchronized <T> byte[] get(String scope, T key, Function<? super T, ? extends URL> url_provider) {
        // System.out.println("Getting " + key + " from " + scope);
        // System.out.println("Current Cache: " + toString(cache));
        var map = cache.computeIfAbsent(scope, (e) -> new WeakHashMap<>());
        try {
            var url = url_provider.apply(key);
            synchronized (map) {
                if (map.containsKey(key)) {
                    if (map.get(key).url.equals(url)) {
                        return map.get(key).data();
                    } else {
                        // the URL has changed, so we need to reload the image
                        map.remove(key);
                        return get(scope, key, url_provider);
                    }
                }
            }

            synchronized (storage) {
                if (storage.containsKey(url)) {
                    var path = storage.get(url);
                    var data = Files.readAllBytes(path);
                    synchronized (map) {
                        map.put(key, new Media(url, data));
                    }
                    synchronized (to_store) {
                        to_store.put(url, data);
                    }
                    return data;
                }
            }

            var data = url.openStream().readAllBytes();

            synchronized (map) {
                map.put(key, new Media(url, data));
            }
            synchronized (to_store) {
                to_store.put(url, data);
            }
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> boolean has(String scope, T key, Function<? super T, ? extends URL> url_provider) {
        var map = cache.get(scope);
        if (map == null) {
            return false;
        }
        var url = url_provider.apply(key);
        synchronized (map) {
            return map.containsKey(key) && map.get(key).url.equals(url);
        }
    }
}
