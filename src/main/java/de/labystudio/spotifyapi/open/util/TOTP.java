package de.labystudio.spotifyapi.open.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class TOTP {

    private static final String DEFAULT_ALGORITHM = "HmacSHA1";

    private static final int DEFAULT_DIGITS = 6;
    private static final int DEFAULT_PERIOD = 30;

    public static String generateOtp(byte[] secret, long time) {
        long counter = time / DEFAULT_PERIOD;


        // Convert counter to byte array (Big Endian)
        ByteBuffer buffer = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN);
        buffer.putLong(counter);
        byte[] counterBytes = buffer.array();

        try {
            Mac mac = Mac.getInstance(DEFAULT_ALGORITHM);
            mac.init(new SecretKeySpec(secret, DEFAULT_ALGORITHM));
            byte[] hmac = mac.doFinal(counterBytes);

            // Extract dynamic offset
            int offset = hmac[hmac.length - 1] & 0x0F;

            // Compute binary value
            int binary = ((hmac[offset] & 0x7F) << 24) |
                    ((hmac[offset + 1] & 0xFF) << 16) |
                    ((hmac[offset + 2] & 0xFF) << 8) |
                    (hmac[offset + 3] & 0xFF);

            // Compute OTP
            int otp = binary % ((int) Math.pow(10, DEFAULT_DIGITS));

            // Return zero-padded OTP
            return String.format("%0" + DEFAULT_DIGITS + "d", otp);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Failed to generate TOTP", e);
        }
    }


}
