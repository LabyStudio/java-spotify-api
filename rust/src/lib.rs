use jni::objects::JClass;
use jni::sys::{jint, jlong};
use jni::JNIEnv;
use std::ffi::CString;
use std::os::raw::c_char;

use futures::executor::block_on;
use windows::Win32::Foundation::E_FAIL;
use windows::{core::*, Media::Control::*};

mod session;
pub use session::get_spotify_session;

#[no_mangle]
pub extern "system" fn isSpotifyAvailable(_env: JNIEnv, _class: JClass) -> jint {
    let result = std::panic::catch_unwind(|| {
        block_on(async {
            match get_spotify_session() {
                Ok(Some(_session)) => Ok(1),
                Ok(None) => Ok(0),
                Err(_) => Err(()),
            }
        })
    });

    match result {
        Ok(Ok(val)) => val,
        _ => 0,
    }
}

#[no_mangle]
pub extern "system" fn getPlaybackPosition(_env: JNIEnv, _class: JClass) -> jlong {
    let result = std::panic::catch_unwind(|| {
        block_on(async {
            match get_spotify_session() {
                Ok(Some(session)) => {
                    let timeline = session.GetTimelineProperties()?;
                    let position = timeline.Position()?;
                    Ok((position.Duration / 10_000) as jlong)
                }
                _ => Err(Error::new(E_FAIL, "Spotify session not available")),
            }
        })
    });

    match result {
        Ok(Ok(position)) => position,
        _ => -1,
    }
}

#[no_mangle]
pub extern "system" fn getTrackDuration(_env: JNIEnv, _class: JClass) -> jlong {
    let result = std::panic::catch_unwind(|| {
        block_on(async {
            match get_spotify_session()? {
                Some(session) => {
                    let timeline = session.GetTimelineProperties()?;
                    let duration = timeline.EndTime()?.Duration;
                    Ok((duration / 10_000) as jlong)
                }
                _ => Err(Error::new(E_FAIL, "Spotify session not found")),
            }
        })
    });

    match result {
        Ok(Ok(duration)) => duration,
        _ => -1,
    }
}

#[no_mangle]
pub extern "system" fn getTrackTitle(_env: JNIEnv, _class: JClass) -> *const c_char {
    let result = std::panic::catch_unwind(|| {
        block_on(async {
            match get_spotify_session()? {
                Some(session) => {
                    let props = session.TryGetMediaPropertiesAsync()?.get()?;
                    let title = props.Title()?.to_string();
                    // Safely create CString, fallback to empty string if null bytes present
                    Ok(CString::new(title).unwrap_or_default().into_raw())
                }
                _ => Err(Error::new(E_FAIL, "Spotify session not found")),
            }
        })
    });

    match result {
        Ok(Ok(ptr)) => ptr,
        _ => std::ptr::null(),
    }
}

#[no_mangle]
pub extern "system" fn getArtistName(_env: JNIEnv, _class: JClass) -> *const c_char {
    let result = std::panic::catch_unwind(|| {
        block_on(async {
            match get_spotify_session()? {
                Some(session) => {
                    let props = session.TryGetMediaPropertiesAsync()?.get()?;
                    let artist = props.Artist()?.to_string();
                    Ok(CString::new(artist).unwrap_or_default().into_raw())
                }
                _ => Err(Error::new(E_FAIL, "Spotify session not found")),
            }
        })
    });

    match result {
        Ok(Ok(ptr)) => ptr,
        _ => std::ptr::null(),
    }
}

#[no_mangle]
pub extern "system" fn isPlaying(_env: JNIEnv, _class: JClass) -> jint {
    let result = std::panic::catch_unwind(|| {
        block_on(async {
            match get_spotify_session()? {
                Some(session) => {
                    let playback_info = session.GetPlaybackInfo()?;
                    let status = playback_info.PlaybackStatus()?;
                    Ok(
                        (status == GlobalSystemMediaTransportControlsSessionPlaybackStatus::Playing)
                            as jint,
                    )
                }
                _ => Err(Error::new(E_FAIL, "Spotify session not found")),
            }
        })
    });

    match result {
        Ok(Ok(val)) => val,
        _ => -1,
    }
}

#[no_mangle]
pub extern "system" fn getCoverArt(out_ptr: *mut *mut u8, out_len: *mut usize) -> i32 {
    if out_ptr.is_null() || out_len.is_null() {
        return 0; // false
    }
    let result = std::panic::catch_unwind(|| {
        block_on(async {
            match get_spotify_session()? {
                Some(session) => {
                    let props = session.TryGetMediaPropertiesAsync()?.get()?;
                    let thumbnail = props.Thumbnail()?;
                    let stream = thumbnail.OpenReadAsync()?.get()?;
                    let size = stream.Size()?;

                    if size == 0 {
                        return Err(Error::new(E_FAIL, "Cover art size is zero"));
                    }

                    use windows::Storage::Streams::DataReader;
                    let reader = DataReader::CreateDataReader(&stream)?;
                    reader.LoadAsync(size as u32)?.get()?;

                    let mut buffer = vec![0u8; size as usize];
                    reader.ReadBytes(&mut buffer)?;
                    Ok(buffer)
                }
                _ => Err(Error::empty()),
            }
        })
    });

    match result {
        Ok(Ok(buffer)) => unsafe {
            let len = buffer.len();
            let ptr = libc::malloc(len);
            if ptr.is_null() {
                return 0; // false
            }
            std::ptr::copy_nonoverlapping(buffer.as_ptr(), ptr as *mut u8, len);
            *out_ptr = ptr as *mut u8;
            *out_len = len;
            1 // true
        },
        _ => {
            0 // false
        }
    }
}

#[no_mangle]
pub extern "system" fn freeString(s: *mut c_char) {
    if !s.is_null() {
        unsafe {
            let _ = CString::from_raw(s);
        }
    }
}

#[no_mangle]
pub extern "system" fn freeCoverArt(ptr: *mut u8) {
    unsafe {
        if !ptr.is_null() {
            libc::free(ptr as *mut _);
        }
    }
}
