package de.labystudio.spotifyapi;

import de.labystudio.spotifyapi.model.Track;

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
     *
     * @return the initialized SpotifyAPI
     */
    SpotifyAPI initialize();

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
     * Returns true if the api is connected to the Spotify application.
     *
     * @return true if the api is connected to the Spotify application
     */
    boolean isConnected();

    /**
     * Registers a listener to be notified about changes.
     *
     * @param listener the listener to register
     */
    void registerListener(SpotifyListener listener);

    /**
     * Disconnect from the Spotify application and stop all background tasks.
     * It will also remove all listeners.
     */
    void stop();
}
