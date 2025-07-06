![Banner](.github/assets/banner.png)

<b>A Spotify API written in Java to access the current playing song.</b><br>
There is no need for an access token, any internet connection or a premium account
because the API reads the information directly from the application itself.

## Feature Overview
- Track id
- Track title & artist
- Track progress & length
- Playing state (Playing, paused)
- Track cover
- Media keys (Previous song, play/pause & next song)

#### Supported operating systems:
- Windows
- macOS
- Linux distros that uses systemd

## Gradle Setup
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.LabyStudio:java-spotify-api:+:all'
}
```

## Example
Create the API and get the current playing song and position:
```java
// Create a new SpotifyAPI for your operating system
SpotifyAPI api = SpotifyAPIFactory.createInitialized();

// It has no track until the song started playing once
if (api.hasTrack()) {
    System.out.println(api.getTrack());
}

// It has no position until the song is paused, the position changed or the song changed
if (api.hasPosition()) {
    System.out.println(api.getPosition());
}
```

Register a listener to get notified when the song changes:
```java
SpotifyAPI localApi = SpotifyAPIFactory.create();
localApi.registerListener(new SpotifyListener() {
    @Override
    public void onConnect() {
        System.out.println("Connected to Spotify!");
    }

    @Override
    public void onTrackChanged(Track track) {
        System.out.printf("Track changed: %s (%s)\n", track, formatDuration(track.getLength()));

        if (track.getCoverArt() != null) {
            BufferedImage coverArt = track.getCoverArt();
            System.out.println("Track cover: " + coverArt.getWidth() + "x" + coverArt.getHeight());
        }
    }

    @Override
    public void onPositionChanged(int position) {
        if (!localApi.hasTrack()) {
            return;
        }

        int length = localApi.getTrack().getLength();
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
        // System.out.println(formatDuration(api.getPosition()));
    }

    @Override
    public void onDisconnect(Exception exception) {
        System.out.println("Disconnected: " + exception.getMessage());

        // api.stop();
    }
});

// Initialize the API
localApi.initialize();
```

Request information of any track id using open.spotify.com:
```java
// Create a secret provider
SecretProvider secretProvider = new DefaultSecretProvider(
        // Note: You have to update the secret with the latest TOTP secret from open.spotify.com
        // or find a way to retrieve it automatically from their website
        Secret.fromString("=n:b#OuEfH\fE])e*K", 10)
);

// Create an instance of the Open Spotify API
OpenSpotifyAPI openSpotifyAPI = new OpenSpotifyAPI(secretProvider);

// Fetch cover art image by track id
BufferedImage coverArt = openSpotifyAPI.requestImage(trackId);

// Fetch track information by track id
OpenTrack openTrack = openSpotifyAPI.requestOpenTrack(trackId);
```

You can also skip the current song using the Media Key API:
```java
SpotifyAPI api = SpotifyAPIFactory.createInitialized();

// Send media key to the operating system
api.pressMediaKey(MediaKey.NEXT);
```
