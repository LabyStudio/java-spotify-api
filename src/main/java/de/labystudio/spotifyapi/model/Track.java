package de.labystudio.spotifyapi.model;

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

    public Track(String id, String name, String artist, int length) {
        this.id = id;
        this.name = name;
        this.artist = artist;
        this.length = length;
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
