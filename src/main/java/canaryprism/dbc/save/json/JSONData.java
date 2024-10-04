package canaryprism.dbc.save.json;

import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Optional;

import org.apache.commons.io.input.ReaderInputStream;
import org.json.JSONArray;
import org.json.JSONObject;

import canaryprism.dbc.save.Data;

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
        return new ReaderInputStream(new StringReader(this.get().toString()), Charset.defaultCharset());
    }
    
}
