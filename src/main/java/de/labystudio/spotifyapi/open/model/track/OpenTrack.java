package de.labystudio.spotifyapi.open.model.track;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * OpenTrack model that parses Spotify's GraphQL trackUnion response
 */
public class OpenTrack {

    public String id;
    public String name;
    public String uri;

    @SerializedName("duration")
    public Duration duration;

    @SerializedName("albumOfTrack")
    public AlbumOfTrack album;

    @SerializedName("firstArtist")
    public FirstArtist artistsData;

    @SerializedName("contentRating")
    public ContentRating contentRating;

    @SerializedName("playcount")
    public String playcount;

    private transient String joinedArtists;
    private transient List<Artist> artistsList;

    public static class Duration {
        @SerializedName("totalMilliseconds")
        public Integer durationMs;
    }

    public static class ContentRating {
        public String label;

        public boolean isExplicit() {
            return "EXPLICIT".equals(this.label);
        }
    }

    public static class AlbumOfTrack {
        public String id;
        public String name;
        public String uri;

        @SerializedName("coverArt")
        public CoverArt coverArt;

        public static class CoverArt {
            public List<Image> sources;
        }

        /**
         * Get album images (alias for coverArt.sources for backward compatibility)
         */
        public List<Image> getImages() {
            return this.coverArt != null ? this.coverArt.sources : null;
        }
    }

    public static class FirstArtist {
        public List<ArtistItem> items;

        public static class ArtistItem {
            public String id;
            public String uri;
            public Profile profile;

            public static class Profile {
                public String name;
            }
        }
    }

    /**
     * Get duration in milliseconds
     */
    public Integer getDurationMs() {
        return this.duration != null ? this.duration.durationMs : null;
    }

    /**
     * Check if track is explicit
     */
    public boolean isExplicit() {
        return this.contentRating != null && this.contentRating.isExplicit();
    }

    /**
     * Get artists as a list (converts from GraphQL structure)
     */
    public List<Artist> getArtists() {
        if (this.artistsList == null && this.artistsData != null && this.artistsData.items != null) {
            this.artistsList = new java.util.ArrayList<>();
            for (FirstArtist.ArtistItem item : this.artistsData.items) {
                Artist artist = new Artist();
                artist.id = item.id;
                artist.uri = item.uri;
                if (item.profile != null) {
                    artist.name = item.profile.name;
                }
                this.artistsList.add(artist);
            }
        }
        return this.artistsList;
    }

    /**
     * Joins the artists to a single string.
     *
     * @return the artists name, split with comma
     */
    public String getArtistsString() {
        if (this.joinedArtists == null) {
            this.joinedArtists = this.getArtistsString(", ");
        }
        return this.joinedArtists;
    }

    /**
     * Joins the artists to a single string.
     *
     * @param delimiter The delimiter to split the artists with
     * @return the artists name, split the provided delimiter
     */
    public String getArtistsString(String delimiter) {
        List<Artist> artists = this.getArtists();
        if (artists == null || artists.isEmpty()) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        for (Artist artist : artists) {
            builder.append(delimiter);
            builder.append(artist.name);
        }

        return builder.substring(delimiter.length());
    }
}
