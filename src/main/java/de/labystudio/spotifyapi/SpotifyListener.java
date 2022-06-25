package de.labystudio.spotifyapi;

import de.labystudio.spotifyapi.model.Track;

/**
 * Interface to receive playback updates from Spotify.
 *
 * @author LabyStudio
 */
public interface SpotifyListener {

    /**
     * Called when the api successfully connected to the Spotify application.
     */
    void onConnect();

    /**
     * Called when the id of the current playing track changed.
     *
     * @param track the new track
     */
    void onTrackChanged(Track track);

    /**
     * This event is only called when the user jumps to another position during the song,
     * when the playback state changed or when the track changed.
     * To keep track of the actual interpolated position, use {@link SpotifyAPI#getPosition()}.
     *
     * @param position the new position in milliseconds
     */
    void onPositionChanged(int position);

    /**
     * Called when the playback state changed.
     *
     * @param isPlaying true if the playback is playing, false if it is paused
     */
    void onPlayBackChanged(boolean isPlaying);

    /**
     * Called when the api successfully fetched the latest data from Spotify.
     */
    void onSync();

    /**
     * Called when the api failed to fetch data from Spotify.
     * The api will immediately try to reconnect to Spotify.
     *
     * @param exception the exception that occurred
     */
    void onDisconnect(Exception exception);
}
