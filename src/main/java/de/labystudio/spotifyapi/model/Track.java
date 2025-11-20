package de.labystudio.spotifyapi.model;

import java.awt.image.BufferedImage;

/**
 * A Spotify track containing the track id, name, artist and length of a song.
 *
 * @author LabyStudio
 */
public class Track {

    public static final int ID_LENGTH = 22;

    private final String id;
    private final String name;
    private final String artist;

    private final int length;

    private final BufferedImage coverArt;

    public Track(
            String id,
            String name,
            String artist,
            int length,
            BufferedImage coverArt
    ) {
        this.id = id;
        this.name = name;
        this.artist = artist;
        this.length = length;
        this.coverArt = coverArt;
    }

    public boolean isIdValid() {
        return isTrackIdValid(this.id);
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getArtist() {
        return this.artist;
    }

    public int getLength() {
        return this.length;
    }

    public BufferedImage getCoverArt() {
        return this.coverArt;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Track && this.id.equals(((Track) obj).id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s", this.id, this.name, this.artist);
    }

    /**
     * Checks if the given track ID is valid.
     * A track ID is valid if there are no characters with a value of zero.
     * It also has to be exactly 22 characters long.
     *
     * @param trackId The track ID to check.
     * @return True if the track ID is valid, false otherwise.
     */
    public static boolean isTrackIdValid(String trackId) {
        if (trackId == null) {
            return false;
        }

        for (char c : trackId.toCharArray()) {
            boolean isValidCharacter = c >= 'a' && c <= 'z'
                    || c >= 'A' && c <= 'Z'
                    || c >= '0' && c <= '9';
            if (!isValidCharacter) {
                return false;
            }
        }
        return !trackId.contains(" ") && trackId.length() == ID_LENGTH;
    }
}
