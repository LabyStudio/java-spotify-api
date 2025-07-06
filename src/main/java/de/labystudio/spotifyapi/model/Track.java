package de.labystudio.spotifyapi.model;

import java.awt.image.BufferedImage;

/**
 * A Spotify track containing the track id, name, artist and length of a song.
 *
 * @author LabyStudio
 */
public class Track {

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
}
