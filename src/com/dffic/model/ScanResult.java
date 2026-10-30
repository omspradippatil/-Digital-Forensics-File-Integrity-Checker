package com.dffic.model;

import java.util.ArrayList;
import java.util.List;

/**
 * ScanResult class
 * Contains the complete result of a directory scan operation.
 * Stores all scanned files, statistics, and any errors encountered.
 */
public class ScanResult {
    private final String directoryPath;
    private final List<ScannedFile> files;
    private final List<String> unreadableFiles;
    private final List<String> errorMessages;
    private final boolean isComplete;
    private final long scanDurationMillis;

    /**
     * Constructor for ScanResult.
     *
     * @param directoryPath      The root directory that was scanned
     * @param files              List of successfully scanned files
     * @param unreadableFiles    List of files that could not be read
     * @param errorMessages      List of error messages encountered
     * @param isComplete         Whether the scan completed successfully
     * @param scanDurationMillis Time taken to complete the scan
     */
    public ScanResult(String directoryPath, List<ScannedFile> files,
                      List<String> unreadableFiles, List<String> errorMessages,
                      boolean isComplete, long scanDurationMillis) {
        this.directoryPath = directoryPath;
        this.files = new ArrayList<>(files);
        this.unreadableFiles = new ArrayList<>(unreadableFiles);
        this.errorMessages = new ArrayList<>(errorMessages);
        this.isComplete = isComplete;
        this.scanDurationMillis = scanDurationMillis;
    }

    /**
     * Get the root directory path that was scanned.
     *
     * @return Directory path string
     */
    public String getDirectoryPath() {
        return directoryPath;
    }

    /**
     * Get the list of successfully scanned files.
     *
     * @return List of ScannedFile objects
     */
    public List<ScannedFile> getFiles() {
        return new ArrayList<>(files);
    }

    /**
     * Get the count of successfully scanned files.
     *
     * @return Number of files
     */
    public int getFileCount() {
        return files.size();
    }

    /**
     * Get the list of files that could not be read.
     *
     * @return List of file paths that were unreadable
     */
    public List<String> getUnreadableFiles() {
        return new ArrayList<>(unreadableFiles);
    }

    /**
     * Get the list of error messages encountered during scan.
     *
     * @return List of error messages
     */
    public List<String> getErrorMessages() {
        return new ArrayList<>(errorMessages);
    }

    /**
     * Check if the scan completed successfully.
     * A complete scan means all accessible files were scanned.
     *
     * @return true if scan is complete, false otherwise
     */
    public boolean isComplete() {
        return isComplete;
    }

    /**
     * Get the time taken to complete the scan.
     *
     * @return Duration in milliseconds
     */
    public long getScanDurationMillis() {
        return scanDurationMillis;
    }

    /**
     * Get the duration as a formatted string.
     *
     * @return Formatted duration string
     */
    public String getScanDurationString() {
        long seconds = scanDurationMillis / 1000;
        long milliseconds = scanDurationMillis % 1000;
        return String.format("%d.%03d seconds", seconds, milliseconds);
    }

    /**
     * Check if there were any errors during scanning.
     *
     * @return true if errors were encountered
     */
    public boolean hasErrors() {
        return !errorMessages.isEmpty() || !unreadableFiles.isEmpty();
    }

    /**
     * Add a scanned file to the result.
     *
     * @param file The file to add
     */
    public void addFile(ScannedFile file) {
        files.add(file);
    }

    /**
     * Add an unreadable file path to the result.
     *
     * @param path The file path that was unreadable
     */
    public void addUnreadableFile(String path) {
        unreadableFiles.add(path);
    }

    /**
     * Add an error message to the result.
     *
     * @param message The error message to add
     */
    public void addErrorMessage(String message) {
        errorMessages.add(message);
    }

    @Override
    public String toString() {
        return String.format("ScanResult{directory='%s', files=%d, complete=%s, duration=%s}",
            directoryPath, files.size(), isComplete, getScanDurationString());
    }
}
