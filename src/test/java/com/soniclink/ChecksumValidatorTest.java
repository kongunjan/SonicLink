package com.soniclink;

import com.soniclink.util.ChecksumValidator;
import java.nio.charset.StandardCharsets;

/**
 * Unit tests for CRC-8 Checksum computation and verification.
 */
public class ChecksumValidatorTest {

    public static void main(String[] args) {
        System.out.println("Running ChecksumValidatorTest...");
        int passed = 0;
        int total = 0;

        // Test 1: Empty data
        total++;
        byte[] empty = new byte[0];
        byte crcEmpty = ChecksumValidator.computeChecksum(empty);
        if (crcEmpty == 0x00 && ChecksumValidator.verify(empty, (byte) 0x00)) {
            passed++;
        } else {
            System.err.println("FAIL: Empty data test");
        }

        // Test 2: Known data determinism
        total++;
        byte[] data1 = "123456789".getBytes(StandardCharsets.US_ASCII);
        byte crc1 = ChecksumValidator.computeChecksum(data1);
        byte crc2 = ChecksumValidator.computeChecksum(data1);
        if (crc1 == crc2 && ChecksumValidator.verify(data1, crc1)) {
            passed++;
        } else {
            System.err.println("FAIL: Determinism test");
        }

        // Test 3: Changed byte detection
        total++;
        byte[] dataChanged = "123456788".getBytes(StandardCharsets.US_ASCII);
        byte crcChanged = ChecksumValidator.computeChecksum(dataChanged);
        if (crc1 != crcChanged && !ChecksumValidator.verify(dataChanged, crc1)) {
            passed++;
        } else {
            System.err.println("FAIL: Changed byte detection test");
        }

        // Test 4: Checksum mismatch
        total++;
        byte wrongCrc = (byte) (crc1 ^ 0x55);
        if (!ChecksumValidator.verify(data1, wrongCrc)) {
            passed++;
        } else {
            System.err.println("FAIL: Checksum mismatch test");
        }

        // Test 5: Null input handling on verify
        total++;
        if (!ChecksumValidator.verify(null, (byte) 0x00)) {
            passed++;
        } else {
            System.err.println("FAIL: Null verify test");
        }

        // Test 6: Null input handling on compute throws exception
        total++;
        try {
            ChecksumValidator.computeChecksum(null);
            System.err.println("FAIL: Null compute should throw IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }

        // Test 7: Multi-byte and binary values (0x00, 0xFF)
        total++;
        byte[] binaryData = new byte[]{0x00, (byte) 0xFF, 0x55, (byte) 0xAA};
        byte crcBin = ChecksumValidator.computeChecksum(binaryData);
        if (ChecksumValidator.verify(binaryData, crcBin)) {
            passed++;
        } else {
            System.err.println("FAIL: Binary data test");
        }

        System.out.printf("ChecksumValidatorTest: %d/%d passed.\n\n", passed, total);
        if (passed != total) {
            System.exit(1);
        }
    }
}
