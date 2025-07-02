import de.labystudio.spotifyapi.open.OpenSpotifyAPI;
import de.labystudio.spotifyapi.open.model.track.OpenTrack;
import de.labystudio.spotifyapi.open.totp.model.Secret;
import de.labystudio.spotifyapi.open.totp.provider.DefaultSecretProvider;
import de.labystudio.spotifyapi.open.totp.provider.SecretProvider;

public class OpenSpotifyApiTest {

    public static void main(String[] args) throws Exception {
        SecretProvider secretProvider = new DefaultSecretProvider(
                // Note: You have to update the secret with the latest TOTP secret from open.spotify.com
                Secret.fromString("meZcB\\tlUFV1D6W2Hy4@9+$QaH5)N8", 9)
        );
        OpenSpotifyAPI openSpotifyAPI = new OpenSpotifyAPI(secretProvider);
        OpenTrack openTrack = openSpotifyAPI.requestOpenTrack("38T0tPVZHcPZyhtOcCP7pF");
        System.out.println(openTrack.name);
    }
}
