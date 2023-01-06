import de.labystudio.spotifyapi.platform.windows.api.WinProcess;
import de.labystudio.spotifyapi.platform.windows.api.jna.Psapi;
import de.labystudio.spotifyapi.platform.windows.api.playback.PlaybackAccessor;

public class SpotifyProcessTest {

    private static final byte[] PREFIX_CONTEXT = new byte[]{0x63, 0x6F, 0x6E, 0x74, 0x65, 0x78, 0x74};

    public static void main(String[] args) {
        WinProcess process = new WinProcess("Spotify.exe");
        Psapi.ModuleInfo moduleInfo = process.getModuleInfo("chrome_elf.dll");
        System.out.println("chrome_elf.dll address: 0x" + Long.toHexString(moduleInfo.getBaseOfDll()));

        long addressTrackId = process.findAddressOfText(moduleInfo.getBaseOfDll(), "spotify:track:", 0);
        System.out.println("Track Id Address: 0x" + Long.toHexString(addressTrackId));

        long addressPlayBack = process.findInMemory(
                0,
                addressTrackId,
                PREFIX_CONTEXT,
                (address, index) -> {
                    PlaybackAccessor accessor = new PlaybackAccessor(process, address);
                    return accessor.isValid();
                }
        );
        System.out.println("Playback Address: 0x" + Long.toHexString(addressPlayBack));
    }

}
