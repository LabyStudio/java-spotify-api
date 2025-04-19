package de.labystudio.spotifyapi.platform;

import de.labystudio.spotifyapi.SpotifyAPI;
import de.labystudio.spotifyapi.SpotifyListener;
import de.labystudio.spotifyapi.config.SpotifyConfiguration;
import de.labystudio.spotifyapi.open.OpenSpotifyAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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

    private OpenSpotifyAPI openAPI;

    private SpotifyConfiguration configuration;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private ScheduledFuture<?> task;

    private long timeLastException = -1;

    /**
     * Initialize the SpotifyAPI abstract tick implementation.
     * It will create a task that will update the current track and position every second.
     *
     * @return the initialized SpotifyAPI
     * @throws IllegalStateException if the API is already initialized or has been shutdown
     */
    @Override
    public SpotifyAPI initialize(SpotifyConfiguration configuration) {
        synchronized (this) {
            this.configuration = configuration;

            if (this.executor.isShutdown()) {
                throw new IllegalStateException("This SpotifyAPI has been shutdown and cannot be reused");
            }

            if (this.isInitialized()) {
                throw new IllegalStateException("This SpotifyAPI is already initialized");
            }

            // Start task to update every second
            this.task = this.executor.scheduleWithFixedDelay(this::onInternalTick, 0L, 1L, TimeUnit.SECONDS);
        }
        return this;
    }

    protected void onInternalTick() {
        try {
            // Check if we passed the exception timeout
            long timeSinceLastException = System.currentTimeMillis() - this.timeLastException;
            if (timeSinceLastException < this.configuration.getExceptionReconnectDelay()) {
                return;
            }

            this.onTick();
        } catch (Exception e) {
            this.timeLastException = System.currentTimeMillis();
            this.stop();

            // Fire on disconnect
            this.listeners.forEach(listener -> listener.onDisconnect(e));

            // Restart the process
            if (this.configuration.isAutoReconnect()) {
                this.initialize(this.configuration);
            }
        }
    }

    protected abstract void onTick() throws Exception;

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
    public SpotifyConfiguration getConfiguration() {
        return this.configuration;
    }

    @Override
    public final OpenSpotifyAPI getOpenAPI() {
        if (this.openAPI == null) {
            this.openAPI = new OpenSpotifyAPI();
        }
        return this.openAPI;
    }

    @Override
    public void stop() {
        synchronized (this) {
            if (this.task != null) {
                this.task.cancel(true);
                this.task = null;
            }
        }
    }

    @Override
    public void shutdown() {
        this.stop();
        this.executor.shutdownNow();
    }
}
