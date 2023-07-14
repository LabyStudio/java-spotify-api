package de.labystudio.spotifyapi.platform.windows.api.playback;

import de.labystudio.spotifyapi.platform.windows.api.spotify.SpotifyProcess;

public class PseudoPlaybackAccessor implements PlaybackAccessor {

    private final SpotifyProcess spotifyProcess;
    private boolean playing;

    public PseudoPlaybackAccessor(SpotifyProcess spotifyProcess) {
        this.spotifyProcess = spotifyProcess;
    }

    @Override
    public boolean update() {
        this.playing = this.spotifyProcess.isPlayingUsingTitle();
        return true;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public int getLength() {
        return -1;
    }

    @Override
    public int getPosition() {
        return -1;
    }

    @Override
    public boolean isPlaying() {
        return this.playing;
    }
}
