package com.soniclink;

import com.soniclink.util.BitStreamUtils;

/**
 * Simple manual test runner (no JUnit dependency needed for a fast setup —
 * add JUnit later if you want proper assertions).
 *
 * Run with: java -cp target/classes com.soniclink.BitStreamUtilsTest
 *
 * TODO: once BitStreamUtils is implemented, verify that converting bytes
 * to bits and back to bytes gives you the original bytes (a "round trip"
 * test). This is exactly the kind of test worth including in your report's
 * "Testing Approach" section.
 */
public class BitStreamUtilsTest {

    public static void main(String[] args) {
        byte[] original = "Hi".getBytes();

        boolean[] bits = BitStreamUtils.bytesToBits(original);
        byte[] reconstructed = BitStreamUtils.bitsToBytes(bits);

        boolean pass = java.util.Arrays.equals(original, reconstructed);
        System.out.println("BitStreamUtils round-trip test: " + (pass ? "PASS" : "FAIL"));

        if (!pass) {
            System.out.println("Original:      " + java.util.Arrays.toString(original));
            System.out.println("Reconstructed: " + java.util.Arrays.toString(reconstructed));
        }
    }
}
