package com.soniclink;

import com.soniclink.codec.GoertzelDetector;

public class GoertzelDetectorTest {

    private static final float SAMPLE_RATE = 44100f;

    public static void main(String[] args) {

        test1200Hz();
        test2200Hz();
        testBitDetection();

        System.out.println(
                "✓ GoertzelDetectorTest: ALL TESTS PASSED"
        );
    }

    private static void test1200Hz() {

        short[] samples = generateTone(1200.0, 50);

        double power1200 =
                GoertzelDetector.detectPower(
                        samples,
                        0,
                        samples.length,
                        SAMPLE_RATE,
                        1200.0
                );

        double power2200 =
                GoertzelDetector.detectPower(
                        samples,
                        0,
                        samples.length,
                        SAMPLE_RATE,
                        2200.0
                );

        assertTrue(
                power1200 > power2200,
                "1200 Hz should dominate 2200 Hz"
        );
    }

    private static void test2200Hz() {

        short[] samples = generateTone(2200.0, 50);

        double power1200 =
                GoertzelDetector.detectPower(
                        samples,
                        0,
                        samples.length,
                        SAMPLE_RATE,
                        1200.0
                );

        double power2200 =
                GoertzelDetector.detectPower(
                        samples,
                        0,
                        samples.length,
                        SAMPLE_RATE,
                        2200.0
                );

        assertTrue(
                power2200 > power1200,
                "2200 Hz should dominate 1200 Hz"
        );
    }

    private static void testBitDetection() {

        short[] zeroTone =
                generateTone(1200.0, 50);

        short[] oneTone =
                generateTone(2200.0, 50);

        boolean zero =
                GoertzelDetector.detectBit(
                        zeroTone,
                        0,
                        zeroTone.length,
                        SAMPLE_RATE,
                        1200.0,
                        2200.0
                );

        boolean one =
                GoertzelDetector.detectBit(
                        oneTone,
                        0,
                        oneTone.length,
                        SAMPLE_RATE,
                        1200.0,
                        2200.0
                );

        assertTrue(
                !zero,
                "1200 Hz should decode as bit 0"
        );

        assertTrue(
                one,
                "2200 Hz should decode as bit 1"
        );
    }

    private static short[] generateTone(
            double frequency,
            int durationMs) {

        int sampleCount =
                Math.round(
                        SAMPLE_RATE
                                * durationMs
                                / 1000f
                );

        short[] samples =
                new short[sampleCount];

        for (int i = 0; i < sampleCount; i++) {

            double time = i / SAMPLE_RATE;

            samples[i] =
                    (short) (
                            0.8
                                    * Short.MAX_VALUE
                                    * Math.sin(
                                            2.0
                                                    * Math.PI
                                                    * frequency
                                                    * time
                                    )
                    );
        }

        return samples;
    }

    private static void assertTrue(
            boolean condition,
            String message) {

        if (!condition) {
            throw new AssertionError(message);
        }
    }
}