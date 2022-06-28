package de.labystudio.spotifyapi.platform.osx.api.spotify;

import de.labystudio.spotifyapi.platform.osx.api.Action;
import de.labystudio.spotifyapi.platform.osx.api.AppleScript;

/**
 * Spotify AppleScript API.
 * Wraps all necessary AppleScript commands to interact with the Spotify application.
 *
 * @author LabyStudio
 */
public class SpotifyAppleScript extends AppleScript {

    public static final Action CURRENT_TRACK = new Action("current", "track");

    public static final Action ID = new Action("id");
    public static final Action NAME = new Action("name");
    public static final Action ARTIST = new Action("artist");
    public static final Action LENGTH = new Action("duration");

    public static final Action PLAYER_POSITION = new Action("player", "position");
    public static final Action PLAYER_STATE = new Action("player", "state");

    public static final Action PLAY_PAUSE = new Action("playpause");
    public static final Action NEXT_TRACK = new Action("next", "track");
    public static final Action PREVIOUS_TRACK = new Action("previous", "track");

    public SpotifyAppleScript() {
        super("Spotify");
    }

    /**
     * Get the current track ID without the "spotify:track:" prefix.
     *
     * @return The current track ID
     * @throws Exception If the request failed
     */
    public String getTrackId() throws Exception {
        return this.getOf(ID, CURRENT_TRACK).substring(14);
    }

    /**
     * Get the current track name.
     *
     * @return The current track name
     * @throws Exception If the request failed
     */
    public String getTrackName() throws Exception {
        return this.getOf(NAME, CURRENT_TRACK);
    }

    /**
     * Get the current track artist.
     *
     * @return The current track artist
     * @throws Exception If the request failed
     */
    public String getTrackArtist() throws Exception {
        return this.getOf(ARTIST, CURRENT_TRACK);
    }

    /**
     * Get the current track length in seconds.
     *
     * @return The current track length in seconds
     * @throws Exception If the request failed
     */
    public int getTrackLength() throws Exception {
        return Integer.parseInt(this.getOf(LENGTH, CURRENT_TRACK));
    }

    /**
     * Get the current track position in milliseconds.
     *
     * @return The current track position in milliseconds
     * @throws Exception If the request failed
     */
    public int getPlayerPosition() throws Exception {
        return (int) (Double.parseDouble(this.get(PLAYER_POSITION)) * 1000);
    }

    /**
     * Get the current player state.
     * It returns true if the current track is playing, false otherwise.
     *
     * @return The current player state (true if playing, false otherwise)
     * @throws Exception If the request failed
     */
    public boolean getPlayerState() throws Exception {
        return this.get(PLAYER_STATE).equals("playing");
    }

    /**
     * Play or pause the current track.
     *
     * @throws Exception If the request failed
     */
    public void playPause() throws Exception {
        this.execute(PLAY_PAUSE);
    }

    /**
     * Skip to the next track.
     *
     * @throws Exception If the request failed
     */
    public void nextTrack() throws Exception {
        this.execute(NEXT_TRACK);
    }

    /**
     * Skip to the previous track.
     *
     * @throws Exception If the request failed
     */
    public void previousTrack() throws Exception {
        this.execute(PREVIOUS_TRACK);
    }

}
