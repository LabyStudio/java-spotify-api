use lazy_static::lazy_static;
use std::sync::Mutex;
use std::time::{Duration, Instant};
use windows::core::Result;
use windows::Media::Control::*;

struct CachedSession {
    session: GlobalSystemMediaTransportControlsSession,
    timestamp: Instant,
}

lazy_static! {
    static ref SPOTIFY_SESSION_CACHE: Mutex<Option<CachedSession>> = Mutex::new(None);
}

const CACHE_TTL: Duration = Duration::from_secs(3);

pub fn is_spotify(session: &GlobalSystemMediaTransportControlsSession) -> Result<bool> {
    let app_id = session.SourceAppUserModelId()?;
    let app_id = app_id.to_string();

    Ok(
        app_id == "Spotify.exe"
            || (app_id.starts_with("SpotifyAB") && app_id.ends_with("!Spotify")),
    )
}

pub fn get_spotify_session() -> Result<Option<GlobalSystemMediaTransportControlsSession>> {
    let mut cache = SPOTIFY_SESSION_CACHE.lock().unwrap();

    // Check cache expiration and validity
    if let Some(cached) = cache.as_ref() {
        if cached.timestamp.elapsed() < CACHE_TTL && is_spotify(&cached.session)? {
            return Ok(Some(cached.session.clone()));
        }
        // Cache expired or invalid, drop it
        *cache = None;
    }

    // Refresh the session cache
    let manager_operation = GlobalSystemMediaTransportControlsSessionManager::RequestAsync()?;
    let manager = manager_operation.get()?;
    let sessions = manager.GetSessions()?;

    for session in sessions {
        if is_spotify(&session)? {
            // Cache new session with current timestamp
            *cache = Some(CachedSession {
                session: session.clone(),
                timestamp: Instant::now(),
            });
            return Ok(Some(session));
        }
    }

    // No valid session found
    *cache = None;
    Ok(None)
}
