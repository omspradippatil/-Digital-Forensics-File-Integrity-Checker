package com.dffic.model;

import java.sql.Timestamp;

/**
 * ScanRecord class
 * Represents a scan record from the database.
 * Used to retrieve and display scan history information.
 */
public class ScanRecord {
    private final int scanId;
    private final String directoryPath;
    private final Timestamp scanDate;
    private final Timestamp completedAt;
    private final int totalFiles;
    private final String scanStatus;
    private final String errorMessage;

    /**
     * Constructor for ScanRecord.
     *
     * @param scanId         Database scan ID
     * @param directoryPath  Path that was scanned
     * @param scanDate       When scan started
     * @param completedAt    When scan completed (nullable)
     * @param totalFiles     Number of files scanned
     * @param scanStatus     IN_PROGRESS, COMPLETED, or FAILED
     * @param errorMessage   Error message if failed
     */
    public ScanRecord(int scanId, String directoryPath, Timestamp scanDate,
                      Timestamp completedAt, int totalFiles, String scanStatus,
                      String errorMessage) {
        this.scanId = scanId;
        this.directoryPath = directoryPath;
        this.scanDate = scanDate;
        this.completedAt = completedAt;
        this.totalFiles = totalFiles;
        this.scanStatus = scanStatus;
        this.errorMessage = errorMessage;
    }

    /**
     * Get the scan ID.
     *
     * @return Scan ID integer
     */
    public int getScanId() {
        return scanId;
    }

    /**
     * Get the directory path.
     *
     * @return Directory path string
     */
    public String getDirectoryPath() {
        return directoryPath;
    }

    /**
     * Get the scan start timestamp.
     *
     * @return Timestamp object
     */
    public Timestamp getScanDate() {
        return scanDate;
    }

    /**
     * Get the scan completion timestamp.
     *
     * @return Timestamp or null if not completed
     */
    public Timestamp getCompletedAt() {
        return completedAt;
    }

    /**
     * Get the total files count.
     *
     * @return File count
     */
    public int getTotalFiles() {
        return totalFiles;
    }

    /**
     * Get the scan status.
     *
     * @return Status string (IN_PROGRESS, COMPLETED, FAILED)
     */
    public String getScanStatus() {
        return scanStatus;
    }

    /**
     * Get the error message if scan failed.
     *
     * @return Error message or null
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Check if scan completed successfully.
     *
     * @return true if COMPLETED
     */
    public boolean isCompleted() {
        return "COMPLETED".equals(scanStatus);
    }

    /**
     * Check if scan failed.
     *
     * @return true if FAILED
     */
    public boolean isFailed() {
        return "FAILED".equals(scanStatus);
    }

    /**
     * Get the duration in seconds, or null if not completed.
     *
     * @return Duration seconds or null
     */
    public Long getDurationSeconds() {
        if (completedAt == null || scanDate == null) {
            return null;
        }
        long diff = completedAt.getTime() - scanDate.getTime();
        return diff / 1000;
    }

    /**
     * Format the scan record for display.
     *
     * @return Formatted string
     */
    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Scan ID: %d | Directory: %s\n", scanId, directoryPath));
        sb.append(String.format("Status: %s | Files: %d | Date: %s",
            scanStatus, totalFiles, scanDate));

        if (completedAt != null) {
            sb.append(String.format(" | Duration: %ds", getDurationSeconds()));
        }

        if (isFailed() && errorMessage != null && !errorMessage.isEmpty()) {
            sb.append(String.format("\nError: %s", errorMessage));
        }

        return sb.toString();
    }
}
