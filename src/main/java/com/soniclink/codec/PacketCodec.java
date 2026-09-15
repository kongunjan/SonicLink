package com.soniclink.codec;

import com.soniclink.codec.PacketException.CorruptedPacketException;
import com.soniclink.codec.PacketException.InvalidPacketException;
import com.soniclink.util.ChecksumValidator;
import com.soniclink.util.SonicConfig;
import java.util.Arrays;

/**
 * Encodes and decodes SonicLink protocol packets to/from raw byte arrays.
 *
 * Binary Wire Format:
 * ┌───────────┬───────────┬─────────┬──────────────┬──────────────┬───────────────────┬───────────┐
 * │ MAGIC_1   │ MAGIC_2   │ VERSION │ LENGTH (MSB) │ LENGTH (LSB) │ PAYLOAD (0..256B) │ CRC-8     │
 * │ 1 byte    │ 1 byte    │ 1 byte  │ 1 byte       │ 1 byte       │ N bytes           │ 1 byte    │
 * │ 0x53 ('S')│ 0x4C ('L')│ 0x01    │ (len >> 8)   │ (len & 0xFF) │ UTF-8 Data        │ Checksum  │
 * └───────────┴───────────┴─────────┴──────────────┴──────────────┴───────────────────┴───────────┘
 *
 * Total Framing Overhead: 6 bytes (5 bytes header + 1 byte CRC-8 footer).
 * CRC-8 covers all previous bytes [0 .. HEADER_SIZE + Length - 1] so corrupted headers
 * (like falsified lengths or bad versions) are caught before or during CRC verification.
 */
public final class PacketCodec {

    public static final int HEADER_SIZE = 5;
    public static final int CRC_SIZE = 1;
    public static final int MIN_PACKET_SIZE = HEADER_SIZE + CRC_SIZE; // 6 bytes (empty payload)

    private PacketCodec() {
        // Utility class
    }

    /**
     * Encodes a Packet object into raw binary wire format.
     *
     * @param packet Packet to serialize.
     * @return Serialized byte array ready for bitstream conversion and FSK modulation.
     * @throws IllegalArgumentException if packet is null.
     */
    public static byte[] encode(Packet packet) {
        if (packet == null) {
            throw new IllegalArgumentException("Packet to encode cannot be null");
        }

        byte[] payload = packet.getPayload();
        int payloadLength = payload.length;
        if (payloadLength > SonicConfig.MAX_PAYLOAD_SIZE) {
            throw new IllegalArgumentException("Payload length exceeds MAX_PAYLOAD_SIZE");
        }

        int totalSize = HEADER_SIZE + payloadLength + CRC_SIZE;
        byte[] frame = new byte[totalSize];

        // Header
        frame[0] = SonicConfig.MAGIC_1;
        frame[1] = SonicConfig.MAGIC_2;
        frame[2] = packet.getVersion();
        frame[3] = (byte) ((payloadLength >> 8) & 0xFF);
        frame[4] = (byte) (payloadLength & 0xFF);

        // Payload
        System.arraycopy(payload, 0, frame, HEADER_SIZE, payloadLength);

        // CRC-8 over header + payload
        byte[] dataToHash = Arrays.copyOfRange(frame, 0, HEADER_SIZE + payloadLength);
        frame[HEADER_SIZE + payloadLength] = ChecksumValidator.computeChecksum(dataToHash);

        return frame;
    }

    /**
     * Convenience method to construct and encode a packet from a string message.
     *
     * @param message Text string to encode.
     * @return Encoded byte array.
     */
    public static byte[] encode(String message) {
        return encode(new Packet(message));
    }

    /**
     * Decodes a raw binary wire frame into a validated Packet object.
     *
     * @param rawFrame Raw bytes captured from acoustic demodulation.
     * @return Validated Packet object with verified CRC and intact payload.
     * @throws InvalidPacketException if magic bytes, version, or length formatting is invalid.
     * @throws CorruptedPacketException if CRC-8 verification fails.
     */
    public static Packet decode(byte[] rawFrame) throws PacketException {
        if (rawFrame == null) {
            throw new InvalidPacketException("Packet raw byte array cannot be null");
        }
        if (rawFrame.length < MIN_PACKET_SIZE) {
            throw new InvalidPacketException("Packet too short (" + rawFrame.length
                    + " bytes). Minimum valid packet size is " + MIN_PACKET_SIZE + " bytes.");
        }

        // 1. Verify Magic Bytes
        if (rawFrame[0] != SonicConfig.MAGIC_1 || rawFrame[1] != SonicConfig.MAGIC_2) {
            throw new InvalidPacketException(String.format(
                    "Invalid magic bytes: 0x%02X 0x%02X (expected: 0x%02X 0x%02X)",
                    rawFrame[0], rawFrame[1], SonicConfig.MAGIC_1, SonicConfig.MAGIC_2));
        }

        // 2. Verify Protocol Version
        byte version = rawFrame[2];
        if (version != SonicConfig.PROTOCOL_VERSION) {
            throw new InvalidPacketException("Unsupported protocol version: " + version
                    + " (expected: " + SonicConfig.PROTOCOL_VERSION + ")");
        }

        // 3. Extract and Validate Length
        int declaredLength = ((rawFrame[3] & 0xFF) << 8) | (rawFrame[4] & 0xFF);
        if (declaredLength < 0 || declaredLength > SonicConfig.MAX_PAYLOAD_SIZE) {
            throw new InvalidPacketException("Invalid declared payload length: " + declaredLength
                    + " (maximum allowed: " + SonicConfig.MAX_PAYLOAD_SIZE + ")");
        }

        int expectedTotalLength = HEADER_SIZE + declaredLength + CRC_SIZE;
        if (rawFrame.length != expectedTotalLength) {
            throw new InvalidPacketException("Packet size mismatch: frame is " + rawFrame.length
                    + " bytes, but header declares " + declaredLength + " payload bytes (expected "
                    + expectedTotalLength + " total bytes)");
        }

        // 4. Verify CRC-8 Checksum
        byte receivedCrc = rawFrame[HEADER_SIZE + declaredLength];
        byte[] coveredData = Arrays.copyOfRange(rawFrame, 0, HEADER_SIZE + declaredLength);
        if (!ChecksumValidator.verify(coveredData, receivedCrc)) {
            byte calculatedCrc = ChecksumValidator.computeChecksum(coveredData);
            throw new CorruptedPacketException(String.format(
                    "CRC-8 verification failed! Received: 0x%02X, Calculated: 0x%02X",
                    receivedCrc, calculatedCrc));
        }

        // 5. Extract Payload
        byte[] payload = Arrays.copyOfRange(rawFrame, HEADER_SIZE, HEADER_SIZE + declaredLength);
        return new Packet(version, payload);
    }
}
