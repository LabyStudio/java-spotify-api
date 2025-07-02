package de.labystudio.spotifyapi.open.totp.model;

/**
 * This class represents a TOTP secret used for generating time-based one-time passwords.
 * It contains methods to convert the secret into a byte array and to retrieve the version of the secret.
 * It can be created from an array of integers or a string representation of the secret.
 *
 * @author LabyStudio
 */
public class Secret {

    private final int[] secret;
    private final int version;

    private Secret(int[] secret, int version) {
        this.secret = secret;
        this.version = version;
    }

    /**
     * Converts the secret into a byte array for TOTP generation in java.
     *
     * @return A byte array representing the secret, suitable for use in TOTP generation.
     */
    public byte[] toBytes() {
        // Convert secret numbers to xor results
        StringBuilder xorResults = new StringBuilder();
        for (int i = 0; i < this.secret.length; i++) {
            int result = this.secret[i] ^ (i % 33 + 9);
            xorResults.append(result);
        }

        // Convert xor results to hex
        StringBuilder hexResult = new StringBuilder();
        for (int i = 0; i < xorResults.length(); i++) {
            hexResult.append(String.format("%02x", (int) xorResults.charAt(i)));
        }

        // Convert hex to byte array
        byte[] byteArray = new byte[hexResult.length() / 2];
        for (int i = 0; i < hexResult.length(); i += 2) {
            int byteValue = Integer.parseInt(hexResult.substring(i, i + 2), 16);
            byteArray[i / 2] = (byte) byteValue;
        }
        return byteArray;
    }

    public int getVersion() {
        return this.version;
    }

    public static Secret fromNumbers(int[] secret, int version) {
        return new Secret(secret, version);
    }

    public static Secret fromString(String secret, int version) {
        int[] array = new int[secret.length()];
        for (int i = 0; i < secret.length(); i++) {
            array[i] = secret.charAt(i);
        }
        return new Secret(array, version);
    }
}
