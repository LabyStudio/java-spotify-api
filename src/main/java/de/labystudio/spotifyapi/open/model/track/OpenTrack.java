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

	/**
	 * Joins the artists to a single string.
	 *
	 * @return the artists name, split with comma
	 */
	public String getArtists() {
		if (this.joinedArtists == null) {
			this.joinedArtists = this.getArtists(", ");
		}

		return this.joinedArtists;
	}

	/**
	 * Joins the artists to a single string.
	 *
	 * @param delimiter The delimiter to split the artists with
	 * @return the artists name, split the provided delimiter
	 */
	public String getArtists(String delimiter) {
		if (this.artists == null || this.artists.isEmpty()) {
			return null;
		}

		StringBuilder builder = new StringBuilder();
		for (Artist artist : this.artists) {
			builder.append(delimiter);
			builder.append(artist.name);
		}

		return builder.substring(delimiter.length());
	}

	/**
	 * Create a new {@link Track} based on the current object.
	 *
	 * @return The new {@link Track} object
	 */
	public Track toTrack() {
		return new Track(this.id, this.name, this.getArtists(), this.durationMs);
	}
}
