
import de.labystudio.spotifyapi.SpotifyAPI;
import de.labystudio.spotifyapi.SpotifyAPIFactory;
import de.labystudio.spotifyapi.SpotifyListener;
import de.labystudio.spotifyapi.model.Track;
import de.labystudio.spotifyapi.open.OpenSpotifyAPI;

import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class SpotifyListenerTest {

    public static void main(String[] args) {
        OpenSpotifyAPI openSpotifyAPI = new OpenSpotifyAPI();
        SpotifyAPI api = SpotifyAPIFactory.create();
        api.registerListener(new SpotifyListener() {
            @Override
            public void onConnect() {
                System.out.println("Connected to Spotify!");
            }

            @Override
            public void onTrackChanged(Track track) {
                System.out.printf("Track changed: %s (%s)\n", track, formatDuration(track.getLength()));

                try {
                    BufferedImage imageTrackCover = openSpotifyAPI.requestImage(track);
                    System.out.println("Loaded track cover: " + imageTrackCover.getWidth() + "x" + imageTrackCover.getHeight());
                } catch (Exception e) {
                    System.out.println("Could not load track cover: " + e.getMessage());
                }
            }

            @Override
            public void onPositionChanged(int position) {
                if (!api.hasTrack()) {
                    return;
                }

                int length = api.getTrack().getLength();
                float percentage = 100.0F / length * position;

                System.out.printf(
                        "Position changed: %s of %s (%d%%)\n",
                        formatDuration(position),
                        formatDuration(length),
                        (int) percentage
                );
            }

            @Override
            public void onPlayBackChanged(boolean isPlaying) {
                System.out.println(isPlaying ? "Song started playing" : "Song stopped playing");
            }

            @Override
            public void onSync() {

            }

            @Override
            public void onDisconnect(Exception exception) {
                System.out.println("Disconnected: " + exception.getMessage());

                // api.stop();
            }
        });

        // Initialize the API
        api.initialize();
    }

    private static String formatDuration(long ms) {
        Duration duration = Duration.ofMillis(ms);
        return String.format("%sm %ss", duration.toMinutes(), duration.getSeconds() - TimeUnit.MINUTES.toSeconds(duration.toMinutes()));
    }
}
