package de.labystudio.spotifyapi.platform.linux.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBusResponse {

    private static final String splitRegex = "\\s+(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

    private final Map<String, Object> result = new HashMap<>();

    public DBusResponse(String raw) {
        String[] lines = raw.split("\n");

        int arrayStartPos = 0;
        String key = "";
        Object value = new Object();
        for (int i = 1; i < lines.length - 1; i++) {
            String line = lines[i].trim();

            if (arrayStartPos != 0) {
                if (line.contains("]")) {
                    StringBuilder arrayString = new StringBuilder();
                    int j = arrayStartPos;
                    for (; j < i + 1; j++) {
                        arrayString.append(lines[j]).append("\n");
                    }
                    i = j - 1;
                    arrayStartPos = 0;

                    value = this.parseList(arrayString.toString());
                }
            } else if (line.startsWith("variant                ")) {
                // Should be value of the dict entry
                String[] words = line.replaceFirst("variant {16}", "").split(splitRegex);

                if (words[0].equals("array")) {
                    arrayStartPos = i;
                } else {
                    value = this.parseValue(words[0], words[1]);
                }
            } else if (line.startsWith("dict entry(")) {
                // start of dict entry
                key = "";
                value = null;
            } else if (line.endsWith(")")) {
                // end of dict entry
                this.result.put(key, value);
            } else {
                // Should be key of the dict entry
                String[] words = line.split(splitRegex);
                key = (String) this.parseValue(words[0], words[1]);
            }
        }
    }

    public boolean has(String key) {
        return this.result.containsKey(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) this.result.get(key);
    }

    private List<Object> parseList(String arrayString) {
        List<Object> result = new ArrayList<>();

        String[] lines = arrayString.split("\n");
        for (int i = 1; i < lines.length - 1; i++) {
            String[] words = lines[i].trim().split(splitRegex);

            result.add(this.parseValue(words[0], words[1]));
        }

        return result;
    }

    private Object parseValue(String type, String value) {
        switch (type) {
            case "string":
                return value.split("\"")[1];
            case "uint64":
                return Long.parseLong(value);
            case "double":
                return Double.parseDouble(value);
            case "int32":
                return Integer.parseInt(value);
            default:
                return value;
        }
    }

    private Object parseValueFromString(String string) {
        String[] words = string.split(splitRegex);
        return this.parseValue(words[0], words[1]);
    }
}
