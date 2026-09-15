package com.soniclink.audio;

import com.soniclink.util.SonicConfig;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * Transmits raw PCM audio data through the system speaker using javax.sound.sampled.SourceDataLine.
 *
 * Implements safe resource handling, validation of device format compatibility,
 * chunked writing, and strict line closure in finally blocks to avoid audio device locking.
 */
public class AudioTransmitter {

    private static final int WRITE_CHUNK_SIZE = 4096;

    /**
     * Obtains the standard AudioFormat configured for SonicLink transmissions.
     *
     * @return AudioFormat instance (44100 Hz, 16-bit signed PCM, 1 channel, Little-Endian).
     */
    public static AudioFormat getAudioFormat() {
        return new AudioFormat(
                SonicConfig.SAMPLE_RATE,
                SonicConfig.SAMPLE_SIZE_BITS,
                SonicConfig.CHANNELS,
                SonicConfig.SIGNED,
                SonicConfig.BIG_ENDIAN
        );
    }

    /**
     * Plays raw 16-bit linear PCM audio bytes through the default audio output line (speaker).
     *
     * @param pcmData Raw PCM bytes to output.
     * @throws LineUnavailableException if no audio output device is available or format is unsupported.
     */
    public void play(byte[] pcmData) throws LineUnavailableException {
        if (pcmData == null || pcmData.length == 0) {
            return;
        }

        AudioFormat format = getAudioFormat();
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("SourceDataLine for format " + format
                    + " is not supported on this system's audio output devices.");
        }

        SourceDataLine line = null;
        try {
            line = (SourceDataLine) AudioSystem.getLine(info);
            // Open with a standard buffer size (e.g. 1/4 second or line default)
            line.open(format);
            line.start();

            int offset = 0;
            while (offset < pcmData.length) {
                int bytesToWrite = Math.min(WRITE_CHUNK_SIZE, pcmData.length - offset);
                int written = line.write(pcmData, offset, bytesToWrite);
                if (written <= 0) {
                    break;
                }
                offset += written;
            }

            // Ensure all queued audio has physically played through speakers before closing
            line.drain();
        } catch (LineUnavailableException e) {
            throw new LineUnavailableException("Failed to acquire or open speaker line: " + e.getMessage());
        } finally {
            if (line != null) {
                try {
                    line.stop();
                } catch (Exception ignored) {
                }
                try {
                    line.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
