package platform;

import de.labystudio.spotifyapi.SpotifyAPI;
import de.labystudio.spotifyapi.SpotifyAPIFactory;
import de.labystudio.spotifyapi.SpotifyListener;
import de.labystudio.spotifyapi.model.Track;

import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class SpotifyListenerTest {

    public static void main(String[] args) {
        SpotifyAPI localApi = SpotifyAPIFactory.create();
        localApi.registerListener(new SpotifyListener() {
            @Override
            public void onConnect() {
                System.out.println("Connected to Spotify!");
            }

            @Override
            public void onTrackChanged(Track track) {
                System.out.printf("Track changed: %s (%s)\n", track, formatDuration(track.getLength()));

                if (track.getCoverArt() != null) {
                    BufferedImage coverArt = track.getCoverArt();
                    System.out.println("Track cover: " + coverArt.getWidth() + "x" + coverArt.getHeight());
                }
            }

            @Override
            public void onPositionChanged(int position) {
                if (!localApi.hasTrack()) {
                    return;
                }

                int length = localApi.getTrack().getLength();
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
                // System.out.println(formatDuration(api.getPosition()));
            }

            @Override
            public void onDisconnect(Exception exception) {
                System.out.println("Disconnected: " + exception.getMessage());

                // api.stop();
            }
        });

        // Initialize the API
        localApi.initialize();
    }

    private static String formatDuration(long ms) {
        Duration duration = Duration.ofMillis(ms);
        return String.format("%sm %ss", duration.toMinutes(), duration.getSeconds() - TimeUnit.MINUTES.toSeconds(duration.toMinutes()));
    }
}
