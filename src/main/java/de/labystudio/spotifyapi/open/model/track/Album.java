
package de.labystudio.spotifyapi.open.model.track;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Album {

    @SerializedName("album_type")
    public String albumType;

    public List<Artist> artists = null;

    @SerializedName("available_markets")
    public List<Object> availableMarkets = null;

    @SerializedName("external_urls")
    public ExternalUrls externalUrls;

    public String href;
    public String id;
    public List<Image> images = null;
    public String name;

    @SerializedName("release_date")
    public String releaseDate;

    @SerializedName("release_date_precision")
    public String releaseDatePrecision;

    @SerializedName("total_tracks")
    public Integer totalTracks;

    public String type;
    public String uri;

}
