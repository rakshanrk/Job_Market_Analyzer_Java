package com.jobanalyzer.services;

import com.jobanalyzer.models.Resume;
import com.jobanalyzer.utils.FileValidator;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;

/**
 * ResumeParser Service Class
 *
 * Extracts text from resume files using:
 * - PDFBox for PDF files
 * - Tesseract OCR for image files
 */
public class ResumeParser {

    // Tesseract instance for OCR
    private Tesseract tesseract;

    /**
     * Constructor - initializes Tesseract
     */
    public ResumeParser() {
        initializeTesseract();
    }

    /**
     * Initialize Tesseract OCR engine
     */
    private void initializeTesseract() {
        tesseract = new Tesseract();

        // Set tessdata path (where language files are stored)
        // Try multiple possible locations
        String[] possiblePaths = {
                "C:\\tessdata",                                    // Custom installation
                "C:\\Program Files\\Tesseract-OCR\\tessdata",     // Default Windows installation
                System.getenv("TESSDATA_PREFIX"),                  // Environment variable
                "tessdata"                                         // Local folder
        };

        boolean tessdataFound = false;
        for (String path : possiblePaths) {
            if (path != null && new File(path).exists()) {
                tesseract.setDatapath(path);
                tessdataFound = true;
                System.out.println("✅ Tesseract data path set: " + path);
                break;
            }
        }

        if (!tessdataFound) {
            System.err.println("⚠️ Warning: Tessdata path not found. OCR may not work properly.");
            System.err.println("   Please ensure tessdata is in C:\\tessdata");
        }

        // Set language to English
        tesseract.setLanguage("eng");

        // Set OCR Engine Mode (1 = LSTM neural net mode - more accurate)
        tesseract.setOcrEngineMode(1);

        // Set Page Segmentation Mode (1 = Automatic page segmentation with OSD)
        tesseract.setPageSegMode(1);

        System.out.println("✅ Tesseract OCR initialized");
    }

    /**
     * Parse resume file and extract text
     * @param file Resume file (PDF or image)
     * @return Resume object with extracted text
     * @throws Exception if parsing fails
     */
    public Resume parseResume(File file) throws Exception {
        // Validate file first
        if (!FileValidator.isValidFile(file)) {
            throw new IllegalArgumentException("Invalid file: " + FileValidator.getValidationError(file));
        }

        System.out.println("\n📄 Parsing resume: " + file.getName());

        // Create Resume object
        Resume resume = new Resume(file.getName());

        // Extract text based on file type
        String extractedText;

        if (FileValidator.isPDF(file)) {
            extractedText = extractTextFromPDF(file);
        } else if (FileValidator.isImage(file)) {
            extractedText = extractTextFromImage(file);
        } else {
            throw new IllegalArgumentException("Unsupported file type: " + file.getName());
        }

        // Clean and set extracted text
        String cleanedText = cleanExtractedText(extractedText);
        resume.setExtractedText(cleanedText);

        // Try to extract basic info (name, email, phone)
        extractBasicInfo(resume, extractedText);

        System.out.println("✅ Text extraction complete. Length: " + extractedText.length() + " characters");

        return resume;
    }

    /**
     * Extract text from PDF file using PDFBox 3.x
     * @param pdfFile PDF file
     * @return Extracted text
     * @throws IOException if reading fails
     */
    private String extractTextFromPDF(File pdfFile) throws IOException {
        System.out.println("📖 Extracting text from PDF...");

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            // Create text stripper
            PDFTextStripper stripper = new PDFTextStripper();

            // Extract text from all pages
            String text = stripper.getText(document);

            System.out.println("✅ PDF text extracted successfully");
            return text;
        }
    }

    /**
     * Extract text from image file using Tesseract OCR
     * @param imageFile Image file
     * @return Extracted text
     * @throws TesseractException if OCR fails
     */
    private String extractTextFromImage(File imageFile) throws TesseractException {
        System.out.println("🖼️ Extracting text from image using OCR...");
        System.out.println("   This may take 10-30 seconds depending on image size...");

        // Perform OCR
        String text = tesseract.doOCR(imageFile);

        System.out.println("✅ OCR extraction complete");
        return text;
    }

    /**
     * Extract basic information from resume text
     * Uses simple pattern matching (can be improved with regex)
     * @param resume Resume object to update
     * @param text Extracted text
     */
    private void extractBasicInfo(Resume resume, String text) {
        if (text == null || text.isEmpty()) {
            return;
        }

        String[] lines = text.split("\n");

        // Simple heuristics for extracting info
        for (String line : lines) {
            line = line.trim();

            // Try to find email (simple pattern)
            if (line.contains("@") && line.contains(".")) {
                String[] words = line.split("\\s+");
                for (String word : words) {
                    if (word.contains("@") && word.contains(".")) {
                        resume.setEmail(word.replaceAll("[^a-zA-Z0-9@._-]", ""));
                        break;
                    }
                }
            }

            // Try to find phone (simple pattern - looks for numbers)
            if (line.matches(".*\\d{10}.*") || line.matches(".*\\d{3}[-.\\s]?\\d{3}[-.\\s]?\\d{4}.*")) {
                // Extract phone number
                String phone = line.replaceAll("[^0-9]", "");
                if (phone.length() >= 10) {
                    resume.setPhone(phone.substring(0, 10));
                }
            }

            // First non-empty line might be the name (very simple heuristic)
            if (resume.getUserName() == null && line.length() > 2 && line.length() < 50) {
                // Check if it looks like a name (contains letters, not too many numbers)
                if (line.matches("^[a-zA-Z\\s.]+$")) {
                    resume.setUserName(line);
                }
            }
        }
    }

    /**
     * Clean and preprocess extracted text
     * @param text Raw extracted text
     * @return Cleaned text
     */
    private String cleanExtractedText(String text) {
        if (text == null) return "";

        // Remove extra whitespace
        text = text.replaceAll("\\s+", " ");

        // Remove special characters that might interfere
        text = text.replaceAll("[^a-zA-Z0-9\\s.+#/-]", " ");

        // Normalize common variations
        text = text.replace("C++", "cpp");
        text = text.replace("C#", "csharp");
        text = text.replace("Node.js", "nodejs");
        text = text.replace("React.js", "reactjs");

        return text.trim();
    }

    /**
     * Test resume parser with a sample text file
     */
    public static void testParser() {
        System.out.println("\n=== Testing ResumeParser ===");

        ResumeParser parser = new ResumeParser();

        // Create a temporary test file
        try {
            File testFile = new File("uploads/test_resume.txt");
            testFile.getParentFile().mkdirs();

            java.nio.file.Files.writeString(testFile.toPath(),
                    "John Doe\njohn.doe@email.com\n+1234567890\n\nSkills: Java, Python, SQL");

            System.out.println("✅ Test file created: " + testFile.getAbsolutePath());
            System.out.println("   Note: For full testing, upload a real PDF or image resume.");

        } catch (Exception e) {
            System.err.println("❌ Error creating test file: " + e.getMessage());
        }

        System.out.println("\n=== ResumeParser Test Complete ===\n");
    }
}