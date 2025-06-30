package de.labystudio.spotifyapi.open.totp;

public class Secret {

    private final int[] secret;
    private final int version;

    public Secret(int[] secret, int version) {
        this.secret = secret;
        this.version = version;
    }

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
}
