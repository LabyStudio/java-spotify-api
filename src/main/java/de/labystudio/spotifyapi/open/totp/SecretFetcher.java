package de.labystudio.spotifyapi.open.totp;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import static de.labystudio.spotifyapi.open.OpenSpotifyAPI.GSON;
import static de.labystudio.spotifyapi.open.OpenSpotifyAPI.USER_AGENT;

public class SecretFetcher {

    public static final String URL_OPEN_SPOTIFY_WEB_APP = "https://open.spotify.com/";

    private static final Pattern WEB_PLAYER_JS_REGEX = Pattern.compile(
            "<script\\s+src=\"https://[^/]+(?:/[^/]+)*/web-player/web-player\\.[a-z0-9]+\\.js\"></script>"
    );

    public Secret fetchLatest() throws IOException {
        Secret[] secrets = this.fetchSecrets();

        int latestVersion = 0;
        Secret latestSecret = null;

        // Find the latest secret based on version
        for (Secret secret : secrets) {
            if (secret.getVersion() > latestVersion) {
                latestVersion = secret.getVersion();
                latestSecret = secret;
            }
        }

        if (latestSecret == null) {
            throw new IOException("No secrets found");
        }
        return latestSecret;
    }

    public Secret[] fetchSecrets() throws IOException {
        String url = this.fetchWebPlayerJsUrl();
        JsonObject secretStorage = this.fetchSecretStorage(url);
        if (secretStorage == null || !secretStorage.has("secrets")) {
            throw new IOException("No secrets found in secret storage");
        }

        // Parse the secrets array from the JSON object
        String secretsJson = secretStorage.get("secrets").toString();
        Secret[] secrets = GSON.fromJson(secretsJson, Secret[].class);
        if (secrets == null || secrets.length == 0) {
            throw new IOException("No secrets found in the JSON response");
        }

        return secrets;
    }

    private String fetchWebPlayerJsUrl() throws IOException {
        String html = this.fetchUrl(URL_OPEN_SPOTIFY_WEB_APP);

        // Use regex to find the web player JS URL
        Matcher matcher = WEB_PLAYER_JS_REGEX.matcher(html);
        if (matcher.find()) {
            String[] segments = matcher.group(0).split("\"");
            return segments[1]; // Extract the URL from the script tag
        }

        throw new IOException("Web player JS URL not found");
    }

    private JsonObject fetchSecretStorage(String jsUrl) throws IOException {
        String jsContent = this.fetchUrl(jsUrl);


        int pos = 0;
        while ((pos = jsContent.indexOf('{', pos)) != -1) {
            try {
                JsonElement candidateJson = this.extractJsonObject(jsContent, pos);
                if (candidateJson.isJsonObject() && this.containsSecretAndVersion(candidateJson)) {
                    return candidateJson.getAsJsonObject();
                }
            } catch (Exception e) {
                // Ignore parse exceptions and try next '{'
            }
            pos++;
        }
        throw new IOException("No JSON object with 'secret' and 'version' keys found in: " + jsUrl);
    }

    private String fetchUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;application/json");
        connection.setRequestProperty("Accept-Encoding", "gzip, deflate");

        InputStream inputStream;
        String encoding = connection.getContentEncoding();

        if ("gzip".equalsIgnoreCase(encoding)) {
            inputStream = new GZIPInputStream(connection.getInputStream());
        } else {
            inputStream = connection.getInputStream();
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    private boolean containsSecretAndVersion(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.has("secret") && obj.has("version")) {
                return true;
            }
            // Recursively check nested objects
            for (String key : obj.keySet()) {
                if (this.containsSecretAndVersion(obj.get(key))) {
                    return true;
                }
            }
        } else if (element.isJsonArray()) {
            for (JsonElement el : element.getAsJsonArray()) {
                if (this.containsSecretAndVersion(el)) return true;
            }
        }
        return false;
    }

    private JsonElement extractJsonObject(String text, int startIndex) throws IOException {
        int braceCount = 0;
        for (int i = startIndex; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '{') braceCount++;
            else if (c == '}') braceCount--;

            if (braceCount == 0) {
                return GSON.fromJson(text.substring(startIndex, i + 1), JsonElement.class);
            }
        }
        throw new IOException("Unbalanced braces in JSON");
    }


}
