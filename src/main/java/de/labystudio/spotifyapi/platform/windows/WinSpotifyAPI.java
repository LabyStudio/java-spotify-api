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
    private boolean isPlaying;

    private long lastTimePositionUpdated;
    private boolean positionKnown = false;
    private long lastAccessorPosition = -1;
    private long lastTimeSynced;


    /**
     * Updates the current track, position and playback state.
     * If the process is not connected, it will try to connect to the Spotify process.
     */
    protected void onTick() throws Exception {
        if (!this.isConnected()) {
            // Connect
            this.process = new SpotifyProcess();

            // Fire on connect
            this.listeners.forEach(SpotifyListener::onConnect);
        }

        PlaybackAccessor playback = this.process.getPlaybackAccessor();
        String trackId = this.process.getTrackId();

        // Update playback status and check if it is valid
        if (!playback.update() || !this.process.isTrackIdValid(trackId)) {
            this.positionKnown = false;
            this.currentPosition = -1;
            throw new IllegalStateException("Could not update playback");
        }

        // Handle track changes
        if (!Objects.equals(trackId, this.currentTrack == null ? null : this.currentTrack.getId())) {
            // Wait two seconds before updating the track, so Spotify has enough time to update the playback
            if ((!this.hasTrack() || playback.getLength() == this.currentTrack.getLength())
                    && playback.getPosition() >= this.currentPosition
                    && System.currentTimeMillis() - this.lastTimeSynced < 2000) {
                return;
            }

            SpotifyTitle title = this.process.getTitle();
            if (title != SpotifyTitle.UNKNOWN) {
                int trackLength = playback.getLength();
                boolean isFirstTrack = !this.hasTrack();

                Track track = new Track(trackId, title.getTrackName(), title.getTrackArtist(), trackLength);
                this.currentTrack = track;

                // Fire on track changed
                this.listeners.forEach(listener -> listener.onTrackChanged(track));

                // Reset position on song change
                if (!isFirstTrack) {
                    this.updatePosition(0, true);
                }
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
        if (position != this.lastAccessorPosition) {
            this.lastAccessorPosition = position;
            this.updatePosition(position, false);
        }

        // Fire keep alive
        this.listeners.forEach(SpotifyListener::onSync);
        this.lastTimeSynced = System.currentTimeMillis();
    }

    private void updatePosition(int position, boolean force) {
        if (position == this.currentPosition && !force) {
            return;
        }

        // The position is known if the song is currently paused
        // or if the position has changed during the runtime of the program.
        // (The position update is also called when the memory content is empty)
        this.positionKnown = force || this.currentPosition != -1 || !this.isPlaying;

        // Update position
        this.currentPosition = position;
        this.lastTimePositionUpdated = System.currentTimeMillis();

        // Fire on position changed if the position is known
        if (this.positionKnown) {
            this.listeners.forEach(listener -> listener.onPositionChanged(position));
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
        return this.positionKnown;
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
    }

}
