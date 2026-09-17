package com.soniclink.core;

import com.soniclink.audio.AudioTransmitter;
import com.soniclink.cli.FileChunker;
import com.soniclink.codec.Modulator;
import com.soniclink.codec.Packet;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class FileTransmitter {

    private static final int INTER_CHUNK_GAP_MS = 150;

    private final Modulator modulator;
    private final AudioTransmitter audioTransmitter;

    public FileTransmitter() {
        this.modulator = new Modulator();
        this.audioTransmitter = new AudioTransmitter();
    }

    public void transmitFile(String filePath) throws IOException, LineUnavailableException {
        Path path = Path.of(filePath);

        if (!Files.exists(path)) {
            throw new IOException("File not found: " + filePath);
        }
        if (!Files.isRegularFile(path)) {
            throw new IOException("Not a regular file: " + filePath);
        }

        byte[] data = Files.readAllBytes(path);
        FileChunker.FileType fileType = FileChunker.FileType.fromExtension(path.getFileName().toString());

        transmitFile(data, fileType);
    }

    public void transmitFile(byte[] data, FileChunker.FileType fileType)
            throws LineUnavailableException {

        int fileId = ThreadLocalRandom.current().nextInt(0, 0x10000);

        List<byte[]> chunks = FileChunker.chunk(fileId, fileType, data);

        System.out.printf(
                "Sending %s file: %d bytes in %d chunk(s) (fileId=%d)%n",
                fileType, data.length, chunks.size(), fileId
        );

        for (int i = 0; i < chunks.size(); i++) {
            byte[] chunkPayload = chunks.get(i);

            Packet packet = new Packet(chunkPayload);
            byte[] pcm = modulator.modulatePacket(packet);

            System.out.printf(
                    "  chunk %d/%d (%d bytes payload)%n",
                    i + 1, chunks.size(), chunkPayload.length
            );

            audioTransmitter.play(pcm);

            if (i < chunks.size() - 1) {
                sleepQuietly(INTER_CHUNK_GAP_MS);
            }
        }

        System.out.println("✓ File transmission complete.");
    }

    private void sleepQuietly(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
