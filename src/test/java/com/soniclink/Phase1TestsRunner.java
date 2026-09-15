package com.soniclink;

/**
 * Runner that executes all Phase 1 unit tests and reports aggregated results.
 */
public class Phase1TestsRunner {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("            SONICLINK PHASE 1 TEST SUITE          ");
        System.out.println("==================================================");

        try {
            ChecksumValidatorTest.main(new String[0]);
            BitStreamUtilsTest.main(new String[0]);
            PacketCodecTest.main(new String[0]);
            ModulatorTest.main(new String[0]);

            System.out.println("==================================================");
            System.out.println("✓ ALL PHASE 1 TESTS PASSED SUCCESSFULLY!");
            System.out.println("==================================================");
        } catch (Exception e) {
            System.err.println("❌ Test suite encountered a failure:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
