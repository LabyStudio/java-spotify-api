package de.labystudio.spotifyapi.platform.windows.api.jna;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.WinDef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface Tlhelp32 {
    WinDef.DWORD TH32CS_SNAPMODULE = new WinDef.DWORD(0x00000008);
    WinDef.DWORD TH32CS_SNAPPROCESS = new WinDef.DWORD(0x00000002);

    class MODULEENTRY32W extends Structure {
        public static class ByReference extends MODULEENTRY32W implements Structure.ByReference {
        }

        public WinDef.DWORD dwSize;
        public WinDef.DWORD th32ModuleID;
        public WinDef.DWORD th32ProcessID;
        public WinDef.DWORD GlblcntUsage;
        public WinDef.DWORD ProccntUsage;
        public Pointer modBaseAddr;
        public WinDef.DWORD modBaseSize;
        public WinDef.HMODULE hModule;
        public char[] szModule = new char[255 + 1];
        public char[] szExePath = new char[Kernel32.MAX_PATH];

        public MODULEENTRY32W() {
            this.dwSize = new WinDef.DWORD(this.size());
        }

        public MODULEENTRY32W(Pointer memory) {
            super(memory);
            this.read();
        }

        @Override
        protected List<String> getFieldOrder() {
            return new ArrayList<>(Arrays.asList("dwSize", "th32ModuleID", "th32ProcessID", "GlblcntUsage",
                    "ProccntUsage", "modBaseAddr", "modBaseSize", "hModule",
                    "szModule", "szExePath"));
        }
    }

    class PROCESSENTRY32 extends Structure {
        public WinDef.DWORD dwSize;
        public WinDef.DWORD cntUsage;
        public WinDef.DWORD th32ProcessID;
        public BaseTSD.ULONG_PTR th32DefaultHeapID;
        public WinDef.DWORD th32ModuleID;
        public WinDef.DWORD cntThreads;
        public WinDef.DWORD th32ParentProcessID;
        public WinDef.LONG pcPriClassBase;
        public WinDef.DWORD dwFlags;
        public char[] szExeFile = new char[260];

        public PROCESSENTRY32() {
            this.dwSize = new WinDef.DWORD((long) this.size());
        }

        public PROCESSENTRY32(Pointer memory) {
            super(memory);
            this.read();
        }

        public static class ByReference extends com.sun.jna.platform.win32.Tlhelp32.PROCESSENTRY32 implements Structure.ByReference {
            public ByReference() {
            }

            public ByReference(Pointer memory) {
                super(memory);
            }

            @Override
            protected List<String> getFieldOrder() {
                return new ArrayList<>(Arrays.asList("dwSize", "cntUsage", "th32ProcessID", "th32DefaultHeapID",
                        "th32ModuleID", "cntThreads", "th32ParentProcessID", "pcPriClassBase", "dwFlags",
                        "szExeFile"));
            }
        }

        @Override
        protected List<String> getFieldOrder() {
            return new ArrayList<>(Arrays.asList("dwSize", "cntUsage", "th32ProcessID", "th32DefaultHeapID",
                    "th32ModuleID", "cntThreads", "th32ParentProcessID", "pcPriClassBase", "dwFlags",
                    "szExeFile"));
        }
    }
}