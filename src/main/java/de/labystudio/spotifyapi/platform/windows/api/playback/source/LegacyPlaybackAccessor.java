package de.labystudio.spotifyapi.platform.windows.api.playback.source;

import de.labystudio.spotifyapi.platform.windows.api.playback.PlaybackAccessor;
import de.labystudio.spotifyapi.platform.windows.api.spotify.SpotifyProcess;
import de.labystudio.spotifyapi.platform.windows.api.spotify.SpotifyWindowTitle;

/**
 * Accessor to read the playback state from the Spotify process.
 * It uses a pseudo method to determine if Spotify is playing based on the window title.
 *
 * @author LabyStudio
 * @deprecated Scheduled for removal in future versions since we have a more reliable method using the Windows Media Control API.
 */
public class LegacyPlaybackAccessor implements PlaybackAccessor {

    private final SpotifyProcess spotifyProcess;

    private boolean playing;
    private SpotifyWindowTitle windowTitle = SpotifyWindowTitle.UNKNOWN;

    public LegacyPlaybackAccessor(SpotifyProcess spotifyProcess) {
        this.spotifyProcess = spotifyProcess;
    }

    @Override
    public void updatePlayback() {
        SpotifyWindowTitle windowTitle = SpotifyWindowTitle.of(this.spotifyProcess.getWindowTitle());
        if (windowTitle == null) {
            this.playing = false;
        } else {
            this.windowTitle = windowTitle;
            this.playing = true;
        }
    }

    @Override
    public void updateTrack() {
        // Nothing to do here, as the track information is derived from the window title
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

    @Override
    public String getTitle() {
        return this.windowTitle.getTrackName();
    }

    @Override
    public String getArtist() {
        return this.windowTitle.getTrackArtist();
    }

    @Override
    public byte[] getCoverArt() {
        return null;
    }
}
