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
    private boolean isPlaying;

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

            Track track = new Track(
                    trackId,
                    trackName,
                    trackArtist,
                    trackLength,
                    null // TODO: Add cover art support if possible
            );
            this.currentTrack = track;
            this.clock.resync();

            // Fire on track changed
            this.listeners.forEach(listener -> listener.onTrackChanged(track));
        }

        // Handle is playing changes
        boolean isPlaying = this.appleScript.getPlayerState();
        if (isPlaying != this.isPlaying) {
            this.isPlaying = isPlaying;

            // Fire on play back changed
            this.listeners.forEach(listener -> listener.onPlayBackChanged(isPlaying));
        }

        // Handle position changes
        long before = this.clock.now();
        int position = this.appleScript.getPlayerPosition();
        long after = this.clock.now();
        this.updatePosition(position, before + (after - before) / 2, isPlaying); // half the round trip

        // Fire keep alive
        this.listeners.forEach(SpotifyListener::onSync);
    }

    @Override
    public void stop() {
        super.stop();

        this.connected = false;
        this.currentTrack = null;
        this.isPlaying = false;
        this.clock.reset();
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
        return (int) this.clock.getPosition();
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
        return this.clock.hasPosition();
    }

}
