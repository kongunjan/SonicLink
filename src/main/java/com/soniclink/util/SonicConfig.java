package com.soniclink.util;

/**
 * Central configuration class for all acoustic, modulation, protocol,
 * and audio hardware parameters used across SonicLink.
 *
 * Keeping these constants centralized ensures Modulator, Demodulator,
 * GoertzelDetector, PacketCodec, and Audio I/O agree on identical schemes
 * without hardcoding magic numbers across the codebase.
 */
public final class SonicConfig {

    // Audio format specification (javax.sound.sampled compatible)
    public static final float SAMPLE_RATE = 44100.0f; // samples per second
    public static final int SAMPLE_SIZE_BITS = 16;     // 16-bit linear PCM
    public static final int CHANNELS = 1;              // Mono
    public static final boolean SIGNED = true;         // Signed integers (-32768 to 32767)
    public static final boolean BIG_ENDIAN = false;    // Little-endian byte order

    // FSK modulation scheme: two distinct audio tones represent bit 0 and bit 1
    public static final double FREQ_BIT_0 = 1200.0;    // Hz
    public static final double FREQ_BIT_1 = 2200.0;    // Hz

    // Transmission timing
    // 50 ms per symbol = 20 symbols (bits) per second raw data rate
    public static final int SYMBOL_DURATION_MS = 50;

    // Preamble tone: unique frequency and duration preceding every packet
    // Used by receiver for energy detection, threshold calibration, and symbol synchronization
    public static final double PREAMBLE_FREQ = 3000.0; // Hz
    public static final int PREAMBLE_DURATION_MS = 200; // ms

    // Signal shaping
    public static final double AMPLITUDE = 0.8;        // 80% full-scale to prevent clipping distortion
    public static final int RAMP_DURATION_MS = 2;      // 2 ms raised-cosine/linear ramp to eliminate clicks

    // Packet protocol framing
    public static final byte MAGIC_1 = 0x53;           // ASCII 'S'
    public static final byte MAGIC_2 = 0x4C;           // ASCII 'L' (SonicLink)
    public static final byte PROTOCOL_VERSION = 0x01;  // Version 1
    public static final int MAX_PAYLOAD_SIZE = 256;    // Maximum payload bytes per packet

    // Detection thresholds for Goertzel receiver
    public static final double DETECTION_THRESHOLD_RATIO = 2.0; // Minimum dominant-to-other frequency ratio
    public static final double MIN_SIGNAL_MAGNITUDE = 500.0;     // Minimum absolute Goertzel magnitude

    private SonicConfig() {
        // Prevent instantiation
    }
}
