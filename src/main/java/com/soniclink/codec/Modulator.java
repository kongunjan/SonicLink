package com.soniclink.codec;

import com.soniclink.util.BitStreamUtils;
import com.soniclink.util.SonicConfig;

/**
 * Converts a bitstream into 16-bit signed PCM audio samples using Binary Frequency Shift Keying (2-FSK).
 *
 * Acoustic Modulation Architecture:
 * - Bit 0 is represented by a pure sine wave at FREQ_BIT_0 (1200 Hz).
 * - Bit 1 is represented by a pure sine wave at FREQ_BIT_1 (2200 Hz).
 * - Each transmission is preceded by a known preamble tone at PREAMBLE_FREQ (3000 Hz) for 200 ms.
 * - Each symbol duration is SYMBOL_DURATION_MS (50 ms), generating exactly 2,205 samples at 44.1 kHz.
 * - Each tone includes a short linear fade-in and fade-out ramp (2 ms) to prevent high-frequency
 *   clicks and spectral leakage caused by instantaneous phase boundaries.
 * - Output format is 16-bit signed linear PCM, mono, little-endian.
 */
public class Modulator {

    /**
     * Modulates an array of boolean bits into a continuous 16-bit signed PCM byte stream.
     * Includes the 3000 Hz preamble followed by all symbol tones in MSB-first order.
     *
     * @param bits Array of boolean bits (true = 1, false = 0).
     * @return Raw 16-bit signed PCM byte array formatted for AudioTransmitter.
     * @throws IllegalArgumentException if bits is null.
     */
    public byte[] modulate(boolean[] bits) {
        if (bits == null) {
            throw new IllegalArgumentException("Bits array cannot be null");
        }

        int preambleSamplesCount = getSampleCount(SonicConfig.PREAMBLE_DURATION_MS);
        int symbolSamplesCount = getSampleCount(SonicConfig.SYMBOL_DURATION_MS);
        int totalSamples = preambleSamplesCount + (bits.length * symbolSamplesCount);

        double[] allSamples = new double[totalSamples];

        // 1. Generate Preamble Tone
        double[] preambleTone = generateTone(SonicConfig.PREAMBLE_FREQ, SonicConfig.PREAMBLE_DURATION_MS);
        System.arraycopy(preambleTone, 0, allSamples, 0, preambleSamplesCount);

        // 2. Generate Symbol Tones for each bit
        int offset = preambleSamplesCount;
        for (boolean bit : bits) {
            double freq = bit ? SonicConfig.FREQ_BIT_1 : SonicConfig.FREQ_BIT_0;
            double[] symbolTone = generateTone(freq, SonicConfig.SYMBOL_DURATION_MS);
            System.arraycopy(symbolTone, 0, allSamples, offset, symbolSamplesCount);
            offset += symbolSamplesCount;
        }

        // 3. Convert normalized float/double samples to 16-bit signed PCM bytes
        return toPCMBytes(allSamples);
    }

    /**
     * Helper to modulate a Packet object directly into audio PCM bytes.
     *
     * @param packet The packet to transmit.
     * @return Raw PCM audio bytes.
     */
    public byte[] modulatePacket(Packet packet) {
        byte[] frameBytes = PacketCodec.encode(packet);
        boolean[] bits = BitStreamUtils.bytesToBits(frameBytes);
        return modulate(bits);
    }

    /**
     * Generates normalized double audio samples (-1.0 to 1.0) for a single tone of the given frequency
     * and duration, with smooth fade-in and fade-out ramps at the edges.
     *
     * @param frequencyHz Frequency of the sine wave tone in Hertz.
     * @param durationMs Duration of the tone in milliseconds.
     * @return Array of normalized audio samples.
     */
    protected double[] generateTone(double frequencyHz, int durationMs) {
        int numSamples = getSampleCount(durationMs);
        double[] samples = new double[numSamples];
        int rampSamples = (int) Math.round(SonicConfig.SAMPLE_RATE * (SonicConfig.RAMP_DURATION_MS / 1000.0));

        // Avoid ramp exceeding half the symbol duration
        rampSamples = Math.min(rampSamples, numSamples / 4);

        double angularFrequency = 2.0 * Math.PI * frequencyHz / SonicConfig.SAMPLE_RATE;

        for (int n = 0; n < numSamples; n++) {
            double rawSine = Math.sin(angularFrequency * n);
            double envelope = 1.0;

            if (rampSamples > 0) {
                if (n < rampSamples) {
                    envelope = (double) n / rampSamples;
                } else if (n >= numSamples - rampSamples) {
                    envelope = (double) (numSamples - 1 - n) / rampSamples;
                }
            }

            samples[n] = SonicConfig.AMPLITUDE * envelope * rawSine;
        }

        return samples;
    }

    /**
     * Converts normalized double audio samples [-1.0, 1.0] into 16-bit signed little-endian PCM bytes
     * (2 bytes per sample: low byte first, high byte second).
     *
     * @param samples Array of normalized audio samples.
     * @return 16-bit little-endian signed PCM byte array.
     */
    protected byte[] toPCMBytes(double[] samples) {
        if (samples == null) {
            return new byte[0];
        }

        byte[] pcm = new byte[samples.length * 2];
        for (int i = 0; i < samples.length; i++) {
            double s = samples[i];
            // Clamp sample to prevent integer overflow/wrap-around distortion
            if (s > 1.0) s = 1.0;
            else if (s < -1.0) s = -1.0;

            short pcmVal = (short) Math.round(s * 32767.0);

            // Little-endian: LSB first, MSB second
            pcm[i * 2] = (byte) (pcmVal & 0xFF);
            pcm[i * 2 + 1] = (byte) ((pcmVal >> 8) & 0xFF);
        }
        return pcm;
    }

    /**
     * Calculates the exact number of audio samples corresponding to a duration in milliseconds.
     */
    public static int getSampleCount(int durationMs) {
        return (int) Math.round(SonicConfig.SAMPLE_RATE * (durationMs / 1000.0));
    }
}
