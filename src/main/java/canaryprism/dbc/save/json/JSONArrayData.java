package canaryprism.dbc.save.json;

import org.json.JSONArray;

public final class JSONArrayData implements JSONData<JSONArray> {
    private final JSONArray data;

    public JSONArrayData(JSONArray data) {
        this.data = data;
    }

    @Override
    public JSONArray get() {
        return data;
    }
}
