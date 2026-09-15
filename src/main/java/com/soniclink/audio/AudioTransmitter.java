package com.soniclink.audio;

import com.soniclink.util.SonicConfig;
import javax.sound.sampled.*;

/**
 * Plays raw PCM audio bytes (produced by Modulator) through the system's
 * default speaker using javax.sound.sampled.SourceDataLine.
 *
 * This class is mostly Java Sound API plumbing — less "your original
 * algorithm work" than Modulator/Demodulator, but you should still read
 * through and understand each step rather than blindly copying elsewhere.
 *
 * TODO (you implement):
 *  - play(byte[] pcmData): open a SourceDataLine with the AudioFormat
 *    matching SonicConfig (sample rate, bits, channels, signed, endianness),
 *    write pcmData to it, then drain/close the line properly.
 *
 * Reference shape (don't just copy — understand each call):
 *   AudioFormat format = new AudioFormat(SonicConfig.SAMPLE_RATE,
 *       SonicConfig.SAMPLE_SIZE_BITS, SonicConfig.CHANNELS,
 *       SonicConfig.SIGNED, SonicConfig.BIG_ENDIAN);
 *   DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
 *   SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
 *   line.open(format);
 *   line.start();
 *   line.write(pcmData, 0, pcmData.length);
 *   line.drain();
 *   line.close();
 *
 * Remember to handle LineUnavailableException — wrap it in a clear error
 * message for the CLI to print (part of your "error handling" NFR).
 */
public class AudioTransmitter {

    public void play(byte[] pcmData) throws LineUnavailableException {
        // TODO: implement using the reference shape above
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
