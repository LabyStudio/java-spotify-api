import de.labystudio.spotifyapi.SpotifyAPI;
import de.labystudio.spotifyapi.SpotifyAPIFactory;
import de.labystudio.spotifyapi.SpotifyListener;
import de.labystudio.spotifyapi.model.Track;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class SpotifyTest {

    public static void main(String[] args) throws Exception {
        SpotifyAPI api = SpotifyAPIFactory.create();

        // It has no track until the song started playing once
        if (api.hasTrack()) {
            System.out.println(api.getTrack());
        }

        // It has no position until the song is paused, the position changed or the song changed
        if (api.hasPosition()) {
            System.out.println(api.getPosition());
        }

        api.registerListener(new SpotifyListener() {
            @Override
            public void onConnect() {
                System.out.println("Connected to Spotify!");
            }

            @Override
            public void onTrackChanged(Track track) {
                System.out.printf("Track changed: %s (%s)\n", track, formatDuration(track.getLength()));
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
    }

    private static String formatDuration(long ms) {
        Duration duration = Duration.ofMillis(ms);
        return String.format("%sm %ss", duration.toMinutes(), duration.getSeconds() - TimeUnit.MINUTES.toSeconds(duration.toMinutes()));
    }
}
