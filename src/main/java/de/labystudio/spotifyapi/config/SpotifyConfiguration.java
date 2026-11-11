package de.labystudio.spotifyapi.config;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A configuration for the spotify api
 *
 * @author LabyStudio
 */
public class SpotifyConfiguration {

    private final long exceptionReconnectDelay;
    private final boolean autoReconnect;
    private final Path nativesDirectory;
    private final long tickInterval;

    private SpotifyConfiguration(
            long exceptionReconnectDelay,
            boolean autoReconnect,
            Path nativesDirectory,
            long tickInterval
    ) {
        this.exceptionReconnectDelay = exceptionReconnectDelay;
        this.autoReconnect = autoReconnect;
        this.nativesDirectory = nativesDirectory;
        this.tickInterval = tickInterval;
    }

    public long getExceptionReconnectDelay() {
        return this.exceptionReconnectDelay;
    }

    public boolean isAutoReconnect() {
        return this.autoReconnect;
    }

    public Path getNativesDirectory() {
        return this.nativesDirectory;
    }

    public long getTickInterval() {
        return this.tickInterval;
    }

    /**
     * Builder to create a new spotify configuration
     */
    public static class Builder {

        private long exceptionReconnectDelay = 1000 * 10L;
        private boolean autoReconnect = true;
        private Path nativesDirectory = Paths.get(System.getProperty("java.io.tmpdir"), "spotify-api-natives");
        private long tickInterval = 1000L;

        /**
         * Set the delay between reconnects when an exception occurs
         *
         * @param exceptionReconnectDelay The delay in milliseconds
         * @return The builder instance
         */
        public Builder exceptionReconnectDelay(long exceptionReconnectDelay) {
            this.exceptionReconnectDelay = exceptionReconnectDelay;
            return this;
        }

        /**
         * Set if the api should automatically reconnect when an exception occurs
         *
         * @param autoReconnect The auto reconnect state
         * @return The builder instance
         */
        public Builder autoReconnect(boolean autoReconnect) {
            this.autoReconnect = autoReconnect;
            return this;
        }

        /**
         * All libraries that are required to run the spotify api will be extracted to this directory.
         *
         * @param nativesDirectory The directory where the native libraries will be extracted to
         *                         If null, the system temporary directory will be used.
         * @return The builder instance
         */
        public Builder nativesDirectory(Path nativesDirectory) {
            this.nativesDirectory = nativesDirectory;
            return this;
        }

        /**
         * Set how often Spotify is polled for track, playback state and position changes.
         * A lower interval detects seeks, pauses and track changes sooner but queries Spotify more often.
         *
         * @param tickInterval The interval in milliseconds, at least 50
         * @return The builder instance
         */
        public Builder tickInterval(long tickInterval) {
            this.tickInterval = Math.max(50L, tickInterval);
            return this;
        }

        public SpotifyConfiguration build() {
            return new SpotifyConfiguration(
                    this.exceptionReconnectDelay,
                    this.autoReconnect,
                    this.nativesDirectory,
                    this.tickInterval
            );
        }
    }
}
