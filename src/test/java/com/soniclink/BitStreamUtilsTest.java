package com.soniclink;

import com.soniclink.util.BitStreamUtils;
import java.util.Arrays;

/**
 * Comprehensive tests for BitStreamUtils.
 */
public class BitStreamUtilsTest {

    public static void main(String[] args) {
        System.out.println("Running BitStreamUtilsTest...");
        int passed = 0;
        int total = 0;

        // Test 1: Empty array round-trip
        total++;
        byte[] empty = new byte[0];
        boolean[] emptyBits = BitStreamUtils.bytesToBits(empty);
        byte[] roundTripEmpty = BitStreamUtils.bitsToBytes(emptyBits);
        if (emptyBits.length == 0 && roundTripEmpty.length == 0) {
            passed++;
        } else {
            System.err.println("FAIL: Empty array test");
        }

        // Test 2: One byte
        total++;
        byte[] oneByte = new byte[]{(byte) 0xA5}; // 10100101
        boolean[] oneByteBits = BitStreamUtils.bytesToBits(oneByte);
        byte[] roundTripOne = BitStreamUtils.bitsToBytes(oneByteBits);
        if (oneByteBits.length == 8 && Arrays.equals(oneByte, roundTripOne)) {
            passed++;
        } else {
            System.err.println("FAIL: One byte test");
        }

        // Test 3: MSB-first bit pattern check
        total++;
        byte[] msbTest = new byte[]{(byte) 0x81}; // 10000001
        boolean[] bits81 = BitStreamUtils.bytesToBits(msbTest);
        if (bits81[0] && !bits81[1] && !bits81[2] && !bits81[3] &&
            !bits81[4] && !bits81[5] && !bits81[6] && bits81[7]) {
            passed++;
        } else {
            System.err.println("FAIL: MSB-first bit order test");
        }

        // Test 4: 0x00 byte
        total++;
        byte[] zeroByte = new byte[]{0x00};
        boolean[] zeroBits = BitStreamUtils.bytesToBits(zeroByte);
        byte[] roundTripZero = BitStreamUtils.bitsToBytes(zeroBits);
        if (Arrays.equals(zeroByte, roundTripZero)) {
            passed++;
        } else {
            System.err.println("FAIL: 0x00 byte test");
        }

        // Test 5: 0xFF byte
        total++;
        byte[] ffByte = new byte[]{(byte) 0xFF};
        boolean[] ffBits = BitStreamUtils.bytesToBits(ffByte);
        byte[] roundTripFF = BitStreamUtils.bitsToBytes(ffBits);
        if (Arrays.equals(ffByte, roundTripFF)) {
            passed++;
        } else {
            System.err.println("FAIL: 0xFF byte test");
        }

        // Test 6: Multiple bytes ("Hello, SonicLink!")
        total++;
        byte[] multi = "Hello, SonicLink!".getBytes();
        boolean[] multiBits = BitStreamUtils.bytesToBits(multi);
        byte[] roundTripMulti = BitStreamUtils.bitsToBytes(multiBits);
        if (multiBits.length == multi.length * 8 && Arrays.equals(multi, roundTripMulti)) {
            passed++;
        } else {
            System.err.println("FAIL: Multiple bytes test");
        }

        // Test 7: All 256 possible bytes round-trip
        total++;
        byte[] all256 = new byte[256];
        for (int i = 0; i < 256; i++) {
            all256[i] = (byte) i;
        }
        boolean[] allBits = BitStreamUtils.bytesToBits(all256);
        byte[] roundTripAll = BitStreamUtils.bitsToBytes(allBits);
        if (Arrays.equals(all256, roundTripAll)) {
            passed++;
        } else {
            System.err.println("FAIL: All 256 bytes test");
        }

        // Test 8: Null input rejection
        total++;
        int nullChecks = 0;
        try {
            BitStreamUtils.bytesToBits(null);
        } catch (IllegalArgumentException e) {
            nullChecks++;
        }
        try {
            BitStreamUtils.bitsToBytes(null);
        } catch (IllegalArgumentException e) {
            nullChecks++;
        }
        if (nullChecks == 2) {
            passed++;
        } else {
            System.err.println("FAIL: Null rejection test");
        }

        // Test 9: Incomplete byte rejection (not a multiple of 8)
        total++;
        try {
            BitStreamUtils.bitsToBytes(new boolean[]{true, false, true});
            System.err.println("FAIL: Incomplete byte should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            passed++;
        }

        System.out.printf("BitStreamUtilsTest: %d/%d passed.\n\n", passed, total);
        if (passed != total) {
            System.exit(1);
        }
    }
}
