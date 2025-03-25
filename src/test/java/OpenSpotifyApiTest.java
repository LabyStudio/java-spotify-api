import de.labystudio.spotifyapi.open.OpenSpotifyAPI;
import de.labystudio.spotifyapi.open.model.track.OpenTrack;

public class OpenSpotifyApiTest {

    public static void main(String[] args) throws Exception {
        OpenSpotifyAPI openSpotifyAPI = new OpenSpotifyAPI();
        OpenTrack openTrack = openSpotifyAPI.requestOpenTrack("38T0tPVZHcPZyhtOcCP7pF");
        System.out.println(openTrack.name);
    }
}
