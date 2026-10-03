package de.labystudio.spotifyapi.platform.osx.api.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 * The autorelease pool functions of the Objective-C runtime.
 * A Java thread has no autorelease pool, objects the system frameworks autorelease on it would never be freed.
 */
public interface ObjC extends Library {

    ObjC INSTANCE = Native.load("objc", ObjC.class);

    Pointer objc_autoreleasePoolPush();

    void objc_autoreleasePoolPop(Pointer pool);
}
