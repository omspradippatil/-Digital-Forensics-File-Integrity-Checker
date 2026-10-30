package com.dffic.hashing;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/**
 * Sha256HashService class
 * Implements SHA-256 hashing using Java's MessageDigest.
 * Reads files in buffered chunks to handle large files efficiently.
 */
public class Sha256HashService implements HashService {

    private static final String ALGORITHM = "SHA-256";
    private static final int BUFFER_SIZE = 8192; // 8KB buffer

    private MessageDigest messageDigest;

    /**
     * Constructor that initializes the MessageDigest instance.
     *
     * @throws Exception If SHA-256 algorithm is not available
     */
    public Sha256HashService() throws Exception {
        this.messageDigest = MessageDigest.getInstance(ALGORITHM);
    }

    /**
     * Generate SHA-256 hash for a file.
     * Reads the file in 8KB chunks to avoid memory issues.
     *
     * @param file The file to hash
     * @return 64-character hexadecimal hash string
     * @throws Exception If file cannot be read or hashing fails
     */
    @Override
    public String generateHash(File file) throws Exception {
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + file.getPath());
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException("Not a file: " + file.getPath());
        }
        if (!file.canRead()) {
            throw new SecurityException("Cannot read file: " + file.getPath());
        }

        // Reset message digest for a new calculation
        messageDigest.reset();

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            // Read file in chunks
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                messageDigest.update(buffer, 0, bytesRead);
            }

            // Get the final hash
            byte[] digestBytes = messageDigest.digest();

            // Convert to hexadecimal string
            return bytesToHex(digestBytes);
        } catch (Exception e) {
            throw new Exception("Failed to hash file: " + file.getPath() + " - " + e.getMessage(), e);
        }
    }

    /**
     * Generate hashes for multiple files.
     *
     * @param files Array of files to hash
     * @return Array of hash strings
     * @throws Exception If any file cannot be processed
     */
    @Override
    public String[] generateHashes(File[] files) throws Exception {
        List<String> hashes = new ArrayList<>();
        for (File file : files) {
            hashes.add(generateHash(file));
        }
        return hashes.toArray(new String[0]);
    }

    /**
     * Verify if a file matches the expected hash.
     *
     * @param file The file to check
     * @param expectedHash The expected SHA-256 hash
     * @return true if file's hash matches the expected hash
     * @throws Exception If file cannot be read or hashing fails
     */
    @Override
    public boolean verifyHash(File file, String expectedHash) throws Exception {
        String actualHash = generateHash(file);
        return actualHash.equalsIgnoreCase(expectedHash);
    }

    /**
     * Get the algorithm name.
     *
     * @return "SHA-256"
     */
    @Override
    public String getAlgorithmName() {
        return ALGORITHM;
    }

    /**
     * Get the hash length in characters (SHA-256 produces 64 hex characters).
     *
     * @return 64
     */
    @Override
    public int getHashLength() {
        return 64;
    }

    /**
     * Convert a byte array to hexadecimal string.
     *
     * @param bytes Byte array to convert
     * @return Hexadecimal string
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    /**
     * Calculate hash for a string (not file).
     * Useful for testing or small text hashing.
     *
     * @param input The string to hash
     * @return SHA-256 hash of the input string
     * @throws Exception If hashing fails
     */
    public String generateHash(String input) throws Exception {
        messageDigest.reset();
        byte[] bytes = input.getBytes();
        messageDigest.update(bytes);
        byte[] digestBytes = messageDigest.digest();
        return bytesToHex(digestBytes);
    }

    /**
     * Batch verification of multiple files.
     *
     * @param files Array of files to verify
     * @param expectedHashes Array of expected hashes (must match files length)
     * @return Array of booleans indicating verification results
     * @throws Exception If verification fails
     */
    public boolean[] verifyHashes(File[] files, String[] expectedHashes) throws Exception {
        if (files.length != expectedHashes.length) {
            throw new IllegalArgumentException("Files and hashes arrays must have same length");
        }

        boolean[] results = new boolean[files.length];
        for (int i = 0; i < files.length; i++) {
            try {
                results[i] = verifyHash(files[i], expectedHashes[i]);
            } catch (Exception e) {
                // If any file fails, mark as false
                results[i] = false;
            }
        }
        return results;
    }
}
