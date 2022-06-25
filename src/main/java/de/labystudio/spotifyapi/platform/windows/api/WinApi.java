package de.labystudio.spotifyapi.platform.windows.api;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Tlhelp32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Windows API Utilities
 *
 * @author LabyStudio
 */
public interface WinApi {

    int PROCESS_VM_READ = 0x0010;
    int PROCESS_VM_WRITE = 0x0020;
    int PROCESS_VM_OPERATION = 0x0008;

    default int getProcessIdByName(String exeName) {
        Kernel32 kernel = Kernel32.INSTANCE;

        WinNT.HANDLE snapshot = kernel.CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPPROCESS, new WinDef.DWORD(0));
        Tlhelp32.PROCESSENTRY32.ByReference processEntry = new Tlhelp32.PROCESSENTRY32.ByReference();

        while (kernel.Process32Next(snapshot, processEntry)) {
            if (Native.toString(processEntry.szExeFile).equals(exeName)) {
                int processId = processEntry.th32ProcessID.intValue();
                kernel.CloseHandle(snapshot);
                return processId;
            }
        }
        kernel.CloseHandle(snapshot);
        return -1;
    }

    default List<Integer> getProcessIdsByName(String exeName) {
        List<Integer> processIds = new ArrayList<>();
        Kernel32 kernel = Kernel32.INSTANCE;

        WinNT.HANDLE snapshot = kernel.CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPPROCESS, new WinDef.DWORD(0));
        Tlhelp32.PROCESSENTRY32.ByReference processEntry = new Tlhelp32.PROCESSENTRY32.ByReference();

        while (kernel.Process32Next(snapshot, processEntry)) {
            if (Native.toString(processEntry.szExeFile).equals(exeName)) {
                processIds.add(processEntry.th32ProcessID.intValue());
            }
        }
        kernel.CloseHandle(snapshot);
        return processIds;
    }

    default WinNT.HANDLE openProcessHandle(int processId) {
        Kernel32 kernel = Kernel32.INSTANCE;
        return kernel.OpenProcess(PROCESS_VM_READ | PROCESS_VM_WRITE | PROCESS_VM_OPERATION, false, processId);
    }

    default WinDef.HWND openWindow(int processId) {
        AtomicReference<WinDef.HWND> window = new AtomicReference<>();

        // Iterate over all windows and find the one with the given processId
        User32.INSTANCE.EnumWindows((hWnd, data) -> {

            // Get the process id of the window
            IntByReference reference = new IntByReference();
            User32.INSTANCE.GetWindowThreadProcessId(hWnd, reference);

            // Check if the window belongs to the process
            if (reference.getValue() == processId && this.isWindowVisible(hWnd)) {
                window.set(hWnd);
                return false;
            }

            return true;
        }, null);

        // Return the window handle
        return window.get();
    }

    default boolean isWindowVisible(WinDef.HWND hWnd) {
        return User32.INSTANCE.IsWindowVisible(hWnd);
    }

    default String getWindowTitle(WinDef.HWND window) {
        char[] buffer = new char[512];
        int length = User32.INSTANCE.GetWindowText(window, buffer, buffer.length);
        return Native.toString(Arrays.copyOf(buffer, length));
    }

    default Map<Long, Long> getModules(long pid) {
        Map<Long, Long> map = new HashMap<>();

        WinNT.HANDLE snapshot = Kernel32.INSTANCE.CreateToolhelp32Snapshot(
                Tlhelp32.TH32CS_SNAPMODULE,
                new WinDef.DWORD(pid)
        );
        if (snapshot == null) {
            return map;
        }

        Tlhelp32.MODULEENTRY32W moduleEntry = new Tlhelp32.MODULEENTRY32W.ByReference();
        while (Kernel32.INSTANCE.Module32NextW(snapshot, moduleEntry)) {
            map.put(Pointer.nativeValue(moduleEntry.modBaseAddr), moduleEntry.modBaseSize.longValue());
        }
        Kernel32.INSTANCE.CloseHandle(snapshot);
        return map;
    }

    default long getModuleAddress(long pid, String moduleName) {
        WinNT.HANDLE snapshot = Kernel32.INSTANCE.CreateToolhelp32Snapshot(
                Tlhelp32.TH32CS_SNAPMODULE,
                new WinDef.DWORD(pid)
        );
        if (snapshot == null) {
            return 0;
        }

        Tlhelp32.MODULEENTRY32W moduleEntry = new Tlhelp32.MODULEENTRY32W.ByReference();
        while (Kernel32.INSTANCE.Module32NextW(snapshot, moduleEntry)) {
            String name = Native.toString(moduleEntry.szModule);
            if (name.equals(moduleName)) {
                Kernel32.INSTANCE.CloseHandle(snapshot);
                return Pointer.nativeValue(moduleEntry.modBaseAddr);
            }
        }
        Kernel32.INSTANCE.CloseHandle(snapshot);
        return -1;
    }
}
