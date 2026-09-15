package com.soniclink.cli;

import com.soniclink.audio.AudioTransmitter;
import com.soniclink.codec.Modulator;
import com.soniclink.codec.Packet;
import com.soniclink.codec.PacketCodec;
import com.soniclink.util.BitStreamUtils;
import com.soniclink.util.SonicConfig;
import java.nio.charset.StandardCharsets;
import javax.sound.sampled.LineUnavailableException;

/**
 * Command-Line Interface for SonicLink Acoustic Communication.
 *
 * Supported Commands:
 *   send <message>      Encodes and transmits a message as sound via the speaker
 *   listen              Listens via microphone and decodes incoming audio
 *   self-test           Runs automated diagnostics and loopback tests
 *   help                Displays command documentation
 */
public class SonicLinkCLI {

    public static void main(String[] args) {
        if (args.length < 1) {
            printUsage();
            return;
        }

        String command = args[0].toLowerCase();

        switch (command) {
            case "send":
                if (args.length < 2) {
                    System.err.println("Error: Please specify a message to send.");
                    System.err.println("Usage: java -cp out com.soniclink.cli.SonicLinkCLI send \"Hello World\"");
                    return;
                }
                String message = joinArguments(args, 1);
                handleSend(message);
                break;

            case "listen":
                handleListen();
                break;

            case "self-test":
                handleSelfTest();
                break;

            case "help":
            case "--help":
            case "-h":
                printUsage();
                break;

            default:
                System.err.println("Unknown command: " + args[0]);
                printUsage();
                break;
        }
    }

    /**
     * Joins CLI arguments starting from a given index with single spaces.
     */
    private static String joinArguments(String[] args, int startIndex) {
        StringBuilder sb = new StringBuilder();
        for (int i = startIndex; i < args.length; i++) {
            if (i > startIndex) {
                sb.append(" ");
            }
            sb.append(args[i]);
        }
        return sb.toString();
    }

    /**
     * Executes the transmitter pipeline:
     * UTF-8 encode -> Packet creation -> CRC-8 calculation -> Bit conversion ->
     * Binary FSK Modulation -> Signed 16-bit PCM -> Audio playback via SourceDataLine.
     */
    private static void handleSend(String message) {
        if (message.isEmpty()) {
            System.err.println("Error: Cannot send an empty message.");
            return;
        }

        try {
            byte[] payload = message.getBytes(StandardCharsets.UTF_8);
            if (payload.length > SonicConfig.MAX_PAYLOAD_SIZE) {
                System.err.printf("Error: Message length (%d bytes) exceeds maximum payload limit (%d bytes).\n",
                        payload.length, SonicConfig.MAX_PAYLOAD_SIZE);
                return;
            }

            Packet packet = new Packet(payload);
            byte[] packetBytes = PacketCodec.encode(packet);
            boolean[] bits = BitStreamUtils.bytesToBits(packetBytes);

            Modulator modulator = new Modulator();
            byte[] pcmData = modulator.modulate(bits);

            double durationSeconds = (double) pcmData.length / (SonicConfig.SAMPLE_RATE * 2.0);
            double baudRate = 1000.0 / SonicConfig.SYMBOL_DURATION_MS;

            System.out.println("SonicLink Acoustic Communication");
            System.out.println("────────────────────────────────────────────");
            System.out.println("Mode:           TRANSMITTER");
            System.out.println("Message:        \"" + message + "\"");
            System.out.println("Payload:        " + payload.length + " bytes (UTF-8)");
            System.out.println("Packet Size:    " + packetBytes.length + " bytes (Header: 5 B, CRC-8: 1 B)");
            System.out.println("Bits:           " + bits.length + " bits");
            System.out.println("Modulation:     Binary FSK");
            System.out.println("  Bit 0:        " + (int) SonicConfig.FREQ_BIT_0 + " Hz");
            System.out.println("  Bit 1:        " + (int) SonicConfig.FREQ_BIT_1 + " Hz");
            System.out.println("  Preamble:     " + (int) SonicConfig.PREAMBLE_FREQ + " Hz (" + SonicConfig.PREAMBLE_DURATION_MS + " ms)");
            System.out.println("  Symbol:       " + SonicConfig.SYMBOL_DURATION_MS + " ms (" + String.format("%.1f", baudRate) + " bits/s raw)");
            System.out.printf("Audio Duration: %.2f s (%d bytes PCM)\n", durationSeconds, pcmData.length);
            System.out.println("────────────────────────────────────────────");
            System.out.println("Transmitting...");

            AudioTransmitter transmitter = new AudioTransmitter();
            transmitter.play(pcmData);

            System.out.println("✓ Transmission complete");
        } catch (LineUnavailableException e) {
            System.err.println("\nAudio Playback Error: Line unavailable.");
            System.err.println("Details: " + e.getMessage());
            System.err.println("Verify that speaker hardware is connected and accessible.");
        } catch (Exception e) {
            System.err.println("\nTransmission Error: " + e.getMessage());
        }
    }

    private static void handleListen() {
        System.out.println("SonicLink Acoustic Communication");
        System.out.println("────────────────────────────────────────────");
        System.out.println("Mode: RECEIVER");
        System.out.println("Status: Audio receiver module will be initialized in Phase 2.");
        System.out.println("Run 'self-test' to verify software loopback and DSP algorithms.");
    }

    private static void handleSelfTest() {
        System.out.println("SonicLink Self-Test");
        System.out.println("────────────────────────────────────────────");
        System.out.println("Executing system self-tests...");
        // Placeholder for comprehensive self-test suite (Phase 6)
    }

    private static void printUsage() {
        System.out.println("SonicLink - Acoustic Data Communication System");
        System.out.println("Transmits data using audible sound between computers without networks.");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  send <message>   Encodes message and transmits via speaker");
        System.out.println("                   Example: java -cp out com.soniclink.cli.SonicLinkCLI send \"Hello World\"");
        System.out.println("                   Example: java -cp out com.soniclink.cli.SonicLinkCLI send Hello World");
        System.out.println("  listen           Captures sound via microphone and decodes message");
        System.out.println("  self-test        Executes internal DSP loopback tests");
        System.out.println("  help             Displays this help message");
    }
}
