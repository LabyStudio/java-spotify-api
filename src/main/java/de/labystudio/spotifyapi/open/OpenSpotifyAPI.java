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

    private final Cache<BufferedImage> imageCache = new Cache<>(10);
    private final Cache<OpenTrack> openTrackCache = new Cache<>(100);

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
     * Request the cover image url of the given track asynchronously.
     * If the track is already in the cache, it will be returned.
     *
     * @param track    The track to lookup
     * @param callback Response with the image url of the track. It won't be called on an error.
     */
    public void requestImageUrlAsync(Track track, Consumer<String> callback) {
        this.executor.execute(() -> {
            try {
                String imageUrl = this.requestImageUrl(track);
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
     * @param track The track to lookup
     * @param callback Response with the open track. It won't be called on an error.
     */
    public void requestOpenTrackAsync(Track track, Consumer<OpenTrack> callback) {
        this.executor.execute(() -> {
            try {
                OpenTrack openTrack = this.requestOpenTrack(track);
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
        // Try to get image from cache by track id
        BufferedImage cachedImage = this.imageCache.get(track.getId());
        if (cachedImage != null) {
            return cachedImage;
        }

        // Request the image url
        String url = this.requestImageUrl(track);
        if (url == null) {
            return null;
        }

        // Download the image
        BufferedImage image = ImageIO.read(new URL(url));
        if (image == null) {
            throw new IOException("Could not load image: " + url);
        }

        // Cache the image and return it
        this.imageCache.push(track.getId(), image);
        return image;
    }

    /**
     * Request the cover image url of the given track.
     * If the track is already in the cache, it will be returned.
     *
     * @param track The track to lookup
     * @return The url of the track or null if it failed
     * @throws IOException if the request failed
     */
    private String requestImageUrl(Track track) throws IOException {
        // Request track information
        OpenTrack openTrack = this.requestOpenTrack(track);
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
        OpenTrack cachedOpenTrack = this.openTrackCache.get(track.getId());
        if (cachedOpenTrack != null) {
            return cachedOpenTrack;
        }

        // Create REST API url
        String url = String.format(URL_API_TRACKS, track.getId());
        OpenTrack openTrack = this.request(url, OpenTrack.class, true);

        // Cache the open track and return it
        this.openTrackCache.push(track.getId(), openTrack);
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
        // Connect
        HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
        connection.addRequestProperty("User-Agent", USER_AGENT);
        connection.addRequestProperty("referer", "https://open.spotify.com/");
        connection.addRequestProperty("app-platform", "WebPlayer");
        connection.addRequestProperty("origin", "https://open.spotify.com");

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
        JsonReader reader = new JsonReader(new InputStreamReader(connection.getInputStream()));
        return new Gson().fromJson(reader, clazz);
    }

    public Cache<BufferedImage> getImageCache() {
        return this.imageCache;
    }

    public Cache<OpenTrack> getOpenTrackCache() {
        return this.openTrackCache;
    }
}
