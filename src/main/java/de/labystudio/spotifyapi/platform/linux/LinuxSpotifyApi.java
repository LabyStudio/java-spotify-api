package de.labystudio.spotifyapi.platform.linux;

import de.labystudio.spotifyapi.SpotifyListener;
import de.labystudio.spotifyapi.model.MediaKey;
import de.labystudio.spotifyapi.model.Track;
import de.labystudio.spotifyapi.platform.AbstractTickSpotifyAPI;
import de.labystudio.spotifyapi.platform.linux.api.MPRISCommunicator;
import de.labystudio.spotifyapi.platform.linux.api.model.Metadata;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
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
    private boolean isPlaying;

    private final MPRISCommunicator mediaPlayer = new MPRISCommunicator();

    @Override
    protected void onTick() throws Exception {
        Metadata metadata = this.mediaPlayer.readMetadata();
        String trackId = metadata.getTrackId();

        // Handle on connect
        if (!this.connected) {
            this.connected = true;
            this.listeners.forEach(SpotifyListener::onConnect);
        }

        // Handle track changes
        String currentTrackId = this.currentTrack == null ? null : this.currentTrack.getId();
        if (!Objects.equals(trackId, currentTrackId)) {
            String trackName = metadata.getTrackName();
            String trackArtist = metadata.getArtistsJoined();
            int trackLength = metadata.getTrackLength();
            BufferedImage coverArt = this.toBufferedImage(metadata.getArtUrl());

            Track track = new Track(trackId, trackName, trackArtist, trackLength, coverArt);
            this.currentTrack = track;
            this.clock.setLength(trackLength);
            this.clock.resync();

            // Fire on track changed
            this.listeners.forEach(listener -> listener.onTrackChanged(track));
        }

        // Handle is playing changes
        boolean isPlaying = this.mediaPlayer.readIsPlaying();
        if (isPlaying != this.isPlaying) {
            this.isPlaying = isPlaying;

            // Fire on play back changed
            this.listeners.forEach(listener -> listener.onPlayBackChanged(isPlaying));
        }

        // Handle position changes
        long before = this.clock.now();
        int position = this.mediaPlayer.readPosition();
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

    private BufferedImage toBufferedImage(String artUrl) {
        if (artUrl == null || artUrl.isEmpty()) {
            return null; // No cover art available
        }
        try {
            return ImageIO.read(new URL(artUrl));
        } catch (Throwable e) {
            e.printStackTrace();
            return null; // Failed to load cover art
        }
    }

}
