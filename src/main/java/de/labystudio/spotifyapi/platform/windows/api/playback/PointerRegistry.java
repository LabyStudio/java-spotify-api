package de.labystudio.spotifyapi.platform.windows.api.playback;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for relative pointers.
 * The memory addresses are randomly generated and are not guaranteed to be unique.
 * Therefore, the registry is used to map the relative pointers to the absolute pointers.
 *
 * @author LabyStudio
 */
public class PointerRegistry {

    /**
     * The offset of the current absolute pointer.
     */
    private final long offset;

    private final Map<String, Long> pointerMap = new HashMap<>();

    /**
     * Create a new pointer registry with the given reference address and the actual base address.
     *
     * @param referenceBaseAddress the reference base address that is used to calculate the absolute addresses
     * @param baseAddress          the actual base address
     */
    public PointerRegistry(long referenceBaseAddress, long baseAddress) {
        this.offset = referenceBaseAddress - baseAddress;
    }

    /**
     * Register a new pointer with the given identifier and a relative pointer.
     *
     * @param identifier       the identifier of the pointer
     * @param referenceAddress the reference address
     */
    public void register(String identifier, long referenceAddress) {
        this.pointerMap.put(identifier, referenceAddress);
    }

    /**
     * Get the absolute address of the given identifier.
     *
     * @param identifier the identifier of the pointer
     * @return the absolute address
     */
    public long getAddress(String identifier) {
        Long pointer = this.pointerMap.get(identifier);
        return pointer == null ? -1 : pointer - this.offset;
    }

    public long getOffset() {
        return this.offset;
    }
}
