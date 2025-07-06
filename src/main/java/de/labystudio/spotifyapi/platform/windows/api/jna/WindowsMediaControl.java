package de.labystudio.spotifyapi.platform.windows.api.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.PointerByReference;

import java.nio.file.Path;

public interface WindowsMediaControl extends Library {

    boolean isSpotifyAvailable();

    long getPlaybackPosition();

    long getTrackDuration();

    Pointer getTrackTitle();

    Pointer getArtistName();

    int isPlaying();

    boolean getCoverArt(PointerByReference outPtr, NativeLongByReference outLen);

    void freeString(Pointer str);

    void freeCoverArt(Pointer ptr);

    static WindowsMediaControl loadLibrary(Path dllPath) {
        return Native.load(dllPath.toAbsolutePath().toString(), WindowsMediaControl.class);
    }
}