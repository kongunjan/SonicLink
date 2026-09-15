package com.soniclink.util;

public class BitStreamUtils {

    public static boolean[] bytesToBits(byte[] data) {
        boolean[] bits = new boolean[data.length * 8];
        int index = 0;
        for (byte b : data) {
            for (int i = 7; i >= 0; i--) {
                bits[index++] = ((b >> i) & 1) == 1;
            }
        }
        return bits;
    }

    public static byte[] bitsToBytes(boolean[] bits) {
        int byteCount = bits.length / 8;
        byte[] bytes = new byte[byteCount];
        for (int i = 0; i < byteCount; i++) {
            byte value = 0;
            for (int j = 0; j < 8; j++) {
                value = (byte) ((value << 1) | (bits[i * 8 + j] ? 1 : 0));
            }
            bytes[i] = value;
        }
        return bytes;
    }
}