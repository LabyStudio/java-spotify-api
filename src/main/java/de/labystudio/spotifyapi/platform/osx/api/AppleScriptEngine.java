package de.labystudio.spotifyapi.platform.osx.api;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import de.labystudio.spotifyapi.platform.osx.api.jna.Carbon;
import de.labystudio.spotifyapi.platform.osx.api.jna.ObjC;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Executes AppleScript inside the current process using the Open Scripting Architecture.
 * <p>
 * The alternative, an osascript process per request, is not only slow: macOS registers every one of
 * them as an application of their own. In a process that was started by an application, like a game
 * started by its launcher, each of these short-lived processes shows up as another Dock icon of that
 * application, several times per second.
 * <p>
 * The scripting component is shared by the whole process and may only be used by one thread at a time.
 */
public class AppleScriptEngine {

    private static final int MAX_COMPILED_SCRIPTS = 32;

    // AppleScript runs the event loop of the calling thread while it talks to another application, both
    // to wait for the reply and in between to let the user cancel. Off the main thread that loop never
    // receives anything while the main thread runs an event loop of its own (any GUI application, the
    // java launcher itself), the request hangs until it times out. AESendMessage waits on a reply port
    // instead and there is no user to cancel anything.
    private static final Carbon.OSASendProc SEND_PROC = (appleEvent, reply, sendMode, sendPriority, timeOutInTicks, idleProc, filterProc, refCon)
            -> (short) Carbon.INSTANCE.AESendMessage(appleEvent, reply, sendMode, timeOutInTicks);
    private static final Carbon.OSAActiveProc ACTIVE_PROC = refCon -> 0;

    private static AppleScriptEngine instance;
    private static boolean unavailable;

    private final Pointer component;

    private final Map<String, Integer> compiledScripts = new LinkedHashMap<String, Integer>(16, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Integer> eldest) {
            if (this.size() <= MAX_COMPILED_SCRIPTS) {
                return false;
            }
            Carbon.INSTANCE.OSADispose(AppleScriptEngine.this.component, eldest.getValue());
            return true;
        }
    };

    private AppleScriptEngine() {
        this.component = Carbon.INSTANCE.OpenDefaultComponent(Carbon.K_OSA_COMPONENT_TYPE, Carbon.K_APPLE_SCRIPT_SUBTYPE);
        if (this.component == null) {
            throw new IllegalStateException("AppleScript component is not available");
        }

        int error = Carbon.INSTANCE.OSASetSendProc(this.component, SEND_PROC, null);
        if (error != 0) {
            throw new IllegalStateException("Could not set the AppleScript send procedure: " + error);
        }

        error = Carbon.INSTANCE.OSASetActiveProc(this.component, ACTIVE_PROC, null);
        if (error != 0) {
            throw new IllegalStateException("Could not set the AppleScript active procedure: " + error);
        }
    }

    /**
     * Get the shared engine of this process.
     *
     * @return The engine or null if AppleScript can't be executed in-process on this system
     */
    public static synchronized AppleScriptEngine getInstance() {
        if (instance == null && !unavailable) {
            try {
                instance = new AppleScriptEngine();
            } catch (Throwable e) {
                unavailable = true;
            }
        }
        return instance;
    }

    /**
     * Compile and execute an AppleScript source.
     * The compiled script is kept, executing the same source again skips the compilation.
     *
     * @param source The AppleScript source code
     * @return The result in the same human-readable form osascript prints it
     * @throws Exception If the script could not be compiled or failed during execution
     */
    public synchronized String execute(String source) throws Exception {
        Pointer pool = ObjC.INSTANCE.objc_autoreleasePoolPush();
        try {
            int scriptId = this.compile(source);

            IntByReference resultId = new IntByReference(Carbon.K_OSA_NULL_SCRIPT);
            this.check(Carbon.INSTANCE.OSAExecute(
                    this.component,
                    scriptId,
                    Carbon.K_OSA_NULL_SCRIPT,
                    Carbon.K_OSA_MODE_NULL,
                    resultId
            ));

            try {
                return this.display(resultId.getValue());
            } finally {
                Carbon.INSTANCE.OSADispose(this.component, resultId.getValue());
            }
        } finally {
            ObjC.INSTANCE.objc_autoreleasePoolPop(pool);
        }
    }

    private int compile(String source) throws Exception {
        Integer compiledScriptId = this.compiledScripts.get(source);
        if (compiledScriptId != null) {
            return compiledScriptId;
        }

        byte[] bytes = source.getBytes(StandardCharsets.UTF_8);
        Carbon.AEDesc sourceDesc = new Carbon.AEDesc();
        short error = Carbon.INSTANCE.AECreateDesc(Carbon.TYPE_UTF8_TEXT, bytes, bytes.length, sourceDesc);
        if (error != 0) {
            throw new Exception("Could not create AppleScript source descriptor: " + error);
        }

        IntByReference scriptId = new IntByReference(Carbon.K_OSA_NULL_SCRIPT);
        try {
            this.check(Carbon.INSTANCE.OSACompile(this.component, sourceDesc, Carbon.K_OSA_MODE_NULL, scriptId));
        } finally {
            Carbon.INSTANCE.AEDisposeDesc(sourceDesc);
        }

        this.compiledScripts.put(source, scriptId.getValue());
        return scriptId.getValue();
    }

    private String display(int scriptValueId) throws Exception {
        Carbon.AEDesc text = new Carbon.AEDesc();
        this.check(Carbon.INSTANCE.OSADisplay(
                this.component,
                scriptValueId,
                Carbon.TYPE_UTF8_TEXT,
                Carbon.K_OSA_MODE_DISPLAY_FOR_HUMANS,
                text
        ));

        try {
            return new String(readData(text), StandardCharsets.UTF_8);
        } finally {
            Carbon.INSTANCE.AEDisposeDesc(text);
        }
    }

    private void check(int error) throws Exception {
        if (error == 0) {
            return;
        }

        // Anything but a script error has no further details
        if (error != Carbon.ERR_OSA_SCRIPT_ERROR) {
            throw new Exception("error " + error);
        }

        Carbon.AEDesc number = new Carbon.AEDesc();
        Carbon.AEDesc message = new Carbon.AEDesc();
        try {
            Carbon.INSTANCE.OSAScriptError(this.component, Carbon.K_OSA_ERROR_NUMBER, Carbon.TYPE_SINT32, number);
            Carbon.INSTANCE.OSAScriptError(this.component, Carbon.K_OSA_ERROR_MESSAGE, Carbon.TYPE_UTF8_TEXT, message);

            byte[] numberData = readData(number);
            int errorNumber = numberData.length == 4
                    ? ByteBuffer.wrap(numberData).order(ByteOrder.nativeOrder()).getInt()
                    : error;
            throw new Exception("error " + errorNumber + ": " + new String(readData(message), StandardCharsets.UTF_8));
        } finally {
            Carbon.INSTANCE.AEDisposeDesc(number);
            Carbon.INSTANCE.AEDisposeDesc(message);
        }
    }

    private static byte[] readData(Carbon.AEDesc desc) {
        long size = Carbon.INSTANCE.AEGetDescDataSize(desc);
        byte[] data = new byte[(int) size];
        if (size > 0) {
            Carbon.INSTANCE.AEGetDescData(desc, data, size);
        }
        return data;
    }

}
