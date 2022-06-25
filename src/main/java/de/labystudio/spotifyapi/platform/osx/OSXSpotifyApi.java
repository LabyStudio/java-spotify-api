package de.labystudio.spotifyapi.platform.osx;

import de.labystudio.spotifyapi.SpotifyAPI;
import de.labystudio.spotifyapi.model.MediaKey;
import de.labystudio.spotifyapi.model.Track;
import de.labystudio.spotifyapi.platform.AbstractSpotifyAPI;

/**
 * OSX implementation of the SpotifyAPI.
 *
 * @author LabyStudio
 */
public class OSXSpotifyApi extends AbstractSpotifyAPI {

    public OSXSpotifyApi() {
        super();
    }

    @Override
    public SpotifyAPI initialize() {
        return this;
    }

    @Override
    public boolean isInitialized() {
        return false;
    }

    @Override
    public Track getTrack() {
        return null; // TODO Implement OSX SpotifyAPI
    }

    @Override
    public int getPosition() {
        return 0; // TODO Implement OSX SpotifyAPI
    }

    @Override
    public boolean hasPosition() {
        return false; // TODO Implement OSX SpotifyAPI
    }

    @Override
    public void pressMediaKey(MediaKey mediaKey) {

    }

    @Override
    public boolean isPlaying() {
        return false; // TODO Implement OSX SpotifyAPI
    }

    @Override
    public boolean isConnected() {
        return false; // TODO Implement OSX SpotifyAPI
    }

    @Override
    public void stop() {

    }

}
