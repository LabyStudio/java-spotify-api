package de.labystudio.spotifyapi.platform.windows.api.playback.source;

import de.labystudio.spotifyapi.platform.windows.api.jna.WindowsMediaControl;
import de.labystudio.spotifyapi.platform.windows.api.playback.PlaybackAccessor;

/**
 * Accessor to read the duration, position and playing state from the Spotify process.
 * It uses the Windows Media Control API to retrieve playback information.
 *
 * @author LabyStudio
 */
public class MediaControlPlaybackAccessor implements PlaybackAccessor {

    private final WindowsMediaControl mediaControl;

    private long playbackPosition;
    private long trackDuration;
    private boolean isPlaying;

    public MediaControlPlaybackAccessor(WindowsMediaControl mediaControl) {
        this.mediaControl = mediaControl;
    }

    @Override
    public boolean update() {
        this.playbackPosition = this.mediaControl.getPlaybackPosition();
        this.trackDuration = this.mediaControl.getTrackDuration();
        this.isPlaying = this.mediaControl.isPlaying();
        return true;
    }

    @Override
    public boolean isValid() {
        return this.playbackPosition >= 0 && this.trackDuration > 0 && this.playbackPosition <= this.trackDuration;
    }

    @Override
    public int getLength() {
        return (int) this.trackDuration;
    }

    @Override
    public int getPosition() {
        return (int) this.playbackPosition;
    }

    @Override
    public boolean isPlaying() {
        return this.isPlaying;
    }
}
