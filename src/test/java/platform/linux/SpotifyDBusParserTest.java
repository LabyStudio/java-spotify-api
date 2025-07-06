package platform.linux;

import de.labystudio.spotifyapi.platform.linux.api.model.Metadata;
import de.labystudio.spotifyapi.platform.linux.api.model.Variant;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SpotifyDBusParserTest {

    public static void main(String[] args) throws IOException {
        // Test metadata variant parsing
        Variant response = Variant.parse(readString("/dbus/metadata.variant"));
        Map<String, Object> map = new HashMap<>();
        for (Variant entry : response.<Variant[]>getValue()) {
            map.put(entry.getSig(), entry.getValue());
        }
        Metadata metadata = new Metadata(map);
        if (!metadata.getTrackId().equals("0r1kH7SIkkPP9W7mUknObF")) {
            throw new IllegalStateException("Invalid track ID: " + metadata.getTrackId());
        }

        // Test playing variant parsing
        Variant response2 = Variant.parse(readString("/dbus/playing.variant"));
        if (!response2.getSig().equals("variant")) {
            throw new IllegalStateException("Invalid sig key: " + response2.getSig());
        }
        if (!response2.<String>getValue().equals("Playing")) {
            throw new IllegalStateException("Invalid value: " + response2.<String>getValue());
        }
    }

    private static String readString(String path) throws IOException {
        InputStream stream = SpotifyDBusParserTest.class.getResourceAsStream(path);
        if (stream == null) {
            throw new IOException("Resource not found: " + path);
        }
        StringBuilder builder = new StringBuilder();
        int character;
        while ((character = stream.read()) != -1) {
            builder.append((char) character);
        }
        stream.close();
        return builder.toString();
    }

}
