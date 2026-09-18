package com.soniclink.cli;

import com.soniclink.audio.AudioReceiver;
import com.soniclink.audio.AudioTransmitter;
import com.soniclink.codec.Demodulator;
import com.soniclink.codec.Modulator;
import com.soniclink.codec.Packet;
import com.soniclink.codec.PacketCodec;
import com.soniclink.codec.PacketException;
import com.soniclink.codec.PreambleDetector;
import com.soniclink.core.FileTextExtractor;
import com.soniclink.core.SonicSelfTest;
import com.soniclink.util.BitStreamUtils;
import com.soniclink.util.SonicConfig;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class SonicLinkCLI {

    private final SonicSelfTest selfTest;

    public SonicLinkCLI() {
        this.selfTest = new SonicSelfTest();
    }

    public static void main(String[] args) {

        SonicLinkCLI cli = new SonicLinkCLI();

        if (args.length == 0) {
            cli.printUsage();
            return;
        }

        String command = args[0].toLowerCase();

        switch (command) {

            case "send":

                if (args.length < 2) {
                    System.err.println("✗ Please enter a message.");
                    System.err.println(
                            "Usage: bash build.sh run send \"Hello SonicLink\""
                    );
                    return;
                }

                cli.handleSend(cli.joinArguments(args, 1));
                break;

            case "send-file":

                if (args.length < 2) {
                    System.err.println("✗ Please provide a file path.");
                    System.err.println(
                            "Usage: bash build.sh run send-file ./test.txt"
                    );
                    return;
                }

                cli.handleSendFile(args[1]);
                break;

            case "listen":

                cli.handleListen();
                break;

            case "self-test":

                cli.selfTest.run();
                break;

            case "help":
            case "--help":
            case "-h":

                cli.printUsage();
                break;

            default:

                System.err.println("✗ Unknown command: " + command);
                cli.printUsage();
        }
    }

    private String joinArguments(String[] args, int start) {

        StringBuilder result = new StringBuilder();

        for (int i = start; i < args.length; i++) {

            if (i > start) {
                result.append(" ");
            }

            result.append(args[i]);
        }

        return result.toString();
    }

    private void handleSend(String message) {

        if (message == null || message.trim().isEmpty()) {

            System.err.println("✗ Cannot send empty message.");
            return;
        }

        transmitText(message, "TEXT MESSAGE");
    }

    private void handleSendFile(String filePath) {

        try {

            System.out.println();
            System.out.println("==========================================");
            System.out.println("       SONICLINK FILE → AUDIO");
            System.out.println("==========================================");
            System.out.println("File: " + filePath);
            System.out.println();

            String text = FileTextExtractor.extract(filePath);

            if (text == null || text.trim().isEmpty()) {

                System.err.println("✗ No readable text found.");
                return;
            }

            System.out.println("✓ Text extraction successful.");
            System.out.println("  Characters: " + text.length());

            String preview = text
                    .replaceAll("\\s+", " ")
                    .trim();

            if (preview.length() > 120) {
                preview = preview.substring(0, 120) + "...";
            }

            System.out.println("  Preview: " + preview);
            System.out.println();

            transmitText(text, "FILE CONTENT");

        } catch (Exception e) {

            System.err.println(
                    "✗ File processing error: " + e.getMessage()
            );
        }
    }

    private void transmitText(String text, String mode) {

        try {

            byte[] payload =
                    text.getBytes(StandardCharsets.UTF_8);

            System.out.println("==========================================");
            System.out.println("        SONICLINK TRANSMITTER");
            System.out.println("==========================================");
            System.out.println("Mode: " + mode);
            System.out.println("Payload: " + payload.length + " bytes");
            System.out.println();

            if (payload.length > SonicConfig.MAX_PAYLOAD_SIZE) {

                System.err.println(
                        "✗ Text is too large for one SonicLink packet."
                );

                System.err.println(
                        "  Maximum payload: "
                                + SonicConfig.MAX_PAYLOAD_SIZE
                                + " bytes"
                );

                System.err.println(
                        "  Use a smaller file for the demo."
                );

                return;
            }

            Packet packet = new Packet(payload);

            byte[] packetBytes =
                    PacketCodec.encode(packet);

            boolean[] bits =
                    BitStreamUtils.bytesToBits(packetBytes);

            System.out.println(
                    "✓ Packet created: "
                            + packetBytes.length
                            + " bytes"
            );

            System.out.println(
                    "✓ Converted to "
                            + bits.length
                            + " FSK bits"
            );

            Modulator modulator =
                    new Modulator();

            byte[] pcm =
                    modulator.modulate(bits);

            double duration =
                    (double) pcm.length
                            / (SonicConfig.SAMPLE_RATE * 2.0);

            System.out.printf(
                    "✓ Audio generated: %.2f seconds%n",
                    duration
            );

            System.out.println();
            System.out.println("🔊 Transmitting through speaker...");

            AudioTransmitter transmitter =
                    new AudioTransmitter();

            transmitter.play(pcm);

            System.out.println();
            System.out.println("==========================================");
            System.out.println("✓ TRANSMISSION COMPLETE");
            System.out.println("==========================================");

        } catch (Exception e) {

            System.err.println();
            System.err.println(
                    "✗ Transmission failed: "
                            + e.getMessage()
            );
        }
    }

    private void handleListen() {

        final int RECORD_DURATION_MS = 60000;

        try {

            System.out.println();
            System.out.println("==========================================");
            System.out.println("          SONICLINK RECEIVER");
            System.out.println("==========================================");
            System.out.println();
            System.out.println(
                    "Listening for acoustic transmission..."
            );
            System.out.println();

            AudioReceiver receiver =
                    new AudioReceiver();

            byte[] pcm =
                    receiver.record(RECORD_DURATION_MS);

            System.out.println();
            System.out.println(
                    "PCM bytes: " + pcm.length
            );

            System.out.println();
            System.out.println(
                    "🔎 Searching for SonicLink preamble..."
            );

            int preambleEnd =
                    PreambleDetector.findPreambleEnd(
                            pcm,
                            SonicConfig.SAMPLE_RATE,
                            SonicConfig.PREAMBLE_FREQ,
                            SonicConfig.PREAMBLE_DURATION_MS
                    );

            System.out.println("✓ Preamble detected.");
            System.out.println(
                    "  Data starts at byte: "
                            + preambleEnd
            );

            Demodulator demodulator =
                    new Demodulator();

            int headerBits =
                    PacketCodec.HEADER_SIZE * 8;

            int samplesPerSymbol =
                    demodulator.getSamplesPerSymbol();

            int headerPcmBytes =
                    headerBits
                            * samplesPerSymbol
                            * 2;

            if (pcm.length - preambleEnd
                    < headerPcmBytes) {

                System.err.println(
                        "✗ Recording ended before header."
                );

                return;
            }

            byte[] headerPcm =
                    Arrays.copyOfRange(
                            pcm,
                            preambleEnd,
                            preambleEnd + headerPcmBytes
                    );

            boolean[] headerBitsDecoded =
                    demodulator.demodulate(headerPcm);

            byte[] header =
                    BitStreamUtils.bitsToBytes(
                            headerBitsDecoded
                    );

            if (header.length < PacketCodec.HEADER_SIZE) {

                System.err.println(
                        "✗ Invalid header."
                );

                return;
            }

            if (header[0] != SonicConfig.MAGIC_1
                    || header[1] != SonicConfig.MAGIC_2
                    || header[2] != SonicConfig.PROTOCOL_VERSION) {

                System.err.println(
                        "✗ Invalid SonicLink packet header."
                );

                System.err.printf(
                        "  Expected magic: %02X %02X%n",
                        SonicConfig.MAGIC_1,
                        SonicConfig.MAGIC_2
                );

                return;
            }

            int payloadLength =
                    ((header[3] & 0xFF) << 8)
                            | (header[4] & 0xFF);

            if (payloadLength < 0
                    || payloadLength
                    > SonicConfig.MAX_PAYLOAD_SIZE) {

                System.err.println(
                        "✗ Invalid payload length: "
                                + payloadLength
                );

                return;
            }

            int packetBytes =
                    PacketCodec.HEADER_SIZE
                            + payloadLength
                            + PacketCodec.CRC_SIZE;

            int packetBits =
                    packetBytes * 8;

            int packetPcmBytes =
                    packetBits
                            * samplesPerSymbol
                            * 2;

            if (pcm.length - preambleEnd
                    < packetPcmBytes) {

                System.err.println(
                        "✗ Recording ended before complete packet."
                );

                return;
            }

            byte[] packetPcm =
                    Arrays.copyOfRange(
                            pcm,
                            preambleEnd,
                            preambleEnd + packetPcmBytes
                    );

            boolean[] recoveredBits =
                    demodulator.demodulate(packetPcm);

            byte[] recoveredBytes =
                    BitStreamUtils.bitsToBytes(
                            recoveredBits
                    );

            Packet packet =
                    PacketCodec.decode(recoveredBytes);

            String message =
                    packet.getPayloadAsString();

            System.out.println();
            System.out.println("==========================================");
            System.out.println("        ✓ MESSAGE RECEIVED");
            System.out.println("==========================================");
            System.out.println();
            System.out.println(message);
            System.out.println();
            System.out.println("==========================================");

        } catch (PacketException e) {

            System.err.println(
                    "✗ Packet error: " + e.getMessage()
            );

        } catch (Exception e) {

            System.err.println(
                    "✗ Receiver error: " + e.getMessage()
            );
        }
    }

    private void printUsage() {

        System.out.println();
        System.out.println("==========================================");
        System.out.println("              SONICLINK");
        System.out.println("     Acoustic FSK Communication System");
        System.out.println("==========================================");
        System.out.println();

        System.out.println("Commands:");
        System.out.println();

        System.out.println(
                "  send <message>"
        );

        System.out.println(
                "      Convert text into FSK audio."
        );

        System.out.println();

        System.out.println(
                "  send-file <path>"
        );

        System.out.println(
                "      Extract text from TXT/PDF/image"
        );

        System.out.println(
                "      and transmit it as FSK audio."
        );

        System.out.println();

        System.out.println(
                "  listen"
        );

        System.out.println(
                "      Receive and decode acoustic audio."
        );

        System.out.println();

        System.out.println(
                "  self-test"
        );

        System.out.println(
                "      Run software loopback tests."
        );

        System.out.println();

        System.out.println("Examples:");
        System.out.println();

        System.out.println(
                "  bash build.sh run send \"Hello SonicLink\""
        );

        System.out.println(
                "  bash build.sh run send-file ./test.txt"
        );

        System.out.println(
                "  bash build.sh run send-file ./report.pdf"
        );

        System.out.println(
                "  bash build.sh run send-file ./notes.png"
        );

        System.out.println(
                "  bash build.sh run listen"
        );

        System.out.println();
    }
}