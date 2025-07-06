package de.labystudio.spotifyapi.platform.windows;

import de.labystudio.spotifyapi.SpotifyListener;
import de.labystudio.spotifyapi.model.MediaKey;
import de.labystudio.spotifyapi.model.Track;
import de.labystudio.spotifyapi.platform.AbstractTickSpotifyAPI;
import de.labystudio.spotifyapi.platform.windows.api.WinApi;
import de.labystudio.spotifyapi.platform.windows.api.jna.WindowsMediaControl;
import de.labystudio.spotifyapi.platform.windows.api.playback.PlaybackAccessor;
import de.labystudio.spotifyapi.platform.windows.api.spotify.SpotifyProcess;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * Windows implementation of the SpotifyAPI.
 * The implementation uses the Windows API to access the memory of the Spotify process.
 * The currently playing track name and artist are read from the Windows title bar.
 *
 * @author LabyStudio
 */
public class WinSpotifyAPI extends AbstractTickSpotifyAPI {

    private static WindowsMediaControl mediaControl;

    private SpotifyProcess process;

    private Track currentTrack;
    private int currentPosition = -1;
    private boolean hasTrackPosition = false;
    private boolean isPlaying;

    private long lastTimePositionUpdated;
    private long prevLastReportedPosition = -1;

    @Override
    protected void onInitialized() {
        try {
            this.initializeMediaControl(this.configuration.getNativesDirectory());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void initializeMediaControl(Path nativesDirectory) throws IOException {
        if (mediaControl != null) {
            return; // Already initialized
        }

        boolean is64Bit = System.getProperty("os.arch").contains("64");

        String path = "/natives/windows-x" + (is64Bit ? 64 : 86) + "/windowsmediacontrol.dll";
        try (InputStream nativesStream = SpotifyProcess.class.getResourceAsStream(path)) {
            if (nativesStream == null) {
                throw new IOException("Could not find native library: " + path);
            }

            Path nativeLibraryPath = nativesDirectory.resolve("windowsmediacontrol.dll");
            try {
                // Ensure the natives directory exists
                if (!Files.exists(nativesDirectory)) {
                    Files.createDirectories(nativesDirectory);
                }

                // Extract the native library to the specified directory
                Files.copy(nativesStream, nativeLibraryPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new IOException("Failed to copy native library to " + nativeLibraryPath, e);
            }

            // Load the native library
            mediaControl = WindowsMediaControl.loadLibrary(nativeLibraryPath);
        }
    }

    /**
     * Updates the current track, position and playback state.
     * If the process is not connected, it will try to connect to the Spotify process.
     */
    protected void onTick() {
        if (!this.isConnected()) {
            // Connect
            this.process = new SpotifyProcess(mediaControl);

            // Fire on connect
            this.listeners.forEach(SpotifyListener::onConnect);
        }

        // Read track id and check if track id is valid
        String trackId = this.process.readTrackId();
        if (!this.process.isTrackIdValid(trackId)) {
            throw new IllegalStateException("Invalid track ID: " + trackId);
        }

        // Update playback state
        PlaybackAccessor accessor = this.process.getPlaybackAccessor();
        accessor.updatePlayback();

        // Handle track changes
        String currentTrackId = this.currentTrack == null ? null : this.currentTrack.getId();
        if (!Objects.equals(trackId, currentTrackId)) {
            // Update track information
            accessor.updateTrack();

            String trackTitle = accessor.getTitle();
            String trackArtist = accessor.getArtist();
            BufferedImage coverArt = this.toBufferedImage(accessor.getCoverArt());

            Track track = new Track(
                    trackId,
                    trackTitle,
                    trackArtist,
                    accessor.getLength(),
                    coverArt
            );
            this.currentTrack = track;

            // Fire on track changed
            this.listeners.forEach(listener -> listener.onTrackChanged(track));
        }

        // Handle is playing changes
        boolean isPlaying = accessor.isPlaying();
        if (isPlaying != this.isPlaying) {
            this.isPlaying = isPlaying;

            // Fire on play back changed
            this.listeners.forEach(listener -> listener.onPlayBackChanged(isPlaying));
        }

        if (accessor.hasTrackPosition()) {
            this.hasTrackPosition = true;

            int lastReportedPosition = accessor.getPosition();

            if (this.prevLastReportedPosition != lastReportedPosition) {
                this.prevLastReportedPosition = lastReportedPosition;

                // Get the daemon position (Last reported position + relative time)
                int expectedPosition = this.getPosition();

                // Compare if the expected position based on time and the last reported position are close enough
                boolean seeked = Math.abs(lastReportedPosition - expectedPosition) > TICK_INTERVAL;

                this.currentPosition = lastReportedPosition;
                this.lastTimePositionUpdated = System.currentTimeMillis();

                // Fire on position changed
                if (seeked) {
                    this.listeners.forEach(listener -> listener.onPositionChanged(this.currentPosition));
                }
            }
        } else {
            this.currentPosition = -1;
            this.hasTrackPosition = false;
            this.lastTimePositionUpdated = System.currentTimeMillis();
        }

        // Fire keep alive
        this.listeners.forEach(SpotifyListener::onSync);
    }

    @Override
    public Track getTrack() {
        return this.currentTrack;
    }

    @Override
    public int getPosition() {
        if (!this.hasPosition()) {
            throw new IllegalStateException("Position is not known yet. Pause the song for a second and try again.");
        }

        if (this.isPlaying) {
            // Interpolate position
            long timePassed = System.currentTimeMillis() - this.lastTimePositionUpdated;
            long interpolatedPosition = this.currentPosition + timePassed;

            if (this.hasTrack()) {
                return (int) Math.min(interpolatedPosition, this.currentTrack.getLength());
            } else {
                return (int) interpolatedPosition;
            }
        } else {
            return this.currentPosition;
        }
    }

    @Override
    public boolean hasPosition() {
        if (!this.isConnected()) {
            return false;
        }
        return this.hasTrackPosition;
    }

    @Override
    public void pressMediaKey(MediaKey mediaKey) {
        if (!this.isConnected()) {
            throw new IllegalStateException("Spotify is not connected");
        }

        switch (mediaKey) {
            case NEXT:
                this.process.pressKey(WinApi.VK_MEDIA_NEXT_TRACK);
                break;
            case PREV:
                this.process.pressKey(WinApi.VK_MEDIA_PREV_TRACK);
                break;
            case PLAY_PAUSE:
                this.process.pressKey(WinApi.VK_MEDIA_PLAY_PAUSE);
                break;
        }

        // Update state immediately
        this.onInternalTick();
    }

    @Override
    public boolean isPlaying() {
        return this.isPlaying;
    }

    @Override
    public boolean isConnected() {
        return this.process != null && this.process.isOpen();
    }

    @Override
    public void stop() {
        super.stop();

        if (this.process != null) {
            this.process.close();
            this.process = null;
        }

        this.currentTrack = null;
        this.currentPosition = -1;
        this.hasTrackPosition = false;
        this.isPlaying = false;
        this.lastTimePositionUpdated = 0;
        this.prevLastReportedPosition = -1;
    }

    private BufferedImage toBufferedImage(byte[] data) {
        if (data == null || data.length == 0) {
            return null; // No cover art available
        }
        try {
            return ImageIO.read(new ByteArrayInputStream(data));
        } catch (Throwable e) {
            e.printStackTrace();
            return null; // Failed to load cover art
        }
    }

}
