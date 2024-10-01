package canaryprism.dbc.save.channels;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.javacord.api.entity.channel.Channel;
import org.json.JSONObject;

import canaryprism.dbc.save.json.JSONSaveSystem;

public class ChannelSaveSystem {

    private static final Path channels_path = Path.of("channels");
    public static class Property<T> {

        public static final Property<Optional<JSONObject>> self = new Property<>(Function.identity(), (e, v) -> {});

        public static final Property<Boolean> collapsed = new Property<>(
            (o) -> o.map((e) -> e.optBoolean("expanded", true)).orElse(true), 
            (o, v) -> o.put("expanded", v)
        );

        public static final Property<Instant> last_msg = new Property<>(
            (o) -> o.map((e) -> Instant.ofEpochMilli(e.optLong("last_msg", 0))).orElse(Instant.EPOCH), 
            (o, v) -> o.put("last_msg", v.toEpochMilli())
        );

        final Function<? super Optional<JSONObject>, ? extends T> getter;
        final BiConsumer<? super JSONObject, ? super T> setter;
        T get(Optional<JSONObject> data) {
            return getter.apply(data);
        }

        void set(JSONObject data, T value) {
            setter.accept(data, value);
        }

        Property(Function<? super Optional<JSONObject>, ? extends T> getter, BiConsumer<? super JSONObject, ? super T> setter) {
            this.getter = getter;
            this.setter = setter;
        }
    }

    public static Path pathFor(Channel channel) {
        return channels_path.resolve(channel.getIdAsString());
    }

    public static Optional<JSONObject> get(Channel channel) {
        return JSONSaveSystem.getObject(pathFor(channel));
    }

    public static <T> T get(Channel channel, Property<T> property) {
        return property.get(ChannelSaveSystem.get(channel));
    }

    public static <T> void set(Channel channel, Property<T> property, T value) {
        var opt = ChannelSaveSystem.get(channel);
        if (opt.isPresent()) {
            var data = opt.get();
            if (Objects.equals(property.get(opt), value)) {
                return; // No need to save default value
            }
            property.set(data, value);
        } else {
            var data = new JSONObject();
            if (Objects.equals(property.get(Optional.empty()), value)) {
                return; // No need to save default value
            }
            property.set(data, value);
            JSONSaveSystem.put(pathFor(channel), data);
        }
    }
}
