package de.labystudio.spotifyapi.platform.windows.api.spotify;

import de.labystudio.spotifyapi.model.Track;
import de.labystudio.spotifyapi.platform.windows.api.WinProcess;
import de.labystudio.spotifyapi.platform.windows.api.jna.Psapi;
import de.labystudio.spotifyapi.platform.windows.api.jna.WindowsMediaControl;
import de.labystudio.spotifyapi.platform.windows.api.playback.PlaybackAccessor;
import de.labystudio.spotifyapi.platform.windows.api.playback.source.LegacyPlaybackAccessor;
import de.labystudio.spotifyapi.platform.windows.api.playback.source.MediaControlPlaybackAccessor;

/**
 * This class represents the Spotify Windows application.
 *
 * @author LabyStudio
 */
public class SpotifyProcess extends WinProcess {

    private static final boolean DEBUG = System.getProperty("SPOTIFY_API_DEBUG") != null;

    // Spotify track id
    private static final String PREFIX_SPOTIFY_TRACK = "spotify:track:";
    private static final long[] OFFSETS_TRACK_ID = {
            0x18bc90, // 64-Bit (1.2.66.447.g4e37e896)
            0x154A60, // 64-Bit (1.2.26.1187.g36b715a1)
            0x14FA30, // 64-Bit (1.2.21.1104.g42cf0a50)
            0x106198, // 32-Bit (1.2.21.1104.g42cf0a50)
            0x14C9F0, // 64-Bit (Old)
            0x102178, // 32-Bit (Old)
            0x1499F0, // 64-Bit (Old)
            0xFEFE8 // 32-Bit (Old)
    };

    private final long addressTrackId;
    private final PlaybackAccessor playbackAccessor;

    /**
     * Creates a new instance of the {@link SpotifyProcess} class.
     * It will immediately try to connect to the Spotify application.
     *
     * @throws IllegalStateException if the Spotify process could not be found.
     */
    public SpotifyProcess(WindowsMediaControl mediaControl) {
        super("Spotify.exe");

        if (DEBUG) {
            System.out.println("Spotify process loaded! Searching for addresses...");
        }

        long timeScanStart = System.currentTimeMillis();

        // Find the track id address in the memory
        this.addressTrackId = this.findTrackIdAddress();

        if (DEBUG) {
            System.out.println("Scanning took " + (System.currentTimeMillis() - timeScanStart) + "ms");
        }

        PlaybackAccessor accessor;
        try {
            if (mediaControl == null) {
                throw new IllegalArgumentException("MediaControl not available");
            }

            // Create accessor for playback control
            accessor = new MediaControlPlaybackAccessor(mediaControl);
        } catch (Throwable e) {
            e.printStackTrace();

            // We can continue without Media Control access but some features may not work
            accessor = new LegacyPlaybackAccessor(this);
        }
        this.playbackAccessor = accessor;
    }

    private long findTrackIdAddress() {
        Psapi.ModuleInfo chromeElfModule = this.getModuleInfo("chrome_elf.dll");
        if (chromeElfModule == null) {
            throw new IllegalStateException("Could not find chrome_elf.dll module");
        }

        // Find address of track id (Located in the chrome_elf.dll module)
        long chromeElfAddress = chromeElfModule.getBaseOfDll();

        // Check all offsets for valid track id
        long addressTrackId = -1;
        long minTrackIdOffset = Long.MAX_VALUE;
        long maxTrackIdOffset = Long.MIN_VALUE;
        for (long trackIdOffset : OFFSETS_TRACK_ID) {
            // Get min and max of hardcoded offset
            minTrackIdOffset = Math.min(minTrackIdOffset, trackIdOffset);
            maxTrackIdOffset = Math.max(maxTrackIdOffset, trackIdOffset);

            // Check if the hardcoded offset is valid
            long targetAddressTrackId = chromeElfAddress + trackIdOffset;
            if (Track.isTrackIdValid(this.readTrackId(targetAddressTrackId))) {
                // If the offset works, exit the loop
                addressTrackId = targetAddressTrackId;
                break;
            }
        }

        // If the hardcoded offsets are not valid, try to find it dynamically
        if (addressTrackId == -1) {
            if (DEBUG) {
                System.out.println("Could not find track id with hardcoded offsets. Trying to find it dynamically...");
            }

            long threshold = (maxTrackIdOffset - minTrackIdOffset) * 3;
            long scanAddressFrom = chromeElfAddress + minTrackIdOffset - threshold;
            long scanAddressTo = chromeElfAddress + maxTrackIdOffset + threshold;
            addressTrackId = this.findAddressOfText(scanAddressFrom, scanAddressTo, PREFIX_SPOTIFY_TRACK, (address, index) -> {
                return Track.isTrackIdValid(this.readTrackId(address));
            });
        }

        if (addressTrackId == -1) {
            throw new IllegalStateException("Could not find track id in memory");
        }

        if (DEBUG) {
            System.out.printf(
                    "Found track id address: %s (+%s) [%s%s]%n",
                    Long.toHexString(addressTrackId),
                    Long.toHexString(addressTrackId - chromeElfAddress),
                    PREFIX_SPOTIFY_TRACK,
                    this.readTrackId(addressTrackId)
            );
        }
        return addressTrackId;
    }

    /**
     * Read the track id from the memory.
     *
     * @param address The address where the prefix "spotify:track:" starts
     * @return the track id without the prefix "spotify:track:"
     */
    private String readTrackId(long address) {
        return this.readString(address + 14, 22);
    }

    /**
     * Read the track id from the memory.
     *
     * @return the track id without the prefix "spotify:track:"
     */
    public String readTrackId() {
        return this.readTrackId(this.addressTrackId);
    }

    public PlaybackAccessor getPlaybackAccessor() {
        return this.playbackAccessor;
    }
}