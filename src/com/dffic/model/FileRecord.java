package com.dffic.model;

import java.sql.Timestamp;

/**
 * FileRecord class
 * Represents a file record from the database.
 * Used to store and retrieve file metadata for integrity tracking.
 */
public class FileRecord {
    private final int fileId;
    private final String rootPath;
    private final String filePath;
    private final String fileName;
    private final long fileSize;
    private final String fileHash;
    private final Timestamp lastModified;
    private final Integer lastScanId;
    private final String fileStatus;
    private final boolean isActive;
    private final Timestamp createdAt;
    private final Timestamp updatedAt;

    /**
     * Constructor for FileRecord.
     *
     * @param fileId       Unique database ID
     * @param rootPath     Root directory path
     * @param filePath     Full file path
     * @param fileName     File name with extension
     * @param fileSize     File size in bytes
     * @param fileHash     SHA-256 hash value
     * @param lastModified Last modification timestamp
     * @param lastScanId   ID of last scan that processed this file
     * @param fileStatus   File status (ADDED, MODIFIED, UNCHANGED, DELETED)
     * @param isActive     Whether file is currently monitored
     * @param createdAt    When record was created
     * @param updatedAt    When record was last updated
     */
    public FileRecord(int fileId, String rootPath, String filePath, String fileName,
                      long fileSize, String fileHash, Timestamp lastModified,
                      Integer lastScanId, String fileStatus, boolean isActive,
                      Timestamp createdAt, Timestamp updatedAt) {
        this.fileId = fileId;
        this.rootPath = rootPath;
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileHash = fileHash;
        this.lastModified = lastModified;
        this.lastScanId = lastScanId;
        this.fileStatus = fileStatus;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Get the unique file ID.
     *
     * @return File ID integer
     */
    public int getFileId() {
        return fileId;
    }

    /**
     * Get the root directory path.
     *
     * @return Root path string
     */
    public String getRootPath() {
        return rootPath;
    }

    /**
     * Get the full file path.
     *
     * @return Full file path string
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Get the file name with extension.
     *
     * @return File name string
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
     * Get the SHA-256 hash value.
     *
     * @return 64-character hash string
     */
    public String getFileHash() {
        return fileHash;
    }

    /**
     * Get the last modification timestamp.
     *
     * @return Last modified timestamp
     */
    public Timestamp getLastModified() {
        return lastModified;
    }

    /**
     * Get the ID of the last scan that processed this file.
     *
     * @return Scan ID or null if never scanned
     */
    public Integer getLastScanId() {
        return lastScanId;
    }

    /**
     * Get the current file status.
     *
     * @return Status string (ADDED, MODIFIED, UNCHANGED, DELETED)
     */
    public String getFileStatus() {
        return fileStatus;
    }

    /**
     * Check if file is currently active (monitored).
     *
     * @return true if file is active
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Get the creation timestamp.
     *
     * @return Created timestamp
     */
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    /**
     * Get the last update timestamp.
     *
     * @return Updated timestamp
     */
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Check if this file record represents an unchanged file.
     *
     * @return true if status is UNCHANGED
     */
    public boolean isUnchanged() {
        return "UNCHANGED".equals(fileStatus);
    }

    /**
     * Check if this file record represents a modified file.
     *
     * @return true if status is MODIFIED
     */
    public boolean isModified() {
        return "MODIFIED".equals(fileStatus);
    }

    /**
     * Check if this file record represents an added file.
     *
     * @return true if status is ADDED
     */
    public boolean isAdded() {
        return "ADDED".equals(fileStatus);
    }

    /**
     * Check if this file record represents a deleted file.
     *
     * @return true if status is DELETED
     */
    public boolean isDeleted() {
        return "DELETED".equals(fileStatus);
    }

    /**
     * Get a formatted string representation of file size.
     *
     * @return Human-readable file size
     */
    public String getFormattedFileSize() {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
        }
    }

    @Override
    public String toString() {
        return String.format("FileRecord{id=%d, name='%s', size=%d, hash='%s', active=%s}",
                fileId, fileName, fileSize, fileHash, isActive);
    }
}
