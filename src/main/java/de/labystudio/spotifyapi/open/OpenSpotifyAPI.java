package de.labystudio.spotifyapi.open;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import de.labystudio.spotifyapi.model.Track;
import de.labystudio.spotifyapi.open.model.AccessTokenResponse;
import de.labystudio.spotifyapi.open.model.track.OpenTrack;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/71.0.3578.98";

    private static final String URL_API_GEN_ACCESS_TOKEN = "https://open.spotify.com/get_access_token?reason=transport&productType=web_player";
    private static final String URL_API_TRACKS = "https://api.spotify.com/v1/tracks/%s";

    private final Executor executor = Executors.newSingleThreadExecutor();

    private final Map<Track, BufferedImage> imageCache = new ConcurrentHashMap<>();
    private final List<Track> cacheQueue = new ArrayList<>();
    private int cacheSize = 10;

    private AccessTokenResponse accessTokenResponse;

    public OpenSpotifyAPI() {
        this.generateAccessTokenAsync(accessTokenResponse -> this.accessTokenResponse = accessTokenResponse);
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
     * Generate an access token for the open spotify api
     */
    private AccessTokenResponse generateAccessToken() throws IOException {
        // Open connection
        HttpsURLConnection connection = (HttpsURLConnection) new URL(URL_API_GEN_ACCESS_TOKEN).openConnection();
        connection.addRequestProperty("User-Agent", USER_AGENT);
        connection.addRequestProperty("referer", "https://open.spotify.com/");
        connection.addRequestProperty("app-platform", "WebPlayer");

        // Read response
        JsonReader reader = new JsonReader(new InputStreamReader(connection.getInputStream()));
        return new Gson().fromJson(reader, AccessTokenResponse.class);
    }

    /**
     * Request the cover image of the given track asynchronously.
     * If the track is already in the cache, it will be returned.
     *
     * @param track    The track to lookup
     * @param callback Response with the buffered image track. It won't be called on an error.
     */
    public void requestImageAsync(Track track, Consumer<BufferedImage> callback) {
        this.executor.execute(() -> {
            try {
                BufferedImage image = this.requestImage(track);
                if (image != null) {
                    callback.accept(image);
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
        return this.requestImage(track, true);
    }

    /**
     * Request the cover image of the given track.
     * If the track is already in the cache, it will be returned.
     *
     * @param track                     The track to lookup
     * @param canGenerateNewAccessToken It will try again once if it fails
     * @return Buffered image track.
     * @throws IOException if the request failed
     */
    private BufferedImage requestImage(Track track, boolean canGenerateNewAccessToken) throws IOException {
        BufferedImage cachedImage = this.imageCache.get(track);
        if (cachedImage != null) {
            return cachedImage;
        }

        // Create REST API url
        String url = String.format(URL_API_TRACKS, track.getId());

        // Connect
        HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
        connection.addRequestProperty("User-Agent", USER_AGENT);
        connection.addRequestProperty("referer", "https://open.spotify.com/");
        connection.addRequestProperty("app-platform", "WebPlayer");
        connection.addRequestProperty("origin", "https://open.spotify.com");
        connection.addRequestProperty("authorization", "Bearer " + this.accessTokenResponse.accessToken);

        // Access token outdated
        if (connection.getResponseCode() / 100 != 2) {
            // Prevent infinite loop
            if (canGenerateNewAccessToken) {
                // Generate new access token
                this.accessTokenResponse = this.generateAccessToken();

                // Try again
                return this.requestImage(track, false);
            } else {
                // Request failed twice
                return null;
            }
        }

        // Read response
        JsonReader reader = new JsonReader(new InputStreamReader(connection.getInputStream()));
        OpenTrack openTrack = new Gson().fromJson(reader, OpenTrack.class);

        // Get largest image url
        String imageUrl = openTrack.album.images.get(0).url;

        // Download cover image
        if (imageUrl != null) {
            BufferedImage image = ImageIO.read(new URL(imageUrl));
            if (image == null) {
                throw new IOException("Could not load image: " + imageUrl);
            }

            // Remove image from cache if cache is full
            if (this.cacheQueue.size() > this.cacheSize) {
                Track element = this.cacheQueue.remove(0);
                this.imageCache.remove(element);
            }

            // Add new image to cache
            this.imageCache.put(track, image);
            this.cacheQueue.add(track);

            return image;
        }

        return null;
    }

    /**
     * Set the maximal amount of images to cache.
     * Default is 10.
     *
     * @param cacheSize The maximal amount of images to cache
     */
    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    /**
     * Clear the cache of images.
     */
    public void clearCache() {
        this.imageCache.clear();
        this.cacheQueue.clear();
    }
}
