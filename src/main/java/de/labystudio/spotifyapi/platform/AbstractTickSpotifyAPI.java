package de.labystudio.spotifyapi.platform;

import de.labystudio.spotifyapi.SpotifyAPI;
import de.labystudio.spotifyapi.SpotifyListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;

/**
 * Abstract tick class for SpotifyAPI implementations.
 *
 * @author LabyStudio
 */
public abstract class AbstractTickSpotifyAPI implements SpotifyAPI {

    /**
     * The list of all Spotify listeners.
     */
    protected final List<SpotifyListener> listeners = new ArrayList<>();

    private ScheduledFuture<?> task;

    /**
     * Initialize the SpotifyAPI abstract tick implementation.
     * It will create a task that will update the current track and position every second.
     *
     * @return the initialized SpotifyAPI
     * @throws IllegalStateException if the API is already initialized
     */
    public SpotifyAPI initialize() {
        if (this.isInitialized()) {
            throw new IllegalStateException("The SpotifyAPI is already initialized");
        }

        // Initial tick
        this.onTick();

        // Start task to update every second
        this.task = Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(
                this::onTick, 1, 1, java.util.concurrent.TimeUnit.SECONDS
        );

        return this;
    }

    protected abstract void onTick();

    @Override
    public void registerListener(SpotifyListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void unregisterListener(SpotifyListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public boolean isInitialized() {
        return this.task != null;
    }

    @Override
    public void stop() {
        if (this.task != null) {
            this.task.cancel(true);
            this.task = null;
        }
    }
}
