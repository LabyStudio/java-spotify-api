package platform.windows;

import com.sun.jna.Pointer;
import de.labystudio.spotifyapi.platform.windows.api.jna.WindowsMediaControl;

import java.nio.file.Path;
import java.nio.file.Paths;

public class WindowMediaSessionTest {

    public static void main(String[] args) {
        Path dllPath = Paths.get("src/main/resources/natives/windows-x64/windowsmediacontrol.dll");
        WindowsMediaControl media = WindowsMediaControl.loadLibrary(dllPath);

        System.out.println("Playback Position: " + media.getPlaybackPosition());
        System.out.println("Track Duration: " + media.getTrackDuration());
        System.out.println("Is Playing: " + media.isPlaying());

        Pointer trackTitle = media.getTrackTitle();
        Pointer artistName = media.getArtistName();

        System.out.println("Track Title: " + trackTitle.getString(0, "UTF-8"));
        System.out.println("Artist Name: " + artistName.getString(0, "UTF-8"));

        // Free the strings if necessary
        media.freeString(trackTitle);
        media.freeString(artistName);
    }

}
