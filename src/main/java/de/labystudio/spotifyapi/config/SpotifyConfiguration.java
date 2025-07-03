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

    private SpotifyConfiguration(
            long exceptionReconnectDelay,
            boolean autoReconnect,
            Path nativesDirectory
    ) {
        this.exceptionReconnectDelay = exceptionReconnectDelay;
        this.autoReconnect = autoReconnect;
        this.nativesDirectory = nativesDirectory;
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

    /**
     * Builder to create a new spotify configuration
     */
    public static class Builder {

        private long exceptionReconnectDelay = 1000 * 10L;
        private boolean autoReconnect = true;
        private Path nativesDirectory = Paths.get(System.getProperty("java.io.tmpdir"), "spotify-api-natives");

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

        public SpotifyConfiguration build() {
            return new SpotifyConfiguration(
                    this.exceptionReconnectDelay,
                    this.autoReconnect,
                    this.nativesDirectory
            );
        }
    }
}
