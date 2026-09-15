package com.soniclink;

import com.soniclink.codec.Packet;
import com.soniclink.codec.PacketCodec;
import com.soniclink.codec.PacketException;
import com.soniclink.codec.PacketException.CorruptedPacketException;
import com.soniclink.codec.PacketException.InvalidPacketException;
import com.soniclink.util.SonicConfig;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Unit tests for Packet and PacketCodec framing, encoding, decoding, and error validation.
 */
public class PacketCodecTest {

    public static void main(String[] args) {
        System.out.println("Running PacketCodecTest...");
        int passed = 0;
        int total = 0;

        // Test 1: Empty message packet
        total++;
        try {
            Packet p = new Packet("");
            byte[] encoded = PacketCodec.encode(p);
            Packet decoded = PacketCodec.decode(encoded);
            if (decoded.getPayloadLength() == 0 && "".equals(decoded.getPayloadAsString())) {
                passed++;
            } else {
                System.err.println("FAIL: Empty message decode mismatch");
            }
        } catch (Exception e) {
            System.err.println("FAIL: Empty message test threw: " + e.getMessage());
        }

        // Test 2: Standard message round-trip
        total++;
        try {
            String msg = "Hello SonicLink!";
            byte[] encoded = PacketCodec.encode(msg);
            Packet decoded = PacketCodec.decode(encoded);
            if (msg.equals(decoded.getPayloadAsString()) && decoded.getVersion() == SonicConfig.PROTOCOL_VERSION) {
                passed++;
            } else {
                System.err.println("FAIL: Standard message round-trip mismatch");
            }
        } catch (Exception e) {
            System.err.println("FAIL: Standard message test threw: " + e.getMessage());
        }

        // Test 3: Unicode message round-trip
        total++;
        try {
            String unicodeMsg = "Sonic 🔊 Link! 🚀 音響通信 Привет";
            byte[] encoded = PacketCodec.encode(unicodeMsg);
            Packet decoded = PacketCodec.decode(encoded);
            if (unicodeMsg.equals(decoded.getPayloadAsString())) {
                passed++;
            } else {
                System.err.println("FAIL: Unicode message round-trip mismatch");
            }
        } catch (Exception e) {
            System.err.println("FAIL: Unicode test threw: " + e.getMessage());
        }

        // Test 4: Maximum allowable payload (256 bytes)
        total++;
        try {
            byte[] maxPayload = new byte[SonicConfig.MAX_PAYLOAD_SIZE];
            Arrays.fill(maxPayload, (byte) 0x5A);
            Packet maxPacket = new Packet(maxPayload);
            byte[] encoded = PacketCodec.encode(maxPacket);
            Packet decoded = PacketCodec.decode(encoded);
            if (Arrays.equals(maxPayload, decoded.getPayload())) {
                passed++;
            } else {
                System.err.println("FAIL: Max payload round-trip mismatch");
            }
        } catch (Exception e) {
            System.err.println("FAIL: Max payload test threw: " + e.getMessage());
        }

        // Test 5: Reject payload exceeding maximum
        total++;
        try {
            byte[] oversized = new byte[SonicConfig.MAX_PAYLOAD_SIZE + 1];
            new Packet(oversized);
            System.err.println("FAIL: Oversized payload should throw IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }

        // Test 6: Detect corrupted payload via CRC
        total++;
        try {
            byte[] encoded = PacketCodec.encode("Integrity Check");
            // Flip a bit in the payload (byte 6)
            encoded[6] ^= 0x01;
            PacketCodec.decode(encoded);
            System.err.println("FAIL: Corrupted payload should have thrown CorruptedPacketException");
        } catch (CorruptedPacketException expected) {
            passed++;
        } catch (Exception e) {
            System.err.println("FAIL: Unexpected exception for corrupted payload: " + e);
        }

        // Test 7: Detect corrupted CRC byte
        total++;
        try {
            byte[] encoded = PacketCodec.encode("CRC Test");
            encoded[encoded.length - 1] ^= 0x01; // Corrupt CRC
            PacketCodec.decode(encoded);
            System.err.println("FAIL: Corrupted CRC should have thrown CorruptedPacketException");
        } catch (CorruptedPacketException expected) {
            passed++;
        } catch (Exception e) {
            System.err.println("FAIL: Unexpected exception for corrupted CRC: " + e);
        }

        // Test 8: Reject invalid magic bytes
        total++;
        try {
            byte[] encoded = PacketCodec.encode("Magic Test");
            encoded[0] = 0x00; // Invalidate MAGIC_1
            PacketCodec.decode(encoded);
            System.err.println("FAIL: Invalid magic should have thrown InvalidPacketException");
        } catch (InvalidPacketException expected) {
            passed++;
        } catch (Exception e) {
            System.err.println("FAIL: Unexpected exception for invalid magic: " + e);
        }

        // Test 9: Reject invalid version
        total++;
        try {
            byte[] encoded = PacketCodec.encode("Version Test");
            encoded[2] = 0x7F; // Unsupported version
            PacketCodec.decode(encoded);
            System.err.println("FAIL: Invalid version should have thrown InvalidPacketException");
        } catch (InvalidPacketException expected) {
            passed++;
        } catch (Exception e) {
            System.err.println("FAIL: Unexpected exception for invalid version: " + e);
        }

        // Test 10: Reject invalid declared length
        total++;
        try {
            byte[] encoded = PacketCodec.encode("Length Test");
            encoded[4] = (byte) (encoded[4] + 1); // Alter length byte
            PacketCodec.decode(encoded);
            System.err.println("FAIL: Altered length should have thrown InvalidPacketException");
        } catch (InvalidPacketException expected) {
            passed++;
        } catch (Exception e) {
            System.err.println("FAIL: Unexpected exception for altered length: " + e);
        }

        // Test 11: Reject truncated packet (< MIN_PACKET_SIZE)
        total++;
        try {
            byte[] truncated = new byte[]{SonicConfig.MAGIC_1, SonicConfig.MAGIC_2};
            PacketCodec.decode(truncated);
            System.err.println("FAIL: Truncated packet should have thrown InvalidPacketException");
        } catch (InvalidPacketException expected) {
            passed++;
        } catch (Exception e) {
            System.err.println("FAIL: Unexpected exception for truncated packet: " + e);
        }

        System.out.printf("PacketCodecTest: %d/%d passed.\n\n", passed, total);
        if (passed != total) {
            System.exit(1);
        }
    }
}
