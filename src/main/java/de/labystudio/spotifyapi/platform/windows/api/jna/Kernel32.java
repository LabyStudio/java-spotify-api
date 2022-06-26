package de.labystudio.spotifyapi.platform.windows.api.jna;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface Kernel32 extends WinNT, StdCallLibrary {

    Kernel32 INSTANCE = (Kernel32) Native.loadLibrary("kernel32", Kernel32.class, W32APIOptions.UNICODE_OPTIONS);

    boolean ReadProcessMemory(HANDLE hProcess, Pointer lpBaseAddress, Pointer lpBuffer, int nSize, IntByReference lpNumberOfBytesRead);

    boolean Module32NextW(HANDLE hSnapshot, Tlhelp32.MODULEENTRY32W lpme);

    boolean Process32First(HANDLE var1, Tlhelp32.PROCESSENTRY32.ByReference var2);

    boolean Process32Next(HANDLE var1, Tlhelp32.PROCESSENTRY32.ByReference var2);

    HANDLE CreateToolhelp32Snapshot(DWORD dwFlags, DWORD th32ProcessID);

    boolean CloseHandle(HANDLE hObject);

    HANDLE OpenProcess(int fdwAccess, boolean fInherit, int IDProcess);
}