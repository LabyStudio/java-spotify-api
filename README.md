![Banner](.github/assets/banner.png)

<b>A Spotify API written in Java to access the current playing song.</b><br>
There is no need for an access token, any internet connection or a premium account
because the API reads the information directly from the application itself.

#### Supported operating systems:
- Windows

## Gradle Setup
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.LabyStudio:java-spotify-api:1.0.0:all'
}
```

## Example
Create the API and get the current playing song and position:
```java
// Create a new SpotifyAPI for your operating system
SpotifyAPI api = SpotifyAPIFactory.create();

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
api.registerListener(new SpotifyListener() {
    @Override
    public void onConnect() {
        System.out.println("Connected to Spotify!");
    }

    @Override
    public void onTrackChanged(Track track) {
        System.out.println("Track changed: [" + track.getId() + "] " + track.getName() + " - " + track.getArtist() + " (" + formatDuration(track.getLength()) + ")");
    }

    @Override
    public void onPositionChanged(int position) {
        if (api.getTrack() == null) {
            return;
        }

        int length = api.getTrack().getLength();
        float percentage = 100.0F / length * position;

        System.out.println("Seek: " + (int) percentage + "% (" + formatDuration(position) + " / " + formatDuration(length) + ")");
    }

    @Override
    public void onPlayBackChanged(boolean isPlaying) {
        System.out.println(isPlaying ? "Playing" : "Paused");
    }

    @Override
    public void onSync() {

    }

    @Override
    public void onDisconnect(Exception exception) {
        System.out.println("Disconnected: " + exception.getMessage());
    }
});
```
