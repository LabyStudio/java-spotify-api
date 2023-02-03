import de.labystudio.spotifyapi.platform.windows.api.WinProcess;
import de.labystudio.spotifyapi.platform.windows.api.jna.Psapi;
import de.labystudio.spotifyapi.platform.windows.api.playback.PlaybackAccessor;
import de.labystudio.spotifyapi.platform.windows.api.spotify.SpotifyTitle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class SpotifyProcessTest {

    private static final byte[] PREFIX_CONTEXT = new byte[]{0x63, 0x6F, 0x6E, 0x74, 0x65, 0x78, 0x74};

    public static void main(String[] args) {
        WinProcess process = new WinProcess("Spotify.exe");
        Psapi.ModuleInfo moduleInfo = process.getModuleInfo("chrome_elf.dll");
        System.out.println("chrome_elf.dll address: 0x" + Long.toHexString(moduleInfo.getBaseOfDll()));

        long addressTrackId = process.findAddressOfText(moduleInfo.getBaseOfDll(), "spotify:track:", 0);
        System.out.println("Track Id Address: 0x" + Long.toHexString(addressTrackId));

        boolean isPlaying = process.getWindowTitle().contains(SpotifyTitle.DELIMITER);
        long addressPlayBack = process.findInMemory(
                0,
                addressTrackId,
                PREFIX_CONTEXT,
                (address, index) -> {
                    PlaybackAccessor accessor = new PlaybackAccessor(process, address);
                    boolean valid = accessor.isValid() && accessor.isPlaying() == isPlaying; // Check if address is valid

                    // If valid then pull the data again and check if it is still valid
                    if (valid) {
                        accessor.update();
                        return accessor.isValid();
                    }

                    return false;
                }
        );
        if (addressPlayBack == -1) {
            System.out.println("Could not find playback address");
            return;
        }
        System.out.println("Playback Address: 0x" + Long.toHexString(addressPlayBack));

        printModules(process, addressPlayBack);
    }

    public static void printModules(WinProcess process, long addressPlayBack) {
        List<Module> modules = new ArrayList<>();
        for (Map.Entry<String, Psapi.ModuleInfo> entry : process.getModules().entrySet()) {
            modules.add(new Module(entry.getKey(), entry.getValue().getBaseOfDll()));
        }
        modules.sort(Comparator.comparingLong(o -> o.address));

        int passed = 0;
        for (Module entry : modules) {
            long entryPoint = entry.address;
            if (entryPoint > addressPlayBack) {
                if (passed == 0) {
                    System.out.println(Long.toHexString(addressPlayBack) + " CONTEXT ------------------------");
                }
                passed++;
                if (passed > 1) {
                    break;
                }
            }
            System.out.println(Long.toHexString(entryPoint) + " " + entry.name);
        }
    }

    private static class Module {
        private final String name;
        private final long address;

        public Module(String name, long address) {
            this.name = name;
            this.address = address;
        }

        public String getName() {
            return this.name;
        }

        public long getAddress() {
            return this.address;
        }
    }

}
