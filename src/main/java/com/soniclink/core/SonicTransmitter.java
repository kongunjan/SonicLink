package com.soniclink.core;

import com.soniclink.audio.AudioTransmitter;
import com.soniclink.codec.Modulator;
import com.soniclink.codec.Packet;
import com.soniclink.codec.PacketCodec;
import com.soniclink.util.BitStreamUtils;
import com.soniclink.util.SonicConfig;

import java.nio.charset.StandardCharsets;

/**
 * High-level transmitter for SonicLink.
 *
 * Workflow:
 * Message
 *   -> Packet
 *   -> Packet bytes
 *   -> Bits
 *   -> FSK PCM signal
 *   -> Speaker
 */
public class SonicTransmitter {

    private final Modulator modulator;
    private final AudioTransmitter audioTransmitter;

    public SonicTransmitter() {
        this.modulator = new Modulator();
        this.audioTransmitter = new AudioTransmitter();
    }

    /**
     * Transmits a text message using acoustic FSK communication.
     *
     * @param message message to transmit
     * @throws Exception if transmission fails
     */
    public void transmit(String message) throws Exception {

        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException(
                    "Message cannot be empty."
            );
        }

        byte[] payload = message.getBytes(StandardCharsets.UTF_8);

        if (payload.length > SonicConfig.MAX_PAYLOAD_SIZE) {
            throw new IllegalArgumentException(
                    "Message is too large. Maximum payload size is "
                            + SonicConfig.MAX_PAYLOAD_SIZE
                            + " bytes."
            );
        }

        System.out.println();
        System.out.println("📡 Preparing SonicLink transmission...");
        System.out.println("   Message: " + message);
        System.out.println("   Payload: " + payload.length + " bytes");

        // Create packet containing header, payload and CRC.
        Packet packet = new Packet(payload);

        // Encode packet into bytes.
        byte[] packetBytes = PacketCodec.encode(packet);

        // Convert packet bytes into individual bits.
        boolean[] bits = BitStreamUtils.bytesToBits(packetBytes);

        System.out.println("   Packet: " + packetBytes.length + " bytes");
        System.out.println("   Bits: " + bits.length);

        // Convert bits into FSK PCM audio.
        byte[] pcmData = modulator.modulate(bits);

        System.out.println("   FSK: "
                + SonicConfig.FREQ_BIT_0
                + " Hz = 0, "
                + SonicConfig.FREQ_BIT_1
                + " Hz = 1");

        System.out.println("   Symbol duration: "
                + SonicConfig.SYMBOL_DURATION_MS
                + " ms");

        double durationSeconds =
                bits.length
                        * SonicConfig.SYMBOL_DURATION_MS
                        / 1000.0;

        System.out.printf(
                "   Transmission duration: %.2f seconds%n",
                durationSeconds
        );

        System.out.println();
        System.out.println("🔊 Transmitting...");
        
        audioTransmitter.play(pcmData);

        System.out.println("✓ Transmission complete.");
        System.out.println();
    }
}