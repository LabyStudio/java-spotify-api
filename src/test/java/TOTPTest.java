import de.labystudio.spotifyapi.open.OpenSpotifyAPI;

public class TOTPTest {

    public static void main(String[] args) throws Exception {
        OpenSpotifyAPI openSpotifyAPI = new OpenSpotifyAPI();
        String totp = openSpotifyAPI.generateTotp(0);
        if (!totp.equals("371625")) {
            throw new Exception("Invalid TOTP: " + totp);
        }
        System.out.println(totp);
    }

}
