package de.labystudio.spotifyapi.platform.linux;

import de.labystudio.spotifyapi.SpotifyListener;
import de.labystudio.spotifyapi.model.MediaKey;
import de.labystudio.spotifyapi.model.Track;
import de.labystudio.spotifyapi.platform.AbstractTickSpotifyAPI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import static de.labystudio.spotifyapi.platform.linux.api.MetadataParser.*;

/**
 * Linux implementation of the SpotifyAPI.
 * It uses the playerctl to access the Spotify's media control and metadata.
 *
 * @author holybaechu
 * Thanks for LabyStudio for many code snippets.
 */
public class LinuxSpotifyApi extends AbstractTickSpotifyAPI {
    private boolean connected = false;

    private Track currentTrack;
    private int currentPosition = -1;
    private boolean isPlaying;

    private long lastTimePositionUpdated;

    public static String executeShellCommand(String command) {
        StringBuilder output = new StringBuilder();

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
            Process process = processBuilder.start();
            process.waitFor();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return output.toString().substring(output.toString().indexOf('\n')+1);
    }

    @Override
    protected void onTick() {
        String commandResult = executeShellCommand("dbus-send --print-reply --dest=org.mpris.MediaPlayer2.spotify   /org/mpris/MediaPlayer2 org.freedesktop.DBus.Properties.Get   string:'org.mpris.MediaPlayer2.Player'   string:'Metadata'");
        Map<String, Object> metadata = parse(commandResult);

        String trackId = ((String) metadata.get("mpris:trackid")).split("/")[4];

        // Handle on connect
        if (!this.connected && !trackId.isEmpty()) {
            this.connected = true;
            this.listeners.forEach(SpotifyListener::onConnect);
        }

        // Handle track changes
        if (!Objects.equals(trackId, this.currentTrack == null ? null : this.currentTrack.getId())) {
            String trackName = metadata.get("xesam:title").toString();
            String trackArtist = String.join(", ", (ArrayList) metadata.get("xesam:artist"));
            int trackLength = Integer.parseInt(String.valueOf(metadata.get("mpris:length"))) / 1000;

            boolean isFirstTrack = !this.hasTrack();

            Track track = new Track(trackId, trackName, trackArtist, trackLength);
            this.currentTrack = track;

            // Fire on track changed
            this.listeners.forEach(listener -> listener.onTrackChanged(track));

            // Reset position on song change
            if (!isFirstTrack) {
                this.updatePosition(0);
            }
        }

        // Handle is playing changes
        boolean isPlaying = parseValueFromString(executeShellCommand("dbus-send --print-reply --dest=org.mpris.MediaPlayer2.spotify   /org/mpris/MediaPlayer2 org.freedesktop.DBus.Properties.Get   string:'org.mpris.MediaPlayer2.Player'   string:'PlaybackStatus'").trim().replaceFirst("variant       ", "")).equals("Playing");
        if (isPlaying != this.isPlaying) {
            this.isPlaying = isPlaying;

            // Fire on play back changed
            this.listeners.forEach(listener -> listener.onPlayBackChanged(isPlaying));
        }


        this.updatePosition((int) Math.floor(Float.parseFloat((String) parseValueFromString(executeShellCommand("dbus-send --print-reply --dest=org.mpris.MediaPlayer2.spotify   /org/mpris/MediaPlayer2 org.freedesktop.DBus.Properties.Get   string:'org.mpris.MediaPlayer2.Player'   string:'Position'").trim().replaceFirst("variant       ", "")))) / 1000);

        // Fire keep alive
        this.listeners.forEach(SpotifyListener::onSync);
    }

    @Override
    public void stop() {
        super.stop();
        this.connected = false;
    }

    private void updatePosition(int position) {
        if (position == this.currentPosition) {
            return;
        }

        // Update position known state
        this.currentPosition = position;
        this.lastTimePositionUpdated = System.currentTimeMillis();

        // Fire on position changed
        this.listeners.forEach(listener -> listener.onPositionChanged(position));
    }

    @Override
    public void pressMediaKey(MediaKey mediaKey) {
        try {
            switch (mediaKey) {
                case PLAY_PAUSE:
                    executeShellCommand("dbus-send --print-reply --dest=org.mpris.MediaPlayer2.spotify /org/mpris/MediaPlayer2 org.mpris.MediaPlayer2.Player.PlayPause");
                    break;
                case NEXT:
                    executeShellCommand("dbus-send --print-reply --dest=org.mpris.MediaPlayer2.spotify /org/mpris/MediaPlayer2 org.mpris.MediaPlayer2.Player.Next");
                    break;
                case PREV:
                    executeShellCommand("dbus-send --print-reply --dest=org.mpris.MediaPlayer2.spotify /org/mpris/MediaPlayer2 org.mpris.MediaPlayer2.Player.Previous");
                    break;
            }
        } catch (Exception e) {
            this.listeners.forEach(listener -> listener.onDisconnect(e));
            this.connected = false;
        }
    }

    @Override
    public int getPosition() {
        if (!this.hasPosition()) {
            throw new IllegalStateException("Position is not known yet");
        }

        if (this.isPlaying) {
            // Interpolate position
            long timePassed = System.currentTimeMillis() - this.lastTimePositionUpdated;
            return this.currentPosition + (int) timePassed;
        } else {
            return this.currentPosition;
        }
    }

    @Override
    public Track getTrack() {
        return this.currentTrack;
    }

    @Override
    public boolean isPlaying() {
        return this.isPlaying;
    }

    @Override
    public boolean isConnected() {
        return this.connected;
    }

    @Override
    public boolean hasPosition() {
        return this.currentPosition != -1;
    }

}
