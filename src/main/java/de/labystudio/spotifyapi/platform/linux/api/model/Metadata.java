package de.labystudio.spotifyapi.platform.linux.api.model;

import java.util.Map;

public class Metadata {

    private final String trackId;
    private final String trackName;
    private final String[] artists;
    private final int trackLength;
    private final String artUrl;

    public Metadata(Map<String, Object> metadata) {
        this.trackId = ((String) metadata.get("mpris:trackid")).split("/")[4];
        this.trackName = metadata.get("xesam:title").toString();
        this.artists = (String[]) metadata.get("xesam:artist");
        this.trackLength = (int) ((Long) metadata.get("mpris:length") / 1000L) + 1;
        this.artUrl = (String) metadata.get("mpris:artUrl");
    }

    public String getTrackId() {
        return this.trackId;
    }

    public String getTrackName() {
        return this.trackName;
    }

    public String[] getArtists() {
        return this.artists;
    }

    public String getArtistsJoined() {
        return String.join(", ", this.artists);
    }

    public int getTrackLength() {
        return this.trackLength;
    }

    public String getArtUrl() {
        return this.artUrl;
    }
}
