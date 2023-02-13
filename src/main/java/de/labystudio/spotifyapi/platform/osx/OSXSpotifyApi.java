package de.labystudio.spotifyapi.platform.osx;

import de.labystudio.spotifyapi.SpotifyListener;
import de.labystudio.spotifyapi.model.MediaKey;
import de.labystudio.spotifyapi.model.Track;
import de.labystudio.spotifyapi.platform.AbstractTickSpotifyAPI;
import de.labystudio.spotifyapi.platform.osx.api.spotify.SpotifyAppleScript;

import java.util.Objects;

/**
 * OSX implementation of the SpotifyAPI.
 * It uses the AppleScript API to access the Spotify application.
 *
 * @author LabyStudio
 */
public class OSXSpotifyApi extends AbstractTickSpotifyAPI {

    private final SpotifyAppleScript appleScript = new SpotifyAppleScript();

    private boolean connected = false;

    private Track currentTrack;
    private int currentPosition = -1;
    private boolean isPlaying;

    private long lastTimePositionUpdated;

    @Override
    protected void onTick() throws Exception {
        String trackId = this.appleScript.getTrackId();

        // Handle on connect
        if (!this.connected && !trackId.isEmpty()) {
            this.connected = true;
            this.listeners.forEach(SpotifyListener::onConnect);
        }

        // Handle track changes
        if (!Objects.equals(trackId, this.currentTrack == null ? null : this.currentTrack.getId())) {
            String trackName = this.appleScript.getTrackName();
            String trackArtist = this.appleScript.getTrackArtist();
            int trackLength = this.appleScript.getTrackLength();

            boolean isFirstTrack = !this.hasTrack();

            Track track = new Track(trackId, trackName, trackArtist, trackLength);
            this.currentTrack = track;

            // Fire on track changed
            this.listeners.forEach(listener -> listener.onTrackChanged(track));

            // Reset position on song change
            if (!isFirstTrack) {
                this.updatePosition(0);
            }
        }

        // Handle is playing changes
        boolean isPlaying = this.appleScript.getPlayerState();
        if (isPlaying != this.isPlaying) {
            this.isPlaying = isPlaying;

            // Fire on play back changed
            this.listeners.forEach(listener -> listener.onPlayBackChanged(isPlaying));
        }

        // Handle position changes
        int position = this.appleScript.getPlayerPosition();
        if (!this.hasPosition() || Math.abs(position - this.getPosition()) > 1000) {
            this.updatePosition(position);
        }

        // Fire keep alive
        this.listeners.forEach(SpotifyListener::onSync);
    }

    @Override
    public void stop() {
        super.stop();
        this.connected = false;
    }

    private void updatePosition(int position) {
        if (position == this.currentPosition) {
            return;
        }

        // Update position known state
        this.currentPosition = position;
        this.lastTimePositionUpdated = System.currentTimeMillis();

        // Fire on position changed
        this.listeners.forEach(listener -> listener.onPositionChanged(position));
    }

    @Override
    public void pressMediaKey(MediaKey mediaKey) {
        try {
            switch (mediaKey) {
                case PLAY_PAUSE:
                    this.appleScript.playPause();
                    break;
                case NEXT:
                    this.appleScript.nextTrack();
                    break;
                case PREV:
                    this.appleScript.previousTrack();
                    break;
            }
        } catch (Exception e) {
            this.listeners.forEach(listener -> listener.onDisconnect(e));
            this.connected = false;
        }
    }

    @Override
    public int getPosition() {
        if (!this.hasPosition()) {
            throw new IllegalStateException("Position is not known yet");
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
    public Track getTrack() {
        return this.currentTrack;
    }

    @Override
    public boolean isPlaying() {
        return this.isPlaying;
    }

    @Override
    public boolean isConnected() {
        return this.connected;
    }

    @Override
    public boolean hasPosition() {
        return this.currentPosition != -1;
    }

}
