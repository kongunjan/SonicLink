package com.soniclink.audio;

import com.soniclink.util.SonicConfig;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

/**
 * Records mono 16-bit PCM audio from the system microphone.
 */
public class AudioReceiver {

    private final AudioFormat format;

    public AudioReceiver() {
        this.format = new AudioFormat(
                SonicConfig.SAMPLE_RATE,
                SonicConfig.SAMPLE_SIZE_BITS,
                SonicConfig.CHANNELS,
                SonicConfig.SIGNED,
                SonicConfig.BIG_ENDIAN
        );
    }

    /**
     * Records audio for the requested duration.
     *
     * @param durationMs recording duration in milliseconds
     * @return raw PCM audio bytes
     */
    public byte[] record(int durationMs)
            throws LineUnavailableException {

        if (durationMs <= 0) {
            throw new IllegalArgumentException(
                    "Duration must be positive"
            );
        }

        DataLine.Info info =
                new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException(
                    "No compatible microphone input was found"
            );
        }

        try (TargetDataLine microphone =
                     (TargetDataLine) AudioSystem.getLine(info)) {

            int bytesPerSample =
                    SonicConfig.SAMPLE_SIZE_BITS / 8;

                int frameSize =
                    format.getFrameSize();

            int bytesToRecord =
                    (int) (
                            SonicConfig.SAMPLE_RATE
                                    * durationMs
                                    / 1000.0
                                    * SonicConfig.CHANNELS
                                    * bytesPerSample
                    );

                            bytesToRecord -= bytesToRecord % frameSize;

                            microphone.open(format, bytesToRecord);
                            microphone.start();

            byte[] result = new byte[bytesToRecord];

            int totalRead = 0;

            System.out.println("🎤 Recording...");

            while (totalRead < bytesToRecord) {

                int bytesToRead = Math.min(
                    4096,
                    bytesToRecord - totalRead
                );
                bytesToRead -= bytesToRead % frameSize;

                int count = microphone.read(
                        result,
                        totalRead,
                        bytesToRead
                );

                if (count <= 0) {
                    continue;
                }

                totalRead += count - (count % frameSize);
            }

            microphone.stop();

            System.out.println("✓ Recording complete.");

            return result;
        }
    }

    /**
     * Converts little-endian 16-bit PCM bytes into signed samples.
     */
    public static short[] bytesToSamples(byte[] pcm) {

        if (pcm == null) {
            throw new IllegalArgumentException(
                    "PCM data cannot be null"
            );
        }

        if ((pcm.length & 1) != 0) {
            throw new IllegalArgumentException(
                    "16-bit PCM must contain an even number of bytes"
            );
        }

        short[] samples = new short[pcm.length / 2];

        for (int i = 0; i < samples.length; i++) {

            int low = pcm[i * 2] & 0xFF;
            int high = pcm[i * 2 + 1];

            samples[i] =
                    (short) ((high << 8) | low);
        }

        return samples;
    }

    /**
     * Returns the audio format used by the receiver.
     */
    public AudioFormat getFormat() {
        return format;
    }
}