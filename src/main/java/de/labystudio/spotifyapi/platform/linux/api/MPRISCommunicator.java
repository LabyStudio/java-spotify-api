package de.labystudio.spotifyapi.platform.linux.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;

import static de.labystudio.spotifyapi.platform.linux.api.MetadataParser.parse;
import static de.labystudio.spotifyapi.platform.linux.api.MetadataParser.parseValueFromString;

public class MPRISCommunicator {

    public MPRISCommunicator() {

    }

    private final String baseCommand = "dbus-send --print-reply --dest=org.mpris.MediaPlayer2.spotify   /org/mpris/MediaPlayer2 ";

    private Map<String, Object> metadata;

    private String execute(String command) {
        StringBuilder output = new StringBuilder();

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
            Process process = processBuilder.start();
            process.waitFor();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return output.substring(output.toString().indexOf('\n') + 1);
    }

    private void updateMetadata() {
        this.metadata = parse(execute(baseCommand + "org.freedesktop.DBus.Properties.Get   string:'org.mpris.MediaPlayer2.Player'   string:'Metadata'"));
    }

    public String getTrackId(){
        updateMetadata();
        return ((String) metadata.get("mpris:trackid")).split("/")[4];
    }

    public String getTrackName(){
        updateMetadata();
        return metadata.get("xesam:title").toString();
    }

    public String getArtist(){
        updateMetadata();
        return String.join(", ", (ArrayList) metadata.get("xesam:artist"));
    }

    public Integer getTrackLength(){
        updateMetadata();
        return Integer.parseInt(String.valueOf(metadata.get("mpris:length"))) / 1000;
    }

    public boolean isPlaying(){
        return parseValueFromString(execute(baseCommand + "org.freedesktop.DBus.Properties.Get   string:'org.mpris.MediaPlayer2.Player'   string:'PlaybackStatus'").trim().replaceFirst("variant {7}", "")).equals("Playing");
    }

    public Integer getPosition(){
        return (int) Math.floor(Float.parseFloat((String) parseValueFromString(execute(baseCommand + "org.freedesktop.DBus.Properties.Get   string:'org.mpris.MediaPlayer2.Player'   string:'Position'").trim().replaceFirst("variant {7}", "")))) / 1000;
    }

    public void playPause(){
        execute(baseCommand + "org.mpris.MediaPlayer2.Player.PlayPause");
    }

    public void next(){
        execute(baseCommand + "org.mpris.MediaPlayer2.Player.Next");
    }

    public void previous(){
        execute(baseCommand + "org.mpris.MediaPlayer2.Player.Previous");
    }
}
