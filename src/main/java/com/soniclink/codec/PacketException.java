package com.soniclink.codec;

/**
 * Base exception for errors encountered during packet encoding, decoding, framing, or validation.
 */
public class PacketException extends Exception {

    private static final long serialVersionUID = 1L;

    public PacketException(String message) {
        super(message);
    }

    public PacketException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Thrown when packet framing is invalid (bad magic bytes, unknown version, length mismatch).
     */
    public static class InvalidPacketException extends PacketException {
        private static final long serialVersionUID = 1L;

        public InvalidPacketException(String message) {
            super(message);
        }
    }

    /**
     * Thrown when packet checksum verification fails, indicating transmission corruption.
     */
    public static class CorruptedPacketException extends PacketException {
        private static final long serialVersionUID = 1L;

        public CorruptedPacketException(String message) {
            super(message);
        }
    }
}
