package com.dffic.service;

import com.dffic.hashing.Sha256HashService;
import com.dffic.model.ChangeRecord;
import com.dffic.model.ChangeType;
import com.dffic.model.ScannedFile;
import com.dffic.model.ScanResult;
import com.dffic.database.ForensicsRepository;
import com.dffic.model.FileRecord;

import java.io.File;
import java.util.*;

/**
 * IntegrityChecker class
 * Compares current scan results with previous database records.
 * Detects file changes (ADDED, MODIFIED, UNCHANGED, DELETED).
 */
public class IntegrityChecker {

    private final ForensicsRepository repository;
    private final Sha256HashService hashService;

    /**
     * Constructor for IntegrityChecker.
     *
     * @param repository  Database repository for retrieving file records
     * @param hashService Service for calculating SHA-256 hashes
     * @throws Exception If hash service initialization fails
     */
    public IntegrityChecker(ForensicsRepository repository, Sha256HashService hashService) throws Exception {
        this.repository = repository;
        this.hashService = hashService;
    }

    /**
     * Compare current scan with previous baseline and identify changes.
     * This is the main integrity comparison method.
     *
     * @param scanResult       Current scan results
     * @param rootDirectory    Root directory being monitored
     * @param scanId           ID of the current scan
     * @return List of change records
     * @throws Exception If comparison fails
     */
    public List<ChangeRecord> compareWithBaseline(ScanResult scanResult, String rootDirectory, int scanId) throws Exception {
        // Ensure scan is complete before performing comparison
        if (!scanResult.isComplete()) {
            throw new IllegalStateException("Cannot compare with incomplete scan. Scan may have unreadable files.");
        }

        List<ChangeRecord> changeRecords = new ArrayList<>();

        // Get previous active file records for this directory
        List<FileRecord> previousFiles = repository.getActiveFiles(rootDirectory);
        Map<String, FileRecord> previousMap = createFileRecordMap(previousFiles);

        // Map for current files by normalized path
        Map<String, ScannedFile> currentMap = createScannedFileMap(scanResult.getFiles());

        // Calculate hashes for all current files
        calculateHashes(scanResult.getFiles());

        // Compare each current file with previous records
        for (ScannedFile currentFile : scanResult.getFiles()) {
            String normalizedPath = normalizePath(rootDirectory, currentFile.getAbsolutePath());
            FileRecord previousRecord = previousMap.get(normalizedPath);

            ChangeRecord changeRecord = compareFile(currentFile, previousRecord, normalizedPath);
            changeRecords.add(changeRecord);
        }

        // Identify deleted files (in previous but not in current)
        for (FileRecord previousRecord : previousFiles) {
            String normalizedPath = normalizePath(rootDirectory, previousRecord.getFilePath());
            if (!currentMap.containsKey(normalizedPath)) {
                // File was in previous scan but not in current scan
                ChangeRecord changeRecord = createDeletedRecord(previousRecord, normalizedPath);
                changeRecords.add(changeRecord);
            }
        }

        return changeRecords;
    }

    /**
     * Compare a single file with its previous record.
     *
     * @param currentFile      Current file from scan
     * @param previousRecord   Previous file record from database (null if new)
     * @param normalizedPath   Normalized file path
     * @return ChangeRecord for this file
     */
    private ChangeRecord compareFile(ScannedFile currentFile, FileRecord previousRecord, String normalizedPath) {
        if (previousRecord == null) {
            // No previous record = ADDED
            return new ChangeRecord(
                currentFile.getAbsolutePath(),
                currentFile.getFileName(),
                currentFile.getFileSize(),
                null,  // No old hash
                currentFile.getFileHash(),
                ChangeType.ADDED
            );
        }

        // Compare hashes
        if (currentFile.getFileHash() == null) {
            throw new IllegalStateException("File hash not calculated: " + currentFile.getAbsolutePath());
        }

        if (currentFile.getFileHash().equals(previousRecord.getFileHash())) {
            // Hash unchanged = UNCHANGED
            return new ChangeRecord(
                currentFile.getAbsolutePath(),
                currentFile.getFileName(),
                currentFile.getFileSize(),
                previousRecord.getFileHash(),
                currentFile.getFileHash(),
                ChangeType.UNCHANGED
            );
        } else {
            // Hash changed = MODIFIED
            return new ChangeRecord(
                currentFile.getAbsolutePath(),
                currentFile.getFileName(),
                currentFile.getFileSize(),
                previousRecord.getFileHash(),
                currentFile.getFileHash(),
                ChangeType.MODIFIED
            );
        }
    }

    /**
     * Create a deleted file record.
     *
     * @param previousRecord   Previous file record
     * @param normalizedPath   Normalized file path
     * @return ChangeRecord for deleted file
     */
    private ChangeRecord createDeletedRecord(FileRecord previousRecord, String normalizedPath) {
        return new ChangeRecord(
            previousRecord.getFilePath(),
            previousRecord.getFileName(),
            previousRecord.getFileSize(),
            previousRecord.getFileHash(),
            null,  // No new hash for deleted files
            ChangeType.DELETED
        );
    }

    /**
     * Calculate SHA-256 hashes for all scanned files.
     * This is done in batches for efficiency.
     *
     * @param files List of scanned files
     */
    private void calculateHashes(List<ScannedFile> files) {
        for (ScannedFile file : files) {
            try {
                File javaFile = new File(file.getAbsolutePath());
                String hash = hashService.generateHash(javaFile);
                file.setFileHash(hash);
            } catch (Exception e) {
                throw new RuntimeException("Failed to calculate hash for file: " + file.getAbsolutePath(), e);
            }
        }
    }

    /**
     * Create a map of file records keyed by normalized path.
     *
     * @param fileRecords List of file records
     * @return Map with normalized path as key
     */
    private Map<String, FileRecord> createFileRecordMap(List<FileRecord> fileRecords) {
        Map<String, FileRecord> map = new HashMap<>();
        for (FileRecord record : fileRecords) {
            String normalizedPath = normalizePath(record.getRootPath(), record.getFilePath());
            map.put(normalizedPath, record);
        }
        return map;
    }

    /**
     * Create a map of scanned files keyed by normalized path.
     *
     * @param scannedFiles List of scanned files
     * @return Map with normalized path as key
     */
    private Map<String, ScannedFile> createScannedFileMap(List<ScannedFile> scannedFiles) {
        Map<String, ScannedFile> map = new HashMap<>();
        for (ScannedFile file : scannedFiles) {
            String normalizedPath = normalizePath(file.getParentPath(), file.getAbsolutePath());
            map.put(normalizedPath, file);
        }
        return map;
    }

    /**
     * Normalize a file path relative to a root directory.
     *
     * @param rootPath Root directory path
     * @param filePath Full file path
     * @return Normalized path relative to root
     */
    private String normalizePath(String rootPath, String filePath) {
        // Remove any trailing slashes from root
        String normalizedRoot = rootPath.replace("\\", "/");
        if (normalizedRoot.endsWith("/")) {
            normalizedRoot = normalizedRoot.substring(0, normalizedRoot.length() - 1);
        }

        // Normalize file path
        String normalizedFile = filePath.replace("\\", "/");

        // Return relative path if file is within root
        if (normalizedFile.startsWith(normalizedRoot + "/")) {
            return normalizedFile.substring(normalizedRoot.length());
        }

        // If file is outside root, use absolute path
        return normalizedFile;
    }

    /**
     * Calculate summary statistics from change records.
     *
     * @param changeRecords List of change records
     * @return Array with [added, modified, unchanged, deleted] counts
     */
    public int[] calculateSummaryCounts(List<ChangeRecord> changeRecords) {
        int added = 0, modified = 0, unchanged = 0, deleted = 0;
        for (ChangeRecord record : changeRecords) {
            switch (record.getChangeType()) {
                case ADDED:
                    added++;
                    break;
                case MODIFIED:
                    modified++;
                    break;
                case UNCHANGED:
                    unchanged++;
                    break;
                case DELETED:
                    deleted++;
                    break;
            }
        }
        return new int[]{added, modified, unchanged, deleted};
    }

    /**
     * Filter change records by type.
     *
     * @param changeRecords Full list of change records
     * @param changeType    Type to filter by
     * @return Filtered list
     */
    public List<ChangeRecord> filterByType(List<ChangeRecord> changeRecords, ChangeType changeType) {
        List<ChangeRecord> filtered = new ArrayList<>();
        for (ChangeRecord record : changeRecords) {
            if (record.getChangeType() == changeType) {
                filtered.add(record);
            }
        }
        return filtered;
    }
}
