package com.soniclink.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FileTextExtractor {

    private FileTextExtractor() {
    }

    public static String extract(String filePath)
            throws Exception {

        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "File path cannot be empty."
            );
        }

        Path path = Path.of(filePath);

        if (!Files.exists(path)) {
            throw new FileNotFoundException(
                    "File not found: " + filePath
            );
        }

        if (!Files.isRegularFile(path)) {
            throw new IOException(
                    "Path is not a regular file: " + filePath
            );
        }

        String name =
                path.getFileName()
                        .toString()
                        .toLowerCase();

        if (name.endsWith(".txt")) {
            return readText(path);
        }

        if (name.endsWith(".pdf")) {
            return extractPDF(path);
        }

        if (name.endsWith(".png")
                || name.endsWith(".jpg")
                || name.endsWith(".jpeg")) {

            return extractImage(path);
        }

        throw new IllegalArgumentException(
                "Unsupported file type. "
                        + "Supported: TXT, PDF, PNG, JPG, JPEG."
        );
    }

    private static String readText(Path path)
            throws IOException {

        System.out.println("📄 Reading TXT file...");

        return Files.readString(
                path,
                StandardCharsets.UTF_8
        );
    }

    private static String extractPDF(Path path)
            throws Exception {

        System.out.println(
                "📄 Extracting text from PDF..."
        );

        ProcessBuilder builder =
                new ProcessBuilder(
                        "pdftotext",
                        "-layout",
                        path.toAbsolutePath().toString(),
                        "-"
                );

        builder.redirectErrorStream(true);

        Process process = builder.start();

        String result;

        try (InputStream input =
                     process.getInputStream()) {

            result =
                    new String(
                            input.readAllBytes(),
                            StandardCharsets.UTF_8
                    );
        }

        int exitCode =
                process.waitFor();

        if (exitCode != 0) {
            throw new IOException(
                    "pdftotext failed with exit code "
                            + exitCode
            );
        }

        return result;
    }

    private static String extractImage(Path path)
            throws Exception {

        System.out.println(
                "🖼️ Running OCR on image..."
        );

        ProcessBuilder builder =
                new ProcessBuilder(
                        "tesseract",
                        path.toAbsolutePath().toString(),
                        "stdout",
                        "-l",
                        "eng"
                );

        builder.redirectErrorStream(true);

        Process process = builder.start();

        String result;

        try (InputStream input =
                     process.getInputStream()) {

            result =
                    new String(
                            input.readAllBytes(),
                            StandardCharsets.UTF_8
                    );
        }

        int exitCode =
                process.waitFor();

        if (exitCode != 0) {
            throw new IOException(
                    "Tesseract OCR failed with exit code "
                            + exitCode
            );
        }

        return result;
    }
}