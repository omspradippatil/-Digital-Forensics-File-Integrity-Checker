package com.dffic.model;

/**
 * ChangeRecord class
 * Represents a single file change detected during integrity comparison.
 * Each ChangeRecord stores information about a file and its change status.
 */
public class ChangeRecord {
    private final String filePath;
    private final String fileName;
    private final long fileSize;
    private final String oldHash;
    private final String newHash;
    private final ChangeType changeType;

    /**
     * Constructor for ChangeRecord.
     *
     * @param filePath   Absolute path of the file
     * @param fileName   Name of the file
     * @param fileSize   Size of the file in bytes
     * @param oldHash    Previous hash value (null for ADDED files)
     * @param newHash    Current hash value
     * @param changeType Type of change detected
     */
    public ChangeRecord(String filePath, String fileName, long fileSize, String oldHash, String newHash, ChangeType changeType) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.oldHash = oldHash;
        this.newHash = newHash;
        this.changeType = changeType;
    }

    /**
     * Get the absolute file path.
     *
     * @return File path as string
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Get the file name.
     *
     * @return File name as string
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Get the file size in bytes.
     *
     * @return File size
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * Get the previous hash value.
     *
     * @return Old hash string or null if file is new (ADDED)
     */
    public String getOldHash() {
        return oldHash;
    }

    /**
     * Get the current hash value.
     *
     * @return New hash string
     */
    public String getNewHash() {
        return newHash;
    }

    /**
     * Get the type of change detected.
     *
     * @return ChangeType enum value
     */
    public ChangeType getChangeType() {
        return changeType;
    }

    /**
     * Format the change status as a readable string.
     *
     * @return Status description string
     */
    public String getStatusDescription() {
        return changeType.toString();
    }

    @Override
    public String toString() {
        return String.format("%s [%s]", filePath, changeType.toString());
    }
}
