package de.labystudio.spotifyapi.platform.linux;

import de.labystudio.spotifyapi.SpotifyListener;
import de.labystudio.spotifyapi.model.MediaKey;
import de.labystudio.spotifyapi.model.Track;
import de.labystudio.spotifyapi.platform.AbstractTickSpotifyAPI;
import de.labystudio.spotifyapi.platform.linux.api.MPRISCommunicator;

import java.util.Objects;

/**
 * Linux implementation of the SpotifyAPI.
 * It uses the MPRIS to access the Spotify's media control and metadata.
 *
 * @author holybaechu, LabyStudio
 * Thanks for LabyStudio for many code snippets.
 */
public class LinuxSpotifyApi extends AbstractTickSpotifyAPI {

    private boolean connected = false;

    private Track currentTrack;
    private int currentPosition = -1;
    private boolean isPlaying;

    private long lastTimePositionUpdated;

    private final MPRISCommunicator mediaPlayer = new MPRISCommunicator();

    @Override
    protected void onTick() throws Exception {
        String trackId = this.mediaPlayer.getTrackId();

        // Handle on connect
        if (!this.connected && !trackId.isEmpty()) {
            this.connected = true;
            this.listeners.forEach(SpotifyListener::onConnect);
        }

        // Handle track changes
        if (!Objects.equals(trackId, this.currentTrack == null ? null : this.currentTrack.getId())) {
            String trackName = this.mediaPlayer.getTrackName();
            String trackArtist = this.mediaPlayer.getArtist();
            int trackLength = this.mediaPlayer.getTrackLength();

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
        boolean isPlaying = this.mediaPlayer.isPlaying();
        if (isPlaying != this.isPlaying) {
            this.isPlaying = isPlaying;

            // Fire on play back changed
            this.listeners.forEach(listener -> listener.onPlayBackChanged(isPlaying));
        }

        // Handle position changes
        int position = this.mediaPlayer.getPosition();
        if (!this.hasPosition() || Math.abs(position - this.getPosition()) >= 0) {
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
                    this.mediaPlayer.playPause();
                    break;
                case NEXT:
                    this.mediaPlayer.next();
                    break;
                case PREV:
                    this.mediaPlayer.previous();
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
