package com.soniclink.cli;

import com.soniclink.core.SonicReceiver;
import com.soniclink.core.SonicSelfTest;
import com.soniclink.core.SonicTransmitter;

/**
 * Command-line interface for SonicLink.
 *
 * The CLI is responsible only for:
 * - Reading commands
 * - Validating basic command syntax
 * - Delegating work to core services
 */
public class SonicLinkCLI {

    private final SonicTransmitter transmitter;
    private final SonicReceiver receiver;
    private final SonicSelfTest selfTest;

    public SonicLinkCLI() {
        this.transmitter = new SonicTransmitter();
        this.receiver = new SonicReceiver();
        this.selfTest = new SonicSelfTest();
    }

    public static void main(String[] args) {

        SonicLinkCLI cli =
                new SonicLinkCLI();

        cli.run(args);
    }

    /**
     * Processes the command-line arguments.
     */
    public void run(String[] args) {

        if (args.length == 0) {
            printUsage();
            return;
        }

        String command =
                args[0].toLowerCase();

        try {

            switch (command) {

                case "send":
                    handleSend(args);
                    break;

                case "listen":
                case "receive":
                    handleListen();
                    break;

                case "self-test":
                case "selftest":
                case "test":
                    handleSelfTest();
                    break;

                case "help":
                case "-h":
                case "--help":
                    printUsage();
                    break;

                default:
                    System.err.println(
                            "✗ Unknown command: "
                                    + args[0]
                    );

                    System.out.println();
                    printUsage();
            }

        } catch (Exception e) {

            System.err.println();
            System.err.println(
                    "✗ SonicLink error: "
                            + e.getMessage()
            );
        }
    }

    /**
     * Handles the send command.
     *
     * Usage:
     *     send <message>
     */
    private void handleSend(String[] args)
            throws Exception {

        if (args.length < 2) {

            System.err.println(
                    "✗ Please provide a message."
            );

            System.out.println();
            System.out.println(
                    "Example:"
            );

            System.out.println(
                    "  java -cp out "
                            + "com.soniclink.cli.SonicLinkCLI "
                            + "send \"Hello SonicLink\""
            );

            return;
        }

        /*
         * Join all arguments after "send".
         *
         * This allows:
         *
         * send Hello SonicLink
         *
         * instead of requiring quotes.
         */
        StringBuilder message =
                new StringBuilder();

        for (int i = 1; i < args.length; i++) {

            if (i > 1) {
                message.append(" ");
            }

            message.append(args[i]);
        }

        transmitter.transmit(
                message.toString()
        );
    }

    /**
     * Handles the listen command.
     */
    private void handleListen()
            throws Exception {

        receiver.listen();
    }

    /**
     * Handles the software self-test.
     */
    private void handleSelfTest() {

        selfTest.run();
    }

    /**
     * Prints command-line usage.
     */
    private void printUsage() {

        System.out.println();
        System.out.println("SonicLink");
        System.out.println(
                "Acoustic FSK Communication System"
        );
        System.out.println();

        System.out.println("Usage:");

        System.out.println(
                "  send <message>"
        );

        System.out.println(
                "      Transmit a text message through audio."
        );

        System.out.println();

        System.out.println(
                "  listen"
        );

        System.out.println(
                "      Listen for and decode a SonicLink packet."
        );

        System.out.println();

        System.out.println(
                "  self-test"
        );

        System.out.println(
                "      Run the software loopback test suite."
        );

        System.out.println();

        System.out.println(
                "  help"
        );

        System.out.println(
                "      Display this help message."
        );

        System.out.println();

        System.out.println("Examples:");

        System.out.println(
                "  ./build.sh run send \"Hello SonicLink\""
        );

        System.out.println();

        System.out.println(
                "  ./build.sh run listen"
        );

        System.out.println();

        System.out.println(
                "  ./build.sh run self-test"
        );

        System.out.println();
    }
}