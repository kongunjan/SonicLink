package com.soniclink.cli;

import com.soniclink.core.FileTransmitter;
import com.soniclink.core.SonicReceiver;
import com.soniclink.core.SonicSelfTest;
import com.soniclink.core.SonicTransmitter;

import java.util.Scanner;

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
    private final FileTransmitter fileTransmitter;

    public SonicLinkCLI() {
        this.transmitter = new SonicTransmitter();
        this.receiver = new SonicReceiver();
        this.selfTest = new SonicSelfTest();
        this.fileTransmitter = new FileTransmitter();
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

                case "send-file":
                case "sendfile":
                    handleSendFile(args);
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
     * Handles the send-file command.
     *
     * Usage:
     *     send-file [path]
     *
     * If no path is given, an interactive menu lets the user choose the
     * file type (Text / PDF / Image) and enter a path. If a path IS given
     * on the command line, the file type is auto-detected from its
     * extension and no prompt is shown.
     */
    private void handleSendFile(String[] args)
            throws Exception {

        String filePath;

        if (args.length >= 2) {
            // Path given directly on the command line, e.g.
            // send-file /path/to/document.pdf
            filePath = args[1];

        } else {
            // No path given - walk the user through an interactive menu.
            filePath = promptForFilePath();

            if (filePath == null) {
                System.out.println("✗ Cancelled.");
                return;
            }
        }

        fileTransmitter.transmitFile(filePath);
    }

    /**
     * Interactive menu: lets the user pick a file type for context, then
     * type the path to the file. The type selection is informational for
     * the user (auto-detection from the extension still happens inside
     * FileTransmitter); it mainly helps guide what kind of file is expected.
     *
     * @return the entered file path, or null if the user cancelled
     */
    private String promptForFilePath() {

        Scanner scanner = new Scanner(System.in);

        System.out.println();
        System.out.println("What would you like to send?");
        System.out.println("  1) Text file (.txt)");
        System.out.println("  2) PDF file (.pdf)");
        System.out.println("  3) Image file (.png/.jpg/.jpeg/.gif/.bmp/.webp)");
        System.out.println("  0) Cancel");
        System.out.print("Choose an option: ");

        String choice = scanner.nextLine().trim();

        String kindLabel;
        switch (choice) {
            case "1":
                kindLabel = "text";
                break;
            case "2":
                kindLabel = "PDF";
                break;
            case "3":
                kindLabel = "image";
                break;
            case "0":
                return null;
            default:
                System.err.println("✗ Invalid option.");
                return null;
        }

        System.out.printf("Enter the path to the %s file: ", kindLabel);
        String path = scanner.nextLine().trim();

        if (path.isEmpty()) {
            System.err.println("✗ No path entered.");
            return null;
        }

        return path;
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
                "  send-file [path]"
        );

        System.out.println(
                "      Transmit a text/PDF/image file through audio."
        );

        System.out.println(
                "      Without a path, shows an interactive menu to pick"
        );

        System.out.println(
                "      the file type and enter a path."
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
                "  ./build.sh run send-file"
        );

        System.out.println();

        System.out.println(
                "  ./build.sh run send-file ./report.pdf"
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