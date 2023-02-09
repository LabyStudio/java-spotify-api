package de.labystudio.spotifyapi.config;

/**
 * A configuration for the spotify api
 *
 * @author LabyStudio
 */
public class SpotifyConfiguration {

    private long exceptionReconnectDelay;
    private boolean autoReconnect;

    private SpotifyConfiguration(
            long exceptionReconnectDelay,
            boolean autoReconnect
    ) {
        this.exceptionReconnectDelay = exceptionReconnectDelay;
        this.autoReconnect = autoReconnect;
    }

    public void setExceptionReconnectDelay(long exceptionReconnectDelay) {
        this.exceptionReconnectDelay = exceptionReconnectDelay;
    }

    public void setAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
    }

    public long getExceptionReconnectDelay() {
        return this.exceptionReconnectDelay;
    }

    public boolean isAutoReconnect() {
        return this.autoReconnect;
    }

    /**
     * Builder to create a new spotify configuration
     */
    public static class Builder {

        private long exceptionReconnectDelay = 1000 * 10L;
        private boolean autoReconnect = true;

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

        public SpotifyConfiguration build() {
            return new SpotifyConfiguration(
                    this.exceptionReconnectDelay,
                    this.autoReconnect
            );
        }
    }
}
