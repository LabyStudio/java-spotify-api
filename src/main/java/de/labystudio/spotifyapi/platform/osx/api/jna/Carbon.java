package de.labystudio.spotifyapi.platform.osx.api.jna;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * The Apple Event Manager and Open Scripting Architecture functions of the Carbon framework.
 * They compile and execute AppleScript inside the current process.
 */
public interface Carbon extends Library {

    Carbon INSTANCE = Native.load("Carbon", Carbon.class);

    int K_OSA_COMPONENT_TYPE = fourCharCode("osa ");
    int K_APPLE_SCRIPT_SUBTYPE = fourCharCode("ascr");

    int K_OSA_NULL_SCRIPT = 0;
    int K_OSA_MODE_NULL = 0;
    int K_OSA_MODE_DISPLAY_FOR_HUMANS = 0x00000008;

    int K_OSA_ERROR_NUMBER = fourCharCode("errn");
    int K_OSA_ERROR_MESSAGE = fourCharCode("errs");

    int TYPE_UTF8_TEXT = fourCharCode("utf8");
    int TYPE_SINT32 = fourCharCode("long");

    int ERR_OSA_SCRIPT_ERROR = -1753;

    Pointer OpenDefaultComponent(int componentType, int componentSubType);

    short AECreateDesc(int typeCode, byte[] dataPtr, long dataSize, AEDesc result);

    long AEGetDescDataSize(AEDesc desc);

    short AEGetDescData(AEDesc desc, byte[] dataPtr, long maximumSize);

    short AEDisposeDesc(AEDesc desc);

    int OSACompile(Pointer scriptingComponent, AEDesc sourceData, int modeFlags, IntByReference previousAndResultingScriptID);

    int OSAExecute(Pointer scriptingComponent, int compiledScriptID, int contextID, int modeFlags, IntByReference resultingScriptValueID);

    int OSADisplay(Pointer scriptingComponent, int scriptValueID, int desiredType, int modeFlags, AEDesc resultingText);

    int OSAScriptError(Pointer scriptingComponent, int selector, int desiredType, AEDesc resultingErrorDescription);

    int OSADispose(Pointer scriptingComponent, int scriptID);

    int OSASetSendProc(Pointer scriptingComponent, OSASendProc sendProc, Pointer refCon);

    int OSASetActiveProc(Pointer scriptingComponent, OSAActiveProc activeProc, Pointer refCon);

    int AESendMessage(Pointer event, Pointer reply, int sendMode, long timeOutInTicks);

    interface OSAActiveProc extends Callback {
        short invoke(Pointer refCon);
    }

    interface OSASendProc extends Callback {
        short invoke(Pointer appleEvent, Pointer reply, int sendMode, short sendPriority, int timeOutInTicks,
                     Pointer idleProc, Pointer filterProc, Pointer refCon);
    }

    /**
     * Convert a four character code like "utf8" into its integer representation.
     *
     * @param code The four character code
     * @return The integer representation of the code
     */
    static int fourCharCode(String code) {
        byte[] bytes = code.getBytes(StandardCharsets.US_ASCII);
        return (bytes[0] & 0xFF) << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }

    class AEDesc extends Structure {

        public int descriptorType;
        public Pointer dataHandle;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("descriptorType", "dataHandle");
        }
    }
}
