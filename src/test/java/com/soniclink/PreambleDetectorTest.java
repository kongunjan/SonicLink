package com.soniclink;

import com.soniclink.codec.Modulator;
import com.soniclink.codec.PreambleDetector;
import com.soniclink.util.SonicConfig;

import java.util.Arrays;

public class PreambleDetectorTest {

    private static final float SAMPLE_RATE =
            SonicConfig.SAMPLE_RATE;

    public static void main(String[] args) {

        testDetectPreambleAtBeginning();
        testDetectPreambleAfterSilence();
        testRejectSilence();

        System.out.println(
                "✓ PreambleDetectorTest: ALL TESTS PASSED"
        );
    }

    private static void testDetectPreambleAtBeginning() {

        boolean[] bits = {
                false, true, false, true
        };

        Modulator modulator = new Modulator();

        byte[] pcm =
                modulator.modulate(bits);

        PreambleDetector detector =
                new PreambleDetector();

        int detectedEnd =
                detector.findPreambleEnd(pcm);

        int expectedEnd =
                Math.round(
                        SAMPLE_RATE
                                * SonicConfig.PREAMBLE_DURATION_MS
                                / 1000.0f
                ) * 2;

        assertEquals(
                expectedEnd,
                detectedEnd,
                "Preamble end detected incorrectly"
        );
    }

    private static void testDetectPreambleAfterSilence() {

        boolean[] bits = {
                true, false, true
        };

        byte[] transmission =
                new Modulator().modulate(bits);

        byte[] silence =
                new byte[44100 * 2 / 10];

        byte[] combined =
                new byte[silence.length + transmission.length];

        System.arraycopy(
                silence,
                0,
                combined,
                0,
                silence.length
        );

        System.arraycopy(
                transmission,
                0,
                combined,
                silence.length,
                transmission.length
        );

        PreambleDetector detector =
                new PreambleDetector();

        int detectedEnd =
                detector.findPreambleEnd(combined);

        int expectedEnd =
                silence.length
                        + Math.round(
                                SAMPLE_RATE
                                        * SonicConfig.PREAMBLE_DURATION_MS
                                        / 1000.0f
                        ) * 2;

        assertEquals(
                expectedEnd,
                detectedEnd,
                "Preamble after silence detected incorrectly"
        );
    }

    private static void testRejectSilence() {

        byte[] silence =
                new byte[44100 * 2];

        PreambleDetector detector =
                new PreambleDetector();

        boolean rejected = false;

        try {
            detector.findPreambleEnd(silence);
        } catch (IllegalArgumentException e) {
            rejected = true;
        }

        assertTrue(
                rejected,
                "Detector should reject audio without preamble"
        );
    }

    private static void assertEquals(
            int expected,
            int actual,
            String message) {

        if (expected != actual) {
            throw new AssertionError(
                    message
                            + " (expected "
                            + expected
                            + ", got "
                            + actual
                            + ")"
            );
        }
    }

    private static void assertTrue(
            boolean condition,
            String message) {

        if (!condition) {
            throw new AssertionError(message);
        }
    }
}