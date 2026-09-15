package com.soniclink.util;

/**
 * Computes and verifies CRC-8 checksums to ensure packet transmission integrity.
 *
 * Mathematical Specification:
 * - Generator Polynomial: x^8 + x^2 + x^1 + 1 (binary: 100000111, hexadecimal: 0x07)
 * - Standard: CRC-8 / SMBus / ATM
 * - Initial Value: 0x00
 * - Final XOR Value: 0x00
 * - Input/Output Reflection: None (MSB-first)
 *
 * This implementation provides single-byte deterministic error detection capable of
 * detecting all single-bit and double-bit burst errors shorter than 8 bits, as well
 * as over 99.6% of arbitrary noise-induced corruption.
 */
public final class ChecksumValidator {

    private static final int POLYNOMIAL = 0x07;
    private static final byte[] CRC_TABLE = new byte[256];

    static {
        // Precompute lookup table for high throughput and zero allocations
        for (int i = 0; i < 256; i++) {
            int curr = i;
            for (int bit = 0; bit < 8; bit++) {
                if ((curr & 0x80) != 0) {
                    curr = ((curr << 1) ^ POLYNOMIAL) & 0xFF;
                } else {
                    curr = (curr << 1) & 0xFF;
                }
            }
            CRC_TABLE[i] = (byte) curr;
        }
    }

    private ChecksumValidator() {
        // Utility class
    }

    /**
     * Computes the CRC-8 checksum for the provided byte array.
     *
     * @param data The input bytes to calculate the checksum over.
     * @return The single-byte CRC-8 checksum.
     * @throws IllegalArgumentException if data is null.
     */
    public static byte computeChecksum(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("Data byte array cannot be null");
        }

        int crc = 0x00;
        for (byte b : data) {
            int tableIndex = (crc ^ (b & 0xFF)) & 0xFF;
            crc = CRC_TABLE[tableIndex] & 0xFF;
        }
        return (byte) crc;
    }

    /**
     * Verifies that the recomputed CRC-8 checksum of the data matches the expected checksum.
     *
     * @param data The input bytes to verify.
     * @param expectedChecksum The checksum byte to compare against.
     * @return true if the recomputed checksum matches expectedChecksum; false if data is null or mismatch.
     */
    public static boolean verify(byte[] data, byte expectedChecksum) {
        if (data == null) {
            return false;
        }
        return computeChecksum(data) == expectedChecksum;
    }
}
