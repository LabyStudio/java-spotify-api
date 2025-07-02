package de.labystudio.spotifyapi.open.totp.provider;

import de.labystudio.spotifyapi.open.totp.model.Secret;

/**
 * This interface is used to provide a secret for TOTP generation.
 * It must be implemented to retrieve the latest secret from open.spotify.com
 *
 * @author LabyStudio
 */
public interface SecretProvider {

    /**
     * Retrieves the latest secret from open.spotify.com used for TOTP generation.
     *
     * @return The latest TOTP secret used for generating time-based one-time passwords.
     */
    Secret getSecret();
}
