package canaryprism.dbc.save.json;

import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Optional;

import org.apache.commons.io.input.ReaderInputStream;
import org.json.JSONArray;
import org.json.JSONObject;

import canaryprism.dbc.save.Data;

public sealed interface JSONData extends Data permits JSONObjectData, JSONArrayData {

    Optional<JSONObject> getObject();
    Optional<JSONArray> getArray();

    Object get();

    @Override
    default InputStream streamData() {
        return new ReaderInputStream(new StringReader(this.get().toString()), Charset.defaultCharset());
    }
    
}
