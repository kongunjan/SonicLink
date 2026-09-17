package com.soniclink.cli;

import com.soniclink.util.SonicConfig;

import java.util.ArrayList;
import java.util.List;

public final class FileChunker {

    public static final int HEADER_SIZE = 7;

    public static final int CHUNK_DATA_SIZE = SonicConfig.MAX_PAYLOAD_SIZE - HEADER_SIZE;

    private FileChunker() {
    }

    public enum FileType {
        TEXT,
        PDF,
        IMAGE;

        public static FileType fromExtension(String fileName) {
            String lower = fileName.toLowerCase();
            if (lower.endsWith(".pdf")) {
                return PDF;
            }
            if (lower.endsWith(".png") || lower.endsWith(".jpg")
                    || lower.endsWith(".jpeg") || lower.endsWith(".gif")
                    || lower.endsWith(".bmp") || lower.endsWith(".webp")) {
                return IMAGE;
            }
            return TEXT;
        }

        public String defaultExtension() {
            switch (this) {
                case PDF:
                    return ".pdf";
                case IMAGE:
                    return ".png";
                default:
                    return ".txt";
            }
        }
    }

    public static List<byte[]> chunk(int fileId, FileType fileType, byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("File data cannot be null");
        }
        if (fileId < 0 || fileId > 0xFFFF) {
            throw new IllegalArgumentException("fileId must fit in 16 bits");
        }

        int total = Math.max(1, (int) Math.ceil(data.length / (double) CHUNK_DATA_SIZE));
        if (total > 0xFFFF) {
            throw new IllegalArgumentException(
                    "File too large: would require " + total + " chunks, max is 65535");
        }

        List<byte[]> chunks = new ArrayList<>(total);

        for (int seq = 0; seq < total; seq++) {
            int start = seq * CHUNK_DATA_SIZE;
            int end = Math.min(start + CHUNK_DATA_SIZE, data.length);
            int dataLen = end - start;

            byte[] chunk = new byte[HEADER_SIZE + dataLen];
            writeHeader(chunk, fileId, seq, total, fileType);
            System.arraycopy(data, start, chunk, HEADER_SIZE, dataLen);

            chunks.add(chunk);
        }

        return chunks;
    }

    private static void writeHeader(byte[] chunk, int fileId, int seq, int total, FileType fileType) {
        chunk[0] = (byte) ((fileId >> 8) & 0xFF);
        chunk[1] = (byte) (fileId & 0xFF);
        chunk[2] = (byte) ((seq >> 8) & 0xFF);
        chunk[3] = (byte) (seq & 0xFF);
        chunk[4] = (byte) ((total >> 8) & 0xFF);
        chunk[5] = (byte) (total & 0xFF);
        chunk[6] = (byte) fileType.ordinal();
    }

    public static final class ChunkHeader {
        public final int fileId;
        public final int seq;
        public final int total;
        public final FileType fileType;

        public ChunkHeader(int fileId, int seq, int total, FileType fileType) {
            this.fileId = fileId;
            this.seq = seq;
            this.total = total;
            this.fileType = fileType;
        }
    }

    public static ChunkHeader readHeader(byte[] chunk) {
        if (chunk == null || chunk.length < HEADER_SIZE) {
            throw new IllegalArgumentException("Chunk too small to contain a header");
        }

        int fileId = ((chunk[0] & 0xFF) << 8) | (chunk[1] & 0xFF);
        int seq = ((chunk[2] & 0xFF) << 8) | (chunk[3] & 0xFF);
        int total = ((chunk[4] & 0xFF) << 8) | (chunk[5] & 0xFF);
        FileType fileType = FileType.values()[chunk[6] & 0xFF];

        return new ChunkHeader(fileId, seq, total, fileType);
    }

    public static byte[] readData(byte[] chunk) {
        if (chunk == null || chunk.length < HEADER_SIZE) {
            throw new IllegalArgumentException("Chunk too small to contain a header");
        }
        byte[] data = new byte[chunk.length - HEADER_SIZE];
        System.arraycopy(chunk, HEADER_SIZE, data, 0, data.length);
        return data;
    }

    public static byte[] reassemble(List<byte[]> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            throw new IllegalArgumentException("No chunks to reassemble");
        }

        ChunkHeader first = readHeader(chunks.get(0));
        int total = first.total;

        byte[][] bySeq = new byte[total][];
        for (byte[] chunk : chunks) {
            ChunkHeader header = readHeader(chunk);
            if (header.fileId != first.fileId) {
                throw new IllegalArgumentException(
                        "Chunk belongs to a different file transfer (fileId mismatch)");
            }
            if (header.seq < 0 || header.seq >= total) {
                throw new IllegalArgumentException("Chunk seq out of range: " + header.seq);
            }
            bySeq[header.seq] = readData(chunk);
        }

        int totalLength = 0;
        for (int i = 0; i < total; i++) {
            if (bySeq[i] == null) {
                throw new IllegalArgumentException("Missing chunk seq=" + i + " of " + total);
            }
            totalLength += bySeq[i].length;
        }

        byte[] result = new byte[totalLength];
        int offset = 0;
        for (int i = 0; i < total; i++) {
            System.arraycopy(bySeq[i], 0, result, offset, bySeq[i].length);
            offset += bySeq[i].length;
        }

        return result;
    }
}
