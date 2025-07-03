package de.labystudio.spotifyapi.platform.windows;

import de.labystudio.spotifyapi.SpotifyListener;
import de.labystudio.spotifyapi.model.MediaKey;
import de.labystudio.spotifyapi.model.Track;
import de.labystudio.spotifyapi.platform.AbstractTickSpotifyAPI;
import de.labystudio.spotifyapi.platform.windows.api.WinApi;
import de.labystudio.spotifyapi.platform.windows.api.playback.PlaybackAccessor;
import de.labystudio.spotifyapi.platform.windows.api.spotify.SpotifyProcess;
import de.labystudio.spotifyapi.platform.windows.api.spotify.SpotifyTitle;

import java.util.Objects;

/**
 * Windows implementation of the SpotifyAPI.
 * The implementation uses the Windows API to access the memory of the Spotify process.
 * The currently playing track name and artist are read from the Windows title bar.
 *
 * @author LabyStudio
 */
public class WinSpotifyAPI extends AbstractTickSpotifyAPI {

    private SpotifyProcess process;

    private Track currentTrack;
    private int currentPosition = -1;
    private boolean hasTrackPosition = false;
    private boolean isPlaying;

    private long lastTimePositionUpdated;
    private long prevLastReportedPosition = -1;

    /**
     * Updates the current track, position and playback state.
     * If the process is not connected, it will try to connect to the Spotify process.
     */
    protected void onTick() {
        if (!this.isConnected()) {
            // Connect
            this.process = new SpotifyProcess(this.configuration);

            // Fire on connect
            this.listeners.forEach(SpotifyListener::onConnect);
        }

        // Read track id and check if track id is valid
        String trackId = this.process.readTrackId();
        if (!this.process.isTrackIdValid(trackId)) {
            throw new IllegalStateException("Invalid track ID: " + trackId);
        }

        // Update playback state
        PlaybackAccessor playback = this.process.getMainPlaybackAccessor();
        PlaybackAccessor pseudoPlayback = this.process.getPseudoPlaybackAccessor();
        if (!playback.update() && pseudoPlayback.update()) {
            playback = pseudoPlayback; // Fallback to pseudo playback if main playback fails
        }

        // Handle track changes
        String currentTrackId = this.currentTrack == null ? null : this.currentTrack.getId();
        if (!Objects.equals(trackId, currentTrackId)) {
            SpotifyTitle title = this.process.getTitle();
            if (title != SpotifyTitle.UNKNOWN) {
                Track track = new Track(
                        trackId,
                        title.getTrackName(),
                        title.getTrackArtist(),
                        playback.getLength()
                );
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

        if (playback.hasTrackPosition()) {
            this.hasTrackPosition = true;

            int lastReportedPosition = playback.getPosition();

            if (this.prevLastReportedPosition != lastReportedPosition) {
                this.prevLastReportedPosition = lastReportedPosition;

                // Get the daemon position (Last reported position + relative time)
                int expectedPosition = this.getPosition();

                // Compare if the expected position based on time and the last reported position are close enough
                boolean seeked = Math.abs(lastReportedPosition - expectedPosition) > TICK_INTERVAL;

                this.currentPosition = lastReportedPosition;
                this.lastTimePositionUpdated = System.currentTimeMillis();

                // Fire on position changed
                if (seeked) {
                    this.listeners.forEach(listener -> listener.onPositionChanged(this.currentPosition));
                }
            }
        } else {
            this.currentPosition = -1;
            this.hasTrackPosition = false;
            this.lastTimePositionUpdated = System.currentTimeMillis();
        }

        // Fire keep alive
        this.listeners.forEach(SpotifyListener::onSync);
    }

    @Override
    public Track getTrack() {
        return this.currentTrack;
    }

    @Override
    public int getPosition() {
        if (!this.hasPosition()) {
            throw new IllegalStateException("Position is not known yet. Pause the song for a second and try again.");
        }

        if (this.isPlaying) {
            // Interpolate position
            long timePassed = System.currentTimeMillis() - this.lastTimePositionUpdated;
            long interpolatedPosition = this.currentPosition + timePassed;

            if (this.hasTrack()) {
                return (int) Math.min(interpolatedPosition, this.currentTrack.getLength());
            } else {
                return (int) interpolatedPosition;
            }
        } else {
            return this.currentPosition;
        }
    }

    @Override
    public boolean hasPosition() {
        if (!this.isConnected()) {
            return false;
        }
        return this.hasTrackPosition;
    }

    @Override
    public void pressMediaKey(MediaKey mediaKey) {
        if (!this.isConnected()) {
            throw new IllegalStateException("Spotify is not connected");
        }

        switch (mediaKey) {
            case NEXT:
                this.process.pressKey(WinApi.VK_MEDIA_NEXT_TRACK);
                break;
            case PREV:
                this.process.pressKey(WinApi.VK_MEDIA_PREV_TRACK);
                break;
            case PLAY_PAUSE:
                this.process.pressKey(WinApi.VK_MEDIA_PLAY_PAUSE);
                break;
        }

        // Update state immediately
        this.onInternalTick();
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

        if (this.process != null) {
            this.process.close();
            this.process = null;
        }

        this.currentTrack = null;
        this.currentPosition = -1;
        this.hasTrackPosition = false;
    }

}
