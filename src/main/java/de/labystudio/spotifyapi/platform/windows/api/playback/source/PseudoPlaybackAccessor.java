package de.labystudio.spotifyapi.platform.windows.api.playback.source;

import de.labystudio.spotifyapi.platform.windows.api.playback.PlaybackAccessor;
import de.labystudio.spotifyapi.platform.windows.api.spotify.SpotifyProcess;

/**
 * Accessor to read the playback state from the Spotify process.
 * It uses a pseudo method to determine if Spotify is playing based on the window title.
 *
 * @author LabyStudio
 */
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
