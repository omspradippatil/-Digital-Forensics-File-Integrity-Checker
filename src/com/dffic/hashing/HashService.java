package com.dffic.hashing;

import java.io.File;

/**
 * HashService interface
 * Defines the contract for file hashing operations.
 * Allows different hashing algorithms to be implemented.
 */
public interface HashService {

    /**
     * Generate a hash for the given file.
     * The file should be read in buffered chunks to avoid memory issues.
     *
     * @param file The file to hash
     * @return Hexadecimal hash string
     * @throws Exception If file cannot be read or hash cannot be generated
     */
    String generateHash(File file) throws Exception;

    /**
     * Get the name of the hashing algorithm.
     *
     * @return Algorithm name
     */
    String getAlgorithmName();

    /**
     * Get the length of the hash in characters.
     *
     * @return Hash length in characters
     */
    int getHashLength();

    /**
     * Verify if a file matches the expected hash.
     *
     * @param file       The file to check
     * @param expectedHash The expected hash value
     * @return true if hash matches
     * @throws Exception If file cannot be read or hash cannot be generated
     */
    boolean verifyHash(File file, String expectedHash) throws Exception;

    /**
     * Calculate hash for multiple files and return as an array.
     * Useful for batch operations.
     *
     * @param files Array of files to hash
     * @return Array of hash strings
     * @throws Exception If any file cannot be processed
     */
    String[] generateHashes(File[] files) throws Exception;
}
