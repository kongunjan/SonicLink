package com.soniclink.codec;

/**
 * Detects frequencies in PCM audio using the Goertzel algorithm.
 *
 * SonicLink frequency mapping:
 *
 * 1200 Hz -> bit 0
 * 2200 Hz -> bit 1
 */
public final class GoertzelDetector {

    private GoertzelDetector() {
        // Utility class
    }

    /**
     * Calculates the energy/power of a target frequency.
     */
    public static double detectPower(
            short[] samples,
            int offset,
            int length,
            float sampleRate,
            double targetFrequency) {

        if (samples == null) {
            throw new IllegalArgumentException(
                    "Samples cannot be null"
            );
        }

        if (sampleRate <= 0) {
            throw new IllegalArgumentException(
                    "Sample rate must be positive"
            );
        }

        if (offset < 0 ||
                length <= 0 ||
                offset + length > samples.length) {

            throw new IllegalArgumentException(
                    "Invalid sample range"
            );
        }

        if (targetFrequency <= 0 ||
                targetFrequency >= sampleRate / 2.0) {

            throw new IllegalArgumentException(
                    "Target frequency must be between 0 and Nyquist frequency"
            );
        }

        double k = Math.round(
                length * targetFrequency / sampleRate
        );

        double omega =
                2.0 * Math.PI * k / length;

        double coefficient =
                2.0 * Math.cos(omega);

        double q0;
        double q1 = 0.0;
        double q2 = 0.0;

        for (int i = offset;
             i < offset + length;
             i++) {

            q0 =
                    coefficient * q1
                            - q2
                            + samples[i];

            q2 = q1;
            q1 = q0;
        }

        return q1 * q1
                + q2 * q2
                - coefficient * q1 * q2;
    }

    /**
     * Detects whether a symbol represents 0 or 1.
     *
     * 1200 Hz -> false -> 0
     * 2200 Hz -> true  -> 1
     */
    public static boolean detectBit(
            short[] samples,
            int offset,
            int length,
            float sampleRate,
            double freqBit0,
            double freqBit1) {

        double power0 =
                detectPower(
                        samples,
                        offset,
                        length,
                        sampleRate,
                        freqBit0
                );

        double power1 =
                detectPower(
                        samples,
                        offset,
                        length,
                        sampleRate,
                        freqBit1
                );

        return power1 > power0;
    }

    /**
     * Calculates detection confidence.
     *
     * Returns a value from 0 to 1.
     */
    public static double confidence(
            short[] samples,
            int offset,
            int length,
            float sampleRate,
            double freqBit0,
            double freqBit1) {

        double power0 =
                detectPower(
                        samples,
                        offset,
                        length,
                        sampleRate,
                        freqBit0
                );

        double power1 =
                detectPower(
                        samples,
                        offset,
                        length,
                        sampleRate,
                        freqBit1
                );

        double total =
                power0 + power1;

        if (total == 0.0) {
            return 0.0;
        }

        return Math.abs(power1 - power0)
                / total;
    }
}