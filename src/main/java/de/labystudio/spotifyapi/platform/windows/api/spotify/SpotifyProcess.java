package de.labystudio.spotifyapi.platform.windows.api.spotify;

import de.labystudio.spotifyapi.platform.windows.api.WinProcess;
import de.labystudio.spotifyapi.platform.windows.api.jna.Psapi;
import de.labystudio.spotifyapi.platform.windows.api.playback.PlaybackAccessor;

/**
 * This class represents the Spotify Windows application.
 *
 * @author LabyStudio
 */
public class SpotifyProcess extends WinProcess {

    private static final boolean DEBUG = System.getProperty("SPOTIFY_API_DEBUG") != null;

    // Spotify track id
    private static final String PREFIX_SPOTIFY_TRACK = "spotify:track:";
    private static final long ADDRESS_OFFSET_TRACK_ID = 0x1499F0;

    private final long addressTrackId;
    private final long addressPlayBack;

    private final PlaybackAccessor playbackAccessor;

    private SpotifyTitle previousTitle = SpotifyTitle.UNKNOWN;

    /**
     * Creates a new instance of the {@link SpotifyProcess} class.
     * It will immediately try to connect to the Spotify application.
     *
     * @throws IllegalStateException if the Spotify process could not be found.
     */
    public SpotifyProcess() {
        super("Spotify.exe");

        if (DEBUG) {
            System.out.println("Spotify process loaded! Searching for addresses...");
        }

        long timeScanStart = System.currentTimeMillis();

        Psapi.ModuleInfo chromeElfModule = this.getModuleInfo("chrome_elf.dll");
        if (chromeElfModule == null) {
            throw new IllegalStateException("Could not find chrome_elf.dll module");
        }

        // Find address of track id (Located in the chrome_elf.dll module)
        long chromeElfAddress = chromeElfModule.getBaseOfDll();
        this.addressTrackId = chromeElfAddress + ADDRESS_OFFSET_TRACK_ID;

        if (this.addressTrackId == -1 || !this.isTrackIdValid(this.getTrackId())) {
            throw new IllegalStateException("Could not find track id in memory");
        }

        if (DEBUG) {
            System.out.printf(
                    "Found track id address: %s (+%s) [%s%s]%n",
                    Long.toHexString(this.addressTrackId),
                    Long.toHexString(this.addressTrackId - chromeElfAddress),
                    PREFIX_SPOTIFY_TRACK,
                    this.getTrackId()
            );
        }

        // Find addresses of playback states
        this.addressPlayBack = this.findAddressOfText(0, 0x0FFFFFFF, "playlist", (address, index) -> {
            return this.hasText(address + 128, "your_library", "home")
                    && this.hasText(address + 408, "context", "autoplay");
        });
        if (this.addressPlayBack == -1) {
            throw new IllegalStateException("Could not find playback in memory");
        }

        // Create the playback accessor with the found address
        this.playbackAccessor = new PlaybackAccessor(this, this.addressPlayBack);
        if (!this.playbackAccessor.isValid()) {
            throw new IllegalStateException("Could not create playback accessor");
        }

        if (DEBUG) {
            System.out.println("Found playback address at: " + Long.toHexString(this.addressPlayBack));
            System.out.println("Scanning took " + (System.currentTimeMillis() - timeScanStart) + "ms");
        }
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
    public String getTrackId() {
        return this.readTrackId(this.addressTrackId);
    }

    /**
     * Read the playback state from the title bar.
     * <p>
     * If the title bar contains the delimiter " - ", the song is playing.
     *
     * @return true if the song is playing, false if the song is paused
     */
    public boolean isPlayingUsingTitle() {
        return this.getWindowTitle().contains(SpotifyTitle.DELIMITER);
    }

    /**
     * Read the currently playing track name and artist from the title bar.
     * If no song is playing it will return a cached value.
     *
     * @return the currently playing track name and artist
     */
    public SpotifyTitle getTitle() {
        SpotifyTitle title = SpotifyTitle.of(this.getWindowTitle());
        if (title == null) {
            return this.previousTitle;
        }
        return (this.previousTitle = title);
    }

    public PlaybackAccessor getPlaybackAccessor() {
        return this.playbackAccessor;
    }

    public long getAddressTrackId() {
        return this.addressTrackId;
    }

    public long getAddressPlayBack() {
        return this.addressPlayBack;
    }

    /**
     * Checks if the given track ID is valid.
     * A track ID is valid if there are no characters with a value of zero.
     *
     * @param trackId The track ID to check.
     * @return True if the track ID is valid, false otherwise.
     */
    public boolean isTrackIdValid(String trackId) {
        for (char c : trackId.toCharArray()) {
            if (c == 0) {
                return false;
            }
        }
        return true;
    }
}
