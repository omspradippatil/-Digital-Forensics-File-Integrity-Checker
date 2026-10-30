package com.dffic.model;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ScannedFile class
 * Represents a single file that has been scanned from the file system.
 * Stores metadata about the file for integrity comparison.
 */
public class ScannedFile {
    private final String fileName;
    private final String absolutePath;
    private final String extension;
    private final long fileSize;
    private final LocalDateTime lastModified;
    private String fileHash;

    /**
     * Constructor for ScannedFile.
     *
     * @param file The File object to scan
     */
    public ScannedFile(File file) {
        this.fileName = file.getName();
        this.absolutePath = file.getAbsolutePath();

        // Extract file extension
        String name = file.getName();
        int lastDotIndex = name.lastIndexOf('.');
        this.extension = (lastDotIndex == -1) ? "" : name.substring(lastDotIndex + 1);

        this.fileSize = file.length();
        this.lastModified = LocalDateTime.ofInstant(
            file.lastModified() > 0 ?
                java.time.Instant.ofEpochMilli(file.lastModified()) :
                java.time.Instant.now(),
            java.time.ZoneId.systemDefault()
        );
        this.fileHash = null;
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
     * Get the absolute file path.
     *
     * @return Absolute file path
     */
    public String getAbsolutePath() {
        return absolutePath;
    }

    /**
     * Get the file extension.
     *
     * @return File extension (without dot) or empty string if none
     */
    public String getExtension() {
        return extension;
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
     * Get the last modified timestamp.
     *
     * @return LocalDateTime of last modification
     */
    public LocalDateTime getLastModified() {
        return lastModified;
    }

    /**
     * Get the formatted last modified date as string.
     *
     * @return Formatted date string
     */
    public String getLastModifiedString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return lastModified.format(formatter);
    }

    /**
     * Get the SHA-256 hash of the file.
     *
     * @return Hash string or null if not calculated yet
     */
    public String getFileHash() {
        return fileHash;
    }

    /**
     * Set the SHA-256 hash for this file.
     *
     * @param fileHash The hash to set
     */
    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    /**
     * Get the parent directory path.
     *
     * @return Parent directory path
     */
    public String getParentPath() {
        Path path = Paths.get(absolutePath);
        Path parent = path.getParent();
        return (parent != null) ? parent.toString() : "";
    }

    @Override
    public String toString() {
        return String.format("%s (%d bytes, ext: %s)", fileName, fileSize, extension);
    }
}
