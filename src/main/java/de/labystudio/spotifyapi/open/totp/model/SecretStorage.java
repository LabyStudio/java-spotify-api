package de.labystudio.spotifyapi.open.totp.model;

/**
 * Storage format for all TOTP secrets used by Spotify.
 * This storage format is used on open.spotify.com
 *
 * @author LabyStudio
 */
public class SecretStorage {

    private String validUntil;
    private Secret[] secrets;

    public String getValidUntil() {
        return this.validUntil;
    }

    public Secret[] getSecrets() {
        return this.secrets;
    }

    /**
     * Retrieves the latest secret from the storage.
     * This method iterates through all stored secrets and returns the one with the highest version number.
     * If no secrets are available, it returns null.
     *
     * @return The latest TOTP secret with the highest version number, or null if no secrets are available.
     */
    public Secret getLatestSecret() {
        if (this.secrets == null || this.secrets.length == 0) {
            return null;
        }
        Secret latestSecret = this.secrets[0];
        for (Secret secret : this.secrets) {
            if (secret.getVersion() > latestSecret.getVersion()) {
                latestSecret = secret;
            }
        }
        return latestSecret;
    }
}
