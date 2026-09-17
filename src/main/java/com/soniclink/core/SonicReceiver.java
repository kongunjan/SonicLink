package com.soniclink.core;

import com.soniclink.audio.AudioReceiver;
import com.soniclink.codec.Demodulator;
import com.soniclink.codec.Packet;
import com.soniclink.codec.PacketCodec;
import com.soniclink.codec.PacketException;
import com.soniclink.codec.PreambleDetector;
import com.soniclink.util.BitStreamUtils;
import com.soniclink.util.SonicConfig;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * High-level receiver for SonicLink.
 *
 * Workflow:
 * Microphone
 *   -> PCM audio
 *   -> Preamble detection
 *   -> Header decoding
 *   -> Determine packet size
 *   -> Exact packet demodulation
 *   -> CRC validation
 *   -> Message
 */
public class SonicReceiver {

    private final AudioReceiver audioReceiver;
    private final Demodulator demodulator;

    public SonicReceiver() {
        this.audioReceiver = new AudioReceiver();
        this.demodulator = new Demodulator();
    }

    /**
     * Records audio and attempts to receive one SonicLink packet.
     *
     * @throws Exception if recording or decoding fails
     */
    public void listen() throws Exception {

        System.out.println();
        System.out.println("🎙 SonicLink Receiver");
        System.out.println("---------------------");
        System.out.println("Listening for acoustic transmission...");
        System.out.println();

        /*
         * Record a fixed amount of audio.
         *
         * AudioReceiver is responsible only for microphone capture.
         */
        byte[] pcmData = audioReceiver.record(20000);

        if (pcmData == null || pcmData.length == 0) {
            System.err.println("✗ No audio data received.");
            return;
        }

        System.out.println("✓ Recording complete.");
        System.out.println("   PCM bytes: " + pcmData.length);

        /*
         * Find the end of the preamble.
         */
        int preambleEnd;

        try {
            preambleEnd =
                    PreambleDetector.findPreambleEnd(
                            pcmData,
                            SonicConfig.SAMPLE_RATE,
                            SonicConfig.PREAMBLE_FREQ,
                            SonicConfig.PREAMBLE_DURATION_MS
                    );
        } catch (Exception e) {
            System.err.println("✗ Preamble detection failed.");
            System.err.println("  " + e.getMessage());
            return;
        }

        if (preambleEnd < 0 || preambleEnd >= pcmData.length) {
            System.err.println("✗ Invalid preamble position.");
            return;
        }

        System.out.println("✓ Preamble detected.");
        System.out.println("   Data starts at byte: " + preambleEnd);

        /*
         * Each PCM sample is 16-bit.
         * Therefore two PCM bytes represent one sample.
         */
        int samplesPerSymbol =
                demodulator.getSamplesPerSymbol();

        /*
         * ---------------------------------------------------------
         * STEP 1: Decode only the fixed-size packet header.
         * ---------------------------------------------------------
         *
         * Header:
         *
         * MAGIC_1       1 byte
         * MAGIC_2       1 byte
         * VERSION       1 byte
         * LENGTH MSB    1 byte
         * LENGTH LSB    1 byte
         *
         * Total = 5 bytes
         */
        int headerBits =
                PacketCodec.HEADER_SIZE * 8;

        int headerPcmBytes =
                headerBits
                        * samplesPerSymbol
                        * 2;

        if (pcmData.length - preambleEnd < headerPcmBytes) {
            System.err.println(
                    "✗ Recording ended before the packet header was received."
            );
            return;
        }

        byte[] headerPcm =
                Arrays.copyOfRange(
                        pcmData,
                        preambleEnd,
                        preambleEnd + headerPcmBytes
                );

        System.out.println("📡 Decoding packet header...");

        boolean[] headerBitsDecoded;

        try {
            headerBitsDecoded =
                    demodulator.demodulate(headerPcm);
        } catch (Exception e) {
            System.err.println("✗ Header demodulation failed.");
            System.err.println("  " + e.getMessage());
            return;
        }

        byte[] headerBytes;

        try {
            headerBytes =
                    BitStreamUtils.bitsToBytes(headerBitsDecoded);
        } catch (Exception e) {
            System.err.println("✗ Could not decode packet header.");
            System.err.println("  " + e.getMessage());
            return;
        }

        /*
         * Validate magic bytes and protocol version.
         */
        if (headerBytes.length < PacketCodec.HEADER_SIZE) {
            System.err.println("✗ Incomplete packet header.");
            return;
        }

        if (headerBytes[0] != SonicConfig.MAGIC_1
                || headerBytes[1] != SonicConfig.MAGIC_2) {

            System.err.println("✗ Invalid SonicLink packet header.");
            System.err.println(
                    "  Expected magic: "
                            + String.format(
                                    "%02X %02X",
                                    SonicConfig.MAGIC_1,
                                    SonicConfig.MAGIC_2
                            )
            );

            return;
        }

        if (headerBytes[2] != SonicConfig.PROTOCOL_VERSION) {
            System.err.println(
                    "✗ Unsupported protocol version: "
                            + (headerBytes[2] & 0xFF)
            );
            return;
        }

        /*
         * Extract payload length.
         */
        int payloadLength =
                ((headerBytes[3] & 0xFF) << 8)
                        | (headerBytes[4] & 0xFF);

        System.out.println(
                "✓ Header received."
        );

        System.out.println(
                "   Payload length: "
                        + payloadLength
                        + " bytes"
        );

        /*
         * Protect against corrupted or malicious lengths.
         */
        if (payloadLength < 0
                || payloadLength > SonicConfig.MAX_PAYLOAD_SIZE) {

            System.err.println(
                    "✗ Invalid payload length: "
                            + payloadLength
            );

            return;
        }

        /*
         * ---------------------------------------------------------
         * STEP 2: Calculate the complete packet size.
         * ---------------------------------------------------------
         *
         * Total packet:
         *
         * HEADER + PAYLOAD + CRC
         */
        int expectedPacketBytes =
                PacketCodec.HEADER_SIZE
                        + payloadLength
                        + PacketCodec.CRC_SIZE;

        int expectedPacketBits =
                expectedPacketBytes * 8;

        int packetPcmBytes =
                expectedPacketBits
                        * samplesPerSymbol
                        * 2;

        /*
         * Make sure enough audio exists.
         */
        if (pcmData.length - preambleEnd < packetPcmBytes) {

            System.err.println(
                    "✗ Recording ended before the complete packet was received."
            );

            System.err.println(
                    "  Expected PCM bytes: "
                            + packetPcmBytes
            );

            System.err.println(
                    "  Available PCM bytes: "
                            + (pcmData.length - preambleEnd)
            );

            return;
        }

        /*
         * ---------------------------------------------------------
         * STEP 3: Extract only the packet.
         * ---------------------------------------------------------
         *
         * This prevents trailing microphone silence from being
         * interpreted as additional FSK bits.
         */
        byte[] packetPcm =
                Arrays.copyOfRange(
                        pcmData,
                        preambleEnd,
                        preambleEnd + packetPcmBytes
                );

        System.out.println("📡 Demodulating packet...");

        boolean[] bits;

        try {
            bits =
                    demodulator.demodulate(packetPcm);
        } catch (Exception e) {
            System.err.println("✗ Demodulation failed.");
            System.err.println("  " + e.getMessage());
            return;
        }

        if (bits.length != expectedPacketBits) {
            System.err.println(
                    "✗ Unexpected number of received bits."
            );

            System.err.println(
                    "  Expected: "
                            + expectedPacketBits
            );

            System.err.println(
                    "  Received: "
                            + bits.length
            );

            return;
        }

        System.out.println(
                "✓ Demodulation complete."
        );

        System.out.println(
                "   Bits received: "
                        + bits.length
        );

        /*
         * Convert bits back into packet bytes.
         */
        byte[] packetBytes;

        try {
            packetBytes =
                    BitStreamUtils.bitsToBytes(bits);
        } catch (Exception e) {
            System.err.println(
                    "✗ Could not convert received bits to bytes."
            );

            System.err.println(
                    "  " + e.getMessage()
            );

            return;
        }

        /*
         * ---------------------------------------------------------
         * STEP 4: Decode packet and verify CRC.
         * ---------------------------------------------------------
         */
        Packet packet;

        try {
            packet =
                    PacketCodec.decode(packetBytes);

        } catch (PacketException e) {

            System.err.println(
                    "✗ Packet validation failed."
            );

            System.err.println(
                    "  " + e.getMessage()
            );

            return;
        }

        /*
         * PacketCodec.decode() performs CRC verification.
         */
        String message =
                new String(
                        packet.getPayload(),
                        StandardCharsets.UTF_8
                );

        System.out.println();
        System.out.println("================================");
        System.out.println("✓ PACKET RECEIVED SUCCESSFULLY");
        System.out.println("================================");

        System.out.println(
                "Message: " + message
        );

        System.out.println(
                "Payload: "
                        + packet.getPayloadLength()
                        + " bytes"
        );

        System.out.println(
                "Protocol version: "
                        + (packet.getVersion() & 0xFF)
        );

        System.out.println(
                "✓ CRC verified"
        );

        System.out.println("================================");
        System.out.println();
    }
}