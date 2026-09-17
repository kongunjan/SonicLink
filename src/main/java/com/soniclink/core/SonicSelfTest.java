package com.soniclink.core;

import com.soniclink.codec.Demodulator;
import com.soniclink.codec.Modulator;
import com.soniclink.codec.Packet;
import com.soniclink.codec.PacketCodec;
import com.soniclink.codec.PreambleDetector;
import com.soniclink.util.BitStreamUtils;
import com.soniclink.util.SonicConfig;

import java.nio.charset.StandardCharsets;

/**
 * Software loopback test for SonicLink.
 *
 * This test does not use the microphone or speaker.
 *
 * It simulates:
 *
 * Message
 * -> Packet
 * -> Bits
 * -> FSK
 * -> Preamble detection
 * -> Demodulation
 * -> Packet decoding
 * -> Message
 */
public class SonicSelfTest {

    private final Modulator modulator;
    private final Demodulator demodulator;

    public SonicSelfTest() {
        this.modulator = new Modulator();
        this.demodulator = new Demodulator();
    }

    /**
     * Runs the complete software loopback test suite.
     */
    public void run() {

        System.out.println();
        System.out.println("======================================");
        System.out.println("      SONICLINK SOFTWARE SELF-TEST");
        System.out.println("======================================");
        System.out.println();

        String[] testMessages = {
                "Hi",
                "Hello, SonicLink!",
                "The quick brown fox jumps over the lazy dog.",
                "1234567890",
                ""
        };

        /*
         * Add a maximum-size payload test.
         */
        testMessages = addMaximumPayloadTest(testMessages);

        int passed = 0;
        int failed = 0;

        for (int i = 0; i < testMessages.length; i++) {

            String message = testMessages[i];

            System.out.println(
                    "Test "
                            + (i + 1)
                            + "/"
                            + testMessages.length
            );

            System.out.println(
                    "Message: "
                            + (message.isEmpty()
                            ? "<empty>"
                            : message)
            );

            try {

                String recovered =
                        loopback(message);

                if (message.equals(recovered)) {

                    System.out.println(
                            "✓ PASS"
                    );

                    passed++;

                } else {

                    System.out.println(
                            "✗ FAIL"
                    );

                    System.out.println(
                            "  Expected: "
                                    + message
                    );

                    System.out.println(
                            "  Received: "
                                    + recovered
                    );

                    failed++;
                }

            } catch (Exception e) {

                System.out.println(
                        "✗ FAIL"
                );

                System.out.println(
                        "  Error: "
                                + e.getMessage()
                );

                failed++;
            }

            System.out.println();
        }

        System.out.println("======================================");
        System.out.println("             TEST RESULTS");
        System.out.println("======================================");

        System.out.println(
                "Passed: "
                        + passed
        );

        System.out.println(
                "Failed: "
                        + failed
        );

        System.out.println(
                "Total:  "
                        + testMessages.length
        );

        if (failed == 0) {
            System.out.println();
            System.out.println(
                    "🎉 ALL SONICLINK SELF-TESTS PASSED!"
            );
        } else {
            System.out.println();
            System.out.println(
                    "⚠ Some self-tests failed."
            );
        }

        System.out.println("======================================");
        System.out.println();
    }

    /**
     * Performs one complete software transmission loop.
     *
     * @param message message to test
     * @return recovered message
     */
    private String loopback(String message) throws Exception {

        /*
         * Convert message into UTF-8 payload.
         */
        byte[] payload =
                message.getBytes(StandardCharsets.UTF_8);

        if (payload.length > SonicConfig.MAX_PAYLOAD_SIZE) {
            throw new IllegalArgumentException(
                    "Test payload exceeds maximum size."
            );
        }

        /*
         * Create packet.
         */
        Packet packet =
                new Packet(payload);

        /*
         * Encode packet.
         */
        byte[] packetBytes =
                PacketCodec.encode(packet);

        /*
         * Convert packet to bits.
         */
        boolean[] bits =
                BitStreamUtils.bytesToBits(packetBytes);

        /*
         * Generate complete FSK signal.
         */
        byte[] pcmData =
                modulator.modulate(bits);

        /*
         * Locate the preamble.
         */
        int preambleEnd =
                PreambleDetector.findPreambleEnd(
                        pcmData,
                        SonicConfig.SAMPLE_RATE,
                        SonicConfig.PREAMBLE_FREQ,
                        SonicConfig.PREAMBLE_DURATION_MS
                );

        if (preambleEnd < 0
                || preambleEnd >= pcmData.length) {

            throw new IllegalStateException(
                    "Could not detect preamble."
            );
        }

        /*
         * Determine exact packet size.
         *
         * We already know the packet size in a software
         * loopback test, so there is no need to simulate
         * microphone silence.
         */
        int expectedPacketBits =
                packetBytes.length * 8;

        int packetPcmBytes =
                expectedPacketBits
                        * demodulator.getSamplesPerSymbol()
                        * 2;

        if (pcmData.length - preambleEnd < packetPcmBytes) {
            throw new IllegalStateException(
                    "Generated PCM is shorter than expected."
            );
        }

        /*
         * Extract only the packet audio.
         */
        byte[] packetPcm =
                java.util.Arrays.copyOfRange(
                        pcmData,
                        preambleEnd,
                        preambleEnd + packetPcmBytes
                );

        /*
         * Demodulate.
         */
        boolean[] receivedBits =
                demodulator.demodulate(packetPcm);

        /*
         * Convert bits back to bytes.
         */
        byte[] receivedBytes =
                BitStreamUtils.bitsToBytes(
                        receivedBits
                );

        /*
         * Decode and CRC-check packet.
         */
        Packet receivedPacket =
                PacketCodec.decode(
                        receivedBytes
                );

        return new String(
                receivedPacket.getPayload(),
                StandardCharsets.UTF_8
        );
    }

    /**
     * Adds a maximum-size payload to the test suite.
     */
    private String[] addMaximumPayloadTest(
            String[] original
    ) {

        String[] result =
                new String[original.length + 1];

        System.arraycopy(
                original,
                0,
                result,
                0,
                original.length
        );

        result[result.length - 1] =
                "X".repeat(
                        SonicConfig.MAX_PAYLOAD_SIZE
                );

        return result;
    }
}