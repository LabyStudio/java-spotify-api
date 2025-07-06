use futures::executor::block_on;
use windows::Storage::Streams::DataReader;
use windows::{core::*, Media::Control::*};

mod session;
pub use session::get_spotify_session;

fn main() -> Result<()> {
    block_on(async {
        let session = get_spotify_session()?;
        if session.is_none() {
            println!("Spotify is not currently running.");
            return Ok(());
        }

        let session = session.unwrap();

        // Print the current playback status
        let playback_info = session.GetPlaybackInfo()?;
        let playback_status = playback_info.PlaybackStatus()?;
        println!(
            "Spotify Playback Status: {:?}",
            playback_status == GlobalSystemMediaTransportControlsSessionPlaybackStatus::Playing
        );

        // Print the current track name
        if let Some(media_properties) = session.TryGetMediaPropertiesAsync()?.get().ok() {
            println!("Current Track Name: {}", media_properties.Title()?);
            println!("Current Artist Name: {}", media_properties.Artist()?);
        } else {
            println!("Could not retrieve media properties.");
        }

        // Get position and track length
        let timeline = session.GetTimelineProperties()?;
        println!(
            "Current Position: {:?}",
            timeline.Position()?.Duration / 10_000
        );
        println!(
            "Track Duration: {:?}",
            timeline.EndTime()?.Duration / 10_000
        );

        if let Some(media_properties) = session.TryGetMediaPropertiesAsync()?.get().ok() {
            println!("Current Track Name: {}", media_properties.Title()?);
            println!("Current Artist Name: {}", media_properties.Artist()?);

            // Get cover art thumbnail
            let thumbnail = media_properties.Thumbnail()?; // No Option here
            let stream_ref = thumbnail.OpenReadAsync()?.get()?;
            let size = stream_ref.Size()?;

            let reader = DataReader::CreateDataReader(&stream_ref)?;
            reader.LoadAsync(size as u32)?.get()?;
            let mut buffer = vec![0u8; size as usize];
            reader.ReadBytes(&mut buffer)?;

            println!("Cover art thumbnail size: {} bytes", buffer.len());
        }

        Ok(())
    })
}
