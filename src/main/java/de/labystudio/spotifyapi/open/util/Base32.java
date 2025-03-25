package de.labystudio.spotifyapi.open.util;

import java.util.Arrays;

class Base32 {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int[] DECODE_MAP = new int[256];

    static {
        Arrays.fill(DECODE_MAP, -1);
        for (int i = 0; i < ALPHABET.length(); i++) {
            DECODE_MAP[ALPHABET.charAt(i)] = i;
        }
    }

    public static String encode(byte[] data) {
        StringBuilder encoded = new StringBuilder();
        int index = 0, digit = 0, currByte, nextByte;
        int dataLength = data.length;
        for (int i = 0; i < dataLength; i++) {
            currByte = data[i] & 0xFF;
            index = (index + 8) % 5;
            digit = currByte >> index;
            encoded.append(ALPHABET.charAt(digit));
            if (index == 0 && i + 1 < dataLength) {
                nextByte = data[i + 1] & 0xFF;
                digit = ((currByte & ((1 << index) - 1)) << (5 - index)) | (nextByte >> (index + 3));
                encoded.append(ALPHABET.charAt(digit));
            }
        }
        return encoded.toString();
    }

    public static byte[] decode(String encoded) {
        // Remove any padding characters ('=')
        encoded = encoded.replace("=", "");

        int length = encoded.length();
        int byteLength = (length * 5) / 8;
        byte[] decoded = new byte[byteLength];
        int buffer = 0;
        int bitsLeft = 0;
        int decodedIndex = 0;

        for (int i = 0; i < length; i++) {
            char c = encoded.charAt(i);
            int value = DECODE_MAP[c];
            if (value == -1) {
                throw new IllegalArgumentException("Invalid Base32 character: " + c);
            }

            buffer = (buffer << 5) | value;
            bitsLeft += 5;

            if (bitsLeft >= 8) {
                bitsLeft -= 8;
                decoded[decodedIndex++] = (byte) (buffer >> bitsLeft);
                buffer &= (1 << bitsLeft) - 1;
            }
        }

        return decoded;
    }
}
