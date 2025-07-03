use jni::objects::JClass;
use jni::sys::{jint, jlong};
use jni::JNIEnv;
use std::ffi::CString;

use futures::executor::block_on;
use windows::{core::*, Media::Control::*};

use std::os::raw::c_char;

mod session;
pub use session::get_spotify_session;

#[no_mangle]
pub extern "system" fn getPlaybackPosition(_env: JNIEnv, _class: JClass) -> jlong {
    let result: Result<jlong> = block_on(async {
        match get_spotify_session()? {
            Some(session) => {
                let timeline = session.GetTimelineProperties()?;
                let position = timeline.Position()?.Duration;

                // Convert ticks to ms
                Ok((position / 10_000) as jlong)
            }
            None => Ok(0),
        }
    });
    result.unwrap_or(0)
}

#[no_mangle]
pub extern "system" fn getTrackDuration(_env: JNIEnv, _class: JClass) -> jlong {
    let result: Result<jlong> = block_on(async {
        match get_spotify_session()? {
            Some(session) => {
                let timeline = session.GetTimelineProperties()?;
                let end = timeline.EndTime()?.Duration;

                // Convert ticks to ms
                Ok((end / 10_000) as jlong)
            }
            None => Ok(0),
        }
    });
    result.unwrap_or(0)
}

#[no_mangle]
pub extern "system" fn getTrackTitle(_env: JNIEnv, _class: JClass) -> *const c_char {
    let track_title = block_on(async {
        match get_spotify_session() {
            Ok(Some(session)) => {
                let props = session.TryGetMediaPropertiesAsync().unwrap().get().unwrap();
                let title = props.Title().unwrap();
                title.to_string()
            }
            _ => "".to_string(),
        }
    });

    // Convert Rust String to CString
    let cstring = CString::new(track_title).unwrap_or_else(|_| CString::new("").unwrap());
    cstring.into_raw()
}

#[no_mangle]
pub extern "system" fn getArtistName(_env: JNIEnv, _class: JClass) -> *const c_char {
    let artist_name = block_on(async {
        match get_spotify_session() {
            Ok(Some(session)) => {
                let props = session.TryGetMediaPropertiesAsync().unwrap().get().unwrap();
                let artist = props.Artist().unwrap();
                artist.to_string()
            }
            _ => "".to_string(),
        }
    });

    // Convert Rust String to CString
    let cstring = CString::new(artist_name).unwrap_or_else(|_| CString::new("").unwrap());
    cstring.into_raw()
}

#[no_mangle]
pub extern "system" fn isPlaying(_env: JNIEnv, _class: JClass) -> jint {
    let result: Result<jint> = block_on(async {
        match get_spotify_session()? {
            Some(session) => {
                let playback_info = session.GetPlaybackInfo()?;
                let status = playback_info.PlaybackStatus()?;
                let is_playing = (status
                    == GlobalSystemMediaTransportControlsSessionPlaybackStatus::Playing)
                    as jint;
                Ok(is_playing)
            }
            None => Ok(0),
        }
    });
    result.unwrap_or(0)
}

#[no_mangle]
pub extern "system" fn freeString(s: *mut c_char) {
    if s.is_null() { return; }
    unsafe {
        let _ = CString::from_raw(s);
    }
}
