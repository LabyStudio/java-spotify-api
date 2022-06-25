package de.labystudio.spotifyapi;

import de.labystudio.spotifyapi.platform.windows.WinSpotifyAPI;
import de.labystudio.spotifyapi.platform.osx.OSXSpotifyApi;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Factory class for creating SpotifyAPI instances.
 *
 * @author LabyStudio
 */
public class SpotifyAPIFactory {

    /**
     * Creates a new SpotifyAPI instance for the current platform.
     * Currently, only Windows and OSX are supported.
     *
     * @return A new SpotifyAPI instance.
     * @throws IllegalStateException if the current platform is not supported.
     */
    public static SpotifyAPI create() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);

        if (os.contains("win")) {
            return new WinSpotifyAPI().initialize();
        }
        if (os.contains("mac")) {
            return new OSXSpotifyApi().initialize();
        }

        throw new IllegalStateException("Unsupported OS: " + os);
    }

    /**
     * Creates a new SpotifyAPI instance for the current platform asynchronously.
     * Currently, only Windows and OSX are supported.
     *
     * @return A future that will contain the SpotifyAPI instance.
     */
    public static CompletableFuture<SpotifyAPI> createAsync() {
        return CompletableFuture.supplyAsync(SpotifyAPIFactory::create);
    }

}
