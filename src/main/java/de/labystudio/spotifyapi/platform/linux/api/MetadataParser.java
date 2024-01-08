package de.labystudio.spotifyapi.platform.linux.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetadataParser {

    private static final String splitRegex = "\\s+(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

    public static Map<String, Object> parse(String input) {
        Map<String, Object> result = new HashMap<>();

        String[] lines = input.split("\n");

        int arrayStartPos = 0;
        String key = "";
        Object value = new Object();
        for (int i = 1; i < lines.length-1; i++) {
            String line = lines[i].trim();

            if(arrayStartPos != 0){
                if(line.contains("]")){
                    StringBuilder arrayString = new StringBuilder();
                    int j = arrayStartPos;
                    for (; j < i+1; j++) {
                        arrayString.append(lines[j]).append("\n");
                    }
                    i = j-1;
                    arrayStartPos = 0;

                    value = parseList(arrayString.toString());
                }else continue;
            }else if (line.startsWith("variant                ")) {
                // Should be value of the dict entry

                String[] words = line.replaceFirst("variant {16}", "").split(splitRegex);

                if (words[0].equals("array")) {
                    arrayStartPos = i;
                } else {
                    value = parseValue(words[0], words[1]);
                }
            }else if (line.startsWith("dict entry(")) {
                // start of dict entry
                key = "";
                value = null;
            } else if (line.endsWith(")")) {
                // end of dict entry
                result.put(key, value);
            } else {
                // Should be key of the dict entry

                String[] words = line.split(splitRegex);
                key = (String) parseValue(words[0], words[1]);
            }
        }

        return result;
    }

    private static List<Object> parseList(String arrayString) {
        List<Object> result = new ArrayList<>();

        String[] lines = arrayString.split("\n");

        for (int i = 1; i < lines.length-1; i++) {
            String[] words = lines[i].trim().split(splitRegex);

            result.add(parseValue(words[0], words[1]));
        }

        return result;
    }

    public static Object parseValue(String type, String value) {
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

    public static Object parseValueFromString(String string) {
        String[] words = string.split(splitRegex);

        return parseValue(words[0], words[1]);
    }
}
