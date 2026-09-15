package com.soniclink.util;

/**
 * Computes and verifies a simple checksum over a byte array, so the receiver
 * can detect (not necessarily correct) transmission errors caused by noise.
 *
 * For a 2-day build, a simple checksum is enough — you don't need full
 * Hamming error-correction unless you have time left over.
 *
 * TODO (you implement):
 *  - computeChecksum(byte[] data): pick a simple, explainable algorithm.
 *    Options (pick ONE, and be ready to explain why in your report):
 *      a) Sum of all bytes modulo 256 (very simple, 1 byte checksum)
 *      b) XOR of all bytes (very simple, 1 byte checksum)
 *      c) A basic CRC-8 (more robust, more to explain/implement)
 *    Start with (a) or (b) if time is tight — you can upgrade later.
 *
 *  - verify(byte[] data, byte expectedChecksum): recompute and compare.
 *
 * Where this plugs in:
 *  - Sender: compute checksum on the message bytes, append it as an extra
 *    byte (or a few bits) to the bitstream before modulating.
 *  - Receiver: after demodulating and reconstructing bytes, split off the
 *    checksum byte(s), recompute checksum on the remaining data, and compare.
 */
public class ChecksumValidator {

    public static byte computeChecksum(byte[] data) {
        // TODO: implement (start with sum-mod-256 or XOR)
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public static boolean verify(byte[] data, byte expectedChecksum) {
        // TODO: implement using computeChecksum above
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
