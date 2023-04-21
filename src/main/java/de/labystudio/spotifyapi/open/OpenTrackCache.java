package de.labystudio.spotifyapi.open;

import de.labystudio.spotifyapi.model.Track;
import de.labystudio.spotifyapi.open.model.track.OpenTrack;

import java.util.ArrayList;
import java.util.List;

public class OpenTrackCache {

		private final List<OpenTrack> tracks = new ArrayList<>();
		private int size;

		public OpenTrackCache(int size) {
			this.size = size;
		}

		public void setSize(int size) {
			this.size = size;
		}

		public void add(OpenTrack track) {
			if (this.tracks.size() >= this.size) {
				this.tracks.remove(0);
			}

			this.tracks.add(track);
		}

		public boolean has(Track track) {
			return this.has(track.getId());
		}

		public boolean has(String trackId) {
			return this.get(trackId) != null;
		}

		public OpenTrack get(Track track) {
			return this.get(track.getId());
		}

		public OpenTrack get(String trackId) {
			for (OpenTrack track : this.tracks) {
				if (track.id.equals(trackId)) {
					return track;
				}
			}

			return null;
		}

		public void clear() {
			this.tracks.clear();
		}
}
