package de.labystudio.spotifyapi.platform.windows;

import de.labystudio.spotifyapi.SpotifyAPI;
import de.labystudio.spotifyapi.platform.windows.api.playback.PlaybackAccessor;
import de.labystudio.spotifyapi.platform.windows.api.spotify.SpotifyTitle;
import de.labystudio.spotifyapi.SpotifyListener;
import de.labystudio.spotifyapi.model.Track;
import de.labystudio.spotifyapi.platform.AbstractSpotifyAPI;
import de.labystudio.spotifyapi.platform.windows.api.spotify.SpotifyProcess;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;

/**
 * Windows implementation of the SpotifyAPI.
 * The implementation uses the Windows API to access the memory of the Spotify process.
 * The currently playing track name and artist are read from the Windows title bar.
 *
 * @author LabyStudio
 */
public class WinSpotifyAPI extends AbstractSpotifyAPI {

    private SpotifyProcess process;

    private Track currentTrack;
    private int currentPosition = -1;
    private boolean isPlaying;

    private long lastTimePositionUpdated;
    private boolean positionKnown = false;

    private ScheduledFuture<?> task;

    /**
     * Initialize the SpotifyAPI Windows implementation.
     * It will create a task that will update the current track and position every second.
     *
     * @return the initialized SpotifyAPI
     * @throws IllegalStateException if the API is already initialized
     */
    public SpotifyAPI initialize() {
        if (this.task != null) {
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

    /**
     * Updates the current track, position and playback state.
     * If the process is not connected, it will try to connect to the Spotify process.
     */
    private void onTick() {
        try {
            if (!this.isConnected()) {
                this.process = new SpotifyProcess();

                // Fire on connect
                this.listeners.forEach(SpotifyListener::onConnect);
            }

            PlaybackAccessor playback = this.process.getPlaybackAccessor();
            String trackId = this.process.getTrackId();

            // Update playback status and check if it is valid
            if (!playback.update() || !this.process.isTrackIdValid(trackId)) {
                throw new IllegalStateException("Could not update playback");
            }

            // Handle track changes
            if (!Objects.equals(trackId, this.currentTrack == null ? null : this.currentTrack.getId())) {
                SpotifyTitle title = this.process.getTitle();
                if (title != SpotifyTitle.UNKNOWN) {
                    int trackLength = playback.getLength();

                    Track track = new Track(trackId, title.getTrackName(), title.getTrackArtist(), trackLength);
                    this.currentTrack = track;

                    // Fire on track changed
                    this.listeners.forEach(listener -> listener.onTrackChanged(track));
                }
            }

            // Handle is playing changes
            boolean isPlaying = playback.isPlaying();
            if (isPlaying != this.isPlaying) {
                this.isPlaying = isPlaying;

                // Fire on play back changed
                this.listeners.forEach(listener -> listener.onPlayBackChanged(isPlaying));
            }

            // Handle position changes
            int position = playback.getPosition();
            if (position != this.currentPosition) {
                // Update position known state
                this.positionKnown = this.currentPosition != -1 || !isPlaying;
                this.currentPosition = position;

                if (this.positionKnown) {
                    this.lastTimePositionUpdated = System.currentTimeMillis();

                    // Fire on position changed
                    this.listeners.forEach(listener -> listener.onPositionChanged(position));
                }
            }

            // Fire keep alive
            this.listeners.forEach(SpotifyListener::onSync);
        } catch (Exception exception) {
            // Fire on disconnect
            this.listeners.forEach(listener -> listener.onDisconnect(exception));
            this.process = null;
        }
    }

    @Override
    public Track getTrack() {
        return this.currentTrack;
    }

    @Override
    public int getPosition() {
        if (!this.positionKnown) {
            throw new IllegalStateException("Position is not known yet. Pause the song for a second and try again.");
        }

        if (this.isPlaying) {
            // Interpolate position
            long timePassed = System.currentTimeMillis() - this.lastTimePositionUpdated;
            return this.currentPosition + (int) timePassed;
        } else {
            return this.currentPosition;
        }
    }

    @Override
    public boolean hasPosition() {
        return this.positionKnown;
    }

    @Override
    public boolean isPlaying() {
        return this.isPlaying;
    }

    @Override
    public boolean isConnected() {
        return this.process != null && this.process.isOpen();
    }

    @Override
    public void stop() {
        super.stop();

        if (this.task != null) {
            this.task.cancel(true);
            this.task = null;
        }

        if (this.process != null) {
            this.process.close();
            this.process = null;
        }
    }

}
