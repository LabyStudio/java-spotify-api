package de.labystudio.spotifyapi.platform.windows.api.playback;

public interface PlaybackAccessor {

    void updatePlayback();

    void updateTrack();

    boolean isValid();

    int getLength();

    int getPosition();

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
