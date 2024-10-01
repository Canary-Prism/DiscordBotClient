package canaryprism.dbc.save.json;

import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

public final class JSONArrayData implements JSONData {
    private final JSONArray data;

    public JSONArrayData(JSONArray data) {
        this.data = data;
    }

    public JSONArray getData() {
        return data;
    }

    @Override
    public Optional<JSONObject> getObject() {
        return Optional.empty();
    }

    @Override
    public Optional<JSONArray> getArray() {
        return Optional.of(data);
    }

    @Override
    public Object get() {
        return data;
    }
}
