package de.labystudio.spotifyapi;

import de.labystudio.spotifyapi.config.SpotifyConfiguration;
import de.labystudio.spotifyapi.platform.linux.LinuxSpotifyApi;
import de.labystudio.spotifyapi.platform.osx.OSXSpotifyApi;
import de.labystudio.spotifyapi.platform.windows.WinSpotifyAPI;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * Factory class for creating SpotifyAPI instances.
 *
 * @author LabyStudio
 */
public class SpotifyAPIFactory {

    /**
     * Creates a new SpotifyAPI instance for the current platform.
     * Currently, only Windows, OSX and Linux are supported.
     *
     * @return A new SpotifyAPI instance.
     * @throws IllegalStateException if the current platform is not supported.
     */
    public static SpotifyAPI create() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);

        if (os.contains("win")) {
            return new WinSpotifyAPI();
        }
        if (os.contains("mac")) {
            return new OSXSpotifyApi();
        }
        if (os.contains("linux")) {
            return new LinuxSpotifyApi();
        }

        throw new IllegalStateException("Unsupported OS: " + os);
    }

    /**
     * Create an initialized SpotifyAPI instance.
     * Initializing will block the current thread until the SpotifyAPI instance is ready.
     * It will use a default configuration.
     *
     * @return A new SpotifyAPI instance.
     */
    public static SpotifyAPI createInitialized() {
        return create().initialize();
    }

    /**
     * Create an initialized SpotifyAPI instance.
     * Initializing will block the current thread until the SpotifyAPI instance is ready.
     *
     * @param configuration The configuration for the SpotifyAPI instance.
     * @return A new SpotifyAPI instance.
     */
    public static SpotifyAPI createInitialized(SpotifyConfiguration configuration) {
        return create().initialize(configuration);
    }

    /**
     * Creates a new SpotifyAPI instance for the current platform asynchronously.
     * Currently, only Windows and OSX are supported.
     *
     * @return A future that will contain the SpotifyAPI instance.
     */
    public static CompletableFuture<SpotifyAPI> createInitializedAsync() {
        return CompletableFuture.supplyAsync(SpotifyAPIFactory::createInitialized);
    }

    /**
     * Creates a new SpotifyAPI instance for the current platform asynchronously.
     * Currently, only Windows and OSX are supported.
     * It will use a default configuration.
     *
     * @param configuration The configuration for the SpotifyAPI instance.
     * @return A future that will contain the SpotifyAPI instance.
     */
    public static CompletableFuture<SpotifyAPI> createInitializedAsync(SpotifyConfiguration configuration) {
        return CompletableFuture.supplyAsync(() -> createInitialized(configuration));
    }
}
