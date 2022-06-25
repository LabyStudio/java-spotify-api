package de.labystudio.spotifyapi.platform.windows.api.spotify;

import de.labystudio.spotifyapi.platform.windows.api.WinProcess;
import de.labystudio.spotifyapi.platform.windows.api.playback.PlaybackAccessor;

/**
 * This class represents the Spotify Windows application.
 *
 * @author LabyStudio
 */
public class SpotifyProcess extends WinProcess {

    private static final byte[] PREFIX_TRACK_ID = "spotify:track:".getBytes();
    private static final long CHROME_ELF_ADDRESS = 0x5f710000L;
    private static final long CHROME_ELF_SIZE = 0x5f710000L;

    private static final byte[] PREFIX_CONTEXT = new byte[]{0x63, 0x6F, 0x6E, 0x74, 0x65, 0x78, 0x74, 0x00};
    private static final long CONTEXT_ADDRESS = 0x0794E42C;
    private static final long CONTEXT_SIZE = CHROME_ELF_ADDRESS;

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

        // Check if the song is currently playing using the title bar
        boolean isPlaying = this.isPlayingUsingTitle();

        // Find addresses of track id and playback states
        this.addressTrackId = this.findInMemory(CHROME_ELF_ADDRESS, CHROME_ELF_SIZE, PREFIX_TRACK_ID);
        this.addressPlayBack = this.findInMemory(
                CONTEXT_ADDRESS,
                CONTEXT_SIZE,
                PREFIX_CONTEXT,
                address -> {
                    PlaybackAccessor accessor = new PlaybackAccessor(this, address);
                    return accessor.isValid() && accessor.isPlaying() == isPlaying; // Check if address is valid
                }
        );

        // Check if we have found the addresses
        if (this.addressTrackId == -1) {
            throw new IllegalStateException("Could not find track id in memory");
        }
        if (this.addressPlayBack == -1) {
            throw new IllegalStateException("Could not find playback in memory");
        }

        // Create the playback accessor with the found address
        this.playbackAccessor = new PlaybackAccessor(this, this.addressPlayBack);
    }

    /**
     * Read the track id from the memory.
     *
     * @return the track id without the prefix "spotify:track:"
     */
    public String getTrackId() {
        return new String(this.readBytes(this.addressTrackId + 14, 22));
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
}
