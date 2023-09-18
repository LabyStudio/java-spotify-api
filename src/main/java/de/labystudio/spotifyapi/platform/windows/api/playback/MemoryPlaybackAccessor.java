package de.labystudio.spotifyapi.platform.windows.api.playback;

import de.labystudio.spotifyapi.platform.windows.api.WinProcess;

/**
 * Accessor to read the duration, position and playing state from the Spotify process.
 *
 * @author LabyStudio
 */
public class MemoryPlaybackAccessor implements PlaybackAccessor {

    private static final long MIN_TRACK_DURATION = 1000; // 1 second
    private static final long MAX_TRACK_DURATION = 1000 * 60 * 10; // 10 minutes

    private final WinProcess process;
    private final PointerRegistry pointerRegistry;

    private int length;
    private int position;
    private boolean isPlaying;

    /**
     * Creates a new instance of the PlaybackAccessor.
     *
     * @param process The Spotify process to read from.
     * @param address The reference address of the playback section
     */
    public MemoryPlaybackAccessor(WinProcess process, long address) {
        this.process = process;

        // Create pointer registry to calculate the absolute addresses using the relative offsets
        this.pointerRegistry = new PointerRegistry(0x0AD59F08, address);
        this.pointerRegistry.register("position", 0x0AD5A290);
        this.pointerRegistry.register("length", 0x0AD5A2A0);
        this.pointerRegistry.register("is_playing", 0x0AD5A2D8); // 1=true, 0=false

        this.update();
    }

    /**
     * Read the current length, position and playing state from the Spotify process.
     *
     * @return true if the new values are valid, false otherwise
     */
    @Override
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
    @Override
    public boolean isValid() {
        return this.position <= this.length
                && this.position >= 0
                && this.length <= MAX_TRACK_DURATION
                && this.length >= MIN_TRACK_DURATION;
    }

    @Override
    public int getLength() {
        return this.length;
    }

    @Override
    public int getPosition() {
        return this.position;
    }

    @Override
    public boolean isPlaying() {
        return this.isPlaying;
    }

}
