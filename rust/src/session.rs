use std::sync::Mutex;
use lazy_static::lazy_static;
use windows::core::Result;
use windows::Media::Control::*;

lazy_static! {
    static ref SPOTIFY_SESSION_CACHE: Mutex<Option<GlobalSystemMediaTransportControlsSession>> = Mutex::new(None);
}

pub fn is_spotify(session: &GlobalSystemMediaTransportControlsSession) -> Result<bool> {
    let app_id = session.SourceAppUserModelId()?;
    let app_id = app_id.to_string();

    Ok(
        app_id == "Spotify.exe" ||
            (app_id.starts_with("SpotifyAB") && app_id.ends_with("!Spotify"))
    )
}

pub fn get_spotify_session() -> Result<Option<GlobalSystemMediaTransportControlsSession>> {
    let mut cache = SPOTIFY_SESSION_CACHE.lock().unwrap();

    if let Some(session) = cache.as_ref() {
        // Validate cached session
        if is_spotify(session)? {
            return Ok(Some(session.clone()));
        }

        // Session invalid: clear cache
        *cache = None;
    }

    // Cache is empty or invalid, find a new session
    let manager_operation = GlobalSystemMediaTransportControlsSessionManager::RequestAsync()?;
    let manager = manager_operation.get()?;
    let sessions = manager.GetSessions()?;

    for session in sessions {
        if is_spotify(&session)? {
            // Found a valid Spotify session, cache it
            *cache = Some(session.clone());
            return Ok(Some(session));
        }
    }

    // No session found, clear cache
    *cache = None;
    Ok(None)
}