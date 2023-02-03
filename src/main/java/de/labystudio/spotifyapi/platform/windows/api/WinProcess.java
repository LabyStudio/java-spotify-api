package de.labystudio.spotifyapi.platform.windows.api;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import de.labystudio.spotifyapi.platform.windows.api.jna.Kernel32;
import de.labystudio.spotifyapi.platform.windows.api.jna.Psapi;

import java.util.Map;

/**
 * Windows Process API
 * Used to connect to a Windows process and read its memory.
 *
 * @author LabyStudio
 */
public class WinProcess implements WinApi {

    protected final int processId;
    protected final WinNT.HANDLE handle;
    protected final WinDef.HWND window;

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
     * @param minAddress  The minimum address to start searching from.
     * @param maxAddress  The maximum address to stop searching at.
     * @param searchBytes The bytes to search for.
     * @return The address of the first matching bytes.
     */
    public long findInMemory(long minAddress, long maxAddress, byte[] searchBytes) {
        int chunkSize = 1024 * 64;

        for (long cursor = minAddress; cursor < maxAddress; cursor += chunkSize) {
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
     * @param minAddress  The address to start searching from.
     * @param maxAddress  The address to stop searching at.
     * @param searchBytes The bytes to search for.
     * @param condition   The condition function to call for each matching address.
     * @return The address of the first matching bytes.
     */
    public long findInMemory(long minAddress, long maxAddress, byte[] searchBytes, SearchCondition condition) {
        long cursor = minAddress;
        int index = 0;
        while (cursor < maxAddress) {
            long target = this.findInMemory(cursor, maxAddress, searchBytes);
            if (target == -1 || condition.matches(target, index)) {
                return target;
            }
            cursor = target + 1;
            index++;
        }
        return -1;
    }

    /**
     * Check if the given bytes are at the given address.
     *
     * @param address The address to check.
     * @param bytes   The bytes to check.
     * @return True if the bytes are at the given address.
     */
    public boolean hasBytes(long address, int... bytes) {
        byte[] chunk = this.readBytes(address, bytes.length);
        for (int i = 0; i < chunk.length; i++) {
            if (chunk[i] != (byte) bytes[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if the given bytes are at the given address.
     *
     * @param address The address to check.
     * @param bytes   The bytes to check.
     * @return True if the bytes are at the given address.
     */
    public boolean hasBytes(long address, byte[] bytes) {
        byte[] chunk = this.readBytes(address, bytes.length);
        for (int i = 0; i < chunk.length; i++) {
            if (chunk[i] != bytes[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if the given text is at the given address.
     *
     * @param address The address to check.
     * @param text    The text to check.
     * @return True if the text is at the given address.
     */
    public boolean hasText(long address, String text) {
        return this.hasBytes(address, text.getBytes());
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
        return this.findAddressOfText(start, text, (address, matchIndex) -> matchIndex == index);
    }

    /**
     * Find the address of a text inside the memory.
     * If there are multiple matches of the text, the given index will be used to select the correct one.
     *
     * @param start     The address to start searching from.
     * @param text      The text to search for.
     * @param condition The condition function to call for each matching address.
     * @return The address of the text at the given index
     */
    public long findAddressOfText(long start, String text, SearchCondition condition) {
        return this.findInMemory(start, Integer.MAX_VALUE, text.getBytes(), condition);
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
     * Find an address by matching multiple rules inside the memory.
     * It will start searching for the first rule and continues the next at the position where the previous match was found.
     *
     * @param rules The list of rules to search for.
     * @return The final addresses after all rules were matched.
     */
    public long findAddressUsingRules(SearchRule... rules) {
        long cursor = -1;
        for (SearchRule rule : rules) {
            cursor = this.findAddressOfText(cursor + 1, rule.getText(), rule.getCondition());
            if (cursor == -1) {
                return -1;
            }
        }
        return cursor;
    }

    /**
     * Get the module information of the given module name.
     *
     * @param moduleName The name of the module.
     * @return Information of the given module
     */
    public Psapi.ModuleInfo getModuleInfo(String moduleName) {
        return this.getModuleInfo(this.handle, moduleName);
    }

    /**
     * Collect all module names and address information.
     *
     * @return A map of all modules with their name and address information.
     */
    public Map<String, Psapi.ModuleInfo> getModules() {
        return this.getModules(this.handle);
    }

    /**
     * Find the first address of the modules.
     *
     * @return The address of the first module.
     */
    public long getFirstModuleAddress() {
        long minAddress = Long.MAX_VALUE;
        for (Map.Entry<String, Psapi.ModuleInfo> module : this.getModules().entrySet()) {
            long baseOfDll = module.getValue().getBaseOfDll();
            if (baseOfDll <= 0) {
                continue;
            }
            minAddress = Math.min(minAddress, baseOfDll);
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
        for (Map.Entry<String, Psapi.ModuleInfo> module : this.getModules().entrySet()) {
            maxAddress = Math.max(maxAddress, module.getValue().getBaseOfDll() + module.getValue().getSizeOfImage());
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

    public static class SearchRule {

        private final String text;
        private final SearchCondition condition;

        public SearchRule(String text, SearchCondition condition) {
            this.text = text;
            this.condition = condition;
        }

        public String getText() {
            return this.text;
        }

        public SearchCondition getCondition() {
            return this.condition;
        }
    }

}
