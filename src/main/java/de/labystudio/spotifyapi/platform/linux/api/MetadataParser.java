package de.labystudio.spotifyapi.platform.linux.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetadataParser {

    private static String splitRegex = "\\s+(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

    public static Map<String, Object> parse(String input) {
        Map<String, Object> result = new HashMap<>();

        String[] lines = input.split("\n");

        int arrStartIndex = 0;
        String key = "";
        Object value = new Object();
        for (int i = 1; i < lines.length-1; i++) {
            String line = lines[i].trim();

            if(arrStartIndex != 0){
                if(line.contains("]")){
                    String arrStr = "";
                    int j = arrStartIndex;
                    for (; j < i+1; j++) {
                        arrStr = arrStr + lines[j] + "\n";
                    }
                    i = j-1;
                    arrStartIndex = 0;

                    value = parseList(arrStr);
                }else continue;
            }else if (line.startsWith("variant                ")) {
                // Should be value of the dict entry

                String[] words = line.replaceFirst("variant                ", "").split(splitRegex);

                if (words[0].equals("array")) {
                    arrStartIndex=i;
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

    private static List<Object> parseList(String arrString) {
        List<Object> result = new ArrayList<>();

        String[] lines = arrString.split("\n");

        for (int i = 1; i < lines.length-1; i++) {
            String line = lines[i].trim();
            String[] words = line.split(splitRegex);

            Object value = parseValue(words[0], words[1]);

            result.add(value);
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

    public static Object parseValueFromString(String str) {
        String[] words = str.split(splitRegex);

        return parseValue(words[0], words[1]);
    }
}
