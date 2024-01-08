package de.labystudio.spotifyapi.platform.linux.api;

/**
 * Parameter wrapper for the DBusSend class.
 * <p>
 * It appends the parameter key and value to the command.
 * If the value is null, it will only append the key using "--key".
 * If the value is not null, it will append the key and value using "--key=value".
 *
 * @author LabyStudio
 */
public class Parameter {

    private final String key;
    private final String value;

    public Parameter(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public Parameter(String key) {
        this(key, null);
    }

    public String toString() {
        return String.format(
                "--%s%s",
                this.key,
                this.value == null ? "" : "=" + this.value
        );
    }

}
