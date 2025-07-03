package de.labystudio.spotifyapi.platform.windows.api.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import java.nio.file.Path;

public interface WindowsMediaControl extends Library {

    long getPlaybackPosition();

    long getTrackDuration();

    Pointer getTrackTitle();

    Pointer getArtistName();

    boolean isPlaying();

    void freeString(Pointer str);

    public static WindowsMediaControl loadLibrary(Path dllPath) {
        return Native.load(dllPath.toAbsolutePath().toString(), WindowsMediaControl.class);
    }

}