package canaryprism.dbc.save.json;

import canaryprism.dbc.save.Data;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public sealed interface JSONData<T> extends Data permits JSONObjectData, JSONArrayData {

    default Optional<JSONObject> getObject() {
        return getAs(JSONObject.class);
    }
    default Optional<JSONArray> getArray() {
        return getAs(JSONArray.class);
    }

    default <U> Optional<U> getAs(Class<U> type) {
        return type.isInstance(get()) ? Optional.of(type.cast(get())) : Optional.empty();
    }

    T get();

    @Override
    default InputStream streamData() {
        return new ByteArrayInputStream(this.get().toString().getBytes(StandardCharsets.UTF_8));
    }
    
}
