package canaryprism.dbc;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class MediaCache {

    private static final AsyncLoadingCache<URI, byte[]> cache = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(5))
            .buildAsync((uri, ex) -> {
                try (var http_client = HttpClient.newBuilder()
                        .executor(ex)
                        .build()) {
                    
                    var request = HttpRequest.newBuilder(uri)
                            .build();
                    
                    return http_client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                            .thenApply(HttpResponse::body);
                }
            });

    private record Media(URI uri, byte[] data) {}

    private static final Map<URI, Path> storage = new HashMap<>();

    private static final Map<URI, byte[]> to_store = new HashMap<>();
    
    public static void loadStorageCache(Path dir) {
        int i = 0;
        while (Files.exists(dir.resolve("k" + i))) {
            var key = dir.resolve("k" + i);
            var value = dir.resolve("v" + i);
            try {
                storage.put(new URI(Files.readString(key)), value);
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
                    Files.writeString(key, entry.getKey().toString());
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

//    private static final Map<String, Map<Object, Media>> cache = new HashMap<>();

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
        try {
            return cache.get(url_provider.apply(key).toURI()).join();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> boolean has(String scope, T key, Function<? super T, ? extends URL> url_provider) {
        try {
            return cache.getIfPresent(url_provider.apply(key).toURI()) != null;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
