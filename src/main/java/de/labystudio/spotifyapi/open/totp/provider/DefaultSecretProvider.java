package de.labystudio.spotifyapi.open.totp.provider;

import de.labystudio.spotifyapi.open.totp.model.Secret;

/**
 * Default implementation to provide a single secret for TOTP generation.
 *
 * @author LabyStudio
 */
public class DefaultSecretProvider implements SecretProvider {

    private final Secret secret;

    public DefaultSecretProvider(Secret secret) {
        this.secret = secret;
    }

    @Override
    public Secret getSecret() {
        return this.secret;
    }

}
