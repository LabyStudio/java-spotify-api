package de.labystudio.spotifyapi.platform.windows.api;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;

import java.util.Map;

/**
 * Windows Process API
 * Used to connect to a Windows process and read its memory.
 *
 * @author LabyStudio
 */
public class WinProcess implements WinApi {

    private final int processId;
    private final WinNT.HANDLE handle;
    private final WinDef.HWND window;

    /**
     * Creates a new instance of the {@link WinProcess} class.
     *
     * @param executableName The name of the executable to connect to.
     * @throws IllegalStateException if the process could not be found.
     */
    public WinProcess(String executableName) {
        this.processId = this.getProcessIdByName(executableName);
        if (this.processId == -1) {
            throw new IllegalStateException("Process of executable " + executableName + " not found");
        }

        this.handle = this.openProcessHandle(this.processId);
        if (this.handle == null) {
            throw new IllegalStateException("Process handle of " + this.processId + " not found");
        }

        this.window = this.openWindow(this.processId);
        if (this.getWindowTitle().isEmpty()) {
            throw new IllegalStateException("Window for process " + this.processId + " not found");
        }
    }

    /**
     * Read a boolean value from the process memory.
     *
     * @param address The address to read from.
     * @return The boolean value.
     */
    public boolean readBoolean(long address) {
        return this.readByte(address) == 1;
    }

    /**
     * Read a byte value from the process memory.
     *
     * @param address The address to read from.
     * @return The byte value.
     */
    public byte readByte(long address) {
        return this.readBytes(address, 1)[0];
    }

    /**
     * Read an integer value from the process memory.
     *
     * @param address The address to read from.
     * @return The integer value.
     */
    public int readInteger(long address) {
        byte[] bytes = this.readBytes(address, 4);
        return (bytes[0] & 0xFF) | (bytes[1] & 0xFF) << 8 | (bytes[2] & 0xFF) << 16 | (bytes[3] & 0xFF) << 24;
    }

    /**
     * Read a string from the process memory.
     *
     * @param address The address to read from.
     * @param length  The length of the string.
     * @return The string with the given length.
     */
    public String readString(long address, int length) {
        return new String(this.readBytes(address, length));
    }

    /**
     * Read a byte array from the process memory.
     *
     * @param address The address to read from.
     * @param length  The length of the array.
     * @return The byte array with the given length.
     */
    public byte[] readBytes(long address, int length) {
        Kernel32 kernel = Kernel32.INSTANCE;
        Memory memory = new Memory(length);
        kernel.ReadProcessMemory(this.handle, new Pointer(address), memory, length, new IntByReference(length));
        return memory.getByteArray(0, length);
    }

    /**
     * Find the address of the first matching given bytes between the given start and end address.
     * <p>
     * It will return -1 if no address was found.
     *
     * @param address     The address to start searching from.
     * @param size        The maximum amount of bytes to search.
     * @param searchBytes The bytes to search for.
     * @return The address of the first matching bytes.
     */
    public long findInMemory(long address, long size, byte[] searchBytes) {
        int chunkSize = 1024 * 64;

        for (long cursor = address; cursor < (address + size); cursor += chunkSize) {
            byte[] chunk = this.readBytes(cursor, chunkSize + searchBytes.length);

            for (int i = 0; i < chunk.length - searchBytes.length; i++) {
                boolean found = true;
                for (int k = 0; k < searchBytes.length; k++) {
                    if (chunk[i + k] != searchBytes[k]) {
                        found = false;
                        break;
                    }
                }
                if (found) {
                    return cursor + i;
                }
            }
        }
        return -1;
    }

    /**
     * Find the address of the first matching given bytes between the given start and end address.
     * If an address matches the given bytes, it will call the search condition function.
     * If the condition function returns true, the address will be returned.
     * <p>
     * It will return -1 if no address was found.
     *
     * @param address     The address to start searching from.
     * @param size        The maximum amount of bytes to search.
     * @param searchBytes The bytes to search for.
     * @param condition   The condition function to call for each matching address.
     * @return The address of the first matching bytes.
     */
    public long findInMemory(long address, long size, byte[] searchBytes, SearchCondition condition) {
        long cursor = address;
        long maxAddress = size - address;
        int index = 0;
        while (cursor < maxAddress) {
            long target = this.findInMemory(cursor, maxAddress, searchBytes);
            if (condition.matches(target, index)) {
                return target;
            }
            cursor = target + 1;
            index++;
        }
        return -1;
    }

    /**
     * Find the address of a text inside the memory.
     * If there are multiple matches of the text, the given index will be used to select the correct one.
     *
     * @param start The address to start searching from.
     * @param text  The text to search for.
     * @param index The amount of matches to skip
     * @return The address of the text at the given index
     */
    public long findAddressOfText(long start, String text, int index) {
        long maxAddress = this.getMaxProcessAddress();
        return this.findInMemory(start, maxAddress, text.getBytes(), (address, matchIndex) -> matchIndex == index);
    }

    /**
     * Find multiple strings inside the memory.
     * It will start searching with the first string and continue with the next at the position where the previous was found.
     *
     * @param path The list of strings to search for.
     * @return The addresses of the strings.
     */
    public long findAddressUsingPath(String... path) {
        long cursor = -1;
        for (String part : path) {
            cursor = this.findAddressOfText(cursor + 1, part, 0);
            if (cursor == -1) {
                return -1;
            }
        }
        return cursor;
    }

    /**
     * Find the first address of the modules.
     *
     * @return The address of the first module.
     */
    public long getFirstModuleAddress() {
        long minAddress = Long.MAX_VALUE;
        for (Map.Entry<Long, Long> module : this.getModules(this.processId).entrySet()) {
            minAddress = Math.min(minAddress, module.getKey() + module.getValue());
        }
        return minAddress;
    }

    /**
     * Find the highest process address.
     * The highest process address is determined by the highest module address + module size.
     *
     * @return The highest process address.
     */
    public long getMaxProcessAddress() {
        long maxAddress = 0;
        for (Map.Entry<Long, Long> module : this.getModules(this.processId).entrySet()) {
            maxAddress = Math.max(maxAddress, module.getKey() + module.getValue());
        }
        return maxAddress;
    }

    /**
     * Get the title of the window as a string.
     *
     * @return The title of the window.
     */
    public String getWindowTitle() {
        return this.getWindowTitle(this.window);
    }

    /**
     * Get the PID of the process.
     *
     * @return The PID of the process.
     */
    public int getProcessId() {
        return this.processId;
    }

    /**
     * Get the process handle of the application.
     *
     * @return The process handle of the application.
     */
    public WinNT.HANDLE getHandle() {
        return this.handle;
    }

    /**
     * Checks if the current handle is not null.
     *
     * @return True if the handle is not null.
     */
    public boolean isOpen() {
        return this.handle != null;
    }

    /**
     * Closes the process handle.
     */
    public void close() {
        Kernel32.INSTANCE.CloseHandle(this.handle);
    }

    /**
     * Search condition function to check if the address matches additional criteria.
     */
    public interface SearchCondition {

        /**
         * Called for each matching address.
         *
         * @param address The address of the matching bytes.
         * @param index   The index of the match.
         * @return True if the address matches the criteria.
         */
        boolean matches(long address, int index);
    }

}
