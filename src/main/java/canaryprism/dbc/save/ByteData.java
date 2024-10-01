package canaryprism.dbc.save;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class ByteData implements Data {
    private final byte[] data;

    public ByteData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        System.arraycopy(data, 0, this.data, 0, data.length);
    }

    @Override
    public InputStream streamData() {
        return new ByteArrayInputStream(data);
    }
    
}
