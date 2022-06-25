package de.labystudio.spotifyapi.platform.windows.api.playback;

import de.labystudio.spotifyapi.platform.windows.api.spotify.SpotifyProcess;

/**
 * Accessor to read the duration, position and playing state from the Spotify process.
 *
 * @author LabyStudio
 */
public class PlaybackAccessor {

    private static final long MIN_TRACK_DURATION = 1000; // 1 second
    private static final long MAX_TRACK_DURATION = 1000 * 60 * 10; // 10 minutes

    private final SpotifyProcess process;
    private final PointerRegistry pointerRegistry;

    private int length;
    private int position;
    private boolean isPlaying;

    /**
     * Creates a new instance of the PlaybackAccessor.
     *
     * @param process            The Spotify process to read from.
     * @param contextBaseAddress The base address of the context.
     */
    public PlaybackAccessor(SpotifyProcess process, long contextBaseAddress) {
        this.process = process;

        // Create pointer registry to calculate the absolute addresses using the relative offsets
        this.pointerRegistry = new PointerRegistry(0x0D3A2064, contextBaseAddress);
        this.pointerRegistry.register("position", 0x0D3A2178);
        this.pointerRegistry.register("length", 0x0D3A2188);
        this.pointerRegistry.register("is_playing", 0x0D3A21AC);

        // Parity pointers to make sure that we have the correct base address
        this.pointerRegistry.register("parity_1", 0x0D3A2199);
        this.pointerRegistry.register("parity_2", 0x0D3A21A4);
        this.pointerRegistry.register("parity_3", 0x0D3A216E);

        this.update();
    }

    /**
     * Read the current length, position and playing state from the Spotify process.
     *
     * @return true if the new values are valid, false otherwise
     */
    public boolean update() {
        this.position = this.process.readInteger(this.pointerRegistry.getAddress("position"));
        this.length = this.process.readInteger(this.pointerRegistry.getAddress("length"));
        this.isPlaying = this.process.readBoolean(this.pointerRegistry.getAddress("is_playing"));
        return this.isValid();
    }

    /**
     * Checks if the current values are valid.
     * <p>
     * To make sure that we have correct values from the memory address,
     * we have to set some rules what kind of duration is correct.
     * <p>
     * The values are correct if:<br>
     * - position {@literal <}= length<br>
     * - length {@literal >} 0<br>
     * - length {@literal <}= 10 minutes<br>
     * - position {@literal >}= 1 second<br>
     * - the parity bits are correct<br>
     *
     * @return true if the current values are valid, false otherwise
     */
    public boolean isValid() {
        return this.position <= this.length
                && this.position >= 0
                && this.length <= MAX_TRACK_DURATION
                && this.length >= MIN_TRACK_DURATION
                && this.process.readBoolean(this.pointerRegistry.getAddress("parity_1")) != this.isPlaying
                && this.process.readBoolean(this.pointerRegistry.getAddress("parity_2")) != this.isPlaying
                && (this.process.readByte(this.pointerRegistry.getAddress("parity_3")) == 0) != this.isPlaying;
    }

    public int getLength() {
        return this.length;
    }

    public int getPosition() {
        return this.position;
    }

    public boolean isPlaying() {
        return this.isPlaying;
    }

}
