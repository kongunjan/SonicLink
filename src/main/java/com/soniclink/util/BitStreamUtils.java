package com.soniclink.util;

/**
 * Utility methods for bit-level stream conversions.
 *
 * All operations adhere to MSB-first (Most Significant Bit first) network order:
 * The most significant bit (bit 7) of each byte corresponds to the earliest
 * boolean element in the bitstream, matching acoustic serialization conventions.
 */
public final class BitStreamUtils {

    private BitStreamUtils() {
        // Utility class
    }

    /**
     * Converts an array of bytes into a boolean bit array (MSB-first).
     *
     * @param data Byte array to convert.
     * @return Boolean array where true = 1 and false = 0, length = data.length * 8.
     * @throws IllegalArgumentException if data is null.
     */
    public static boolean[] bytesToBits(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("Byte array cannot be null");
        }
        if (data.length == 0) {
            return new boolean[0];
        }

        boolean[] bits = new boolean[data.length * 8];
        int index = 0;
        for (byte b : data) {
            for (int i = 7; i >= 0; i--) {
                bits[index++] = ((b >> i) & 1) == 1;
            }
        }
        return bits;
    }

    /**
     * Converts a boolean bit array back into a byte array (MSB-first).
     *
     * @param bits Boolean array of bits where true = 1 and false = 0.
     * @return Reconstructed byte array, length = bits.length / 8.
     * @throws IllegalArgumentException if bits is null or bits.length is not a multiple of 8.
     */
    public static byte[] bitsToBytes(boolean[] bits) {
        if (bits == null) {
            throw new IllegalArgumentException("Bit array cannot be null");
        }
        if (bits.length == 0) {
            return new byte[0];
        }
        if (bits.length % 8 != 0) {
            throw new IllegalArgumentException("Bit array length must be a multiple of 8, got length: " + bits.length);
        }

        int byteCount = bits.length / 8;
        byte[] bytes = new byte[byteCount];
        for (int i = 0; i < byteCount; i++) {
            int value = 0;
            for (int j = 0; j < 8; j++) {
                value = (value << 1) | (bits[i * 8 + j] ? 1 : 0);
            }
            bytes[i] = (byte) value;
        }
        return bytes;
    }

    /**
     * Helper to render a bit array as a readable '0' and '1' string for debugging and CLI reporting.
     *
     * @param bits Boolean bit array.
     * @return String representation consisting of '0' and '1' characters.
     */
    public static String toBitString(boolean[] bits) {
        if (bits == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder(bits.length);
        for (boolean b : bits) {
            sb.append(b ? '1' : '0');
        }
        return sb.toString();
    }
}