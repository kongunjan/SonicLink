package com.soniclink.cli;

/**
 * Entry point for SonicLink.
 *
 * Usage:
 *   java com.soniclink.cli.SonicLinkCLI send "Hello World"
 *   java com.soniclink.cli.SonicLinkCLI listen
 *
 * Responsibilities of this class:
 *  - Parse command-line arguments (mode: send/listen, message/options)
 *  - Wire together the Modulator/AudioTransmitter for sending
 *  - Wire together the AudioReceiver/GoertzelDetector/Demodulator for listening
 *  - Print clear, human-readable status to the console
 *
 * Keep this class thin — it should only coordinate other classes,
 * not contain DSP logic itself.
 */
public class SonicLinkCLI {

    public static void main(String[] args) {
        if (args.length < 1) {
            printUsage();
            return;
        }

        String mode = args[0].toLowerCase();

        switch (mode) {
            case "send":
                if (args.length < 2) {
                    System.out.println("Error: Please provide a message to send.");
                    System.out.println("Usage: send \"your message\"");
                    return;
                }
                handleSend(args[1]);
                break;

            case "listen":
                handleListen();
                break;

            default:
                printUsage();
        }
    }

    /**
     * TODO (you implement):
     *  1. Convert the message String to bytes, then bits (use BitStreamUtils)
     *  2. Compute a checksum over the bytes (use ChecksumValidator) and prepend/append it
     *     to the bitstream so the receiver can verify integrity
     *  3. Pass the full bitstream to Modulator to get audio samples (FSK encoding)
     *  4. Pass those samples to AudioTransmitter to actually play them
     *  5. Print progress to the console (e.g. "Encoding 42 bits...", "Transmitting...")
     */
    private static void handleSend(String message) {
        System.out.println("[SEND MODE] Message: " + message);
        // TODO: implement the pipeline described above
        System.out.println("TODO: implement handleSend()");
    }

    /**
     * TODO (you implement):
     *  1. Start AudioReceiver capturing from the microphone
     *  2. Feed captured samples into GoertzelDetector in sliding windows to detect
     *     which frequency (tone) is present in each window
     *  3. Pass the sequence of detected frequencies to Demodulator to reconstruct bits
     *  4. Convert bits back to bytes (BitStreamUtils), verify checksum (ChecksumValidator)
     *  5. Print the decoded message, or an error if checksum verification fails
     *
     * Tip: think about how you detect "silence" vs "start of transmission" —
     * you'll likely want a preamble (a fixed known tone pattern) at the start
     * of every transmission so the receiver knows when real data begins.
     */
    private static void handleListen() {
        System.out.println("[LISTEN MODE] Listening for incoming transmission...");
        // TODO: implement the pipeline described above
        System.out.println("TODO: implement handleListen()");
    }

    private static void printUsage() {
        System.out.println("SonicLink - Data transmission over sound");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  send \"<message>\"   Encode and transmit a message via speaker");
        System.out.println("  listen              Listen via microphone and decode incoming data");
    }
}
