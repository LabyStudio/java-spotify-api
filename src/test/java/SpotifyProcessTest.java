import de.labystudio.spotifyapi.platform.windows.api.WinProcess;
import de.labystudio.spotifyapi.platform.windows.api.jna.Psapi;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class SpotifyProcessTest {

    public static void main(String[] args) {
        WinProcess process = new WinProcess("Spotify.exe");
        Psapi.ModuleInfo moduleInfo = process.getModuleInfo("chrome_elf.dll");
        System.out.println("chrome_elf.dll address: 0x" + Long.toHexString(moduleInfo.getBaseOfDll()));

        long addressTrackId = process.findAddressOfText(moduleInfo.getBaseOfDll(), "spotify:track:", 0);
        System.out.println("Track Id Address: 0x" + Long.toHexString(addressTrackId));
    }

    public static void printModules(WinProcess process, long targetAddress) {
        List<Module> modules = new ArrayList<>();
        for (Map.Entry<String, Psapi.ModuleInfo> entry : process.getModules().entrySet()) {
            modules.add(new Module(entry.getKey(), entry.getValue().getBaseOfDll()));
        }
        modules.sort(Comparator.comparingLong(o -> o.address));

        int passed = 0;
        for (Module entry : modules) {
            long entryPoint = entry.address;
            if (entryPoint > targetAddress) {
                if (passed == 0) {
                    System.out.println(Long.toHexString(targetAddress) + " <-TARGET ------------------------");
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
