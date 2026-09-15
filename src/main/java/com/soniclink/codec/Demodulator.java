package com.soniclink.codec;

import com.soniclink.audio.AudioReceiver;
import com.soniclink.util.SonicConfig;

/**
 * Converts FSK-modulated PCM audio back into binary data.
 *
 * SonicLink:
 *   1200 Hz -> 0
 *   2200 Hz -> 1
 */
public class Demodulator {

    private final float sampleRate;
    private final double freqBit0;
    private final double freqBit1;
    private final int samplesPerSymbol;

    public Demodulator() {

        this.sampleRate = SonicConfig.SAMPLE_RATE;
        this.freqBit0 = SonicConfig.FREQ_BIT_0;
        this.freqBit1 = SonicConfig.FREQ_BIT_1;

        this.samplesPerSymbol =
                Math.round(
                        sampleRate
                                * SonicConfig.SYMBOL_DURATION_MS
                                / 1000.0f
                );
    }

    /**
     * Demodulates raw PCM data into bits.
     *
     * This method expects the preamble to have already been removed.
     */
    public boolean[] demodulate(byte[] pcmData) {

        if (pcmData == null || pcmData.length == 0) {
            throw new IllegalArgumentException(
                    "PCM data cannot be empty"
            );
        }

        short[] samples =
                AudioReceiver.bytesToSamples(pcmData);

        int symbolCount =
                samples.length / samplesPerSymbol;

        boolean[] bits =
                new boolean[symbolCount];

        for (int symbol = 0; symbol < symbolCount; symbol++) {

            int offset =
                    symbol * samplesPerSymbol;

            bits[symbol] =
                    GoertzelDetector.detectBit(
                            samples,
                            offset,
                            samplesPerSymbol,
                            sampleRate,
                            freqBit0,
                            freqBit1
                    );
        }

        return bits;
    }

    /**
     * Demodulates PCM while recording confidence for every symbol.
     */
    public DemodulationResult demodulateWithConfidence(
            byte[] pcmData) {

        if (pcmData == null || pcmData.length == 0) {
            throw new IllegalArgumentException(
                    "PCM data cannot be empty"
            );
        }

        short[] samples =
                AudioReceiver.bytesToSamples(pcmData);

        int symbolCount =
                samples.length / samplesPerSymbol;

        boolean[] bits =
                new boolean[symbolCount];

        double[] confidence =
                new double[symbolCount];

        for (int symbol = 0; symbol < symbolCount; symbol++) {

            int offset =
                    symbol * samplesPerSymbol;

            bits[symbol] =
                    GoertzelDetector.detectBit(
                            samples,
                            offset,
                            samplesPerSymbol,
                            sampleRate,
                            freqBit0,
                            freqBit1
                    );

            confidence[symbol] =
                    GoertzelDetector.confidence(
                            samples,
                            offset,
                            samplesPerSymbol,
                            sampleRate,
                            freqBit0,
                            freqBit1
                    );
        }

        return new DemodulationResult(
                bits,
                confidence
        );
    }

    public int getSamplesPerSymbol() {
        return samplesPerSymbol;
    }

    /**
     * Holds decoded bits and their detection confidence.
     */
    public static class DemodulationResult {

        private final boolean[] bits;
        private final double[] confidence;

        public DemodulationResult(
                boolean[] bits,
                double[] confidence) {

            this.bits = bits;
            this.confidence = confidence;
        }

        public boolean[] getBits() {
            return bits.clone();
        }

        public double[] getConfidence() {
            return confidence.clone();
        }
    }
}