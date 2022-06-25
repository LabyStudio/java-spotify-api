
import de.labystudio.spotifyapi.SpotifyAPI;
import de.labystudio.spotifyapi.SpotifyAPIFactory;

public class SpotifyDirectTest {

    public static void main(String[] args) throws Exception {
        SpotifyAPI api = SpotifyAPIFactory.createInitialized();

        // It has no track until the song started playing once
        if (api.hasTrack()) {
            System.out.println("Current playing track: " + api.getTrack());
        }

        // It has no position until the song is paused, the position changed or the song changed
        if (api.hasPosition()) {
            System.out.println("Current track position: " + api.getPosition());
        }
    }

}
