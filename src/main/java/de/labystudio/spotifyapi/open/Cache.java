package de.labystudio.spotifyapi.open;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A cache for the open spotify api.
 *
 * @param <T> The type of the cached object
 * @author LabyStudio
 */
public class Cache<T> {

    private final Map<String, T> cache = new ConcurrentHashMap<>();
    private final List<String> cacheQueue = new ArrayList<>();
    private int cacheSize;

    /**
     * Create a new cache with a specific size
     *
     * @param cacheSize The size of the cache. The cache will remove the oldest entry if the size is reached.
     */
    public Cache(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    /**
     * Set the maximal amount of entries to cache.
     *
     * @param cacheSize The maximal amount of entries to cache
     */
    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    /**
     * Store an entry in the cache
     * If the max cache size is reached, the oldest entry will be removed.
     *
     * @param key The key of the entry
     */
    public void push(String key, T value) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }

        // Remove entry from cache if cache is full
        if (this.cacheQueue.size() > this.cacheSize) {
            String urlToRemove = this.cacheQueue.remove(0);
            this.cache.remove(urlToRemove);
        }

        // Add new entry to cache
        this.cache.put(key, value);
        this.cacheQueue.add(key);
    }

    /**
     * Check if the cache contains the given key.
     *
     * @param key The key to check
     * @return True if the cache contains the key
     */
    public boolean has(String key) {
        return this.cache.containsKey(key);
    }

    /**
     * Get the cached entry by the given key.
     *
     * @param key The key of the entry
     * @return The cached entry or null if it doesn't exist
     */
    public T get(String key) {
        return this.cache.get(key);
    }

    /**
     * Clear the cache.
     */
    public void clear() {
        this.cache.clear();
        this.cacheQueue.clear();
    }

}