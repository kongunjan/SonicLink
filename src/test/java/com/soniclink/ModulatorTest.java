package com.soniclink;

import com.soniclink.codec.Modulator;
import com.soniclink.util.SonicConfig;

/**
 * Unit tests for Modulator PCM generation, timing, amplitude, and frequency shaping.
 */
public class ModulatorTest {

    public static void main(String[] args) {
        System.out.println("Running ModulatorTest...");
        int passed = 0;
        int total = 0;
        Modulator modulator = new Modulator();

        int preambleSamples = Modulator.getSampleCount(SonicConfig.PREAMBLE_DURATION_MS);
        int symbolSamples = Modulator.getSampleCount(SonicConfig.SYMBOL_DURATION_MS);

        // Test 1: Preamble sample count calculation
        total++;
        if (preambleSamples == 8820 && symbolSamples == 2205) {
            passed++;
        } else {
            System.err.printf("FAIL: Sample counts incorrect: preamble=%d, symbol=%d\n",
                    preambleSamples, symbolSamples);
        }

        // Test 2: Correct total sample count and PCM byte size for 8 bits
        total++;
        boolean[] bits8 = new boolean[]{true, false, true, false, true, true, false, false};
        byte[] pcm8 = modulator.modulate(bits8);
        int expectedSamples8 = preambleSamples + (8 * symbolSamples);
        int expectedBytes8 = expectedSamples8 * 2; // 16-bit = 2 bytes per sample
        if (pcm8.length == expectedBytes8) {
            passed++;
        } else {
            System.err.printf("FAIL: PCM byte count mismatch: got %d, expected %d\n",
                    pcm8.length, expectedBytes8);
        }

        // Test 3: Zero-bit stream modulates preamble only
        total++;
        boolean[] emptyBits = new boolean[0];
        byte[] pcmEmpty = modulator.modulate(emptyBits);
        if (pcmEmpty.length == preambleSamples * 2) {
            passed++;
        } else {
            System.err.printf("FAIL: Empty bits PCM length mismatch: got %d, expected %d\n",
                    pcmEmpty.length, preambleSamples * 2);
        }

        // Test 4: Peak amplitude and no clipping check
        total++;
        short maxVal = 0;
        short minVal = 0;
        for (int i = 0; i < pcm8.length; i += 2) {
            short sample = (short) ((pcm8[i] & 0xFF) | ((pcm8[i + 1] & 0xFF) << 8));
            if (sample > maxVal) maxVal = sample;
            if (sample < minVal) minVal = sample;
        }
        // Amplitude is 0.8 -> peak ~26214. Check it stays safely within bounds without wrapping.
        double expectedPeak = SonicConfig.AMPLITUDE * 32767.0;
        if (maxVal <= 26300 && maxVal >= 25000 && minVal >= -26300 && minVal <= -25000) {
            passed++;
        } else {
            System.err.printf("FAIL: Amplitude out of bounds: max=%d, min=%d, expectedPeak=%.1f\n",
                    maxVal, minVal, expectedPeak);
        }

        // Test 5: Distinct wave forms for Bit 0 (1200 Hz) vs Bit 1 (2200 Hz)
        total++;
        boolean[] singleZero = new boolean[]{false};
        boolean[] singleOne = new boolean[]{true};
        byte[] pcmZero = modulator.modulate(singleZero);
        byte[] pcmOne = modulator.modulate(singleOne);

        // Compare the symbol portion (after preamble)
        int symbolOffsetBytes = preambleSamples * 2;
        int diffCount = 0;
        for (int i = 0; i < symbolSamples * 2; i++) {
            if (pcmZero[symbolOffsetBytes + i] != pcmOne[symbolOffsetBytes + i]) {
                diffCount++;
            }
        }
        if (diffCount > symbolSamples) {
            passed++;
        } else {
            System.err.println("FAIL: Bit 0 and Bit 1 generated identical or nearly identical PCM");
        }

        // Test 6: Null bits rejection
        total++;
        try {
            modulator.modulate(null);
            System.err.println("FAIL: Null bits should throw IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }

        System.out.printf("ModulatorTest: %d/%d passed.\n\n", passed, total);
        if (passed != total) {
            System.exit(1);
        }
    }
}
