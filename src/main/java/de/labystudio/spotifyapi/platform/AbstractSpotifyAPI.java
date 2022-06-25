package de.labystudio.spotifyapi.platform;

import de.labystudio.spotifyapi.SpotifyAPI;
import de.labystudio.spotifyapi.SpotifyListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for SpotifyAPI implementations.
 *
 * @author LabyStudio
 */
public abstract class AbstractSpotifyAPI implements SpotifyAPI {

    /**
     * The list of all Spotify listeners.
     */
    protected final List<SpotifyListener> listeners = new ArrayList<>();

    @Override
    public void registerListener(SpotifyListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void unregisterListener(SpotifyListener listener) {
        this.listeners.remove(listener);
    }
}
