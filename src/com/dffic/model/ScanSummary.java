package com.dffic.model;

import java.util.List;

/**
 * ScanSummary class
 * Contains the summary statistics and change records from a file integrity scan.
 * Used to display results to the user after a scan completes.
 */
public class ScanSummary {
    private final int scanId;
    private final String directoryPath;
    private final String scanDate;
    private final int totalFiles;
    private final int addedCount;
    private final int modifiedCount;
    private final int unchangedCount;
    private final int deletedCount;
    private final List<ChangeRecord> changeRecords;

    /**
     * Constructor for ScanSummary.
     *
     * @param scanId          The ID of the completed scan
     * @param directoryPath   The directory that was scanned
     * @param scanDate        Timestamp when the scan completed
     * @param totalFiles      Total number of files processed
     * @param addedCount      Number of files classified as ADDED
     * @param modifiedCount   Number of files classified as MODIFIED
     * @param unchangedCount  Number of files classified as UNCHANGED
     * @param deletedCount    Number of files classified as DELETED
     * @param changeRecords   List of all change records with details
     */
    public ScanSummary(int scanId, String directoryPath, String scanDate, int totalFiles,
                       int addedCount, int modifiedCount, int unchangedCount, int deletedCount,
                       List<ChangeRecord> changeRecords) {
        this.scanId = scanId;
        this.directoryPath = directoryPath;
        this.scanDate = scanDate;
        this.totalFiles = totalFiles;
        this.addedCount = addedCount;
        this.modifiedCount = modifiedCount;
        this.unchangedCount = unchangedCount;
        this.deletedCount = deletedCount;
        this.changeRecords = changeRecords;
    }

    /**
     * Get the scan ID from the database.
     *
     * @return Scan ID integer
     */
    public int getScanId() {
        return scanId;
    }

    /**
     * Get the directory path that was scanned.
     *
     * @return Directory path string
     */
    public String getDirectoryPath() {
        return directoryPath;
    }

    /**
     * Get the scan completion timestamp.
     *
     * @return Scan date string
     */
    public String getScanDate() {
        return scanDate;
    }

    /**
     * Get the total number of files scanned.
     *
     * @return Total file count
     */
    public int getTotalFiles() {
        return totalFiles;
    }

    /**
     * Get the number of added files.
     *
     * @return Added file count
     */
    public int getAddedCount() {
        return addedCount;
    }

    /**
     * Get the number of modified files.
     *
     * @return Modified file count
     */
    public int getModifiedCount() {
        return modifiedCount;
    }

    /**
     * Get the number of unchanged files.
     *
     * @return Unchanged file count
     */
    public int getUnchangedCount() {
        return unchangedCount;
    }

    /**
     * Get the number of deleted files.
     *
     * @return Deleted file count
     */
    public int getDeletedCount() {
        return deletedCount;
    }

    /**
     * Get the list of all change records.
     *
     * @return List of ChangeRecord objects
     */
    public List<ChangeRecord> getChangeRecords() {
        return changeRecords;
    }

    /**
     * Calculate the percentage of files that were unchanged.
     *
     * @return Percentage as double (0.0 to 100.0)
     */
    public double getUnchangedPercentage() {
        if (totalFiles == 0) return 0.0;
        return (unchangedCount / (double) totalFiles) * 100.0;
    }

    /**
     * Format the summary for display to the user.
     *
     * @return Formatted summary string
     */
    public String formatSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========================================\n");
        sb.append("           SCAN SUMMARY\n");
        sb.append("========================================\n");
        sb.append(String.format("Scan ID:          %d\n", scanId));
        sb.append(String.format("Directory:        %s\n", directoryPath));
        sb.append(String.format("Scan Date:        %s\n", scanDate));
        sb.append(String.format("Total Files:      %d\n", totalFiles));
        sb.append("----------------------------------------\n");
        sb.append(String.format("Added:            %d\n", addedCount));
        sb.append(String.format("Modified:         %d\n", modifiedCount));
        sb.append(String.format("Unchanged:        %d (%.1f%%)\n", unchangedCount, getUnchangedPercentage()));
        sb.append(String.format("Deleted:          %d\n", deletedCount));
        sb.append("========================================\n");

        return sb.toString();
    }

    /**
     * Format detailed results for display.
     *
     * @return Detailed results string
     */
    public String formatDetailedResults() {
        StringBuilder sb = new StringBuilder(formatSummary());

        // Group changes by type
        for (ChangeType type : ChangeType.values()) {
            sb.append(String.format("\n--- %s FILES ---\n", type));
            int count = 0;
            for (ChangeRecord record : changeRecords) {
                if (record.getChangeType() == type) {
                    sb.append(String.format("  [%s] %s\n",
                        record.getFileName(),
                        record.getFilePath()));
                    count++;
                }
            }
            if (count == 0) {
                sb.append("  (none)\n");
            }
        }

        return sb.toString();
    }
}
