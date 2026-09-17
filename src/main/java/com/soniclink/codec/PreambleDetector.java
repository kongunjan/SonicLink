package com.soniclink.codec;

import com.soniclink.audio.AudioReceiver;
import com.soniclink.util.SonicConfig;

public final class PreambleDetector {

    private final float sampleRate;
    private final double preambleFrequency;
    private final int preambleSamples;
    private final double threshold;

    private static final double NOISE_FLOOR = 25.0;

    public PreambleDetector() {
        this(
                SonicConfig.SAMPLE_RATE,
                SonicConfig.PREAMBLE_FREQ,
                SonicConfig.PREAMBLE_DURATION_MS,
                0.35
        );
    }

    public PreambleDetector(
            float sampleRate,
            double preambleFrequency,
            int preambleDurationMs,
            double threshold) {

        if (sampleRate <= 0) {
            throw new IllegalArgumentException(
                    "Sample rate must be positive"
            );
        }

        if (preambleFrequency <= 0
                || preambleFrequency >= sampleRate / 2.0) {

            throw new IllegalArgumentException(
                    "Invalid preamble frequency"
            );
        }

        if (preambleDurationMs <= 0) {
            throw new IllegalArgumentException(
                    "Preamble duration must be positive"
            );
        }

        if (threshold <= 0 || threshold > 1) {
            throw new IllegalArgumentException(
                    "Threshold must be between 0 and 1"
            );
        }

        this.sampleRate = sampleRate;
        this.preambleFrequency = preambleFrequency;
        this.preambleSamples =
                Math.round(
                        sampleRate
                                * preambleDurationMs
                                / 1000.0f
                );

        this.threshold = threshold;
    }

    public static int findPreambleEnd(
            byte[] pcmData,
            float sampleRate,
            double preambleFrequency,
            int preambleDurationMs) {

        PreambleDetector detector =
                new PreambleDetector(
                        sampleRate,
                        preambleFrequency,
                        preambleDurationMs,
                        0.35
                );

        return detector.findPreambleEnd(pcmData);
    }

    public int findPreambleEnd(byte[] pcmData) {

        if (pcmData == null || pcmData.length == 0) {
            throw new IllegalArgumentException(
                    "PCM data cannot be empty"
            );
        }

        short[] samples =
                AudioReceiver.bytesToSamples(pcmData);

        if (samples.length < preambleSamples) {
            throw new IllegalArgumentException(
                    "PCM data is shorter than the preamble"
            );
        }

        int windowSize = preambleSamples;

        /*
         * Check the recording every 10 ms.
         *
         * At 44.1 kHz:
         * 10 ms = approximately 441 samples.
         */
        int scanStep =
                Math.max(
                        1,
                        Math.round(
                                sampleRate / 100.0f
                        )
                );

        int bestOffset = -1;
        double bestScore = 0.0;

        for (
                int offset = 0;
                offset + windowSize <= samples.length;
                offset += scanStep
        ) {

            double score =
                    calculateScore(
                            samples,
                            offset,
                            windowSize
                    );

            if (score > bestScore) {
                bestScore = score;
                bestOffset = offset;
            }
        }

        if (bestOffset < 0 || bestScore < threshold) {
            throw new IllegalArgumentException(
                    "SonicLink preamble not detected"
            );
        }

        int searchRadius = scanStep * 2;
        int refineStart = Math.max(0, bestOffset - searchRadius);
        int refineEnd = Math.min(
                samples.length - windowSize,
                bestOffset + searchRadius
        );
        int exactOffset = bestOffset;
        double exactScore = bestScore;

        for (
                int refineOffset = refineStart;
                refineOffset <= refineEnd;
                refineOffset++
        ) {
            double refinedScore = calculateScore(
                    samples,
                    refineOffset,
                    windowSize
            );

            if (refinedScore > exactScore) {
                exactScore = refinedScore;
                exactOffset = refineOffset;
            }
        }

        return (exactOffset + preambleSamples) * 2;

    }

    private double calculateScore(
            short[] samples,
            int offset,
            int length) {

        double targetPower =
                GoertzelDetector.detectPower(
                        samples,
                        offset,
                        length,
                        sampleRate,
                        preambleFrequency
                );

        double totalPower = 0.0;

        for (
                int i = offset;
                i < offset + length;
                i++
        ) {

            double value = samples[i];

            totalPower += value * value;
        }

        double averagePower =
                totalPower / length;

        if (averagePower < NOISE_FLOOR) {
            return 0.0;
        }

        double score =
                (2.0 * targetPower)
                        / (totalPower * length);

        return Math.min(
                1.0,
                Math.max(0.0, score)
        );
    }

    public double getThreshold() {
        return threshold;
    }

    public int getPreambleSamples() {
        return preambleSamples;
    }
}