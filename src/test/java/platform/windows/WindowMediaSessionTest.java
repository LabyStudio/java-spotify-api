package platform.windows;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.PointerByReference;
import de.labystudio.spotifyapi.platform.windows.api.jna.WindowsMediaControl;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WindowMediaSessionTest {

    public static void main(String[] args) throws IOException {
        Path dllPath = Paths.get("src/main/resources/natives/windows-x64/windowsmediacontrol.dll");
        WindowsMediaControl media = WindowsMediaControl.loadLibrary(dllPath);

        if (!media.isSpotifyAvailable()) {
            System.out.println("Spotify is not available.");
            return;
        }

        System.out.println("Playback Position: " + media.getPlaybackPosition());
        System.out.println("Track Duration: " + media.getTrackDuration());
        System.out.println("Is Playing: " + media.isPlaying());

        Pointer trackTitle = media.getTrackTitle();
        Pointer artistName = media.getArtistName();

        System.out.println("Track Title: " + (trackTitle == null ? null : trackTitle.getString(0, "UTF-8")));
        System.out.println("Artist Name: " + (trackTitle == null ? null : artistName.getString(0, "UTF-8")));

        media.freeString(trackTitle);
        media.freeString(artistName);

        PointerByReference bufferRef = new PointerByReference();
        NativeLongByReference lengthRef = new NativeLongByReference();

        if (media.getCoverArt(bufferRef, lengthRef)) {
            Pointer buffer = bufferRef.getValue();
            int length = lengthRef.getValue().intValue();
            byte[] coverArtBytes = buffer.getByteArray(0, length);
            BufferedImage coverArt = ImageIO.read(new ByteArrayInputStream(coverArtBytes));

            System.out.println("Cover: " + coverArt.getWidth() + "x" + coverArt.getHeight());

            media.freeCoverArt(buffer);
        } else {
            System.out.println("Cover: unavailable");
        }
    }

}
