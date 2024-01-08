package de.labystudio.spotifyapi.platform.linux.api;

import java.util.List;

public class MPRISCommunicator {

    private static final Parameter PARAM_DEST = new Parameter("dest", "org.mpris.MediaPlayer2.spotify");

    private static final InterfaceMember INTERFACE_PLAY_PAUSE = new InterfaceMember("org.mpris.MediaPlayer2.Player.PlayPause");
    private static final InterfaceMember INTERFACE_NEXT = new InterfaceMember("org.mpris.MediaPlayer2.Player.Next");
    private static final InterfaceMember INTERFACE_PREVIOUS = new InterfaceMember("org.mpris.MediaPlayer2.Player.Previous");

    private final DBusSend dbus = new DBusSend(
            new Parameter[]{
                    PARAM_DEST
            },
            "/org/mpris/MediaPlayer2"
    );

    private DBusResponse metadata;

    private void updateMetadata() throws Exception {
        this.metadata = this.dbus.get("org.mpris.MediaPlayer2.Player", "Metadata");
    }

    public String getTrackId() throws Exception {
        this.updateMetadata();
        return ((String) this.metadata.get("mpris:trackid")).split("/")[4];
    }

    public String getTrackName() throws Exception {
        this.updateMetadata();
        return this.metadata.get("xesam:title").toString();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public String getArtist() throws Exception {
        this.updateMetadata();
        return String.join(", ", (List) this.metadata.get("xesam:artist"));
    }

    public Integer getTrackLength() throws Exception {
        this.updateMetadata();
        return Integer.parseInt(String.valueOf(this.metadata.get("mpris:length"))) / 1000;
    }

    public boolean isPlaying() throws Exception {
        return this.dbus.get("org.mpris.MediaPlayer2.Player", "PlaybackStatus").get("Playing").equals("Playing");
    }

    public Integer getPosition() throws Exception {
        return Integer.parseInt(String.valueOf(this.dbus.get("org.mpris.MediaPlayer2.Player", "Position").get("Position"))) / 1000;
    }

    public void playPause() throws Exception {
        this.dbus.send(INTERFACE_PLAY_PAUSE);
    }

    public void next() throws Exception {
        this.dbus.send(INTERFACE_NEXT);
    }

    public void previous() throws Exception {
        this.dbus.send(INTERFACE_PREVIOUS);
    }
}
