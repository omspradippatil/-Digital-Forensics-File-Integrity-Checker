package com.dffic.scanner;

import com.dffic.model.ScanResult;
import com.dffic.model.ScannedFile;
import com.dffic.util.PathUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * DirectoryScanner class
 * Recursively scans a directory, collects file metadata.
 * Uses Java NIO for efficient file system traversal.
 */
public class DirectoryScanner {

    private final boolean followSymlinks;
    private int filesScanned;
    private int directoriesScanned;
    private int symlinksSkipped;
    private int accessErrors;

    /**
     * Constructor with default settings.
     */
    public DirectoryScanner() {
        this(false); // Don't follow symbolic links by default
    }

    /**
     * Constructor with custom settings.
     *
     * @param followSymlinks Whether to follow symbolic links during scanning
     */
    public DirectoryScanner(boolean followSymlinks) {
        this.followSymlinks = followSymlinks;
        this.filesScanned = 0;
        this.directoriesScanned = 0;
        this.symlinksSkipped = 0;
        this.accessErrors = 0;
    }

    /**
     * Scans a directory recursively.
     *
     * @param directoryPath The path of the directory to scan
     * @return ScanResult containing all scanned files and metadata
     * @throws IOException If the directory cannot be accessed
     */
    public ScanResult scanDirectory(String directoryPath) throws IOException {
        // Reset counters
        filesScanned = 0;
        directoriesScanned = 0;
        symlinksSkipped = 0;
        accessErrors = 0;

        // Validate directory
        if (!PathUtils.isDirectory(directoryPath)) {
            throw new IOException("Invalid directory: " + directoryPath);
        }

        Path rootPath = Paths.get(directoryPath);
        List<ScannedFile> scannedFiles = new ArrayList<>();
        List<String> unreadableFiles = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();
        long startTime = System.currentTimeMillis();

        // File visitor to handle directory traversal
        FileVisitor<Path> visitor = new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                directoriesScanned++;

                // Skip symbolic links unless explicitly allowed
                if (!followSymlinks && Files.isSymbolicLink(dir)) {
                    symlinksSkipped++;
                    errorMessages.add("Skipped symbolic link: " + dir.toString());
                    return FileVisitResult.SKIP_SUBTREE;
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                try {
                    // Skip symbolic links unless explicitly allowed
                    if (!followSymlinks && Files.isSymbolicLink(file)) {
                        symlinksSkipped++;
                        errorMessages.add("Skipped symbolic link: " + file.toString());
                        return FileVisitResult.CONTINUE;
                    }

                    // Check if it's a regular file
                    if (attrs.isRegularFile()) {
                        File javaFile = file.toFile();
                        ScannedFile scannedFile = new ScannedFile(javaFile);
                        scannedFiles.add(scannedFile);
                        filesScanned++;
                    } else {
                        errorMessages.add("Skipped non-regular file: " + file.toString());
                    }
                } catch (Exception e) {
                    accessErrors++;
                    String errorPath = file.toString();
                    unreadableFiles.add(errorPath);
                    errorMessages.add("Error reading file: " + errorPath + " - " + e.getMessage());
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                accessErrors++;
                String errorPath = file.toString();
                unreadableFiles.add(errorPath);
                errorMessages.add("Failed to access file: " + errorPath + " - " + exc.getMessage());
                return FileVisitResult.CONTINUE;
            }
        };

        // Perform the scan
        try {
            Files.walkFileTree(rootPath, visitor);
        } catch (IOException e) {
            // Add root level error to messages
            errorMessages.add("Scan failed for directory: " + directoryPath + " - " + e.getMessage());
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Determine if scan is complete (no errors that would make it incomplete)
        boolean isComplete = unreadableFiles.isEmpty() && accessErrors == 0;

        return new ScanResult(directoryPath, scannedFiles, unreadableFiles,
                errorMessages, isComplete, duration);
    }

    /**
     * Quick scan that only validates directory structure without reading all files.
     *
     * @param directoryPath The directory path to validate
     * @return true if directory is accessible and contains at least one readable file
     * @throws IOException If directory cannot be accessed
     */
    public boolean validateDirectory(String directoryPath) throws IOException {
        if (!PathUtils.isDirectory(directoryPath)) {
            throw new IOException("Not a directory: " + directoryPath);
        }

        Path rootPath = Paths.get(directoryPath);

        // Try to list files to check accessibility
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootPath)) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry)) {
                    return true; // Found at least one regular file
                }
            }
        } catch (IOException e) {
            throw new IOException("Cannot access directory: " + directoryPath, e);
        }

        // Directory is accessible but may be empty
        return true;
    }

    /**
     * Get the number of files scanned in the last scan operation.
     *
     * @return Files scanned count
     */
    public int getFilesScanned() {
        return filesScanned;
    }

    /**
     * Get the number of directories scanned in the last scan operation.
     *
     * @return Directories scanned count
     */
    public int getDirectoriesScanned() {
        return directoriesScanned;
    }

    /**
     * Get the number of symbolic links skipped in the last scan operation.
     *
     * @return Symbolic links skipped count
     */
    public int getSymlinksSkipped() {
        return symlinksSkipped;
    }

    /**
     * Get the number of access errors encountered in the last scan operation.
     *
     * @return Access error count
     */
    public int getAccessErrors() {
        return accessErrors;
    }

    /**
     * Scan a directory for statistics only (doesn't store file metadata).
     *
     * @param directoryPath The directory to scan
     * @return Array with [filesCount, directoriesCount, errorCount]
     * @throws IOException If directory cannot be accessed
     */
    public int[] scanForStatistics(String directoryPath) throws IOException {
        if (!PathUtils.isDirectory(directoryPath)) {
            throw new IOException("Invalid directory: " + directoryPath);
        }

        // Reset counters
        filesScanned = 0;
        directoriesScanned = 0;
        accessErrors = 0;

        Path rootPath = Paths.get(directoryPath);

        FileVisitor<Path> visitor = new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                directoriesScanned++;
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (attrs.isRegularFile()) {
                    filesScanned++;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                accessErrors++;
                return FileVisitResult.CONTINUE;
            }
        };

        Files.walkFileTree(rootPath, visitor);
        return new int[]{filesScanned, directoriesScanned, accessErrors};
    }
}
