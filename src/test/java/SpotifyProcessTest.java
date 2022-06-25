import de.labystudio.spotifyapi.platform.windows.api.WinProcess;
import de.labystudio.spotifyapi.platform.windows.api.playback.PlaybackAccessor;

public class SpotifyProcessTest {

    private static final byte[] PREFIX_CONTEXT = new byte[]{0x63, 0x6F, 0x6E, 0x74, 0x65, 0x78, 0x74};

    public static void main(String[] args) {
        WinProcess process = new WinProcess("Spotify.exe");

        long addressTrackId = process.findAddressOfText(
                process.getMaxContentAddress() / 2,
                "spotify:track:",
                (address, index) -> process.hasBytes(address + 1028, 0xDC, 0xA1)
        );
        System.out.println("Track Id Address: " + Long.toHexString(addressTrackId));

        long addressPlayBack = process.findInMemory(
                0,
                addressTrackId,
                PREFIX_CONTEXT,
                (address, index) -> {
                    PlaybackAccessor accessor = new PlaybackAccessor(process, address);
                    return accessor.isValid();
                }
        );
        System.out.println("Playback Address: " + Long.toHexString(addressPlayBack));

    }

}
