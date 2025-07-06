package de.labystudio.spotifyapi.platform.windows.api.spotify;

/**
 * This class represents the title of the Spotify application.
 * It contains the currently played track name and track artist if the song is playing.
 * <p>
 * If the song is not playing, the track name and artist are unknown.
 * It uses {@link #UNKNOWN} for the unknown track name and artist.
 *
 * @author LabyStudio
 */
public class SpotifyWindowTitle {

    /**
     * The delimiter used by the title to separate the track name and artist.
     */
    public static final String DELIMITER = " - ";

    /**
     * The unknown track name and artist.
     * Required if the song is paused or no track is playing.
     */
    public static final SpotifyWindowTitle UNKNOWN = new SpotifyWindowTitle("Unknown", "No song playing");

    private final String name;
    private final String artist;

    public SpotifyWindowTitle(String name, String artist) {
        this.name = name;
        this.artist = artist;
    }

    public String getTrackName() {
        return this.name;
    }

    public String getTrackArtist() {
        return this.artist;
    }

    @Override
    public String toString() {
        return this.name + DELIMITER + this.artist;
    }

    /**
     * Create a SpotifyTitle from a title bar string.
     * It splits the title bar string into the track name and artist using the {@link #DELIMITER}.
     *
     * @param title the title bar string
     * @return the SpotifyTitle
     */
    public static SpotifyWindowTitle of(String title) {
        if (!title.contains(DELIMITER)) {
            return null;
        }

        String[] split = title.split(DELIMITER);
        return new SpotifyWindowTitle(split[1], split[0]);
    }
}
