package de.labystudio.spotifyapi.open.model.track;

import com.google.gson.annotations.SerializedName;
import de.labystudio.spotifyapi.model.Track;

import java.util.List;

public class OpenTrack {

    public Album album;

    public List<Artist> artists = null;

    @SerializedName("available_markets")
    public List<Object> availableMarkets = null;

    @SerializedName("disc_number")
    public Integer discNumber;

    @SerializedName("duration_ms")
    public Integer durationMs;

    public Boolean explicit;

    @SerializedName("external_ids")
    public ExternalIds externalIds;

    @SerializedName("external_urls")
    public ExternalUrls externalUrls;

    public String href;

    public String id;

    @SerializedName("is_local")
    public Boolean isLocal;

    public String name;

    public Integer popularity;

    @SerializedName("preview_url")
    public Object previewUrl;

    @SerializedName("track_number")
    public Integer trackNumber;

    public String type;

    public String uri;

		private transient String joinedArtists;

		public String getArtists() {
			if (this.joinedArtists == null) {
				if (this.artists == null || this.artists.isEmpty()) {
					return null;
				}

				StringBuilder builder = new StringBuilder();
				for (Artist artist : this.artists) {
					builder.append(", ");
					builder.append(artist.name);
				}

				this.joinedArtists = builder.substring(2);
			}

			return this.joinedArtists;
		}

		public Track mergeWith(Track target) {
			return new Track(target.getId(), this.name, this.getArtists(), target.getLength());
		}
}
