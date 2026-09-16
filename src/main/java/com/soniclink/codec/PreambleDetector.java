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
                0.50
        );
    }

    public PreambleDetector(
            float sampleRate,
            double preambleFrequency,
            int preambleDurationMs,
            double threshold) {

        if (sampleRate <= 0) {
            throw new IllegalArgumentException("Sample rate must be positive");
        }

        if (preambleFrequency <= 0 || preambleFrequency >= sampleRate / 2.0) {
            throw new IllegalArgumentException("Invalid preamble frequency");
        }

        if (preambleDurationMs <= 0) {
            throw new IllegalArgumentException("Preamble duration must be positive");
        }

        if (threshold <= 0 || threshold > 1) {
            throw new IllegalArgumentException("Threshold must be between 0 and 1");
        }

        this.sampleRate = sampleRate;
        this.preambleFrequency = preambleFrequency;
        this.preambleSamples = Math.round(sampleRate * preambleDurationMs / 1000.0f);
        this.threshold = threshold;
    }

   
    public int findPreambleEnd(byte[] pcmData) {

        if (pcmData == null || pcmData.length == 0) {
            throw new IllegalArgumentException("PCM data cannot be empty");
        }

        short[] samples = AudioReceiver.bytesToSamples(pcmData);

        if (samples.length < preambleSamples) {
            throw new IllegalArgumentException("PCM data is shorter than the preamble");
        }

        int windowSize = preambleSamples;

        
        int coarseStep = Math.max(1, Math.round(sampleRate / 1000.0f));

        int bestOffset = -1;
        double bestScore = 0.0;

        for (int offset = 0; offset + windowSize <= samples.length; offset += coarseStep) {
            double score = calculateScore(samples, offset, windowSize);
            if (score > bestScore) {
                bestScore = score;
                bestOffset = offset;
            }
        }

        if (bestOffset < 0 || bestScore < threshold) {
            throw new IllegalArgumentException("SonicLink preamble not detected");
        }

        int searchRadius = coarseStep * 2;
        int refineStart = Math.max(0, bestOffset - searchRadius);
        int refineEnd = Math.min(samples.length - windowSize, bestOffset + searchRadius);

        int exactOffset = bestOffset;
        double exactScore = bestScore;

        for (int offset = refineStart; offset <= refineEnd; offset++) {
            double score = calculateScore(samples, offset, windowSize);
            if (score > exactScore) {
                exactScore = score;
                exactOffset = offset;
            }
        }

        if (exactScore < threshold) {
            throw new IllegalArgumentException("SonicLink preamble confidence is too low");
        }

        // exactOffset marks the START of the 200ms preamble tone,
        // so the actual data begins immediately after it ends.
        int preambleEndSample = exactOffset + preambleSamples;

        // Convert sample index -> byte index (16-bit PCM = 2 bytes/sample)
        return preambleEndSample * 2;
    }


    private double calculateScore(short[] samples, int offset, int length) {

        double targetPower = GoertzelDetector.detectPower(
                samples, offset, length, sampleRate, preambleFrequency);

        double totalPower = 0.0;
        for (int i = offset; i < offset + length; i++) {
            double value = samples[i];
            totalPower += value * value;
        }

        double averagePower = totalPower / length;

        // Window has negligible energy overall - treat as silence,
        // regardless of what the raw targetPower/totalPower ratio says.
        if (averagePower < NOISE_FLOOR) {
            return 0.0;
        }

        double score = (2.0 * targetPower) / (totalPower * length);
        return Math.min(1.0, Math.max(0.0, score));
    }

    public double getThreshold() {
        return threshold;
    }

    public int getPreambleSamples() {
        return preambleSamples;
    }
}