package canaryprism.dbc.save.json;

import java.nio.file.Path;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import canaryprism.dbc.save.SaveSystem;

public class JSONSaveSystem {
    public static Optional<JSONData> get(Path path) {
        return SaveSystem.get(path, (is) -> {
            try {
                return new JSONObjectData(new JSONObject(new JSONTokener(is)));
            } catch (JSONException e) {
                return new JSONArrayData(new JSONArray(new JSONTokener(is)));
            }
        }, JSONData.class);
    }

    public static Optional<JSONObject> getObject(Path path) {
        return get(path).flatMap(JSONData::getObject);
    }

    public static Optional<JSONArray> getArray(Path path) {
        return get(path).flatMap(JSONData::getArray);
    }

    public static void put(Path path, JSONObject data) {
        SaveSystem.put(path, new JSONObjectData(data));
    }

    public static void put(Path path, JSONArray data) {
        SaveSystem.put(path, new JSONArrayData(data));
    }
}
