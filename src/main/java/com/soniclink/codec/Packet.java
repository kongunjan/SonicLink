package com.soniclink.codec;

import com.soniclink.util.SonicConfig;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a structured application packet in the SonicLink protocol.
 *
 * Each packet encapsulates a protocol version and an arbitrary byte payload
 * (typically UTF-8 encoded text).
 */
public final class Packet {

    private final byte version;
    private final byte[] payload;

    /**
     * Constructs a packet with the default protocol version and UTF-8 string payload.
     *
     * @param message Text string to transmit.
     * @throws IllegalArgumentException if message is null or UTF-8 bytes exceed MAX_PAYLOAD_SIZE.
     */
    public Packet(String message) {
        this(SonicConfig.PROTOCOL_VERSION, message != null ? message.getBytes(StandardCharsets.UTF_8) : null);
    }

    /**
     * Constructs a packet with the default protocol version and byte payload.
     *
     * @param payload Raw byte array payload.
     * @throws IllegalArgumentException if payload is null or exceeds MAX_PAYLOAD_SIZE.
     */
    public Packet(byte[] payload) {
        this(SonicConfig.PROTOCOL_VERSION, payload);
    }

    /**
     * Constructs a packet with an explicit version and byte payload.
     *
     * @param version Protocol version identifier.
     * @param payload Raw byte array payload.
     * @throws IllegalArgumentException if payload is null or exceeds MAX_PAYLOAD_SIZE.
     */
    public Packet(byte version, byte[] payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Packet payload cannot be null");
        }
        if (payload.length > SonicConfig.MAX_PAYLOAD_SIZE) {
            throw new IllegalArgumentException("Payload size (" + payload.length
                    + " bytes) exceeds MAX_PAYLOAD_SIZE (" + SonicConfig.MAX_PAYLOAD_SIZE + " bytes)");
        }
        this.version = version;
        this.payload = Arrays.copyOf(payload, payload.length);
    }

    /**
     * Gets the protocol version of this packet.
     */
    public byte getVersion() {
        return version;
    }

    /**
     * Gets a defensive copy of the raw payload bytes.
     */
    public byte[] getPayload() {
        return Arrays.copyOf(payload, payload.length);
    }

    /**
     * Gets the payload length in bytes.
     */
    public int getPayloadLength() {
        return payload.length;
    }

    /**
     * Decodes the payload as a UTF-8 string.
     */
    public String getPayloadAsString() {
        return new String(payload, StandardCharsets.UTF_8);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Packet)) return false;
        Packet packet = (Packet) o;
        return version == packet.version && Arrays.equals(payload, packet.payload);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(version);
        result = 31 * result + Arrays.hashCode(payload);
        return result;
    }

    @Override
    public String toString() {
        return "Packet{" +
                "version=" + version +
                ", length=" + payload.length +
                ", payload=\"" + getPayloadAsString() + "\"" +
                '}';
    }
}
