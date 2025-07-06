package de.labystudio.spotifyapi.platform.linux.api.model;

/**
 * Interface member wrapper for the DBusSend class.
 *
 * @author LabyStudio
 */
public class InterfaceMember {

    private final String path;

    public InterfaceMember(String path) {
        this.path = path;
    }

    public String toString() {
        return this.path;
    }

}
