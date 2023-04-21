package de.labystudio.spotifyapi.open;

import de.labystudio.spotifyapi.model.Track;
import de.labystudio.spotifyapi.open.model.track.OpenTrack;

import java.util.ArrayList;
import java.util.List;

public class OpenTrackCache {

	private final List<OpenTrack> tracks = new ArrayList<>();
	private int size;

	/**
	 * Create a new cache with a specific size
	 *
	 * @param size The size of the cache. The cache will remove the oldest entry if the size is reached.
	 */
	public OpenTrackCache(int size) {
		this.size = size;
	}

	/**
	 * Set the maximal amount of entries to cache.
	 *
	 * @param size The maximal amount of entries to cache
	 */
	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * Store a track in the cache
	 * If the max cache size is reached, the oldest entry will be removed.
	 *
	 * @param track The track to add to the cache
	 */
	public void add(OpenTrack track) {
		if (this.tracks.size() >= this.size) {
			this.tracks.remove(0);
		}

		this.tracks.add(track);
	}

	/**
	 * Check if the cache contains a track with the id of the provided track.
	 *
	 * @param track The track to check
	 * @return True if the cache contains the track
	 */
	public boolean has(Track track) {
		return this.has(track.getId());
	}

	/**
	 * Check if the cache contains a track with the provided track id.
	 *
	 * @param trackId The track id to check
	 * @return True if the cache contains the track id
	 */
	public boolean has(String trackId) {
		return this.get(trackId) != null;
	}

	/**
	 * Get a track from the cache by the id of the provided track.
	 *
	 * @param track The track to search the cache for
	 * @return The track from the cache or null if the track is not in the cache
	 */
	public OpenTrack get(Track track) {
		return this.get(track.getId());
	}

	/**
	 * Get a track from the cache by the provided track id.
	 *
	 * @param trackId The track id to search the cache for
	 * @return The track from the cache or null if the track is not in the cache
	 */
	public OpenTrack get(String trackId) {
		for (OpenTrack track : this.tracks) {
			if (track.id.equals(trackId)) {
				return track;
			}
		}

		return null;
	}

	/**
	 * Clear the cache.
	 */
	public void clear() {
		this.tracks.clear();
	}
}
