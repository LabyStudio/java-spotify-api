package de.labystudio.spotifyapi.platform.windows.api.playback;

public interface PlaybackAccessor {

    void updatePlayback();

    void updateTrack();

    boolean isValid();

    int getLength();

    int getPosition();

    /**
     * Returns the time the position was reported at, as unix epoch milliseconds.
     * Returns -1 if the source has no timestamp for the position.
     *
     * @return the time the position was reported at or -1
     */
    default long getPositionTimestamp() {
        return -1;
    }

    boolean isPlaying();

    String getTitle();

    String getArtist();

    byte[] getCoverArt();

    default boolean hasTrackLength() {
        return this.getLength() > 0;
    }

    default boolean hasTrackPosition() {
        return this.getPosition() >= 0 && this.hasTrackLength() && this.getPosition() <= this.getLength();
    }
}
