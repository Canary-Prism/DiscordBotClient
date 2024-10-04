package canaryprism.dbc.save.json;

import org.json.JSONObject;

public final class JSONObjectData implements JSONData<JSONObject> {
    private final JSONObject data;

    public JSONObjectData(JSONObject data) {
        this.data = data;
    }

    @Override
    public JSONObject get() {
        return data;
    }
    
}
