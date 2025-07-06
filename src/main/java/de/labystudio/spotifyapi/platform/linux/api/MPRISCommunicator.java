package de.labystudio.spotifyapi.platform.linux.api;

import de.labystudio.spotifyapi.platform.linux.api.model.InterfaceMember;
import de.labystudio.spotifyapi.platform.linux.api.model.Metadata;
import de.labystudio.spotifyapi.platform.linux.api.model.Parameter;
import de.labystudio.spotifyapi.platform.linux.api.model.Variant;

import java.util.HashMap;
import java.util.Map;

/**
 * MPRIS communicator
 * <p>
 * This class is used to communicate with the MPRIS interface.
 * It can be used to get the current track, track position and to control the playback.
 *
 * @author holybaechu, LabyStudio
 */
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

    public Metadata readMetadata() throws Exception {
        Map<String, Object> metadata = new HashMap<>();
        Variant array = this.dbus.get("org.mpris.MediaPlayer2.Player", "Metadata");
        for (Variant entry : array.<Variant[]>getValue()) {
            metadata.put(entry.getSig(), entry.getValue());
        }
        return new Metadata(metadata);
    }

    public boolean readIsPlaying() throws Exception {
        return this.dbus.get("org.mpris.MediaPlayer2.Player", "PlaybackStatus").getValue().equals("Playing");
    }

    public Integer readPosition() throws Exception {
        return (int) ((Long) this.dbus.get("org.mpris.MediaPlayer2.Player", "Position").getValue() / 1000L);
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
