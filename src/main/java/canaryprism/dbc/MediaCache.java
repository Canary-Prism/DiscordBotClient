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
import java.time.Duration;
import java.util.Map;

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

//    private static final Map<String, Map<Object, Media>> cache = new HashMap<>();

    public static Image getImage(URI uri) {
        try {
            return ImageIO.read(new ByteArrayInputStream(get(uri)));
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

    public static byte[] get(URI uri) {
        return cache.get(uri).join();
    }

    public static <T> boolean has(URL url) {
        try {
            return cache.getIfPresent(url.toURI()) != null;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
