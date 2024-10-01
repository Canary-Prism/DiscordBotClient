package canaryprism.dbc.save.json;

import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

public final class JSONObjectData implements JSONData {
    private final JSONObject data;

    public JSONObjectData(JSONObject data) {
        this.data = data;
    }

    public JSONObject getData() {
        return data;
    }

    @Override
    public Optional<JSONObject> getObject() {
        return Optional.of(data);
    }

    @Override
    public Optional<JSONArray> getArray() {
        return Optional.empty();
    }

    @Override
    public Object get() {
        return data;
    }
    
}
