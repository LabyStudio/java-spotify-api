use std::sync::Mutex;
use lazy_static::lazy_static;
use windows::core::Result;
use windows::Media::Control::*;

lazy_static! {
    static ref SPOTIFY_SESSION_CACHE: Mutex<Option<GlobalSystemMediaTransportControlsSession>> = Mutex::new(None);
}

pub fn get_spotify_session() -> Result<Option<GlobalSystemMediaTransportControlsSession>> {
    let mut cache = SPOTIFY_SESSION_CACHE.lock().unwrap();

    if let Some(session) = cache.as_ref() {
        // Validate cached session
        if let Ok(app_id) = session.SourceAppUserModelId() {
            if app_id == "Spotify.exe" {
                // Session still valid
                return Ok(Some(session.clone()));
            }
        }
        // Session invalid: clear cache
        *cache = None;
    }

    // Cache is empty or invalid, find a new session
    let manager_operation = GlobalSystemMediaTransportControlsSessionManager::RequestAsync()?;
    let manager = manager_operation.get()?;
    let sessions = manager.GetSessions()?;

    for session in sessions {
        let source_app_id = session.SourceAppUserModelId()?;
        if source_app_id == "Spotify.exe" {
            *cache = Some(session.clone());
            return Ok(Some(session));
        }
    }

    // No session found, clear cache
    *cache = None;
    Ok(None)
}