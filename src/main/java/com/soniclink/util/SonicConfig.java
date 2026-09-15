package com.soniclink.util;

/**
 * Central place for all tunable constants, so Modulator, Demodulator,
 * and GoertzelDetector all agree on the same scheme without duplicating
 * magic numbers.
 *
 * These starting values are reasonable defaults for a laptop
 * speaker/mic setup. You will likely need to TUNE these experimentally —
 * that tuning process is great material for your report's "Testing
 * Approach" and "Challenges Faced" sections (e.g. "at 44100Hz sample rate
 * and 50ms symbols, X% of bits were misread at 1 meter distance; increasing
 * symbol duration to 100ms fixed most errors").
 */
public class SonicConfig {

    // Audio format
    public static final float SAMPLE_RATE = 44100f; // samples per second
    public static final int SAMPLE_SIZE_BITS = 16;
    public static final int CHANNELS = 1; // mono
    public static final boolean SIGNED = true;
    public static final boolean BIG_ENDIAN = false;

    // FSK scheme: two distinct tones represent bit 0 and bit 1
    public static final double FREQ_BIT_0 = 1200.0; // Hz
    public static final double FREQ_BIT_1 = 2200.0; // Hz

    // How long each bit's tone plays for for (symbol duration).
    // Longer = more reliable over noisy audio, but slower transmission.
    public static final int SYMBOL_DURATION_MS = 50;

    // A short fixed tone pattern sent before real data, so the receiver
    // knows when a transmission is starting (vs. background noise/silence).
    // TODO: decide on a preamble scheme, e.g. a fixed number of alternating
    // 0/1 tones, or a unique frequency not used for data.
    public static final double PREAMBLE_FREQ = 3000.0; // Hz, example
    public static final int PREAMBLE_DURATION_MS = 200;

    private SonicConfig() {
        // prevent instantiation, this is a constants-only class
    }
}
