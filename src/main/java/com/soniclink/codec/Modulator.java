package com.soniclink.codec;

import com.soniclink.util.SonicConfig;

/**
 * Converts a bitstream into raw PCM audio samples using Frequency Shift
 * Keying (FSK): bit 0 -> one tone frequency, bit 1 -> another tone frequency.
 *
 * This is the heart of the "encoder" side. Understand the math before coding:
 *
 *   For a sine wave tone at frequency f, sample rate sr, the value of
 *   sample number n is:
 *       sample[n] = amplitude * sin(2 * PI * f * n / sr)
 *
 *   To play a tone for a duration of D milliseconds, you need
 *       numSamples = sr * (D / 1000.0)
 *   samples, each computed with the formula above (n ranging from 0 to
 *   numSamples-1).
 *
 * TODO (you implement):
 *  - modulate(boolean[] bits): for each bit, generate SYMBOL_DURATION_MS
 *    worth of samples at FREQ_BIT_0 or FREQ_BIT_1 (see SonicConfig),
 *    concatenate them all into one sample array representing the full
 *    transmission.
 *  - Prepend a preamble tone (see SonicConfig.PREAMBLE_FREQ/DURATION)
 *    before the actual data so the receiver can detect "transmission
 *    starting now".
 *  - Convert your double[] (-1.0 to 1.0 range) samples into the byte[]
 *    format AudioTransmitter expects (16-bit signed PCM) — look up how to
 *    pack a 16-bit sample into two bytes (little-endian per SonicConfig).
 *
 * Things to think about:
 *  - Amplitude: don't use max amplitude (32767) — leave headroom to avoid
 *    clipping/distortion, e.g. 0.8 * 32767.
 *  - Consider a short "ramp up/down" (fade in/out) at each symbol boundary
 *    to avoid audible clicks — this can also improve decode reliability.
 */
public class Modulator {

    public byte[] modulate(boolean[] bits) {
        // TODO: implement using the approach described above
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Helper: generate raw double-valued samples for a single tone.
     * TODO: implement using the sine formula in the class doc comment.
     */
    protected double[] generateTone(double frequencyHz, int durationMs) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Helper: convert normalized double samples (-1.0 to 1.0) into
     * 16-bit signed little-endian PCM bytes (2 bytes per sample).
     * TODO: implement
     */
    protected byte[] toPCMBytes(double[] samples) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
