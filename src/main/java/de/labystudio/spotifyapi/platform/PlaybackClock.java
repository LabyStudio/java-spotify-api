package de.labystudio.spotifyapi.platform;

import java.util.function.LongSupplier;

public class PlaybackClock {

    public static final long SEEK_THRESHOLD = 1000L; // ms, a bigger drift counts as a seek
    public static final long RESYNC_THRESHOLD = 60L; // ms, below this we only nudge the anchor

    private final LongSupplier nanoTime;

    private long anchorPosition = -1; // ms, -1 = unknown
    private long anchorNanos;
    private boolean playing;
    private boolean resync; // next update re-anchors and reports, set on track change
    private long length = -1; // ms, -1 = no clamp

    public PlaybackClock() {
        this(System::nanoTime);
    }

    public PlaybackClock(LongSupplier nanoTime) {
        this.nanoTime = nanoTime;
    }

    public long now() {
        return this.nanoTime.getAsLong();
    }

    /**
     * Feed a position measured at the given time.
     *
     * @param position       the measured position in milliseconds
     * @param sampledAtNanos the {@link #now()} value the measurement belongs to
     * @param playing        true if the track was playing at that time
     * @return true if the position was unknown before or jumped by more than {@link #SEEK_THRESHOLD}
     */
    public synchronized boolean update(long position, long sampledAtNanos, boolean playing) {
        if (this.anchorPosition < 0 || this.resync) {
            this.anchor(position, sampledAtNanos, playing);
            return true;
        }

        long drift = position - this.positionAt(sampledAtNanos);
        if (playing != this.playing || Math.abs(drift) > RESYNC_THRESHOLD) {
            this.anchor(position, sampledAtNanos, playing);
        } else {
            this.anchorPosition += drift / 4; // smooths out query jitter
        }
        return Math.abs(drift) > SEEK_THRESHOLD;
    }

    public synchronized void setLength(long length) {
        this.length = length;
    }

    public synchronized void resync() {
        this.resync = true;
    }

    public synchronized void reset() {
        this.anchorPosition = -1;
        this.playing = false;
        this.resync = false;
    }

    public synchronized boolean hasPosition() {
        return this.anchorPosition >= 0;
    }

    public synchronized long getPosition() {
        if (this.anchorPosition < 0) {
            return -1;
        }
        return this.positionAt(this.nanoTime.getAsLong());
    }

    private void anchor(long position, long sampledAtNanos, boolean playing) {
        this.anchorPosition = Math.max(0, position);
        this.anchorNanos = sampledAtNanos;
        this.playing = playing;
        this.resync = false;
    }

    private long positionAt(long nanos) {
        if (!this.playing) {
            return this.anchorPosition;
        }
        long position = this.anchorPosition + (nanos - this.anchorNanos) / 1_000_000L;
        return this.length > 0 ? Math.min(position, this.length) : position;
    }
}
