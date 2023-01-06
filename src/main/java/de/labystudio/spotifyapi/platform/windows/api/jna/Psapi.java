package de.labystudio.spotifyapi.platform.windows.api.jna;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface Psapi extends WinNT, StdCallLibrary {

    Psapi INSTANCE = Native.loadLibrary("psapi", Psapi.class, W32APIOptions.UNICODE_OPTIONS);

    boolean EnumProcessModulesEx(HANDLE hProcess, Pointer[] lphModule, int cb, IntByReference lpcbNeeded, int dwFilterFlag);

    int GetModuleBaseName(HANDLE hProcess, Pointer hModule, char[] lpBaseName, int nSize);

    boolean GetModuleInformation(HANDLE hProcess, Pointer hModule, ModuleInfo moduleInfo, int size);

    class ModuleFilter {
        public static final int NONE = 0x00;
        public static final int X32BIT = 0x01;
        public static final int X64BIT = 0x02;
        public static final int ALL = 0x03;
    }

    class ModuleInfo extends Structure {

        public Pointer BaseOfDll;
        public int SizeOfImage;
        public Pointer EntryPoint;

        public long getBaseOfDll() {
            return Pointer.nativeValue(this.BaseOfDll);
        }

        public long getEntryPoint() {
            return Pointer.nativeValue(this.EntryPoint);
        }

        public int getSizeOfImage() {
            return this.SizeOfImage;
        }

        @Override
        protected List<String> getFieldOrder() {
            return new ArrayList<>(Arrays.asList("BaseOfDll", "SizeOfImage", "EntryPoint"));
        }
    }
}