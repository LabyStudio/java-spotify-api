package de.labystudio.spotifyapi;

import de.labystudio.spotifyapi.config.SpotifyConfiguration;
import de.labystudio.spotifyapi.model.MediaKey;
import de.labystudio.spotifyapi.model.Track;

import java.util.concurrent.CompletableFuture;

/**
 * This is the main interface for the SpotifyAPI.
 * It is used to get the current track and the current position of a song.
 * There is a method called {@link #registerListener(SpotifyListener)} to register a listener to be notified about changes.
 *
 * @author LabyStudio
 */
public interface SpotifyAPI {

    /**
     * Initialize the SpotifyAPI and connect to the Spotify process.
     * Initializing the api will block the current thread until the connection is established.
     * It will use a default configuration.
     *
     * @return the initialized SpotifyAPI
     */
    default SpotifyAPI initialize() {
        return this.initialize(new SpotifyConfiguration.Builder().build());
    }

    /**
     * Initialize the SpotifyAPI and connect to the Spotify process.
     * Initializing the api will block the current thread until the connection is established.
     *
     * @param configuration the configuration for the api
     * @return the initialized SpotifyAPI
     */
    SpotifyAPI initialize(SpotifyConfiguration configuration);

    /**
     * Initialize the SpotifyAPI and connect to the Spotify process asynchronously.
     *
     * @param configuration the configuration for the api
     * @return a future that will contain the initialized SpotifyAPI
     */
    default CompletableFuture<SpotifyAPI> initializeAsync(SpotifyConfiguration configuration) {
        return CompletableFuture.supplyAsync(() -> this.initialize(configuration));
    }

    /**
     * Initialize the SpotifyAPI and connect to the Spotify process asynchronously.
     * It will use a default configuration.
     *
     * @return a future that will contain the initialized SpotifyAPI
     */
    default CompletableFuture<SpotifyAPI> initializeAsync() {
        return CompletableFuture.supplyAsync(this::initialize);
    }

    /**
     * Returns the current track that is playing right now
     * It can be null if the api haven't received any playback changes yet.
     *
     * @return the current track
     */
    Track getTrack();

    /**
     * Returns true if the current track is playing or cached
     *
     * @return true if the current track is playing or cached
     */
    default boolean hasTrack() {
        return this.getTrack() != null;
    }

    /**
     * Returns the current interpolated position of the song in milliseconds.
     * To check if the position is known, use {@link #hasPosition()}
     *
     * @return the current position of the song in milliseconds
     * @throws IllegalStateException if the position isn't known and the api haven't received any playback changes yet
     */
    int getPosition();

    /**
     * Returns true if the position of the current track is known.
     * The position becomes known after the track has changed or after the song has been paused.
     *
     * @return true if the position of the current track is known
     */
    boolean hasPosition();

    /**
     * Returns true if the current track is playing.
     *
     * @return true if the current track is playing
     */
    boolean isPlaying();

    /**
     * Send a key pres of a media key to the system.<br>
     * The key can be one of the following:
     * <ul>
     *     <li>{@link MediaKey#PLAY_PAUSE}</li>
     *     <li>{@link MediaKey#NEXT}</li>
     *     <li>{@link MediaKey#PREV}</li>
     * </ul>
     *
     * @param mediaKey the key to send
     */
    void pressMediaKey(MediaKey mediaKey);

    /**
     * Returns true if the api is connected to the Spotify application.
     *
     * @return true if the api is connected to the Spotify application
     */
    boolean isConnected();

    /**
     * Returns true if the background process is running.
     *
     * @return true if the background process is running
     */
    boolean isInitialized();

    /**
     * Registers a listener to be notified about changes.
     *
     * @param listener the listener to register
     */
    void registerListener(SpotifyListener listener);

    /**
     * Unregisters a listener.
     *
     * @param listener the listener to unregister
     */
    void unregisterListener(SpotifyListener listener);

    /**
     * Returns the current set configuration of the api.
     *
     * @return the current set configuration of the api
     */
    SpotifyConfiguration getConfiguration();

    /**
     * Disconnect from the Spotify application and stop all background tasks.
     */
    void stop();

    /**
     * Similar to {@link SpotifyAPI#stop()} but releases any held resources.
     * <p>
     * Should only be called if there is no intent to reuse this instance.
     */
    void shutdown();
}
