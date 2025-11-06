package com.jobanalyzer.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

/**
 * FileValidator Utility Class
 *
 * Validates uploaded files to ensure they are:
 * - Valid file types (PDF or images)
 * - Not too large
 * - Readable
 */
public class FileValidator {

    // Allowed file extensions
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "pdf", "png", "jpg", "jpeg", "bmp", "tiff", "tif"
    );

    // Maximum file size: 10 MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * Validate if file is acceptable for processing
     * @param file File to validate
     * @return true if valid
     */
    public static boolean isValidFile(File file) {
        if (file == null || !file.exists()) {
            System.err.println("❌ File does not exist or is null");
            return false;
        }

        if (!file.isFile()) {
            System.err.println("❌ Not a valid file");
            return false;
        }

        if (!file.canRead()) {
            System.err.println("❌ File is not readable");
            return false;
        }

        // Check file extension
        if (!hasValidExtension(file)) {
            System.err.println("❌ Invalid file extension. Allowed: " + ALLOWED_EXTENSIONS);
            return false;
        }

        // Check file size
        if (!isFileSizeValid(file)) {
            System.err.println("❌ File is too large. Maximum size: " + (MAX_FILE_SIZE / 1024 / 1024) + " MB");
            return false;
        }

        System.out.println("✅ File validation successful: " + file.getName());
        return true;
    }

    /**
     * Check if file has a valid extension
     * @param file File to check
     * @return true if extension is allowed
     */
    public static boolean hasValidExtension(File file) {
        String filename = file.getName().toLowerCase();

        for (String ext : ALLOWED_EXTENSIONS) {
            if (filename.endsWith("." + ext)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if file size is within limits
     * @param file File to check
     * @return true if size is acceptable
     */
    public static boolean isFileSizeValid(File file) {
        return file.length() <= MAX_FILE_SIZE;
    }

    /**
     * Determine if file is a PDF
     * @param file File to check
     * @return true if PDF
     */
    public static boolean isPDF(File file) {
        return file.getName().toLowerCase().endsWith(".pdf");
    }

    /**
     * Determine if file is an image
     * @param file File to check
     * @return true if image
     */
    public static boolean isImage(File file) {
        String filename = file.getName().toLowerCase();
        return filename.endsWith(".png") ||
                filename.endsWith(".jpg") ||
                filename.endsWith(".jpeg") ||
                filename.endsWith(".bmp") ||
                filename.endsWith(".tiff") ||
                filename.endsWith(".tif");
    }

    /**
     * Get file extension
     * @param file File to check
     * @return Extension without dot (e.g., "pdf", "jpg")
     */
    public static String getFileExtension(File file) {
        String filename = file.getName();
        int lastDot = filename.lastIndexOf('.');

        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1).toLowerCase();
        }

        return "";
    }

    /**
     * Get human-readable file size
     * @param file File to check
     * @return Formatted size string (e.g., "2.5 MB")
     */
    public static String getFileSizeFormatted(File file) {
        long bytes = file.length();

        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        }
    }

    /**
     * Verify file MIME type matches extension
     * @param file File to check
     * @return true if MIME type is valid
     */
    public static boolean hasValidMimeType(File file) {
        try {
            String mimeType = Files.probeContentType(file.toPath());

            if (mimeType == null) {
                // Cannot determine MIME type, rely on extension
                return hasValidExtension(file);
            }

            // Check if MIME type matches expected types
            return mimeType.equals("application/pdf") ||
                    mimeType.startsWith("image/");

        } catch (IOException e) {
            System.err.println("⚠️ Could not probe MIME type: " + e.getMessage());
            return hasValidExtension(file);
        }
    }

    /**
     * Get detailed validation error message
     * @param file File to validate
     * @return Error message or null if valid
     */
    public static String getValidationError(File file) {
        if (file == null || !file.exists()) {
            return "File does not exist";
        }

        if (!file.isFile()) {
            return "Not a valid file";
        }

        if (!file.canRead()) {
            return "File is not readable";
        }

        if (!hasValidExtension(file)) {
            return "Invalid file type. Supported: PDF, PNG, JPG, JPEG, BMP, TIFF";
        }

        if (!isFileSizeValid(file)) {
            return "File is too large. Maximum size: 10 MB. Your file: " + getFileSizeFormatted(file);
        }

        return null; // No errors
    }

    /**
     * Test file validator
     */
    public static void testValidator() {
        System.out.println("\n=== Testing FileValidator ===");

        // Test with a hypothetical file
        File testFile = new File("test.pdf");
        System.out.println("Checking extensions:");
        System.out.println("  PDF check: " + (testFile.getName().endsWith(".pdf") ? "✅" : "❌"));

        System.out.println("\nAllowed extensions: " + ALLOWED_EXTENSIONS);
        System.out.println("Max file size: " + (MAX_FILE_SIZE / 1024 / 1024) + " MB");

        System.out.println("\n=== FileValidator Test Complete ===\n");
    }
}