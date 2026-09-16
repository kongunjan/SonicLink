package com.soniclink;

/**
 * Runner that executes the full SonicLink unit test suite (codec framing,
 * bit-level utilities, FSK modulation, and the DSP-facing detectors used on
 * the receive path) and reports aggregated results.
 */
public class AllTestsRunner {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("              SONICLINK TEST SUITE                ");
        System.out.println("==================================================");

        try {
            // Framing / protocol / bit-level utilities
            ChecksumValidatorTest.main(new String[0]);
            BitStreamUtilsTest.main(new String[0]);
            PacketCodecTest.main(new String[0]);

            // Modulation (transmit path)
            ModulatorTest.main(new String[0]);

            // Detection / demodulation (receive path)
            GoertzelDetectorTest.main(new String[0]);
            DemodulatorTest.main(new String[0]);
            PreambleDetectorTest.main(new String[0]);

            System.out.println("==================================================");
            System.out.println("✓ ALL TESTS PASSED SUCCESSFULLY!");
            System.out.println("==================================================");
        } catch (Exception e) {
            System.err.println("❌ Test suite encountered a failure:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}