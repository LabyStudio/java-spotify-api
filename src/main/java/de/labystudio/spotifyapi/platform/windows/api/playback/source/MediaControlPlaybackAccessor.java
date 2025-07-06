package de.labystudio.spotifyapi.platform.windows.api.playback.source;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.PointerByReference;
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

    private String title;
    private String artist;

    private byte[] coverArt;

    public MediaControlPlaybackAccessor(WindowsMediaControl mediaControl) {
        if (mediaControl == null) {
            throw new IllegalArgumentException("MediaControl cannot be null");
        }
        this.mediaControl = mediaControl;
    }

    @Override
    public void updatePlayback() {
        this.playbackPosition = this.mediaControl.getPlaybackPosition();
        if (this.playbackPosition == -1) {
            throw new IllegalStateException("Playback information unavailable");
        }

        int isPlaying = this.mediaControl.isPlaying();
        if( isPlaying < 0) {
            throw new IllegalStateException("Failed to retrieve playback state");
        }
        this.isPlaying = isPlaying == 1; // Convert to boolean (1 = playing, 0 = not playing)
    }

    @Override
    public void updateTrack() {
        this.trackDuration = this.mediaControl.getTrackDuration();
        if (this.trackDuration <= 0) {
            throw new IllegalStateException("Track duration is invalid or unavailable");
        }

        // Get the track title
        Pointer titlePtr = this.mediaControl.getTrackTitle();
        if (titlePtr == null) {
            throw new IllegalStateException("Track title pointer is null");
        }
        this.title = titlePtr.getString(0, "UTF-8");
        this.mediaControl.freeString(titlePtr);

        // Get the artist name
        Pointer artistPtr = this.mediaControl.getArtistName();
        if (artistPtr == null) {
            throw new IllegalStateException("Artist name pointer is null");
        }
        this.artist = artistPtr.getString(0, "UTF-8");
        this.mediaControl.freeString(artistPtr);

        // Get the cover art
        PointerByReference bufferRef = new PointerByReference();
        NativeLongByReference lengthRef = new NativeLongByReference();
        if (this.mediaControl.getCoverArt(bufferRef, lengthRef)) {
            Pointer buffer = bufferRef.getValue();

            if (buffer == null) {
                this.coverArt = null; // No cover art available
            } else {
                int length = lengthRef.getValue().intValue();
                this.coverArt = buffer.getByteArray(0, length);
                this.mediaControl.freeCoverArt(buffer);
            }
        }
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

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public String getArtist() {
        return this.artist;
    }

    @Override
    public byte[] getCoverArt() {
        return this.coverArt != null ? this.coverArt : null;
    }
}
