package canaryprism.dbc.save;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;

public class SaveSystem {
    private static Path dir;
    private static final Map<Path, Data> data = new HashMap<>();

    public static void setDirectory(Path dir) {
        SaveSystem.dir = dir;
    }

    public static Path getDirectory() {
        return SaveSystem.dir;
    }

    public static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            saveFinal();
        }));
    }

    private static void saveFinal() {
        try (var ex = Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = new ArrayList<CompletableFuture<?>>();
            for (var entry : data.entrySet()) {
                var path = dir.resolve(entry.getKey());
                futures.add(CompletableFuture.runAsync(() -> {
                    if (!Files.exists(path.getParent())) {
                        try {
                            Files.createDirectories(path.getParent());
                        } catch (IOException e) {
                            System.err.print("Failed to create directory " + path.getParent() + ": ");
                            e.printStackTrace();
                            return;
                        }
                    }
                    try (var os = Files.newOutputStream(path)) {
                        entry.getValue().streamData().transferTo(os);
                    } catch (IOException e) {
                        System.err.print("Failed to save data to " + path + ": ");
                        e.printStackTrace();
                    }
                }, ex));
            }

            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
        }
    }

    private static <T extends Data> Optional<T> load(Path path, Function<? super InputStream, ? extends T> loader) {
        if (Files.isRegularFile(dir.resolve(path))) {
            try (var is = Files.newInputStream(dir.resolve(path));) {
                T data;
                try {
                    data = loader.apply(is);
                } catch (Exception e) {
                    System.err.print("Loader " + loader + " failed to load data from " + path + ": ");
                    e.printStackTrace();
                    return Optional.empty();
                }
                var optional = Optional.ofNullable(data);

                if (optional.isPresent()) {
                    synchronized (SaveSystem.data) {
                        SaveSystem.data.put(path, data);
                    }
                }

                return optional;
            } catch (IOException e) {
                System.err.print("Failed to load data from " + path + ": ");
                e.printStackTrace();
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    /**
     * <p>Get or load data from the save directory</p>
     * @param <T> the type of data
     * @param path the path to the data
     * @param loader the loader function
     * @return
     */
    public static <T extends Data> Optional<T> get(Path path, Function<? super InputStream, ? extends T> loader, Class<T> type) {
        synchronized (SaveSystem.data) {
            if (SaveSystem.data.containsKey(path)) {
                var data = SaveSystem.data.get(path);
                if (type.isInstance(data)) {
                    return Optional.of(type.cast(data));
                }
                return Optional.empty();
            }
        }

        return load(path, loader);
    }

    public static <T extends Data> T getOrDefault(Path path, Function<? super InputStream, ? extends T> loader, Supplier<? extends T> default_value, Class<T> type) {
        var optional = get(path, loader, type);
        if (optional.isPresent()) {
            return optional.get();
        } else {
            var def = default_value.get();
            put(path, def);
            return def;
        }
    }


    public static void put(Path path, Data data) {
        synchronized (SaveSystem.data) {
            SaveSystem.data.put(path, data);
        }
    }
}
