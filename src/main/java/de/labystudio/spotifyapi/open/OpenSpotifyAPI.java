package de.labystudio.spotifyapi.open;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import de.labystudio.spotifyapi.model.Track;
import de.labystudio.spotifyapi.open.model.AccessTokenResponse;
import de.labystudio.spotifyapi.open.model.track.OpenTrack;
import de.labystudio.spotifyapi.open.totp.TOTP;
import de.labystudio.spotifyapi.open.totp.gson.SecretDeserializer;
import de.labystudio.spotifyapi.open.totp.gson.SecretSerializer;
import de.labystudio.spotifyapi.open.totp.model.Secret;
import de.labystudio.spotifyapi.open.totp.provider.SecretProvider;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * OpenSpotify REST API.
 * Implements the functionality to request the image of a Spotify track.
 *
 * @author LabyStudio
 */
public class OpenSpotifyAPI {

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Secret.class, new SecretDeserializer())
            .registerTypeAdapter(Secret.class, new SecretSerializer())
            .create();

    public static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537." + (int) (Math.random() * 90);
    public static final String URL_API_GEN_ACCESS_TOKEN = "https://open.spotify.com/api/token?reason=%s&productType=web-player&totp=%s&totpServer=%s&totpVer=%s";

    public static final String URL_API_TRACKS = "https://api.spotify.com/v1/tracks/%s";
    public static final String URL_API_SERVER_TIME = "https://open.spotify.com/api/server-time";

    private final Executor executor = Executors.newSingleThreadExecutor();

    private final Cache<BufferedImage> imageCache = new Cache<>(10);
    private final Cache<OpenTrack> openTrackCache = new Cache<>(100);

    private final SecretProvider secretProvider;

    private AccessTokenResponse accessTokenResponse;

    public OpenSpotifyAPI(SecretProvider secretProvider) {
        this.secretProvider = secretProvider;
    }

    /**
     * Generate an access token asynchronously for the open spotify api
     */
    private void generateAccessTokenAsync(Consumer<AccessTokenResponse> callback) {
        this.executor.execute(() -> {
            try {
                // Generate access token
                callback.accept(this.generateAccessToken());
            } catch (Exception error) {
                error.printStackTrace();
            }
        });
    }

    /**
     * Request server time of Spotify for time-based one time password
     *
     * @return server time in seconds
     */
    public long requestServerTime() throws IOException {
        // Get server time
        URL url = new URL(URL_API_SERVER_TIME);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Host", "open.spotify.com");
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setRequestProperty("Accept", "application/json");

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String response = reader.readLine();
        reader.close();

        JsonObject obj = GSON.fromJson(response, JsonObject.class);
        return obj.get("serverTime").getAsLong();
    }

    /**
     * Generate an access token for the open spotify api
     */
    private AccessTokenResponse generateAccessToken() throws IOException {
        Secret secret = this.secretProvider.getSecret();
        if (secret == null) {
            throw new IOException("No TOTP secret provided");
        }

        long serverTime = this.requestServerTime();
        String totp = TOTP.generateOtp(secret.getSecretAsBytes(), serverTime, 30, 6);

        AccessTokenResponse response = this.getToken("transport", totp, secret.getVersion());

        if (!this.hasValidAccessToken(response)) {
            response = this.getToken("init", totp, secret.getVersion());
        }

        if (!this.hasValidAccessToken(response)) {
            throw new IOException("Could not generate access token");
        }

        return response;
    }

    /**
     * Retrieve access token using totp
     */
    private AccessTokenResponse getToken(String mode, String totp, int version) throws IOException {
        // Open connection
        String url = String.format(URL_API_GEN_ACCESS_TOKEN, mode, totp, totp, version);
        HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
        connection.addRequestProperty("User-Agent", USER_AGENT);
        connection.addRequestProperty("referer", "https://open.spotify.com/");
        connection.addRequestProperty("app-platform", "WebPlayer");
        connection.setRequestProperty("Accept", "application/json");

        int code = connection.getResponseCode();
        if (code != HttpURLConnection.HTTP_OK) {
            InputStream errorStream = connection.getErrorStream();
            if (errorStream != null) {
                JsonReader reader = new JsonReader(new InputStreamReader(errorStream));
                JsonObject response = GSON.fromJson(reader, JsonObject.class);
                throw new IOException("Could not retrieve access token: " + response.toString());
            }
        }

        // Read response
        JsonReader reader = new JsonReader(new InputStreamReader(connection.getInputStream()));
        return GSON.fromJson(reader, AccessTokenResponse.class);
    }

    /**
     * Request the cover image of the given track asynchronously.
     * If the track is already in the cache, it will be returned.
     *
     * @param track    The track to lookup
     * @param callback Response with the buffered image track. It won't be called on an error.
     */
    public void requestImageAsync(Track track, Consumer<BufferedImage> callback) {
        this.requestImageAsync(track.getId(), callback);
    }

    /**
     * Request the cover image of the given track asynchronously.
     * If the track is already in the cache, it will be returned.
     *
     * @param trackId  The track id to lookup
     * @param callback Response with the buffered image track. It won't be called on an error.
     */
    public void requestImageAsync(String trackId, Consumer<BufferedImage> callback) {
        this.executor.execute(() -> {
            try {
                BufferedImage image = this.requestImage(trackId);
                if (image != null) {
                    callback.accept(image);
                }
            } catch (Exception error) {
                error.printStackTrace();
            }
        });
    }

    /**
     * Request the cover image url of the given track asynchronously.
     * If the track is already in the cache, it will be returned.
     *
     * @param trackId  The track id to lookup
     * @param callback Response with the image url of the track. It won't be called on an error.
     */
    public void requestImageUrlAsync(String trackId, Consumer<String> callback) {
        this.executor.execute(() -> {
            try {
                String imageUrl = this.requestImageUrl(trackId);
                if (imageUrl != null) {
                    callback.accept(imageUrl);
                }
            } catch (Exception error) {
                error.printStackTrace();
            }
        });
    }

    /**
     * Request the track information of the given track asynchronously.
     * If the open track is already in the cache, it will be returned.
     *
     * @param track    The track to lookup
     * @param callback Response with the open track. It won't be called on an error.
     */
    public void requestOpenTrackAsync(Track track, Consumer<OpenTrack> callback) {
        this.requestOpenTrackAsync(track.getId(), callback);
    }

    /**
     * Request the track information of the given track asynchronously.
     * If the open track is already in the cache, it will be returned.
     *
     * @param trackId  The track id to lookup
     * @param callback Response with the open track. It won't be called on an error.
     */
    public void requestOpenTrackAsync(String trackId, Consumer<OpenTrack> callback) {
        this.executor.execute(() -> {
            try {
                OpenTrack openTrack = this.requestOpenTrack(trackId);
                if (openTrack != null) {
                    callback.accept(openTrack);
                }
            } catch (Exception error) {
                error.printStackTrace();
            }
        });
    }

    /**
     * Request the cover image of the given track synchronously.
     * If the track is already in the cache, it will be returned.
     *
     * @param track The track to lookup
     * @return The buffered image of the track or null if it failed
     * @throws IOException if the request failed
     */
    public BufferedImage requestImage(Track track) throws IOException {
        return this.requestImage(track.getId());
    }

    /**
     * Request the cover image of the given track synchronously.
     * If the track is already in the cache, it will be returned.
     *
     * @param trackId The track id to lookup
     * @return The buffered image of the track or null if it failed
     * @throws IOException if the request failed
     */
    public BufferedImage requestImage(String trackId) throws IOException {
        // Try to get image from cache by track id
        BufferedImage cachedImage = this.imageCache.get(trackId);
        if (cachedImage != null) {
            return cachedImage;
        }

        // Request the image url
        String url = this.requestImageUrl(trackId);
        if (url == null) {
            return null;
        }

        // Download the image
        BufferedImage image = ImageIO.read(new URL(url));
        if (image == null) {
            throw new IOException("Could not load image: " + url);
        }

        // Cache the image and return it
        this.imageCache.push(trackId, image);
        return image;
    }

    /**
     * Request the cover image url of the given track.
     * If the track is already in the cache, it will be returned.
     *
     * @param trackId The track id to lookup
     * @return The url of the track or null if it failed
     * @throws IOException if the request failed
     */
    private String requestImageUrl(String trackId) throws IOException {
        // Request track information
        OpenTrack openTrack = this.requestOpenTrack(trackId);
        if (openTrack == null) {
            return null;
        }

        // Get largest image url
        return openTrack.album.images.get(0).url;
    }

    /**
     * Request the track information of the given track.
     * If the open track is already in the cache, it will be returned.
     *
     * @param track The track to lookup
     * @throws IOException if the request failed
     */
    public OpenTrack requestOpenTrack(Track track) throws IOException {
        return this.requestOpenTrack(track.getId());
    }

    /**
     * Request the track information of the given track.
     * If the open track is already in the cache, it will be returned.
     *
     * @param trackId The track id to lookup
     * @throws IOException if the request failed
     */
    public OpenTrack requestOpenTrack(String trackId) throws IOException {
        OpenTrack cachedOpenTrack = this.openTrackCache.get(trackId);
        if (cachedOpenTrack != null) {
            return cachedOpenTrack;
        }

        // Create REST API url
        String url = String.format(URL_API_TRACKS, trackId);
        OpenTrack openTrack = this.request(url, OpenTrack.class, true);

        // Cache the open track and return it
        this.openTrackCache.push(trackId, openTrack);
        return openTrack;
    }

    /**
     * Request the open spotify api with the given url
     * It will try again once if it fails
     *
     * @param url                       The url to request
     * @param clazz                     The class to parse the response to
     * @param canGenerateNewAccessToken It will try again once if it fails
     * @param <T>                       The type of the response
     * @return The parsed response
     * @throws IOException if the request failed
     */
    public <T> T request(String url, Class<?> clazz, boolean canGenerateNewAccessToken) throws IOException {
        // Generate access token if not present
        if (this.accessTokenResponse == null) {
            this.accessTokenResponse = this.generateAccessToken();
        }

        // Connect
        HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
        connection.addRequestProperty("User-Agent", USER_AGENT);
        connection.addRequestProperty("referer", "https://open.spotify.com/");
        connection.addRequestProperty("app-platform", "WebPlayer");
        connection.addRequestProperty("origin", "https://open.spotify.com");

        // Add access token
        if (this.accessTokenResponse != null) {
            connection.addRequestProperty("authorization", "Bearer " + this.accessTokenResponse.accessToken);
        }

        // Access token outdated
        if (connection.getResponseCode() / 100 != 2) {
            // Prevent infinite loop
            if (canGenerateNewAccessToken) {
                // Generate new access token
                this.accessTokenResponse = this.generateAccessToken();

                // Try again
                return this.request(url, clazz, false);
            } else {
                // Request failed twice
                return null;
            }
        }

        // Read response
        JsonReader reader = new JsonReader(new InputStreamReader(
                connection.getInputStream(),
                StandardCharsets.UTF_8
        ));

        return GSON.fromJson(reader, clazz);
    }

    private boolean hasValidAccessToken(AccessTokenResponse response) {
        return response != null && response.accessToken != null && !response.accessToken.isEmpty();
    }

    public Cache<BufferedImage> getImageCache() {
        return this.imageCache;
    }

    public Cache<OpenTrack> getOpenTrackCache() {
        return this.openTrackCache;
    }
}
