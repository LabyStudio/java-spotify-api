package de.labystudio.spotifyapi.platform.windows.api.spotify;

import de.labystudio.spotifyapi.platform.windows.api.WinProcess;
import de.labystudio.spotifyapi.platform.windows.api.playback.PlaybackAccessor;

/**
 * This class represents the Spotify Windows application.
 *
 * @author LabyStudio
 */
public class SpotifyProcess extends WinProcess {

    private static final byte[] PREFIX_CONTEXT = new byte[]{0x63, 0x6F, 0x6E, 0x74, 0x65, 0x78, 0x74};

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

        // Get the highest addresses of the modules
        long highestAddress = this.getMaxProcessAddress();

        // Find addresses of playback states (Located in the chrome_elf.dll module)
        this.addressTrackId = this.findAddressUsingPath(
                "This program cannot be run in DOS mode",
                "This program cannot be run in DOS mode",
                "chrome_elf.dll",
                "spotify:track:"
        );
        if (this.addressTrackId == -1 || !this.isTrackIdValid(this.getTrackId())) {
            throw new IllegalStateException("Could not find track id in memory");
        }

        // Check if the song is currently playing using the title bar
        boolean isPlaying = this.isPlayingUsingTitle();

        // Find addresses of track id
        this.addressPlayBack = this.findInMemory(
                0,
                highestAddress,
                PREFIX_CONTEXT,
                (address, index) -> {
                    PlaybackAccessor accessor = new PlaybackAccessor(this, address);
                    return accessor.isValid() && accessor.isPlaying() == isPlaying; // Check if address is valid
                }
        );
        if (this.addressPlayBack == -1) {
            throw new IllegalStateException("Could not find playback in memory");
        }

        // Create the playback accessor with the found address
        this.playbackAccessor = new PlaybackAccessor(this, this.addressPlayBack);
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
